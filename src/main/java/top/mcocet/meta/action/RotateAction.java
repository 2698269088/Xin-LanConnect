package top.mcocet.meta.action;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import top.mcocet.meta.Action;
import xin.bbtt.mcbot.Bot;

/**
 * 旋转视角动作：改变玩家朝向
 */
public class RotateAction implements Action {

    private final float yaw;
    private final float pitch;

    public RotateAction(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void execute(Session session) {
        double x = 0, y = 0, z = 0;

        // 尝试从 MovementSync 获取当前位置
        try {
            Class<?> movementSyncClass = Class.forName("xin.bbtt.MovementSync");
            Object movementSync = movementSyncClass.getField("INSTANCE").get(null);
            if (movementSync != null) {
                java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d> position =
                        (java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d>) movementSyncClass
                                .getField("position").get(movementSync);
                org.joml.Vector3d current = position.get();
                x = current.x;
                y = current.y;
                z = current.z;
            }
        } catch (Exception e) {
            // MovementSync 未加载，使用默认值
        }

        Bot.INSTANCE.getSession().send(new ServerboundMovePlayerPosRotPacket(
                true,
                true,
                x,
                y,
                z,
                yaw,
                pitch
        ));
    }

    @Override
    public String getType() {
        return "rotate";
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}
