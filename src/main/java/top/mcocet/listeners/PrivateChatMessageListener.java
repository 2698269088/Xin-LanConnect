package top.mcocet.listeners;

import org.geysermc.mcprotocollib.auth.GameProfile;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.PrivateChatEvent;
import xin.bbtt.mcbot.events.SystemChatMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 私聊消息监听器：检测并转发私聊消息
 * 对应 XinMetaPlugin 的 PrivateChatMessageListener
 */
public class PrivateChatMessageListener implements Listener {
    
    private static final Logger log = LoggerFactory.getLogger(PrivateChatMessageListener.class.getSimpleName());
    
    @EventHandler
    public void onChatMessage(SystemChatMessageEvent event) {
        if (event.isOverlay()) return;
        String text = event.getText();
        if (!text.startsWith("§d来自 ")) return;
        
        text = text.replaceFirst("§d来自 ", "");
        String[] parts = text.split(": ");
        if (parts.length < 2) return;
        
        String playerName = parts[0];
        String message = text.replaceFirst(playerName + ": §d", "").trim();
        
        for (GameProfile profile : Bot.INSTANCE.players.values()) {
            if (profile.getName().equals(playerName)) {
                PrivateChatEvent privateChatEvent = new PrivateChatEvent(profile, message);
                Bot.INSTANCE.getPluginManager().events().callEvent(privateChatEvent);
                log.info("[LanConnect] 私聊消息来自 {}: {}", playerName, message);
                break;
            }
        }
    }
}
