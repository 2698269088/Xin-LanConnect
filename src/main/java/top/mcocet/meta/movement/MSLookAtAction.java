package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import top.mcocet.meta.Action;

/**
 * MovementSync LookAt 动作：看向指定坐标
 */
public class MSLookAtAction implements Action {

    private final double x;
    private final double y;
    private final double z;

    public MSLookAtAction(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void execute(Session session) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) {
                System.err.println("[LanConnect] MovementSync 未加载，无法执行 lookat");
                return;
            }

            Object movementController = movementSync.getClass().getMethod("getMovementController").invoke(movementSync);
            Class<?> lookAtMovementClass = Class.forName("xin.bbtt.movements.LookAtMovement");
            Object lookAtMovement = lookAtMovementClass.getConstructor(org.joml.Vector3d.class)
                    .newInstance(new org.joml.Vector3d(x, y, z));

            movementController.getClass().getMethod("addMovement", Class.forName("xin.bbtt.movement.Movement"))
                    .invoke(movementController, lookAtMovement);

            System.out.println("[LanConnect] 看向 (" + x + ", " + y + ", " + z + ")");
        } catch (Exception e) {
            System.err.println("[LanConnect] LookAt 动作执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return "ms_lookat";
    }

    private Object getMovementSyncInstance() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return clazz.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}
