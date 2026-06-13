package top.mcocet.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mcocet.Main;
import top.mcocet.config.PluginConfig;
import xin.bbtt.mcbot.Bot;

public class AutoReconnectListener extends SessionAdapter {
    private static final Logger log = LoggerFactory.getLogger(AutoReconnectListener.class.getSimpleName());
    private int reconnectAttempts = 0;
    private volatile boolean isReconnecting = false;

    @Override
    public void disconnected(DisconnectedEvent event) {
        PluginConfig.ReconnectConfig reconnectConfig = Main.getConfig().getReconnect();
        if (!reconnectConfig.isEnabled()) {
            log.info("[LanConnect] 连接断开，自动重连已禁用");
            return;
        }

        if (isReconnecting) return;
        isReconnecting = true;

        new Thread(() -> {
            try {
                while (shouldContinueReconnecting(reconnectConfig)) {
                    int delay = calculateDelay(reconnectConfig);
                    log.info("[LanConnect] {} 秒后尝试第 {} 次重连...", delay, reconnectAttempts + 1);
                    Thread.sleep(delay * 1000L);

                    try {
                        Bot.INSTANCE.start();
                        log.info("[LanConnect] 重连成功！");
                        reconnectAttempts = 0;
                        isReconnecting = false;
                        return;
                    } catch (Exception e) {
                        reconnectAttempts++;
                        log.warn("[LanConnect] 重连失败: {}", e.getMessage());
                    }
                }

                log.error("[LanConnect] 已达到最大重连次数，停止重连");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[LanConnect] 重连线程被中断");
            } finally {
                isReconnecting = false;
            }
        }, "LanConnect-Reconnect").start();
    }

    private boolean shouldContinueReconnecting(PluginConfig.ReconnectConfig config) {
        if (config.getMaxAttempts() < 0) return true;
        return reconnectAttempts < config.getMaxAttempts();
    }

    private int calculateDelay(PluginConfig.ReconnectConfig config) {
        int baseDelay = config.getDelaySeconds();
        if (config.isExponentialBackoff()) {
            return baseDelay * (1 << Math.min(reconnectAttempts, 6));
        }
        return baseDelay;
    }
}
