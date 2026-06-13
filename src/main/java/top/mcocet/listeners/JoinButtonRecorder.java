package top.mcocet.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mcocet.login.LoginFlowState;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;

/**
 * 加入游戏按钮记录器：记录加入游戏物品的槽位
 * 对应 XinMetaPlugin 的 JoinButtonRecorder
 * 修改：只在登录流完成后才记录按钮，避免在登录前误点击
 */
public class JoinButtonRecorder extends SessionAdapter {

    private static final Logger log = LoggerFactory.getLogger(JoinButtonRecorder.class.getSimpleName());
    private final LoginFlowState loginFlow;

    public JoinButtonRecorder(LoginFlowState loginFlow) {
        this.loginFlow = loginFlow;
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (!(packet instanceof ClientboundContainerSetSlotPacket containerSetSlotPacket)) return;
        if (Bot.INSTANCE.getServer() == Server.Game) return;
        
        // 只在登录流完成后才记录加入游戏按钮
        // 这样可以确保先发送登录命令，再记录/点击加入游戏物品
        if (loginFlow != null && !loginFlow.isCompleted()) {
            return;
        }
        
        if (!containerSetSlotPacket.toString().contains("加入游戏")) return;
        
        int slot = containerSetSlotPacket.getSlot() % 9;
        AutoJoinListener.join_button_slot = slot;
        log.info("[LanConnect] 记录加入游戏按钮槽位: {}", slot);
    }
}
