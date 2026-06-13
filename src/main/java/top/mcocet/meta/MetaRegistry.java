package top.mcocet.meta;

import top.mcocet.meta.action.*;
import top.mcocet.meta.condition.*;
import top.mcocet.meta.movement.*;

import java.util.*;

/**
 * 指令元注册表：管理所有可用的指令元
 * 提供指令元的注册、查询和列举功能
 */
public class MetaRegistry {

    private final Map<String, ConditionMeta> conditionMetas = new HashMap<>();
    private final Map<String, ActionMeta> actionMetas = new HashMap<>();
    private final Set<String> disabledConditionMetas = new HashSet<>();
    private final Set<String> disabledActionMetas = new HashSet<>();

    public MetaRegistry() {
        registerDefaultMetas();
    }

    /**
     * 从配置加载禁用状态
     */
    public void loadDisabledMetas(List<String> disabledConditions, List<String> disabledActions) {
        for (ConditionMeta meta : conditionMetas.values()) {
            meta.setDisabled(false);
        }
        for (ActionMeta meta : actionMetas.values()) {
            meta.setDisabled(false);
        }
        disabledConditionMetas.clear();
        disabledActionMetas.clear();

        if (disabledConditions != null) {
            for (String metaId : disabledConditions) {
                ConditionMeta meta = conditionMetas.get(metaId);
                if (meta != null) {
                    meta.setDisabled(true);
                    disabledConditionMetas.add(metaId);
                }
            }
        }
        if (disabledActions != null) {
            for (String metaId : disabledActions) {
                ActionMeta meta = actionMetas.get(metaId);
                if (meta != null) {
                    meta.setDisabled(true);
                    disabledActionMetas.add(metaId);
                }
            }
        }
    }

