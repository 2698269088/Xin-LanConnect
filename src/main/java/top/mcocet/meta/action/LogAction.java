package top.mcocet.meta.action;

import org.geysermc.mcprotocollib.network.Session;
import top.mcocet.meta.Action;

/**
 * 日志动作：输出日志信息（用于调试和记录）
 */
public class LogAction implements Action {

    private final String message;

    public LogAction(String message) {
        this.message = message;
    }

    @Override
    public void execute(Session session) {
        System.out.println("[LanConnect] " + message);
    }

    @Override
    public String getType() {
        return "log";
    }

    public String getMessage() { return message; }
}
