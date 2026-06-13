package top.mcocet.login;

/**
 * 登录流状态机：管理多步骤登录流程
 * 对应 XinMetaPlugin 的 LoginFlow 功能
 */
public class LoginFlowState {
    
    public enum FlowState {
        IDLE,       // 未开始
        REGISTER,   // 等待注册
        LOGIN,      // 等待登录
        SUCCESS,    // 登录成功
        FAILED      // 登录失败
    }
    
    private FlowState state = FlowState.IDLE;
    private long lastActionTime = 0;
    private int cooldown = 2000;
    
    public void reset() {
        state = FlowState.IDLE;
        lastActionTime = 0;
    }
    
    public void onRegisterDetected() {
        if (state == FlowState.IDLE || state == FlowState.REGISTER) {
            state = FlowState.REGISTER;
        }
    }
    
    public void onLoginDetected() {
        if (state == FlowState.REGISTER || state == FlowState.LOGIN) {
            state = FlowState.LOGIN;
        }
    }
    
    public void onSuccessDetected() {
        state = FlowState.SUCCESS;
    }
    
    public void onFailed() {
        state = FlowState.FAILED;
    }
    
    public boolean canAction() {
        return System.currentTimeMillis() - lastActionTime >= cooldown;
    }
    
    public void markAction() {
        lastActionTime = System.currentTimeMillis();
    }
    
    public FlowState getState() {
        return state;
    }
    
    public boolean isCompleted() {
        return state == FlowState.SUCCESS;
    }
    
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
    
    public int getCooldown() {
        return cooldown;
    }
}