    /**
     * 注册默认的条件指令元和动作指令元
     */
    private void registerDefaultMetas() {
        // ========== 条件指令元 ==========
        // 标题检测
        conditionMetas.put("cond.title.register", new ConditionMeta(
                "cond.title.register", "title", "检测标题包含注册关键词",
                new TitleCondition(Arrays.asList("注册", "register"))
        ));
        conditionMetas.put("cond.title.login", new ConditionMeta(
                "cond.title.login", "title", "检测标题包含登录关键词",
                new TitleCondition(Arrays.asList("登陆", "登录", "login"))
        ));
        conditionMetas.put("cond.title.success", new ConditionMeta(
                "cond.title.success", "title", "检测标题包含成功关键词",
                new TitleCondition(Arrays.asList("成功", "success"))
        ));

        // 服务器状态
        conditionMetas.put("cond.server.login", new ConditionMeta(
                "cond.server.login", "server", "检测当前为登录服",
                new ServerStateCondition(ServerStateCondition.ServerType.LOGIN)
        ));
        conditionMetas.put("cond.server.game", new ConditionMeta(
                "cond.server.game", "server", "检测当前为游戏服",
                new ServerStateCondition(ServerStateCondition.ServerType.GAME)
        ));

        // 聊天消息
        conditionMetas.put("cond.chat.question", new ConditionMeta(
                "cond.chat.question", "chat", "检测聊天包含问题分隔符",
                new ChatCondition(Arrays.asList("丨"))
        ));
        conditionMetas.put("cond.chat.hello", new ConditionMeta(
                "cond.chat.hello", "chat", "检测聊天包含问候关键词",
                new ChatCondition(Arrays.asList("hello", "hi", "你好", "在吗"))
        ));
        conditionMetas.put("cond.chat.queue_position", new ConditionMeta(
                "cond.chat.queue_position", "chat", "检测队列位置消息",
                new ChatCondition(Arrays.asList("队列", "queue", "位置"))
        ));
        conditionMetas.put("cond.chat.private_message", new ConditionMeta(
                "cond.chat.private_message", "chat", "检测私聊消息",
                new ChatCondition(Arrays.asList("来自"))
        ));
        conditionMetas.put("cond.chat.public_message", new ConditionMeta(
                "cond.chat.public_message", "chat", "检测公共聊天消息",
                new ChatCondition(Arrays.asList("<", "[", "说:"))
        ));

        // 断开连接
        conditionMetas.put("cond.disconnect.ms_auth_failed", new ConditionMeta(
                "cond.disconnect.ms_auth_failed", "disconnect", "检测微软认证失败",
                new KeywordCondition(Arrays.asList("微软认证失败", "auth failed", "认证过期"))
        ));

        // 物品检测
        conditionMetas.put("cond.item.join_button", new ConditionMeta(
                "cond.item.join_button", "item", "检测加入游戏物品",
                new ItemCondition(ItemCondition.ItemCheckType.ITEM_NAME, Arrays.asList("加入游戏", "Game"))
        ));
        conditionMetas.put("cond.item.join_button_record", new ConditionMeta(
                "cond.item.join_button_record", "item", "检测加入游戏物品（记录模式）",
                new ItemCondition(ItemCondition.ItemCheckType.ITEM_NAME, Arrays.asList("加入游戏", "Game"))
        ));

        // 登录流状态
        conditionMetas.put("cond.login.completed", new ConditionMeta(
                "cond.login.completed", "login_flow", "检测登录已完成",
                new LoginFlowCondition(LoginFlowCondition.FlowCheckType.COMPLETED)
        ));
        conditionMetas.put("cond.login.not_completed", new ConditionMeta(
                "cond.login.not_completed", "login_flow", "检测登录未完成",
                new LoginFlowCondition(LoginFlowCondition.FlowCheckType.NOT_COMPLETED)
        ));

        // ========== 动作指令元 ==========
        // 发送命令
        actionMetas.put("action.cmd.register", new ActionMeta(
                "action.cmd.register", "command", "发送注册命令",
                new SendCommandAction("reg {password} {password}", true)
        ));
        actionMetas.put("action.cmd.login", new ActionMeta(
                "action.cmd.login", "command", "发送登录命令",
                new SendCommandAction("l {password}", true)
        ));
        actionMetas.put("action.cmd.say_hello", new ActionMeta(
                "action.cmd.say_hello", "command", "发送打招呼命令",
                new SendCommandAction("say Hello!")
        ));

        // 发送聊天消息
        actionMetas.put("action.chat.reply", new ActionMeta(
                "action.chat.reply", "chat", "发送聊天回复",
                new SendChatAction("收到！")
        ));

        // 答题
        actionMetas.put("action.cmd.answer_question", new ActionMeta(
                "action.cmd.answer_question", "command", "自动答题",
                new AnswerQuestionAction("questions.json")
        ));

        // 日志
        actionMetas.put("action.log.queue_position", new ActionMeta(
                "action.log.queue_position", "log", "记录队列位置",
                new LogAction("队列位置更新")
        ));
        actionMetas.put("action.log.private_chat", new ActionMeta(
                "action.log.private_chat", "log", "记录私聊消息",
                new LogAction("收到私聊消息")
        ));
        actionMetas.put("action.log.public_chat", new ActionMeta(
                "action.log.public_chat", "log", "记录公聊消息",
                new LogAction("收到公聊消息")
        ));
        actionMetas.put("action.log.record_join_slot", new ActionMeta(
                "action.log.record_join_slot", "log", "记录加入游戏物品槽位",
                new LogAction("记录加入游戏按钮")
        ));

        // 重置登录流
        actionMetas.put("action.cmd.reset_loginflow", new ActionMeta(
                "action.cmd.reset_loginflow", "noop", "重置登录流程",
                new NoOpAction("重置登录流程")
        ));

        // 刷新认证
        actionMetas.put("action.cmd.refresh_auth", new ActionMeta(
                "action.cmd.refresh_auth", "noop", "刷新微软认证",
                new NoOpAction("刷新微软认证")
        ));

        // 使用物品
        actionMetas.put("action.item.use_slot2", new ActionMeta(
                "action.item.use_slot2", "item", "使用物品栏2号位物品",
                new UseItemAction(2)
        ));
        actionMetas.put("action.item.use_slot4", new ActionMeta(
                "action.item.use_slot4", "item", "使用物品栏4号位物品",
                new UseItemAction(4)
        ));

        // 移动
        actionMetas.put("action.move.forward", new ActionMeta(
                "action.move.forward", "move", "向前移动1格",
                new MoveAction(1, 0, 0, true)
        ));
        actionMetas.put("action.move.jump", new ActionMeta(
                "action.move.jump", "move", "向上跳跃",
                new MoveAction(0, 1, 0, true)
        ));

        // 旋转
        actionMetas.put("action.rotate.north", new ActionMeta(
                "action.rotate.north", "rotate", "面向北方",
                new RotateAction(180.0f, 0.0f)
        ));
        actionMetas.put("action.rotate.south", new ActionMeta(
                "action.rotate.south", "rotate", "面向南方",
                new RotateAction(0.0f, 0.0f)
        ));

        // ========== MovementSync 集成指令元（可选依赖） ==========
        registerMovementSyncMetas();
    }

