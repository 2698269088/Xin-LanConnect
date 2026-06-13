package top.mcocet.meta.action;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import top.mcocet.meta.Action;
import top.mcocet.config.PluginConfig;
import xin.bbtt.mcbot.Bot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * 答题动作：根据问题文件查找答案并发送
 */
public class AnswerQuestionAction implements Action {

    private final String questionFile;

    public AnswerQuestionAction(String questionFile) {
        this.questionFile = questionFile;
    }

    @Override
    public void execute(Session session) {
        // 答题逻辑由 AnswerQuestionListener 处理，这里只是一个占位动作
        // 实际答题在检测到问题时触发
    }

    public void answer(String question, String[] options) {
        Path filePath = PluginConfig.getDataFolder().resolve(questionFile);
        if (!Files.exists(filePath)) {
            System.err.println("[LanConnect] 题库文件不存在: " + filePath);
            return;
        }
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Map<String, String> questions = gson.fromJson(content, new TypeToken<Map<String, String>>(){}.getType());
            String answer = questions.get(question);
            if (answer != null) {
                for (String option : options) {
                    if (option.contains(answer)) {
                        Bot.INSTANCE.getSession().send(new ServerboundChatCommandPacket(option.trim()));
                        System.out.println("[LanConnect] 自动答题: " + question + " -> " + option);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[LanConnect] 读取题库失败: " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "answer_question";
    }

    public String getQuestionFile() { return questionFile; }
}
