package top.mcocet.meta.condition;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import top.mcocet.meta.Condition;
import xin.bbtt.mcbot.Server;

/**
 * 服务器状态条件：检测当前处于登录服还是游戏服
 */
public class ServerStateCondition implements Condition {

    public enum ServerType {
        LOGIN, GAME, ANY
    }

    private final ServerType expectedType;

    public ServerStateCondition(ServerType expectedType) {
        this.expectedType = expectedType;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        if (expectedType == ServerType.ANY) {
            return true;
        }
        // 通过登录包判断服务器类型
        if (packet instanceof ClientboundLoginPacket loginPacket) {
            var gameMode = loginPacket.getCommonPlayerSpawnInfo().getGameMode();
            // ADVENTURE 模式通常表示登录服
            boolean isLogin = (gameMode == org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode.ADVENTURE);
            return (expectedType == ServerType.LOGIN && isLogin) || (expectedType == ServerType.GAME && !isLogin);
        }
        return false;
    }

    @Override
    public String getType() {
        return "server_state";
    }

    public ServerType getExpectedType() {
        return expectedType;
    }
}
