package top.mcocet.meta.action;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import top.mcocet.meta.Action;
import xin.bbtt.mcbot.Bot;

/**
 * 发送聊天消息动作：向服务器发送普通聊天消息（非命令）
 */
public class SendChatAction implements Action {

    private final String message;
    private final boolean templateExpand;

    public SendChatAction(String message) {
        this(message, false);
    }

    public SendChatAction(String message, boolean templateExpand) {
        this.message = message;
        this.templateExpand = templateExpand;
    }

    @Override
    public void execute(Session session) {
        String msg = message;
        if (templateExpand && msg.contains("{password}")) {
            // 从插件配置获取密码，如果为空则从 Bot 全局配置获取
            String password = top.mcocet.Main.getConfig().getLogin().getPassword();
            if (password == null || password.isEmpty()) {
                try {
                    password = Bot.INSTANCE.getConfig().getConfigData().getAccount().getPassword();
                } catch (Exception e) {
                    // 忽略，使用空密码
                }
            }
            msg = msg.replace("{password}", password != null ? password : "");
        }
        // 使用 Bot 的 sendChatMessage 方法发送普通聊天消息
        Bot.INSTANCE.sendChatMessage(msg);
    }

    @Override
    public String getType() {
        return "send_chat";
    }

    public String getMessage() {
        return message;
    }
}