    /**
     * 当 MovementSync 后加载时调用，重新注册 MS 指令元
     */
    public void registerMovementSyncMetasIfLoaded() {
        if (isMovementSyncLoaded() && !msMetasRegistered) {
            registerMovementSyncMetas();
            System.out.println("[LanConnect] MovementSync 已加载，已注册 MS 指令元");
        }
    }

    private boolean msMetasRegistered = false;

    /**
     * 注册 MovementSync 相关的条件指令元和动作指令元
     * 当 MovementSync 未加载或 org.joml 不可用时，这些指令元会被标记为禁用
     */
    private void registerMovementSyncMetas() {
        if (msMetasRegistered) return;
        
        boolean msLoaded = isMovementSyncLoaded();

        if (!msLoaded) {
            // MovementSync 未加载，不创建任何 MS 实例（避免类加载时找不到 org.joml）
            // 只记录元数据 ID，标记为禁用
            markMSMetasDisabled();
            System.out.println("[LanConnect] MovementSync 未加载，已跳过 MS 指令元注册");
            return;
        }

        // 检查 org.joml 是否可用（LanConnect 的类加载器可能无法访问 MovementSync 的依赖）
        try {
            Class.forName("org.joml.Vector3d");
        } catch (ClassNotFoundException e) {
            markMSMetasDisabled();
            System.out.println("[LanConnect] MovementSync 已加载但 org.joml 不可用，已跳过 MS 指令元注册（建议在 plugin.yml 中添加 depend: MovementSync）");
            return;
        }

        msMetasRegistered = true;
        
        // MovementSync 已加载且 org.joml 可用，安全创建 MS 实例
        // --- 条件指令元 ---
        // 位置条件
        conditionMetas.put("cond.ms.pos.x_range", new ConditionMeta(
                "cond.ms.pos.x_range", "ms_position", "检测 X 坐标范围",
                new MSPositionCondition(MSPositionCondition.Axis.X, MSPositionCondition.CompareType.RANGE, 0, 100)
        ));
        conditionMetas.put("cond.ms.pos.y_above", new ConditionMeta(
                "cond.ms.pos.y_above", "ms_position", "检测 Y 坐标高于某值",
                new MSPositionCondition(MSPositionCondition.Axis.Y, MSPositionCondition.CompareType.GREATER, 64)
        ));
        conditionMetas.put("cond.ms.pos.z_equal", new ConditionMeta(
                "cond.ms.pos.z_equal", "ms_position", "检测 Z 坐标等于某值",
                new MSPositionCondition(MSPositionCondition.Axis.Z, MSPositionCondition.CompareType.EQUAL, 0)
        ));

        // 实体条件
        conditionMetas.put("cond.ms.entity.player_near", new ConditionMeta(
                "cond.ms.entity.player_near", "ms_entity", "检测附近是否有玩家",
                new MSEntityCondition(MSEntityCondition.EntityCheckType.DISTANCE, "PLAYER", 10.0)
        ));
        conditionMetas.put("cond.ms.entity.hostile", new ConditionMeta(
                "cond.ms.entity.hostile", "ms_entity", "检测附近是否有敌对生物",
                new MSEntityCondition(MSEntityCondition.EntityCheckType.DISTANCE, "ZOMBIE", 15.0)
        ));

        // 方块条件
        conditionMetas.put("cond.ms.block.ground_solid", new ConditionMeta(
                "cond.ms.block.ground_solid", "ms_block", "检测脚下是否为固体方块",
                new MSBlockCondition(MSBlockCondition.BlockCheckType.IS_SOLID)
        ));
        conditionMetas.put("cond.ms.block.diggable", new ConditionMeta(
                "cond.ms.block.diggable", "ms_block", "检测前方方块是否可挖掘",
                new MSBlockCondition(MSBlockCondition.BlockCheckType.IS_DIGGABLE)
        ));

        // --- 动作指令元 ---
        // 移动动作
        actionMetas.put("action.ms.goto", new ActionMeta(
                "action.ms.goto", "ms_goto", "寻路移动到指定坐标",
                new MSGotoAction(0, 64, 0)
        ));
        actionMetas.put("action.ms.walk_front", new ActionMeta(
                "action.ms.walk_front", "ms_walk", "向前行走",
                new MSWalkAction(MSWalkAction.WalkDirection.FRONT, 1000)
        ));
        actionMetas.put("action.ms.walk_back", new ActionMeta(
                "action.ms.walk_back", "ms_walk", "向后行走",
                new MSWalkAction(MSWalkAction.WalkDirection.BACK, 1000)
        ));
        actionMetas.put("action.ms.jump", new ActionMeta(
                "action.ms.jump", "ms_jump", "跳跃",
                new MSJumpAction()
        ));

        // 视角动作
        actionMetas.put("action.ms.lookat", new ActionMeta(
                "action.ms.lookat", "ms_lookat", "看向指定坐标",
                new MSLookAtAction(0, 64, 0)
        ));

        // 交互动作
        actionMetas.put("action.ms.digblock", new ActionMeta(
                "action.ms.digblock", "ms_digblock", "挖掘指定方块",
                new MSDigBlockAction(0, 64, 0)
        ));
        actionMetas.put("action.ms.attack_entity", new ActionMeta(
                "action.ms.attack_entity", "ms_interactentity", "攻击实体",
                new MSInteractEntityAction(0, MSInteractEntityAction.InteractType.ATTACK)
        ));
        actionMetas.put("action.ms.interact_entity", new ActionMeta(
                "action.ms.interact_entity", "ms_interactentity", "交互实体",
                new MSInteractEntityAction(0, MSInteractEntityAction.InteractType.INTERACT)
        ));
    }

