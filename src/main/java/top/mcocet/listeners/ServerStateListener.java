package top.mcocet.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mcocet.Main;
import xin.bbtt.mcbot.Server;

public class ServerStateListener extends SessionAdapter {
    private static final Logger log = LoggerFactory.getLogger(ServerStateListener.class.getSimpleName());
    private boolean hasLoggedIn = false;

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundLoginPacket) {
            hasLoggedIn = true;
            Server currentServer = Main.getConfig().getServer().getLoginGameModes().isEmpty()
                    ? Server.Game
                    : Server.Login;
            log.info("[LanConnect] 已连接到服务器，当前状态: {}", currentServer);
        }
    }

    public boolean hasLoggedIn() {
        return hasLoggedIn;
    }

    public void reset() {
        hasLoggedIn = false;
    }
}
