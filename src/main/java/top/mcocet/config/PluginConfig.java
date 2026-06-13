package top.mcocet.config;

import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginConfig {
    private ServerConfig server = new ServerConfig();
    private LoginConfig login = new LoginConfig();
    private ReconnectConfig reconnect = new ReconnectConfig();
    private boolean debug = false;

    public ServerConfig getServer() { return server; }
    public void setServer(ServerConfig server) { this.server = server; }
    public LoginConfig getLogin() { return login; }
    public void setLogin(LoginConfig login) { this.login = login; }
    public ReconnectConfig getReconnect() { return reconnect; }
    public void setReconnect(ReconnectConfig reconnect) { this.reconnect = reconnect; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public static class ServerConfig {
        private String host = "localhost";
        private int port = 25565;
        private List<GameMode> loginGameModes = new ArrayList<>();

        public ServerConfig() {
            loginGameModes.add(GameMode.ADVENTURE);
        }

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public List<GameMode> getLoginGameModes() { return loginGameModes; }
        public void setLoginGameModes(List<GameMode> loginGameModes) { this.loginGameModes = loginGameModes; }
    }

    public static class LoginConfig {
        private boolean enabled = false;
        private String password = "";
        private int cooldown = 2000;
        private List<LoginStep> steps = new ArrayList<>();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public int getCooldown() { return cooldown; }
        public void setCooldown(int cooldown) { this.cooldown = cooldown; }
        public List<LoginStep> getSteps() { return steps; }
        public void setSteps(List<LoginStep> steps) { this.steps = steps; }
    }

    public static class LoginStep {
        private List<String> matchPatterns = new ArrayList<>();
        private String command = "";
        private boolean skipWhenMatch = false;

        public List<String> getMatchPatterns() { return matchPatterns; }
        public void setMatchPatterns(List<String> matchPatterns) { this.matchPatterns = matchPatterns; }
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        public boolean isSkipWhenMatch() { return skipWhenMatch; }
        public void setSkipWhenMatch(boolean skipWhenMatch) { this.skipWhenMatch = skipWhenMatch; }
    }

    public static class ReconnectConfig {
        private boolean enabled = true;
        private int maxAttempts = 10;
        private int delaySeconds = 5;
        private boolean exponentialBackoff = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public int getDelaySeconds() { return delaySeconds; }
        public void setDelaySeconds(int delaySeconds) { this.delaySeconds = delaySeconds; }
        public boolean isExponentialBackoff() { return exponentialBackoff; }
        public void setExponentialBackoff(boolean exponentialBackoff) { this.exponentialBackoff = exponentialBackoff; }
    }

    private static Path dataFolder;

    /**
     * 获取插件数据目录（插件JAR所在目录下的 LanConnect/ 文件夹）
     */
    public static Path getDataFolder() {
        if (dataFolder == null) {
            try {
                java.net.URL url = PluginConfig.class.getProtectionDomain().getCodeSource().getLocation();
                java.io.File jarFile = new java.io.File(url.toURI());
                dataFolder = jarFile.getParentFile().toPath().resolve("LanConnect");
            } catch (Exception e) {
                // 回退到当前工作目录
                dataFolder = Path.of("LanConnect");
            }
        }
        return dataFolder;
    }

    public static PluginConfig load() {
        Path configDir = getDataFolder();
        Path configFile = configDir.resolve("LanConnect.yml");

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            if (!Files.exists(configFile)) {
                extractDefaultConfig(configFile);
            }

            String content = Files.readString(configFile, StandardCharsets.UTF_8);
            Yaml yaml = new Yaml();
            Map<String, Object> raw = yaml.load(content);

            return parseConfig(raw);
        } catch (IOException e) {
            throw new RuntimeException("无法加载配置文件", e);
        }
    }

    /**
     * 从插件资源目录释放默认配置文件
     */
    private static void extractDefaultConfig(Path targetPath) throws IOException {
        try (var in = PluginConfig.class.getResourceAsStream("/LanConnect.yml")) {
            if (in != null) {
                Files.copy(in, targetPath);
                System.out.println("[LanConnect] 已释放默认配置文件: " + targetPath);
            } else {
                // 如果资源文件不存在，创建最小默认配置
                Files.writeString(targetPath, "server:\n  host: \"localhost\"\n  port: 25565\n", StandardCharsets.UTF_8);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static PluginConfig parseConfig(Map<String, Object> raw) {
        PluginConfig config = new PluginConfig();

        if (raw != null && raw.containsKey("server")) {
            Map<String, Object> serverMap = (Map<String, Object>) raw.get("server");
            ServerConfig server = new ServerConfig();
            if (serverMap != null) {
                if (serverMap.containsKey("host")) server.setHost((String) serverMap.get("host"));
                if (serverMap.containsKey("port")) server.setPort((Integer) serverMap.get("port"));
                if (serverMap.containsKey("loginGameModes")) {
                    List<String> modesRaw = (List<String>) serverMap.get("loginGameModes");
                    if (modesRaw != null) {
                        List<GameMode> modes = new ArrayList<>();
                        for (String mode : modesRaw) {
                            modes.add(GameMode.valueOf(mode.toUpperCase()));
                        }
                        server.setLoginGameModes(modes);
                    }
                }
            }
            config.setServer(server);
        }

        if (raw != null && raw.containsKey("login")) {
            Map<String, Object> loginMap = (Map<String, Object>) raw.get("login");
            LoginConfig login = new LoginConfig();
            if (loginMap != null) {
                if (loginMap.containsKey("enabled")) login.setEnabled((Boolean) loginMap.get("enabled"));
                if (loginMap.containsKey("password")) login.setPassword((String) loginMap.get("password"));
                if (loginMap.containsKey("cooldown")) login.setCooldown((Integer) loginMap.get("cooldown"));
                if (loginMap.containsKey("steps")) {
                    List<Map<String, Object>> stepsRaw = (List<Map<String, Object>>) loginMap.get("steps");
                    if (stepsRaw != null) {
                        List<LoginStep> steps = new ArrayList<>();
                        for (Map<String, Object> stepRaw : stepsRaw) {
                            if (stepRaw == null) continue;
                            LoginStep step = new LoginStep();
                            if (stepRaw.containsKey("matchPatterns")) {
                                step.setMatchPatterns((List<String>) stepRaw.get("matchPatterns"));
                            }
                            if (stepRaw.containsKey("command")) step.setCommand((String) stepRaw.get("command"));
                            if (stepRaw.containsKey("skipWhenMatch")) step.setSkipWhenMatch((Boolean) stepRaw.get("skipWhenMatch"));
                            steps.add(step);
                        }
                        login.setSteps(steps);
                    }
                }
            }
            config.setLogin(login);
        }

        if (raw != null && raw.containsKey("reconnect")) {
            Map<String, Object> reconnectMap = (Map<String, Object>) raw.get("reconnect");
            ReconnectConfig reconnect = new ReconnectConfig();
            if (reconnectMap != null) {
                if (reconnectMap.containsKey("enabled")) reconnect.setEnabled((Boolean) reconnectMap.get("enabled"));
                if (reconnectMap.containsKey("maxAttempts")) reconnect.setMaxAttempts((Integer) reconnectMap.get("maxAttempts"));
                if (reconnectMap.containsKey("delaySeconds")) reconnect.setDelaySeconds((Integer) reconnectMap.get("delaySeconds"));
                if (reconnectMap.containsKey("exponentialBackoff")) reconnect.setExponentialBackoff((Boolean) reconnectMap.get("exponentialBackoff"));
            }
            config.setReconnect(reconnect);
        }

        if (raw != null && raw.containsKey("debug")) {
            config.setDebug((Boolean) raw.get("debug"));
        }

        return config;
    }
}
