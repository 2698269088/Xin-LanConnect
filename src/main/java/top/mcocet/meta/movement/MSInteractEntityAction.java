package top.mcocet.meta.movement;

import org.geysermc.mcprotocollib.network.Session;
import top.mcocet.meta.Action;

/**
 * MovementSync 实体交互动作：攻击/交互实体
 */
public class MSInteractEntityAction implements Action {

    public enum InteractType {
        ATTACK, INTERACT, INTERACT_AT
    }

    private final int entityId;
    private final InteractType interactType;
    private final org.joml.Vector3d targetPos;  // 仅 INTERACT_AT 使用

    public MSInteractEntityAction(int entityId, InteractType interactType) {
        this(entityId, interactType, null);
    }

    public MSInteractEntityAction(int entityId, InteractType interactType, org.joml.Vector3d targetPos) {
        this.entityId = entityId;
        this.interactType = interactType;
        this.targetPos = targetPos;
    }

    @Override
    public void execute(Session session) {
        try {
            Object movementSync = getMovementSyncInstance();
            if (movementSync == null) {
                System.err.println("[LanConnect] MovementSync 未加载，无法执行 interactentity");
                return;
            }

            Object movementController = movementSync.getClass().getMethod("getMovementController").invoke(movementSync);
            Class<?> interactEntityMovementClass = Class.forName("xin.bbtt.movements.InteractEntityMovement");

            Object interactAction;
            switch (interactType) {
                case ATTACK -> interactAction = Class.forName("org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction")
                        .getEnumConstants()[0]; // ATTACK
                case INTERACT -> interactAction = Class.forName("org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction")
                        .getEnumConstants()[1]; // INTERACT
                case INTERACT_AT -> interactAction = Class.forName("org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction")
                        .getEnumConstants()[2]; // INTERACT_AT
                default -> interactAction = Class.forName("org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction")
                        .getEnumConstants()[1];
            }

            Object interactMovement;
            if (interactType == InteractType.INTERACT_AT && targetPos != null) {
                interactMovement = interactEntityMovementClass.getConstructor(int.class,
                                Class.forName("org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction"),
                                org.joml.Vector3d.class)
                        .newInstance(entityId, interactAction, targetPos);
            } else {
                interactMovement = interactEntityMovementClass.getConstructor(int.class,
                                Class.forName("org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction"))
                        .newInstance(entityId, interactAction);
            }

            movementController.getClass().getMethod("addMovement", Class.forName("xin.bbtt.movement.Movement"))
                    .invoke(movementController, interactMovement);

            System.out.println("[LanConnect] 对实体 " + entityId + " 执行 " + interactType);
        } catch (Exception e) {
            System.err.println("[LanConnect] InteractEntity 动作执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return "ms_interactentity";
    }

    private Object getMovementSyncInstance() {
        try {
            Class<?> clazz = Class.forName("xin.bbtt.MovementSync");
            return clazz.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public int getEntityId() { return entityId; }
    public InteractType getInteractType() { return interactType; }
}