    /**
     * 标记所有 MS 指令元为禁用状态
     */
    private void markMSMetasDisabled() {
        String[] condIds = {
            "cond.ms.pos.x_range", "cond.ms.pos.y_above", "cond.ms.pos.z_equal",
            "cond.ms.entity.player_near", "cond.ms.entity.hostile",
            "cond.ms.block.ground_solid", "cond.ms.block.diggable"
        };
        String[] actionIds = {
            "action.ms.goto", "action.ms.walk_front", "action.ms.walk_back", "action.ms.jump",
            "action.ms.lookat", "action.ms.digblock", "action.ms.attack_entity", "action.ms.interact_entity"
        };
        for (String id : condIds) {
            disabledConditionMetas.add(id);
        }
        for (String id : actionIds) {
            disabledActionMetas.add(id);
        }
    }

    /**
     * 检测 MovementSync 是否已加载
     * 使用多种方式检测，确保可靠性
     */
    private boolean isMovementSyncLoaded() {
        // 方式1：直接类加载检测
        try {
            Class.forName("xin.bbtt.MovementSync");
            return true;
        } catch (ClassNotFoundException e) {
            // 类不在当前类加载器，尝试其他方式
        }
        
        // 方式2：通过 Bot 的 PluginManager 检测
        try {
            if (xin.bbtt.mcbot.Bot.INSTANCE.getPluginManager().isPluginLoaded("MovementSync")) {
                return true;
            }
        } catch (Exception e) {
            // Bot 可能未初始化
        }
        
        return false;
    }

    public void registerConditionMeta(ConditionMeta meta) {
        conditionMetas.put(meta.getMetaId(), meta);
    }

    public void registerActionMeta(ActionMeta meta) {
        actionMetas.put(meta.getMetaId(), meta);
    }

    public ConditionMeta getConditionMeta(String metaId) {
        return conditionMetas.get(metaId);
    }

    public ActionMeta getActionMeta(String metaId) {
        return actionMetas.get(metaId);
    }

    public Collection<ConditionMeta> getAllConditionMetas() {
        return conditionMetas.values();
    }

    public Collection<ActionMeta> getAllActionMetas() {
        return actionMetas.values();
    }

    public Set<String> getConditionMetaIds() {
        return conditionMetas.keySet();
    }

    public Set<String> getActionMetaIds() {
        return actionMetas.keySet();
    }

    public boolean disableConditionMeta(String metaId) {
        ConditionMeta meta = conditionMetas.get(metaId);
        if (meta != null) {
            meta.setDisabled(true);
            disabledConditionMetas.add(metaId);
            return true;
        }
        return false;
    }

    public boolean enableConditionMeta(String metaId) {
        ConditionMeta meta = conditionMetas.get(metaId);
        if (meta != null) {
            meta.setDisabled(false);
            disabledConditionMetas.remove(metaId);
            return true;
        }
        return false;
    }

    public boolean disableActionMeta(String metaId) {
        ActionMeta meta = actionMetas.get(metaId);
        if (meta != null) {
            meta.setDisabled(true);
            disabledActionMetas.add(metaId);
            return true;
        }
        return false;
    }

    public boolean enableActionMeta(String metaId) {
        ActionMeta meta = actionMetas.get(metaId);
        if (meta != null) {
            meta.setDisabled(false);
            disabledActionMetas.remove(metaId);
            return true;
        }
        return false;
    }
}
