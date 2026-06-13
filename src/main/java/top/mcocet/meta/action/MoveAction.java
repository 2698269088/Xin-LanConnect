package top.mcocet.meta.action;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import top.mcocet.meta.Action;
import xin.bbtt.mcbot.Bot;

/**
 * 移动动作：向指定坐标移动
 */
public class MoveAction implements Action {

    private final double x;
    private final double y;
    private final double z;
    private final boolean relative;
    private final boolean onGround;

    public MoveAction(double x, double y, double z) {
        this(x, y, z, false, true);
    }

    public MoveAction(double x, double y, double z, boolean relative) {
        this(x, y, z, relative, true);
    }

    public MoveAction(double x, double y, double z, boolean relative, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.relative = relative;
        this.onGround = onGround;
    }

    @Override
    public void execute(Session session) {
        double targetX = x;
        double targetY = y;
        double targetZ = z;

        if (relative) {
            // 尝试从 MovementSync 获取当前位置进行相对移动
            try {
                Class<?> movementSyncClass = Class.forName("xin.bbtt.MovementSync");
                Object movementSync = movementSyncClass.getField("INSTANCE").get(null);
                if (movementSync != null) {
                    java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d> position =
                            (java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d>) movementSyncClass
                                    .getField("position").get(movementSync);
                    org.joml.Vector3d current = position.get();
                    targetX = current.x + x;
                    targetY = current.y + y;
                    targetZ = current.z + z;
                }
            } catch (Exception e) {
                // MovementSync 未加载，使用默认值
            }
        }

        Bot.INSTANCE.getSession().send(new ServerboundMovePlayerPosRotPacket(
                onGround,
                true, // dismount vehicle
                targetX,
                targetY,
                targetZ,
                0.0f, // yaw
                0.0f  // pitch
        ));
    }

    @Override
    public String getType() {
        return "move";
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public boolean isRelative() { return relative; }
}
