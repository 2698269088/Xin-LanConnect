package top.mcocet.meta.action;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemPacket;
import top.mcocet.meta.Action;
import xin.bbtt.mcbot.Bot;

import java.time.Instant;

/**
 * 使用物品动作：切换到指定槽位并使用物品
 */
public class UseItemAction implements Action {

    private final int slot;
    private final Hand hand;

    public UseItemAction(int slot) {
        this(slot, Hand.MAIN_HAND);
    }

    public UseItemAction(int slot, Hand hand) {
        this.slot = slot;
        this.hand = hand;
    }

    @Override
    public void execute(Session session) {
        Bot.INSTANCE.getSession().send(new ServerboundSetCarriedItemPacket(slot));
        Bot.INSTANCE.getSession().send(new ServerboundUseItemPacket(
                hand,
                (int) Instant.now().toEpochMilli(),
                0,
                0
        ));
    }

    @Override
    public String getType() {
        return "use_item";
    }

    public int getSlot() {
        return slot;
    }
}
