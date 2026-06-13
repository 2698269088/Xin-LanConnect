package top.mcocet.meta.action;

import org.geysermc.mcprotocollib.network.Session;
import top.mcocet.meta.Action;

/**
 * 空动作：不执行任何操作（用于占位或特殊逻辑）
 */
public class NoOpAction implements Action {

    private final String description;

    public NoOpAction(String description) {
        this.description = description;
    }

    @Override
    public void execute(Session session) {
        // 不执行任何操作
    }

    @Override
    public String getType() {
        return "noop";
    }

    public String getDescription() { return description; }
}
