package top.mcocet.meta;

/**
 * 条件指令元：封装一个条件检测作为指令元
 */
public class ConditionMeta implements InstructionMeta {

    private final String metaId;
    private final String metaType;
    private final String description;
    private final Condition condition;
    private boolean disabled;

    public ConditionMeta(String metaId, String metaType, String description, Condition condition) {
        this(metaId, metaType, description, condition, false);
    }

    public ConditionMeta(String metaId, String metaType, String description, Condition condition, boolean disabled) {
        this.metaId = metaId;
        this.metaType = metaType;
        this.description = description;
        this.condition = condition;
        this.disabled = disabled;
    }

    /**
     * 执行条件检测
     * 如果指令元被禁用，则默认返回 true（视为满足条件）
     */
    public boolean check(org.geysermc.mcprotocollib.network.Session session, org.geysermc.mcprotocollib.network.packet.Packet packet) {
        if (disabled) {
            return true;
        }
        return condition != null && condition.check(session, packet);
    }

    @Override
    public String getMetaId() {
        return metaId;
    }

    @Override
    public String getMetaType() {
        return metaType;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return "ConditionMeta{" +
                "id='" + metaId + '\'' +
                ", type='" + metaType + '\'' +
                ", desc='" + description + '\'' +
                ", disabled=" + disabled +
                '}';
    }
}
