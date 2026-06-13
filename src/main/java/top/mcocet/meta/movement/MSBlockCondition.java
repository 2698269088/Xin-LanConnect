package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import top.mcocet.meta.Condition;

/**
 * MovementSync 方块检测条件：检测指定位置的方块类型
 */
public class MSBlockCondition implements Condition {

    public enum BlockCheckType {
        IS_SOLID,      // 是否为固体方块
        IS_PASSABLE,   // 是否可通行
        IS_DIGGABLE,   // 是否可挖掘
        MATCH_TYPE     // 匹配指定类型
    }

    private final BlockCheckType checkType;
    private final String blockType;  // 用于 MATCH_TYPE
    private final org.joml.Vector3d position;  // 检测位置，null 表示当前脚下

    public MSBlockCondition(BlockCheckType checkType) {
        this(checkType, null, null);
    }

    public MSBlockCondition(BlockCheckType checkType, String blockType) {
        this(checkType, blockType, null);
    }

    public MSBlockCondition(BlockCheckType checkType, String blockType, org.joml.Vector3d position) {
        this.checkType = checkType;
        this.blockType = blockType;
        this.position = position;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) return false;

            Object world = movementSync.getClass().getMethod("getWorld").invoke(movementSync);
            if (world == null) return false;

            org.joml.Vector3d checkPos = position;
            if (checkPos == null) {
                org.joml.Vector3d currentPos = ((java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d>) movementSync.getClass()
                        .getField("position").get(movementSync)).get();
                checkPos = new org.joml.Vector3d(currentPos).add(0, -1, 0); // 脚下方块
            }

            Object blockState = world.getClass().getMethod("getBlockStateAt", org.joml.Vector3d.class).invoke(world, checkPos);
            if (blockState == null) return false;

            switch (checkType) {
                case IS_SOLID -> {
                    return (Boolean) blockState.getClass().getMethod("isSolid").invoke(blockState);
                }
                case IS_PASSABLE -> {
                    return (Boolean) blockState.getClass().getMethod("isPassable").invoke(blockState);
                }
                case IS_DIGGABLE -> {
                    return (Boolean) blockState.getClass().getMethod("diggable").invoke(blockState);
                }
                case MATCH_TYPE -> {
                    String actualType = (String) blockState.getClass().getMethod("blockName").invoke(blockState);
                    return actualType != null && actualType.equalsIgnoreCase(blockType);
                }
                default -> { return false; }
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getType() {
        return "ms_block";
    }

    private Object getMovementSyncInstance() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return clazz.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public BlockCheckType getCheckType() { return checkType; }
    public String getBlockType() { return blockType; }
}
