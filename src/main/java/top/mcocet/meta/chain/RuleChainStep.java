package top.mcocet.meta.chain;

import top.mcocet.meta.ActionMeta;
import top.mcocet.meta.ConditionMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则链步骤：规则链中的一个执行步骤
 * 包含触发条件和要执行的动作
 */
public class RuleChainStep {

    private final String id;
    private final String name;
    private final List<ConditionMeta> conditions;
    private final List<ActionMeta> actions;
    private final int delayAfter; // 执行完此步骤后的延迟（毫秒）
    private final int timeout;    // 等待条件满足的超时时间（毫秒），0表示无限等待
    private final boolean enabled;

    public RuleChainStep(String id, String name, List<ConditionMeta> conditions, List<ActionMeta> actions) {
        this(id, name, conditions, actions, 0, 0, true);
    }

    public RuleChainStep(String id, String name, List<ConditionMeta> conditions, List<ActionMeta> actions,
                         int delayAfter, int timeout, boolean enabled) {
        this.id = id;
        this.name = name;
        this.conditions = conditions != null ? conditions : new ArrayList<>();
        this.actions = actions != null ? actions : new ArrayList<>();
        this.delayAfter = delayAfter;
        this.timeout = timeout;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ConditionMeta> getConditions() {
        return conditions;
    }

    public List<ActionMeta> getActions() {
        return actions;
    }

    public int getDelayAfter() {
        return delayAfter;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 检查此步骤是否有条件（无条件步骤会立即执行）
     */
    public boolean hasConditions() {
        return !conditions.isEmpty();
    }

    @Override
    public String toString() {
        return "RuleChainStep{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", conditions=" + conditions.size() +
                ", actions=" + actions.size() +
                ", delayAfter=" + delayAfter +
                ", timeout=" + timeout +
                ", enabled=" + enabled +
                '}';
    }
}
