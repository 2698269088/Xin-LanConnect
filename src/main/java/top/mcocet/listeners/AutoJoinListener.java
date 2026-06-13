package top.mcocet.listeners;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.TextComponent;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ClickItemAction;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerActionType;
import org.geysermc.mcprotocollib.protocol.data.game.item.HashedStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mcocet.login.LoginFlowState;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;
import xin.bbtt.mcbot.Utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * AutoJoin 监听器：自动检测并点击加入游戏物品
 * 对应 XinMetaPlugin 的 AutoJoinListener
 */
public class AutoJoinListener extends SessionAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(AutoJoinListener.class.getSimpleName());
    private int containerId = -1;
    public static long last_action_time = System.currentTimeMillis();
    public static int join_button_slot = 2;
    
    private final LoginFlowState loginFlow;
    
    public AutoJoinListener(LoginFlowState loginFlow) {
        this.loginFlow = loginFlow;
    }
    
    private void join() {
        if (last_action_time > System.currentTimeMillis() - 2000) return;
        if (Bot.INSTANCE.getServer() != Server.Login) return;
        last_action_time = System.currentTimeMillis();
        
        Bot.INSTANCE.getSession().send(new ServerboundSetCarriedItemPacket(join_button_slot));
        Bot.INSTANCE.getSession().send(
                new ServerboundUseItemPacket(
                        Hand.MAIN_HAND,
                        (int) Instant.now().toEpochMilli(),
                        0,
                        0
                )
        );
        log.info("[LanConnect] 自动使用加入游戏物品 (槽位: {})", join_button_slot);
    }
    
    private boolean isKeywordMatch(ItemStack itemStack) {
        if (itemStack == null) return false;
        String str = itemStack.toString();
        return isKeywordMatch(str);
    }
    
    private boolean isKeywordMatch(String str) {
        return str.contains("Game") || str.contains("戏") || str.contains("队") || str.contains("入");
    }
    
    private void clickSlot(ItemStack itemStack, Session session, int slot, int stateId) {
        HashedStack hashedStack = Utils.itemStackToHashedStack(itemStack);
        Int2ObjectMap<HashedStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(slot, null);
        session.send(new ServerboundContainerClickPacket(
            containerId,
            stateId,
            slot,
            ContainerActionType.CLICK_ITEM,
            ClickItemAction.LEFT_CLICK,
            hashedStack,
            changedSlots
        ));
        log.info("[LanConnect] 自动点击容器槽位: {} (物品: {})", slot, itemStack.toString());
    }
    
    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundContainerClosePacket containerClosePacket) onCloseContainer(containerClosePacket);
        if (Bot.INSTANCE.getServer() != Server.Login) return;
        
        // 只在登录流完成后才执行自动加入游戏操作
        // 这样可以确保先发送登录命令，再点击加入游戏物品
        if (loginFlow != null && !loginFlow.isCompleted()) {
            return;
        }
        
        if (packet instanceof ClientboundOpenScreenPacket openScreenPacket) recordContainer(openScreenPacket);
        if (packet instanceof ClientboundContainerSetContentPacket containerSetContentPacket) onSetContent(containerSetContentPacket, session);
        if (packet instanceof ClientboundContainerSetSlotPacket containerSetSlotPacket) onSetSlot(containerSetSlotPacket, session);
        join();
    }
    
    private void onCloseContainer(ClientboundContainerClosePacket containerClosePacket) {
        if (containerClosePacket.getContainerId() != containerId) return;
        containerId = -1;
    }
    
    private void recordContainer(ClientboundOpenScreenPacket openScreenPacket) {
        if (Bot.INSTANCE.getServer() != Server.Login) return;
        if (!(openScreenPacket.getTitle() instanceof TextComponent title)) return;
        if (!isKeywordMatch(title.content())) return;
        containerId = openScreenPacket.getContainerId();
        log.debug("[LanConnect] 记录容器 ID: {}", containerId);
    }
    
    private void onSetContent(ClientboundContainerSetContentPacket containerSetContentPacket, Session session) {
        if (!(containerSetContentPacket.getContainerId() == containerId)) return;
        ItemStack[] items = containerSetContentPacket.getItems();
        
        List<Integer> keywordMatches = new ArrayList<>();
        for (int slot = 0; slot < items.length - 27; slot++) {
            ItemStack itemStack = items[slot];
            if (itemStack == null) continue;
            if (isKeywordMatch(itemStack)) {
                keywordMatches.add(slot);
            }
        }

        int targetSlot = getTargetSlot(keywordMatches, items);

        if (targetSlot != -1) {
            clickSlot(items[targetSlot], session, targetSlot, containerSetContentPacket.getStateId());
        }
    }
    
    private static int countItemOccurrences(ItemStack[] items, ItemStack target) {
        int count = 0;
        for (int i = 0; i < items.length - 27; i++) {
            if (items[i] != null && items[i].equals(target)) count++;
        }
        return count;
    }
    
    private static int getTargetSlot(List<Integer> keywordMatches, ItemStack[] items) {
        int targetSlot = -1;

        if (keywordMatches.size() == 1) {
            targetSlot = keywordMatches.get(0);
        } else if (keywordMatches.size() > 1) {
            for (int slot : keywordMatches) {
                if (countItemOccurrences(items, items[slot]) == 1) {
                    targetSlot = slot;
                    break;
                }
            }
            if (targetSlot == -1) {
                targetSlot = keywordMatches.get(0);
            }
        }

        if (targetSlot == -1) {
            for (int slot = 0; slot < items.length - 27; slot++) {
                ItemStack itemStack = items[slot];
                if (itemStack == null) continue;
                if (itemStack.getId() == 1034) {
                    targetSlot = slot;
                    break;
                }
            }
        }

        if (targetSlot == -1 && items.length > 4 && items[4] != null) {
            targetSlot = 4;
        }
        return targetSlot;
    }
    
    private void onSetSlot(ClientboundContainerSetSlotPacket containerSetSlotPacket, Session session) {
        if (!(containerSetSlotPacket.getContainerId() == containerId)) return;
        ItemStack itemStack = containerSetSlotPacket.getItem();
        if (itemStack == null) return;
        
        boolean click = false;
        if (isKeywordMatch(itemStack)) {
            click = true;
        } else {
            if (itemStack.getId() == 1034) {
                click = true;
            } else if (containerSetSlotPacket.getSlot() == 4) {
                click = true;
            }
        }

        if (click) {
            clickSlot(itemStack, session, containerSetSlotPacket.getSlot(), containerSetSlotPacket.getStateId());
        }
    }
}
