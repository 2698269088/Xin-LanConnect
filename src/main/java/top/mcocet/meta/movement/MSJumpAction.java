package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import top.mcocet.meta.Action;

/**
 * MovementSync Jump 动作：跳跃
 */
public class MSJumpAction implements Action {

    @Override
    public void execute(Session session) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) {
                System.err.println("[LanConnect] MovementSync 未加载，无法执行 jump");
                return;
            }

            Object movementController = movementSync.getClass().getMethod("getMovementController").invoke(movementSync);
            Class<?> jumpMovementClass = Class.forName("xin.bbtt.movements.JumpMovement");
            Object jumpMovement = jumpMovementClass.getConstructor().newInstance();

            movementController.getClass().getMethod("addMovement", Class.forName("xin.bbtt.movement.Movement"))
                    .invoke(movementController, jumpMovement);

            System.out.println("[LanConnect] 执行跳跃");
        } catch (Exception e) {
            System.err.println("[LanConnect] Jump 动作执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return "ms_jump";
    }

    private Object getMovementSyncInstance() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return clazz.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
