package top.mcocet.meta.chain;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mcocet.meta.ActionMeta;
import top.mcocet.meta.ConditionMeta;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 规则链执行器：在独立线程中按顺序执行规则链的步骤
 * 每个步骤等待条件满足后才执行，步骤间支持延迟
 */
public class RuleChainExecutor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RuleChainExecutor.class.getSimpleName());

    private final RuleChain chain;
    private final Session session;
    private final BlockingQueue<Packet> packetQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = false;
    private volatile boolean stopped = false;
    private Thread executorThread;

    public RuleChainExecutor(RuleChain chain, Session session) {
        this.chain = chain;
        this.session = session;
    }

    /**
     * 启动执行器线程
     */
    public void start() {
        if (running) return;
        running = true;
        stopped = false;
        chain.setRunning(true);
        executorThread = new Thread(this, "RuleChain-" + chain.getId());
        executorThread.start();
        log.info("[LanConnect] 规则链 [{}] 开始执行", chain.getName());
    }

    /**
     * 停止执行器
     */
    public void stop() {
        stopped = true;
        running = false;
        chain.setRunning(false);
        if (executorThread != null) {
            executorThread.interrupt();
        }
        log.info("[LanConnect] 规则链 [{}] 已停止", chain.getName());
    }

    /**
     * 向执行器投递数据包（用于条件检测）
     */
    public void deliverPacket(Packet packet) {
        if (running && !stopped) {
            packetQueue.offer(packet);
        }
    }

    @Override
    public void run() {
        try {
            for (int stepIndex = 0; stepIndex < chain.getSteps().size() && !stopped; stepIndex++) {
                RuleChainStep step = chain.getSteps().get(stepIndex);
                if (!step.isEnabled()) {
                    log.debug("[LanConnect] 规则链步骤 [{}] 已禁用，跳过", step.getName());
                    continue;
                }

                log.info("[LanConnect] 规则链 [{}] 执行步骤 {}/{}: {}",
                        chain.getName(), stepIndex + 1, chain.getSteps().size(), step.getName());

                // 1. 等待步骤条件满足（如果步骤有条件）
                if (step.hasConditions()) {
                    boolean conditionMet = waitForConditions(step);
                    if (!conditionMet) {
                        log.warn("[LanConnect] 规则链 [{}] 步骤 [{}] 条件等待超时或中断，链终止",
                                chain.getName(), step.getName());
                        break;
                    }
                }

                // 2. 执行步骤动作
                executeStepActions(step);

                // 3. 步骤间延迟
                if (step.getDelayAfter() > 0 && !stopped) {
                    log.debug("[LanConnect] 规则链 [{}] 步骤 [{}] 延迟 {}ms",
                            chain.getName(), step.getName(), step.getDelayAfter());
                    Thread.sleep(step.getDelayAfter());
                }
            }

            if (!stopped) {
                log.info("[LanConnect] 规则链 [{}] 执行完成", chain.getName());
            }
        } catch (InterruptedException e) {
            log.warn("[LanConnect] 规则链 [{}] 执行被中断", chain.getName());
            Thread.currentThread().interrupt();
        } finally {
            running = false;
            chain.setRunning(false);
            chain.markExecuted();
        }
    }

    /**
     * 等待步骤条件满足
     * @return true if conditions met, false if timeout or interrupted
     */
    private boolean waitForConditions(RuleChainStep step) {
        long startTime = System.currentTimeMillis();
        int timeout = step.getTimeout();

        while (!stopped) {
            // 检查超时
            if (timeout > 0 && System.currentTimeMillis() - startTime > timeout) {
                return false;
            }

            // 从队列中获取数据包检测条件
            Packet packet = null;
            try {
                if (timeout > 0) {
                    long remaining = timeout - (System.currentTimeMillis() - startTime);
                    if (remaining <= 0) return false;
                    packet = packetQueue.poll(remaining, java.util.concurrent.TimeUnit.MILLISECONDS);
                } else {
                    packet = packetQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            if (packet == null) continue;

            // 检测所有条件
            boolean allMet = true;
            for (ConditionMeta condition : step.getConditions()) {
                if (!condition.check(session, packet)) {
                    allMet = false;
                    break;
                }
            }

            if (allMet) {
                return true;
            }
        }
        return false;
    }

    /**
     * 执行步骤的所有动作
     */
    private void executeStepActions(RuleChainStep step) {
        for (ActionMeta action : step.getActions()) {
            if (stopped) break;
            try {
                action.execute(session);
                log.debug("[LanConnect] 规则链步骤 [{}] 动作 [{}] 已执行",
                        step.getName(), action.getMetaId());
            } catch (Exception e) {
                log.error("[LanConnect] 规则链步骤 [{}] 动作 [{}] 执行出错: {}",
                        step.getName(), action.getMetaId(), e.getMessage());
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public RuleChain getChain() {
        return chain;
    }
}
