package top.mcocet.meta;

/**
 * 指令元接口
 * 每个指令元代表一个独立的行为单元，可以是一个条件检测或一个动作执行
 * 是指令系统中最小的可复用单元
 */
public interface InstructionMeta {

    /**
     * 获取指令元的唯一标识
     */
    String getMetaId();

    /**
     * 获取指令元的类型
     */
    String getMetaType();

    /**
     * 获取指令元的描述
     */
    String getDescription();

    /**
     * 检查指令元是否被禁用
     */
    boolean isDisabled();

    /**
     * 设置指令元的禁用状态
     */
    void setDisabled(boolean disabled);
}
