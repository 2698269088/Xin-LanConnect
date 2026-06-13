package top.mcocet.meta.action;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;
import top.mcocet.meta.Action;
import xin.bbtt.mcbot.Bot;

/**
 * 点击容器动作：点击指定容器槽位
 */
public class ClickContainerAction implements Action {

    private final int containerId;
    private final int slot;
    private final int stateId;

    public ClickContainerAction(int containerId, int slot, int stateId) {
        this.containerId = containerId;
        this.slot = slot;
        this.stateId = stateId;
    }

    @Override
    public void execute(Session session) {
        // 简化实现，实际应根据容器状态动态获取
        Bot.INSTANCE.getSession().send(new ServerboundContainerClickPacket(
                containerId,
                stateId,
                slot,
                org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerActionType.CLICK_ITEM,
                org.geysermc.mcprotocollib.protocol.data.game.inventory.ClickItemAction.LEFT_CLICK,
                null,
                new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>()
        ));
    }

    @Override
    public String getType() {
        return "click_container";
    }

    public int getContainerId() { return containerId; }
    public int getSlot() { return slot; }
}
