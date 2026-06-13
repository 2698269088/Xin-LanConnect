package top.mcocet.meta.action;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import top.mcocet.meta.Action;
import xin.bbtt.mcbot.Bot;

/**
 * 发送命令动作：向服务器发送聊天命令
 */
public class SendCommandAction implements Action {

    private final String command;
    private final boolean templateExpand;

    public SendCommandAction(String command) {
        this(command, false);
    }

    public SendCommandAction(String command, boolean templateExpand) {
        this.command = command;
        this.templateExpand = templateExpand;
    }

    @Override
    public void execute(Session session) {
        String cmd = command;
        if (templateExpand && cmd.contains("{password}")) {
            // 从插件配置获取密码，如果为空则从 Bot 全局配置获取
            String password = top.mcocet.Main.getConfig().getLogin().getPassword();
            if (password == null || password.isEmpty()) {
                try {
                    password = Bot.INSTANCE.getConfig().getConfigData().getAccount().getPassword();
                } catch (Exception e) {
                    // 忽略，使用空密码
                }
            }
            cmd = cmd.replace("{password}", password != null ? password : "");
        }
        if (!cmd.startsWith("/")) {
            cmd = "/" + cmd;
        }
        Bot.INSTANCE.getSession().send(new ServerboundChatCommandPacket(cmd));
    }

    @Override
    public String getType() {
        return "send_command";
    }

    public String getCommand() {
        return command;
    }
}
