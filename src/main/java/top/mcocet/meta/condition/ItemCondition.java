package top.mcocet.meta.condition;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import top.mcocet.meta.Condition;

import java.util.List;

/**
 * 物品条件：检测容器/物品栏中是否出现指定物品
 */
public class ItemCondition implements Condition {

    public enum ItemCheckType {
        CONTAINER_TITLE, // 检测容器标题
        ITEM_NAME,       // 检测物品名称
        ITEM_ID          // 检测物品ID
    }

    private final ItemCheckType checkType;
    private final List<String> keywords;
    private final int itemId;

    public ItemCondition(ItemCheckType checkType, List<String> keywords) {
        this.checkType = checkType;
        this.keywords = keywords;
        this.itemId = -1;
    }

    public ItemCondition(int itemId) {
        this.checkType = ItemCheckType.ITEM_ID;
        this.keywords = null;
        this.itemId = itemId;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        switch (checkType) {
            case CONTAINER_TITLE -> {
                if (packet instanceof ClientboundOpenScreenPacket screenPacket) {
                    String title = screenPacket.getTitle().toString();
                    for (String keyword : keywords) {
                        if (title.contains(keyword)) return true;
                    }
                }
            }
            case ITEM_NAME -> {
                String itemStr = null;
                if (packet instanceof ClientboundContainerSetContentPacket contentPacket) {
                    itemStr = contentPacket.toString();
                } else if (packet instanceof ClientboundContainerSetSlotPacket slotPacket) {
                    itemStr = slotPacket.toString();
                }
                if (itemStr != null) {
                    for (String keyword : keywords) {
                        if (itemStr.contains(keyword)) return true;
                    }
                }
            }
            case ITEM_ID -> {
                if (packet instanceof ClientboundContainerSetSlotPacket slotPacket) {
                    if (slotPacket.getItem() != null && slotPacket.getItem().getId() == itemId) {
                        return true;
                    }
                }
                if (packet instanceof ClientboundContainerSetContentPacket contentPacket) {
                    for (var item : contentPacket.getItems()) {
                        if (item != null && item.getId() == itemId) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getType() {
        return "item";
    }
}
