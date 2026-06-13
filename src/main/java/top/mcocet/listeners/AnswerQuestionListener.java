package top.mcocet.listeners;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 答题监听器：检测系统聊天中的答题消息并自动回答
 * 对应 XinMetaPlugin 的 AnswerQuestionListener
 */
public class AnswerQuestionListener extends SessionAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(AnswerQuestionListener.class.getSimpleName());
    private JsonObject questions;
    private final Path questionFile;
    
    public AnswerQuestionListener(Path questionFile) {
        this.questionFile = questionFile;
        loadQuestions();
    }
    
    private void loadQuestions() {
        try {
            if (Files.exists(questionFile)) {
                String content = Files.readString(questionFile, StandardCharsets.UTF_8);
                questions = JsonParser.parseString(content).getAsJsonObject();
                log.info("[LanConnect] 已加载题库: {} 道题目", questions.size());
            } else {
                // 尝试从资源加载
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("questions.json")),
                        StandardCharsets.UTF_8))) {
                    String content = reader.lines().collect(Collectors.joining("\n"));
                    questions = JsonParser.parseString(content).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            log.error("[LanConnect] 加载题库失败: {}", e.getMessage());
            questions = new JsonObject();
        }
    }
    
    @Override
    public void packetReceived(Session session, Packet packet) {
        if (!(packet instanceof ClientboundSystemChatPacket systemChatPacket)) return;
        
        String fullQuestion = Utils.toString(systemChatPacket.getContent());
        if (!fullQuestion.contains("丨")) return;
        
        String[] parts = fullQuestion.split("丨");
        if (parts.length != 2) return;
        
        String question = parts[0].trim();
        String options = parts[1].trim();
        
        if (!questions.has(question)) {
            log.debug("[LanConnect] 未知题目: {}", question);
            return;
        }
        
        String answerPattern = questions.get(question).getAsString();
        Pattern pattern = Pattern.compile(answerPattern);
        Matcher matcher = pattern.matcher(options);
        
        if (!matcher.find()) {
            log.warn("[LanConnect] 题目 {} 未匹配到答案", question);
            return;
        }
        
        String answer = matcher.group(1);
        Bot.INSTANCE.sendChatMessage(answer);
        log.info("[LanConnect] 自动答题: {} -> {}", question, answer);
    }
}
