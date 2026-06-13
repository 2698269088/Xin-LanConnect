package top.mcocet.meta.chain;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import top.mcocet.meta.ConditionMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则链：一组按顺序执行的步骤
 * 当触发条件满足时，按顺序执行所有步骤
 */
public class RuleChain {

    private final String id;
    private final String name;
    private final List<ConditionMeta> triggerConditions; // 触发整个链执行的条件
    private final List<RuleChainStep> steps;
    private final boolean enabled;
    private final boolean repeat;      // 是否重复执行（循环链）
    private final int cooldown;        // 链执行后的冷却时间
    private long lastExecuteTime = 0;
    private volatile boolean isRunning = false;

    public RuleChain(String id, String name, List<ConditionMeta> triggerConditions, List<RuleChainStep> steps) {
        this(id, name, triggerConditions, steps, true, false, 0);
    }

    public RuleChain(String id, String name, List<ConditionMeta> triggerConditions, List<RuleChainStep> steps,
                     boolean enabled, boolean repeat, int cooldown) {
        this.id = id;
        this.name = name;
        this.triggerConditions = triggerConditions != null ? triggerConditions : new ArrayList<>();
        this.steps = steps != null ? steps : new ArrayList<>();
        this.enabled = enabled;
        this.repeat = repeat;
        this.cooldown = cooldown;
    }

    /**
     * 检查触发条件是否满足
     */
    public boolean checkTrigger(Session session, Packet packet) {
        if (!enabled || isRunning) {
            return false;
        }
        if (isOnCooldown()) {
            return false;
        }
        // 如果没有触发条件，则不会自动触发（需要手动启动）
        if (triggerConditions.isEmpty()) {
            return false;
        }
        for (ConditionMeta condition : triggerConditions) {
            if (!condition.check(session, packet)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否在冷却中
     */
    public boolean isOnCooldown() {
        if (cooldown <= 0) {
            return false;
        }
        return System.currentTimeMillis() - lastExecuteTime < cooldown;
    }

    public void markExecuted() {
        lastExecuteTime = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ConditionMeta> getTriggerConditions() {
        return triggerConditions;
    }

    public List<RuleChainStep> getSteps() {
        return steps;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public int getCooldown() {
        return cooldown;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    @Override
    public String toString() {
        return "RuleChain{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", triggerConditions=" + triggerConditions.size() +
                ", steps=" + steps.size() +
                ", enabled=" + enabled +
                ", repeat=" + repeat +
                ", cooldown=" + cooldown +
                '}';
    }
}
