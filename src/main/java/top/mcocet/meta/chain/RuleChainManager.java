package top.mcocet.meta.chain;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则链管理器：管理所有规则链及其执行器
 */
public class RuleChainManager {

    private static final Logger log = LoggerFactory.getLogger(RuleChainManager.class.getSimpleName());

    private final List<RuleChain> chains = new ArrayList<>();
    private final Map<String, RuleChainExecutor> activeExecutors = new ConcurrentHashMap<>();
    private Session session;

    public void registerChain(RuleChain chain) {
        chains.add(chain);
        log.info("[LanConnect] 已注册规则链: {} ({} 个步骤)", chain.getName(), chain.getSteps().size());
    }

    public void clearChains() {
        // 停止所有活跃的执行器
        for (RuleChainExecutor executor : activeExecutors.values()) {
            executor.stop();
        }
        activeExecutors.clear();
        chains.clear();
    }

    public List<RuleChain> getChains() {
        return new ArrayList<>(chains);
    }

    public int getChainCount() {
        return chains.size();
    }

    /**
     * 处理数据包：检查是否有规则链的触发条件满足，如果满足则启动执行
     * 同时向所有活跃的执行器投递数据包
     */
    public void onPacketReceived(Session session, Packet packet) {
        this.session = session;

        // 1. 向所有活跃执行器投递数据包（用于步骤条件检测）
        for (RuleChainExecutor executor : activeExecutors.values()) {
            if (executor.isRunning()) {
                executor.deliverPacket(packet);
            }
        }

        // 2. 检查是否有新的规则链需要启动
        for (RuleChain chain : chains) {
            if (chain.checkTrigger(session, packet)) {
                startChain(chain);
            }
        }
    }

    /**
     * 启动规则链执行
     */
    private void startChain(RuleChain chain) {
        // 如果链已在运行，不重复启动
        if (activeExecutors.containsKey(chain.getId()) && activeExecutors.get(chain.getId()).isRunning()) {
            return;
        }

        if (session == null) {
            log.warn("[LanConnect] 无法启动规则链 [{}]，session 为 null", chain.getName());
            return;
        }

        chain.markExecuted();
        RuleChainExecutor executor = new RuleChainExecutor(chain, session);
        activeExecutors.put(chain.getId(), executor);
        executor.start();
    }

    /**
     * 手动启动规则链（用于没有触发条件的链）
     */
    public void startChainById(String chainId) {
        for (RuleChain chain : chains) {
            if (chain.getId().equals(chainId)) {
                startChain(chain);
                return;
            }
        }
        log.warn("[LanConnect] 未找到规则链: {}", chainId);
    }

    /**
     * 停止指定规则链
     */
    public void stopChain(String chainId) {
        RuleChainExecutor executor = activeExecutors.get(chainId);
        if (executor != null) {
            executor.stop();
            activeExecutors.remove(chainId);
        }
    }

    /**
     * 清理已停止的执行器
     */
    public void cleanupExecutors() {
        activeExecutors.entrySet().removeIf(entry -> !entry.getValue().isRunning());
    }

    public Map<String, RuleChainExecutor> getActiveExecutors() {
        return new ConcurrentHashMap<>(activeExecutors);
    }
}
