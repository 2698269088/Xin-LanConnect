package top.mcocet.meta.condition;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import top.mcocet.login.LoginFlowState;
import top.mcocet.meta.Condition;

/**
 * 登录流状态条件：检测登录流是否处于指定状态
 * 用于规则系统中控制登录流程的顺序（如先登录后点击加入游戏）
 */
public class LoginFlowCondition implements Condition {

    public enum FlowCheckType {
        COMPLETED,   // 登录已完成
        NOT_COMPLETED, // 登录未完成（进行中或未开始）
        ANY          // 任意状态（总是返回true）
    }

    private final FlowCheckType checkType;
    private LoginFlowState loginFlowState;

    public LoginFlowCondition(FlowCheckType checkType) {
        this.checkType = checkType;
    }

    /**
     * 设置要检查的登录流状态实例
     */
    public void setLoginFlowState(LoginFlowState loginFlowState) {
        this.loginFlowState = loginFlowState;
    }

    @Override
    public boolean check(Session session, Packet packet) {
        if (checkType == FlowCheckType.ANY) {
            return true;
        }

        boolean isCompleted = loginFlowState != null && loginFlowState.isCompleted();

        return switch (checkType) {
            case COMPLETED -> isCompleted;
            case NOT_COMPLETED -> !isCompleted;
            default -> true;
        };
    }

    @Override
    public String getType() {
        return "login_flow";
    }

    public FlowCheckType getCheckType() {
        return checkType;
    }
}
