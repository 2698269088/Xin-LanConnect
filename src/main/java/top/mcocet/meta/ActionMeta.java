package top.mcocet.meta;

/**
 * 动作指令元：封装一个动作执行作为指令元
 */
public class ActionMeta implements InstructionMeta {

    private final String metaId;
    private final String metaType;
    private final String description;
    private final Action action;
    private boolean disabled;

    public ActionMeta(String metaId, String metaType, String description, Action action) {
        this(metaId, metaType, description, action, false);
    }

    public ActionMeta(String metaId, String metaType, String description, Action action, boolean disabled) {
        this.metaId = metaId;
        this.metaType = metaType;
        this.description = description;
        this.action = action;
        this.disabled = disabled;
    }

    /**
     * 执行动作
     * 如果指令元被禁用，则不执行任何操作
     */
    public void execute(org.geysermc.mcprotocollib.network.Session session) {
        if (disabled) {
            return;
        }
        if (action != null) {
            action.execute(session);
        }
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

    public Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "ActionMeta{" +
                "id='" + metaId + '\'' +
                ", type='" + metaType + '\'' +
                ", desc='" + description + '\'' +
                ", disabled=" + disabled +
                '}';
    }
}
