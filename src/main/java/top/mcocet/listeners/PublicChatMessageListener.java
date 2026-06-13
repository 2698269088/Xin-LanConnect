package top.mcocet.listeners;

import org.geysermc.mcprotocollib.auth.GameProfile;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Utils;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.PublicChatEvent;
import xin.bbtt.mcbot.events.SystemChatMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公聊消息监听器：检测并转发公共聊天消息
 * 对应 XinMetaPlugin 的 PublicChatMessageListener
 */
public class PublicChatMessageListener implements Listener {
    
    private static final Logger log = LoggerFactory.getLogger(PublicChatMessageListener.class.getSimpleName());
    
    @EventHandler
    public void onChatMessage(SystemChatMessageEvent event) {
        ArrayList<String> strings = Utils.toStrings(event.getContent());
        if (strings.size() != 3) return;
        
        Pattern pattern = Pattern.compile("<(?:§a)?([^§>]+)(?:§f)?>");
        Matcher matcher = pattern.matcher(strings.get(1));
        if (!matcher.find()) return;
        
        String playerName = matcher.group(1);
        String message = strings.get(2);
        if (message.startsWith("§a")) {
            message = message.substring(2);
        }
        
        for (GameProfile profile : Bot.INSTANCE.players.values()) {
            if (profile.getName().equals(playerName)) {
                PublicChatEvent publicChatEvent = new PublicChatEvent(profile, message);
                Bot.INSTANCE.getPluginManager().events().callEvent(publicChatEvent);
                log.info("[LanConnect] 公聊消息 {}: {}", playerName, message);
                break;
            }
        }
    }
}
