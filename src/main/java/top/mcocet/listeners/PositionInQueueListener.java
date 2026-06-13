package top.mcocet.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetActionBarTextPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Utils;

/**
 * 队列位置监听器：检测 ActionBar 中的队列位置信息
 * 对应 XinMetaPlugin 的 PositionInQueueListener
 */
public class PositionInQueueListener extends SessionAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(PositionInQueueListener.class.getSimpleName());
    private int currentPosition = -1;
    
    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundSystemChatPacket systemChatPacket && systemChatPacket.isOverlay()) {
            handleOverlay(Utils.toString(systemChatPacket.getContent()));
        } else if (packet instanceof ClientboundSetActionBarTextPacket actionBarPacket) {
            handleOverlay(Utils.toString(actionBarPacket.getText()));
        }
    }
    
    private void handleOverlay(String text) {
        if (text.startsWith("§0§lPosition in queue: §6§l")) {
            AutoJoinListener.last_action_time = System.currentTimeMillis();
            try {
                String positionStr = text.replace("§0§lPosition in queue: §6§l", "").trim();
                currentPosition = Integer.parseInt(positionStr);
                // log.info("[LanConnect] 队列位置: {}", currentPosition);
            } catch (NumberFormatException ignored) {}
        }
    }
    
    public int getCurrentPosition() {
        return currentPosition;
    }
}
