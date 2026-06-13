package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import top.mcocet.meta.Action;

/**
 * MovementSync Walk 动作：向指定方向行走
 */
public class MSWalkAction implements Action {

    public enum WalkDirection {
        FRONT, BACK, LEFT, RIGHT, NORTH, SOUTH, EAST, WEST
    }

    private final WalkDirection direction;
    private final long timeMs;
    private final double speed;

    public MSWalkAction(WalkDirection direction, long timeMs) {
        this(direction, timeMs, -1);
    }

    public MSWalkAction(WalkDirection direction, long timeMs, double speed) {
        this.direction = direction;
        this.timeMs = timeMs;
        this.speed = speed;
    }

    @Override
    public void execute(Session session) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) {
                System.err.println("[LanConnect] MovementSync 未加载，无法执行 walk");
                return;
            }

            Object movementController = movementSync.getClass().getMethod("getMovementController").invoke(movementSync);
            Class<?> walkMovementClass = Class.forName("xin.bbtt.movements.WalkMovement");

            org.joml.Vector3d velocity = calculateVelocity(movementSync);
            double actualSpeed = speed > 0 ? speed : getMovementSpeed();
            velocity.mul(actualSpeed);

            Object walkMovement = walkMovementClass.getConstructor(org.joml.Vector3d.class, long.class)
                    .newInstance(velocity, timeMs);

            movementController.getClass().getMethod("addMovement", Class.forName("xin.bbtt.movement.Movement"))
                    .invoke(movementController, walkMovement);

            System.out.println("[LanConnect] 开始向 " + direction + " 行走 " + timeMs + "ms");
        } catch (Exception e) {
            System.err.println("[LanConnect] Walk 动作执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private org.joml.Vector3d calculateVelocity(Object movementSync) throws Exception {
        float yaw = ((java.util.concurrent.atomic.AtomicReference<Float>) movementSync.getClass()
                .getField("yaw").get(movementSync)).get();

        double yawRad = Math.toRadians(yaw);
        org.joml.Vector3d forward = new org.joml.Vector3d(-Math.sin(yawRad), 0, Math.cos(yawRad));
        org.joml.Vector3d right = new org.joml.Vector3d(-Math.cos(yawRad), 0, -Math.sin(yawRad));

        switch (direction) {
            case FRONT -> { return forward; }
            case BACK -> { return forward.negate(); }
            case LEFT -> { return right.negate(); }
            case RIGHT -> { return right; }
            case NORTH -> { return new org.joml.Vector3d(0, 0, -1); }
            case SOUTH -> { return new org.joml.Vector3d(0, 0, 1); }
            case EAST -> { return new org.joml.Vector3d(1, 0, 0); }
            case WEST -> { return new org.joml.Vector3d(-1, 0, 0); }
            default -> { return forward; }
        }
    }

    private double getMovementSpeed() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return (Double) clazz.getField("movementSpeed").get(null);
        } catch (Exception e) {
            return 0.2159;
        }
    }

    @Override
    public String getType() {
        return "ms_walk";
    }

    private Object getMovementSyncInstance() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return clazz.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public WalkDirection getDirection() { return direction; }
    public long getTimeMs() { return timeMs; }
}
