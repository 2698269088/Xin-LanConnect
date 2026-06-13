package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import top.mcocet.meta.Action;

/**
 * MovementSync Goto 动作：寻路移动到指定坐标
 * 使用 MovementSync 的 D*Lite 寻路系统
 */
public class MSGotoAction implements Action {

    private final int x;
    private final int y;
    private final int z;

    public MSGotoAction(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void execute(Session session) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) {
                System.err.println("[LanConnect] MovementSync 未加载，无法执行 goto");
                return;
            }

            // 设置目标并触发寻路
            movementSync.getClass().getMethod("setActiveGoal", org.joml.Vector3i.class)
                    .invoke(movementSync, new org.joml.Vector3i(x, y, z));

            org.joml.Vector3d currentPos = ((java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d>) movementSync.getClass()
                    .getField("position").get(movementSync)).get();

            Object world = movementSync.getClass().getMethod("getWorld").invoke(movementSync);
            Object movementController = movementSync.getClass().getMethod("getMovementController").invoke(movementSync);

            // 创建起点和终点节点进行寻路
            Class<?> nodeClass = Class.forName("xin.bbtt.pathfinding.Node");
            Object start = nodeClass.getConstructor(int.class, int.class, int.class)
                    .newInstance((int) Math.floor(currentPos.x), (int) Math.floor(currentPos.y), (int) Math.floor(currentPos.z));
            Object goal = nodeClass.getConstructor(int.class, int.class, int.class)
                    .newInstance(x, y, z);

            // 使用 DStarLite 寻路
            Class<?> dStarLiteClass = Class.forName("xin.bbtt.pathfinding.DStarLite");
            Object pathfinder = dStarLiteClass.getConstructor(nodeClass, nodeClass, world.getClass())
                    .newInstance(start, goal, world);

            java.util.List<?> path = (java.util.List<?>) dStarLiteClass.getMethod("findPath", int.class)
                    .invoke(pathfinder, 5000);

            if (path.size() > 1) {
                // 创建 PathMovement 并添加到控制器
                Class<?> pathMovementClass = Class.forName("xin.bbtt.movements.PathMovement");
                Object pathMovement = pathMovementClass.getConstructor(java.util.List.class).newInstance(path);
                movementController.getClass().getMethod("addMovement", Class.forName("xin.bbtt.movement.Movement"))
                        .invoke(movementController, pathMovement);

                System.out.println("[LanConnect] 开始寻路到 (" + x + ", " + y + ", " + z + ")，路径节点数: " + path.size());
            } else {
                System.out.println("[LanConnect] 无法找到有效路径到 (" + x + ", " + y + ", " + z + ")");
            }
        } catch (Exception e) {
            System.err.println("[LanConnect] Goto 动作执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return "ms_goto";
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
