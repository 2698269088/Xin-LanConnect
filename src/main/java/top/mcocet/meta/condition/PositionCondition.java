package top.mcocet.meta.condition;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import top.mcocet.meta.Condition;

/**
 * 坐标条件：检测玩家当前坐标是否满足条件
 */
public class PositionCondition implements Condition {

    public enum CompareType {
        EQUAL, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL, RANGE
    }

    public enum Axis {
        X, Y, Z
    }

    private final Axis axis;
    private final CompareType compareType;
    private final double value;
    private final double value2; // 用于 RANGE

    public PositionCondition(Axis axis, CompareType compareType, double value) {
        this(axis, compareType, value, 0);
    }

    public PositionCondition(Axis axis, CompareType compareType, double value, double value2) {
        this.axis = axis;
        this.compareType = compareType;
        this.value = value;
        this.value2 = value2;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        if (!(packet instanceof ClientboundPlayerPositionPacket posPacket)) {
            return false;
        }
        double current;
        var position = posPacket.getPosition();
        switch (axis) {
            case X -> current = position.getX();
            case Y -> current = position.getY();
            case Z -> current = position.getZ();
            default -> current = 0;
        }
        return compare(current);
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
        return "position";
    }

    public Axis getAxis() { return axis; }
    public CompareType getCompareType() { return compareType; }
    public double getValue() { return value; }
    public double getValue2() { return value2; }
}
