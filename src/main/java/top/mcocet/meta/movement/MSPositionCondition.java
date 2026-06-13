package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import top.mcocet.meta.Condition;

/**
 * MovementSync 位置条件：检测当前坐标是否满足条件
 * 通过 MovementSync.INSTANCE 获取当前位置
 */
public class MSPositionCondition implements Condition {

    public enum CompareType {
        EQUAL, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL, RANGE
    }

    public enum Axis {
        X, Y, Z
    }

    private final Axis axis;
    private final CompareType compareType;
    private final double value;
    private final double value2;

    public MSPositionCondition(Axis axis, CompareType compareType, double value) {
        this(axis, compareType, value, 0);
    }

    public MSPositionCondition(Axis axis, CompareType compareType, double value, double value2) {
        this.axis = axis;
        this.compareType = compareType;
        this.value = value;
        this.value2 = value2;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) return false;

            java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d> position = (java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d>) movementSync.getClass()
                    .getField("position").get(movementSync);
            org.joml.Vector3d current = position.get();

            double currentValue;
            switch (axis) {
                case X -> currentValue = current.x;
                case Y -> currentValue = current.y;
                case Z -> currentValue = current.z;
                default -> currentValue = 0;
            }
            return compare(currentValue);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean compare(double current) {
        switch (compareType) {
            case EQUAL -> { return Math.abs(current - value) < 0.001; }
            case GREATER -> { return current > value; }
            case LESS -> { return current < value; }
            case GREATER_EQUAL -> { return current >= value; }
            case LESS_EQUAL -> { return current <= value; }
            case RANGE -> { return current >= value && current <= value2; }
            default -> { return false; }
        }
    }

    @Override
    public String getType() {
        return "ms_position";
    }

    private Object getMovementSyncInstance() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return clazz.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public Axis getAxis() { return axis; }
    public CompareType getCompareType() { return compareType; }
    public double getValue() { return value; }
    public double getValue2() { return value2; }
}
