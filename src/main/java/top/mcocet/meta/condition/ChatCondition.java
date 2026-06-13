package top.mcocet.meta.condition;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import top.mcocet.meta.Condition;
import xin.bbtt.mcbot.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 聊天消息条件：检测系统聊天消息是否匹配关键词或正则表达式
 */
public class ChatCondition implements Condition {

    private final List<String> keywords;
    private final String regex;
    private final transient Pattern pattern;

    public ChatCondition(List<String> keywords) {
        this.keywords = keywords;
        this.regex = null;
        this.pattern = null;
    }

    public ChatCondition(String regex) {
        this.keywords = null;
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean check(Session session, Packet packet) {
        if (!(packet instanceof ClientboundSystemChatPacket chatPacket)) {
            return false;
        }
        String text = Utils.toString(chatPacket.getContent());
        if (keywords != null) {
            for (String keyword : keywords) {
                if (text.contains(keyword)) return true;
            }
        }
        if (pattern != null) {
            Matcher matcher = pattern.matcher(text);
            return matcher.find();
        }
        return false;
    }

    @Override
    public String getType() {
        return "chat";
    }
}
