package top.mcocet.meta;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;

/**
 * 条件接口
 * 所有检测条件都需要实现此接口
 */
public interface Condition {

    /**
     * 检测是否满足此条件
     *
     * @param session 当前会话
     * @param packet  当前数据包
     * @return 如果满足条件返回 true
     */
    boolean check(Session session, Packet packet);

    /**
     * 获取条件类型的标识
     */
    String getType();
}
