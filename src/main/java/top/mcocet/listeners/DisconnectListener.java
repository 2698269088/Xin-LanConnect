package top.mcocet.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.LangManager;
import xin.bbtt.mcbot.Utils;
import xin.bbtt.mcbot.auth.AccountLoader;
import xin.bbtt.mcbot.config.BotConfig;
import xin.bbtt.mcbot.config.BotConfigData;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.DisconnectEvent;
import top.mcocet.login.LoginFlowState;

/**
 * 断开连接监听器：处理微软认证失败等断开连接事件
 * 对应 XinMetaPlugin 的 DisconnectListener
 */
public class DisconnectListener implements Listener {
    
    private static final Logger log = LoggerFactory.getLogger(DisconnectListener.class.getSimpleName());
    private final LoginFlowState loginFlow;
    
    public DisconnectListener(LoginFlowState loginFlow) {
        this.loginFlow = loginFlow;
    }
    
    @EventHandler
    public void onDisconnect(DisconnectEvent event) {
        if (loginFlow != null) {
            loginFlow.reset();
        }
        
        String reason = Utils.toString(event.getReason());
        if (!"§c微软认证失败".equals(reason)) return;
        
        log.warn("[LanConnect] 微软认证失败，尝试刷新认证...");
        
        BotConfig config = Bot.INSTANCE.getConfig();
        BotConfigData configData = config.getConfigData();
        
        boolean shouldStopBot = false;
        
        try {
            configData.setAccount(AccountLoader.refresh());
            log.info("[LanConnect] 微软认证刷新成功");
        } catch (Exception e) {
            log.error("[LanConnect] 微软认证刷新失败: {}", e.getMessage());
            configData.getAccount().setFullSession(null);
            shouldStopBot = true;
        } finally {
            try {
                config.saveToFile();
            } catch (Exception e) {
                log.error("[LanConnect] 保存配置失败: {}", e.getMessage());
            }
        }
        
        if (shouldStopBot) {
            Bot.INSTANCE.stop();
        }
    }
}
