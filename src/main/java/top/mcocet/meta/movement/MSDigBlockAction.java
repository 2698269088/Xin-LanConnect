package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import top.mcocet.meta.Action;

/**
 * MovementSync 挖掘方块动作：挖掘指定位置的方块
 */
public class MSDigBlockAction implements Action {

    private final int x;
    private final int y;
    private final int z;

    public MSDigBlockAction(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void execute(Session session) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) {
                System.err.println("[LanConnect] MovementSync 未加载，无法执行 digblock");
                return;
            }

            Object movementController = movementSync.getClass().getMethod("getMovementController").invoke(movementSync);
            Class<?> digBlockMovementClass = Class.forName("xin.bbtt.movements.DigBlockMovement");
            Object digBlockMovement = digBlockMovementClass.getConstructor(
                            Class.forName("org.cloudburstmc.math.vector.Vector3i"))
                    .newInstance(org.cloudburstmc.math.vector.Vector3i.from(x, y, z));

            movementController.getClass().getMethod("addMovement", Class.forName("xin.bbtt.movement.Movement"))
                    .invoke(movementController, digBlockMovement);

            System.out.println("[LanConnect] 开始挖掘方块 (" + x + ", " + y + ", " + z + ")");
        } catch (Exception e) {
            System.err.println("[LanConnect] DigBlock 动作执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return "ms_digblock";
    }

    private Object getMovementSyncInstance() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return clazz.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
}
