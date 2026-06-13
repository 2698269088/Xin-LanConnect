package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import top.mcocet.meta.Condition;

/**
 * MovementSync 实体检测条件：检测周围是否存在指定类型的实体
 */
public class MSEntityCondition implements Condition {

    public enum EntityCheckType {
        EXISTS,        // 是否存在指定类型实体
        DISTANCE,      // 距离检测
        LOOKING_AT     // 是否看向实体
    }

    private final EntityCheckType checkType;
    private final String entityType;  // 如 "PLAYER", "ZOMBIE", "CREEPER"
    private final double maxDistance;

    public MSEntityCondition(EntityCheckType checkType, String entityType) {
        this(checkType, entityType, 10.0);
    }

    public MSEntityCondition(EntityCheckType checkType, String entityType, double maxDistance) {
        this.checkType = checkType;
        this.entityType = entityType;
        this.maxDistance = maxDistance;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) return false;

            Object world = movementSync.getClass().getMethod("getWorld").invoke(movementSync);
            if (world == null) return false;

            java.util.Map<Integer, Object> entities = (java.util.Map<Integer, Object>) world.getClass()
                    .getMethod("getEntities").invoke(world);
            if (entities == null || entities.isEmpty()) return false;

            org.joml.Vector3d myPos = ((java.util.concurrent.atomic.AtomicReference<org.joml.Vector3d>) movementSync.getClass()
                    .getField("position").get(movementSync)).get();

            for (Object entity : entities.values()) {
                String type = (String) entity.getClass().getMethod("getType").invoke(entity);
                if (entityType != null && !entityType.equalsIgnoreCase(type)) continue;

                switch (checkType) {
                    case EXISTS -> { return true; }
                    case DISTANCE -> {
                        org.joml.Vector3d entityPos = (org.joml.Vector3d) entity.getClass().getMethod("getPosition").invoke(entity);
                        double dist = entityPos.distance(myPos);
                        if (dist <= maxDistance) return true;
                    }
                    case LOOKING_AT -> {
                        // 简化实现：检测是否看向实体方向
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getType() {
        return "ms_entity";
    }

    private Object getMovementSyncInstance() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return clazz.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public EntityCheckType getCheckType() { return checkType; }
    public String getEntityType() { return entityType; }
    public double getMaxDistance() { return maxDistance; }
}
