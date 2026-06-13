package top.mcocet.login;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetTitleTextPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mcocet.config.PluginConfig;
import xin.bbtt.mcbot.Bot;

/**
 * 登录流监听器：检测标题文本，自动处理注册/登录流程
 * 参考 XinMetaPlugin 的 LoginFlow 实现，直接在监听器中发送命令
 */
public class LoginFlowListener extends SessionAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(LoginFlowListener.class.getSimpleName());
    private final LoginFlowState loginFlow;
    
    public LoginFlowListener(LoginFlowState loginFlow) {
        this.loginFlow = loginFlow;
    }
    
    /**
     * 获取登录密码，优先级：插件配置 > Bot 全局配置
     */
    private String getPassword() {
        String pluginPassword = PluginConfig.load().getLogin().getPassword();
        if (pluginPassword != null && !pluginPassword.isEmpty()) {
            return pluginPassword;
        }
        // 插件配置为空，尝试从 Bot 全局配置获取
        try {
            String botPassword = Bot.INSTANCE.getConfig().getConfigData().getAccount().getPassword();
            if (botPassword != null && !botPassword.isEmpty()) {
                log.debug("[LanConnect] 使用 Bot 配置文件中的密码");
                return botPassword;
            }
        } catch (Exception e) {
            log.debug("[LanConnect] 无法从 Bot 配置获取密码: {}", e.getMessage());
        }
        log.warn("[LanConnect] 密码未配置，自动登录将发送空密码");
        return "";
    }
    
    @Override
    public void packetReceived(Session session, Packet packet) {
        if (!(packet instanceof ClientboundSetTitleTextPacket titlePacket)) {
            return;
        }
        
        String text = titlePacket.toString();
        
        // 检测注册标题（优先于登录，因为需要先注册）
        if (text.contains("注册") || text.contains("register")) {
            loginFlow.onRegisterDetected();
            if (loginFlow.canAction() && !loginFlow.isCompleted()) {
                loginFlow.markAction();
                String password = getPassword();
                String cmd = "reg " + password + " " + password;
                session.send(new ServerboundChatCommandPacket(cmd));
                log.info("[LanConnect] 检测到注册标题，发送注册命令");
            }
        }
        
        // 检测登录标题（但排除"登录成功"）
        else if ((text.contains("登陆") || text.contains("登录") || text.contains("login")) 
                && !text.contains("登陆成功") && !text.contains("登录成功")) {
            loginFlow.onLoginDetected();
            if (loginFlow.canAction() && !loginFlow.isCompleted()) {
                loginFlow.markAction();
                String password = getPassword();
                String cmd = "l " + password;
                session.send(new ServerboundChatCommandPacket(cmd));
                log.info("[LanConnect] 检测到登录标题，发送登录命令");
            }
        }
        
        // 检测登录成功
        else if (text.contains("登陆成功") || text.contains("登录成功") || text.contains("success")) {
            loginFlow.onSuccessDetected();
            log.info("[LanConnect] 登录成功！");
        }
    }
    
    public LoginFlowState getLoginFlow() {
        return loginFlow;
    }
}
