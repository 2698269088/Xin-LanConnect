package top.mcocet.meta.condition;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetTitleTextPacket;
import top.mcocet.meta.Condition;

import java.util.List;

/**
 * 标题文本条件：检测标题是否包含指定关键词
 */
public class TitleCondition implements Condition {

    private final List<String> keywords;
    private final boolean exactMatch;

    public TitleCondition(List<String> keywords) {
        this(keywords, false);
    }

    public TitleCondition(List<String> keywords, boolean exactMatch) {
        this.keywords = keywords;
        this.exactMatch = exactMatch;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        if (!(packet instanceof ClientboundSetTitleTextPacket titlePacket)) {
            return false;
        }
        String text = titlePacket.toString();
        for (String keyword : keywords) {
            if (exactMatch) {
                if (text.equals(keyword)) return true;
            } else {
                if (text.contains(keyword)) return true;
            }
        }
        return false;
    }

    @Override
    public String getType() {
        return "title";
    }

    public List<String> getKeywords() {
        return keywords;
    }
}
