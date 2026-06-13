package top.mcocet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import top.mcocet.config.PluginConfig;
import top.mcocet.config.RuleConfigLoader;
import top.mcocet.listeners.*;
import top.mcocet.login.LoginFlowState;
import top.mcocet.login.LoginFlowListener;
import top.mcocet.meta.MetaRegistry;
import top.mcocet.meta.chain.RuleChainEngineListener;
import top.mcocet.meta.chain.RuleChainManager;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;
import xin.bbtt.mcbot.plugin.MetaPlugin;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Main implements MetaPlugin {
    private static final Logger log = LoggerFactory.getLogger(Main.class.getSimpleName());
    private static PluginConfig config;
    private static MetaRegistry metaRegistry;
    private static RuleChainManager chainManager;
    private static RuleConfigLoader ruleConfigLoader;
    private static LoginFlowState loginFlowState;

    public static PluginConfig getConfig() {
        return config;
    }

    public static MetaRegistry getMetaRegistry() {
        return metaRegistry;
    }

    public static RuleChainManager getChainManager() {
        return chainManager;
    }

    public static LoginFlowState getLoginFlowState() {
        return loginFlowState;
    }

    @Override
    public void onLoad() {
        config = PluginConfig.load();
        metaRegistry = new MetaRegistry();
        chainManager = new RuleChainManager();
        ruleConfigLoader = new RuleConfigLoader(metaRegistry, chainManager);
        loginFlowState = new LoginFlowState();
        loginFlowState.setCooldown(config.getLogin().getCooldown());
        log.info("[LanConnect] 配置文件加载完成");
        if (config.isDebug()) {
            log.info("[LanConnect] [DEBUG] onLoad() 执行完成，config={}, metaRegistry={}, chainManager={}",
                    config, metaRegistry, chainManager);
        }
    }

    @Override
    public void onUnload() {
    }

    @Override
    public void onEnable() {
        if (config.isDebug()) {
            log.info("[LanConnect] [DEBUG] onEnable() 开始执行");
        }

        // 检测 MovementSync 是否已加载（后加载的情况），如果是则重新注册 MS 指令元
        try {
            metaRegistry.registerMovementSyncMetasIfLoaded();
        } catch (Throwable e) {
            log.error("[LanConnect] [DEBUG] registerMovementSyncMetasIfLoaded() 异常: {}", e.getMessage(), e);
        }

        // 设置登录流状态条件所需的 LoginFlowState 实例
        try {
            setLoginFlowStateForConditions();
        } catch (Throwable e) {
            log.error("[LanConnect] [DEBUG] setLoginFlowStateForConditions() 异常: {}", e.getMessage(), e);
        }

        // 加载规则链
        try {
            ruleConfigLoader.load();
            log.info("[LanConnect] 已加载 {} 条规则链", chainManager.getChainCount());
        } catch (Throwable e) {
            log.error("[LanConnect] [DEBUG] ruleConfigLoader.load() 异常: {}", e.getMessage(), e);
        }

        // 注册数据包监听器
        try {
            Bot.INSTANCE.addPacketListener(new RuleChainEngineListener(chainManager), this);
            Bot.INSTANCE.addPacketListener(new ServerStateListener(), this);
            Bot.INSTANCE.addPacketListener(new AutoReconnectListener(), this);
            Bot.INSTANCE.addPacketListener(new LoginFlowListener(loginFlowState), this);
            Bot.INSTANCE.addPacketListener(new AutoJoinListener(loginFlowState), this);
            Bot.INSTANCE.addPacketListener(new AnswerQuestionListener(PluginConfig.getDataFolder().resolve("questions.json")), this);
            Bot.INSTANCE.addPacketListener(new PositionInQueueListener(), this);
            Bot.INSTANCE.addPacketListener(new JoinButtonRecorder(loginFlowState), this);
        } catch (Throwable e) {
            log.error("[LanConnect] [DEBUG] 注册数据包监听器异常: {}", e.getMessage(), e);
        }

        // 注册事件监听器
        try {
            Bot.INSTANCE.getPluginManager().registerEvents(new DisconnectListener(loginFlowState), this);
            Bot.INSTANCE.getPluginManager().registerEvents(new PrivateChatMessageListener(), this);
            Bot.INSTANCE.getPluginManager().registerEvents(new PublicChatMessageListener(), this);
        } catch (Throwable e) {
            log.error("[LanConnect] [DEBUG] 注册事件监听器异常: {}", e.getMessage(), e);
        }

        log.info("[LanConnect] 插件已启用，目标服务器: {}:{}",
                config.getServer().getHost(), config.getServer().getPort());

        if (config.isDebug()) {
            log.info("[LanConnect] [DEBUG] onEnable() 执行完成");
        }
    }

    @Override
    public void onDisable() {
        log.info("[LanConnect] 插件已禁用");
    }

    @Override
    public SocketAddress getServerSocketAddress() {
        return new InetSocketAddress(config.getServer().getHost(), config.getServer().getPort());
    }

    @Override
    public Server getServer(ClientboundLoginPacket loginPacket) {
        GameMode gameMode = loginPacket.getCommonPlayerSpawnInfo().getGameMode();
        if (config.getServer().getLoginGameModes().contains(gameMode)) {
            loginFlowState.reset();
            AutoJoinListener.last_action_time = System.currentTimeMillis();
            return Server.Login;
        }
        return Server.Game;
    }

    /**
     * 设置登录流状态条件所需的 LoginFlowState 实例
     * 让规则系统中的 cond.login.completed 等条件能够检测登录状态
     */
    private void setLoginFlowStateForConditions() {
        var conditionMetas = metaRegistry.getAllConditionMetas();
        for (var meta : conditionMetas) {
            if (meta.getMetaType().equals("login_flow")) {
                var condition = meta.getCondition();
                if (condition instanceof top.mcocet.meta.condition.LoginFlowCondition loginFlowCondition) {
                    loginFlowCondition.setLoginFlowState(loginFlowState);
                }
            }
        }
    }
}