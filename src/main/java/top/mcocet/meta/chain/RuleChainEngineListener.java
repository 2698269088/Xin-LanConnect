package top.mcocet.meta.chain;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 规则链引擎监听器：监听数据包并驱动规则链执行
 * 替代原有的 RuleEngineListener，支持顺序执行模型
 */
public class RuleChainEngineListener extends SessionAdapter {

    private static final Logger log = LoggerFactory.getLogger(RuleChainEngineListener.class.getSimpleName());
    private final RuleChainManager chainManager;

    public RuleChainEngineListener(RuleChainManager chainManager) {
        this.chainManager = chainManager;
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        try {
            chainManager.onPacketReceived(session, packet);
        } catch (Exception e) {
            log.error("[LanConnect] 规则链引擎处理数据包出错: {}", e.getMessage(), e);
        }
    }
}
