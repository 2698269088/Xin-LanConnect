package top.mcocet.meta;

import org.geysermc.mcprotocollib.network.Session;

/**
 * 动作接口
 * 所有可执行的操作都需要实现此接口
 */
public interface Action {

    /**
     * 执行此动作
     *
     * @param session 当前会话
     */
    void execute(Session session);

    /**
     * 获取动作类型的标识
     */
    String getType();
}
