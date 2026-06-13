package top.mcocet.meta.condition;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import top.mcocet.meta.Condition;

import java.util.List;

/**
 * 关键词条件：检测聊天消息是否包含指定关键词
 */
public class KeywordCondition implements Condition {

    private final List<String> keywords;

    public KeywordCondition(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        // 由 RuleEngineListener 传入解析后的消息内容
        return false;
    }

    public boolean checkMessage(String message) {
        if (message == null) return false;
        for (String keyword : keywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    @Override
    public String getType() {
        return "keyword";
    }

    public List<String> getKeywords() { return keywords; }
}
