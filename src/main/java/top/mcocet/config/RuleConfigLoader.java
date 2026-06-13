package top.mcocet.config;

import org.yaml.snakeyaml.Yaml;
import top.mcocet.meta.*;
import top.mcocet.meta.action.*;
import top.mcocet.meta.chain.*;
import top.mcocet.meta.condition.*;
import top.mcocet.meta.movement.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 规则链配置加载器：从 rules/ 文件夹读取规则链文件并注册到 RuleChainManager
 * 全新顺序执行模型：规则按链组织，链内步骤按顺序执行，步骤间支持延迟
 */
public class RuleConfigLoader {

    private final MetaRegistry metaRegistry;
    private final RuleChainManager chainManager;
    private Path rulesFolder;

    public RuleConfigLoader(MetaRegistry metaRegistry, RuleChainManager chainManager) {
        this.metaRegistry = metaRegistry;
        this.chainManager = chainManager;
    }

    /**
     * 加载规则链配置
     */
    public void load() {
        initRulesFolder();
        loadChainsFromFolder();
    }

    /**
     * 重新加载规则链
     */
    public void reload() {
        chainManager.clearChains();
        load();
    }

    /**
     * 初始化规则文件夹
     */
    private void initRulesFolder() {
        rulesFolder = PluginConfig.getDataFolder().resolve("rules");
        if (PluginConfig.load().isDebug()) {
            System.out.println("[LanConnect] [DEBUG] initRulesFolder() rulesFolder=" + rulesFolder);
        }
        try {
            if (!Files.exists(rulesFolder)) {
                Files.createDirectories(rulesFolder);
            }
            // 始终尝试释放示例规则链
            createExampleChains();
            // 释放资源文件（questions.json 等）
            extractResourceFiles();
        } catch (IOException e) {
            throw new RuntimeException("无法创建规则文件夹", e);
        }
    }

    /**
     * 从插件资源目录释放文件到插件数据目录
     */
    private void extractResourceFiles() {
        Path dataFolder = PluginConfig.getDataFolder();
        // 释放 questions.json（用于自动答题功能）
        extractResource("/questions.json", dataFolder.resolve("questions.json"));
    }

    /**
     * 从 classpath 资源释放到指定路径
     * @return 是否执行了释放操作
     */
    private boolean extractResource(String resourcePath, Path targetPath) {
        try {
            if (Files.exists(targetPath)) return false;
            Files.createDirectories(targetPath.getParent());
            try (var in = getClass().getResourceAsStream(resourcePath)) {
                if (in != null) {
                    Files.copy(in, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[LanConnect] 已释放资源文件: " + targetPath);
                    return true;
                } else {
                    System.err.println("[LanConnect] [DEBUG] 资源文件不存在于 classpath: " + resourcePath);
                }
            }
        } catch (IOException e) {
            System.err.println("[LanConnect] 释放资源文件失败: " + resourcePath + " -> " + e.getMessage());
        }
        return false;
    }

    /**
     * 创建示例规则链文件
     */
    private void createExampleChains() throws IOException {
        boolean debug = PluginConfig.load().isDebug();
        if (debug) {
            System.out.println("[LanConnect] [DEBUG] createExampleChains() 开始释放规则链文件");
        }
        String[] chainFiles = {
            "login_flow.yml",
            "register_flow.yml",
            "auto_reply.yml",
            "queue_position.yml",
            "answer_question.yml"
        };

        int releasedCount = 0;
        for (String fileName : chainFiles) {
            Path targetPath = rulesFolder.resolve(fileName);
            boolean extracted = extractResource("/rules/" + fileName, targetPath);
            if (extracted) releasedCount++;
        }

        if (debug) {
            System.out.println("[LanConnect] [DEBUG] createExampleChains() 释放了 " + releasedCount + " 个规则链文件（共 " + chainFiles.length + " 个）");
        }
        System.out.println("[LanConnect] 已从资源文件释放 " + chainFiles.length + " 个示例规则链");
    }

    /**
     * 从 rules 文件夹加载所有规则链文件
     */
    private void loadChainsFromFolder() {
        boolean debug = PluginConfig.load().isDebug();
        try {
            var files = Files.list(rulesFolder)
                    .filter(p -> p.toString().endsWith(".yml"))
                    .toList();

            if (debug) {
                System.out.println("[LanConnect] [DEBUG] loadChainsFromFolder() 找到 " + files.size() + " 个 .yml 文件");
            }

            if (files.isEmpty()) {
                System.out.println("[LanConnect] 规则文件夹中没有找到 .yml 规则链文件");
                return;
            }

            for (Path chainFile : files) {
                String chainId = chainFile.getFileName().toString().replace(".yml", "");
                try {
                    String content = Files.readString(chainFile, StandardCharsets.UTF_8);
                    Yaml yaml = new Yaml();
                    Map<String, Object> raw = yaml.load(content);
                    if (raw == null) continue;

                    RuleChain chain = parseChain(chainId, raw);
                    if (chain != null) {
                        chainManager.registerChain(chain);
                        System.out.println("[LanConnect] 已加载规则链: " + chainId + " (" + chain.getSteps().size() + " 个步骤)");
                    }
                } catch (Exception e) {
                    System.err.println("[LanConnect] 加载规则链文件 " + chainFile.getFileName() + " 时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("[LanConnect] 读取规则文件夹失败: " + e.getMessage());
        }
    }

    /**
     * 解析规则链配置
     */
    @SuppressWarnings("unchecked")
    private RuleChain parseChain(String chainId, Map<String, Object> raw) {
        String name = (String) raw.getOrDefault("name", chainId);
        boolean enabled = (Boolean) raw.getOrDefault("enabled", true);
        boolean repeat = (Boolean) raw.getOrDefault("repeat", false);
        int cooldown = (Integer) raw.getOrDefault("cooldown", 0);

        // 解析触发条件
        List<ConditionMeta> triggerConditions = parseConditions((List<Map<String, Object>>) raw.get("trigger"));

        // 解析步骤
        List<RuleChainStep> steps = new ArrayList<>();
        List<Map<String, Object>> stepsList = (List<Map<String, Object>>) raw.get("steps");
        if (stepsList != null) {
            int index = 0;
            for (Map<String, Object> stepRaw : stepsList) {
                String stepId = chainId + "_step" + index;
                String stepName = (String) stepRaw.getOrDefault("name", "步骤" + index);
                boolean stepEnabled = (Boolean) stepRaw.getOrDefault("enabled", true);
                int delayAfter = (Integer) stepRaw.getOrDefault("delay_after", 0);
                int timeout = (Integer) stepRaw.getOrDefault("timeout", 0);

                List<ConditionMeta> stepConditions = parseConditions((List<Map<String, Object>>) stepRaw.get("conditions"));
                List<ActionMeta> stepActions = parseActions((List<Map<String, Object>>) stepRaw.get("actions"));

                steps.add(new RuleChainStep(stepId, stepName, stepConditions, stepActions, delayAfter, timeout, stepEnabled));
                index++;
            }
        }

        return new RuleChain(chainId, name, triggerConditions, steps, enabled, repeat, cooldown);
    }

    /**
     * 解析条件列表
     */
    @SuppressWarnings("unchecked")
    private List<ConditionMeta> parseConditions(List<Map<String, Object>> condList) {
        List<ConditionMeta> conditions = new ArrayList<>();
        if (condList == null) return conditions;

        for (Map<String, Object> condMap : condList) {
            String metaId = (String) condMap.get("meta_id");
            if (metaId != null) {
                ConditionMeta meta = metaRegistry.getConditionMeta(metaId);
                if (meta != null) {
                    // 解析参数并创建新的条件实例
                    Map<String, Object> params = (Map<String, Object>) condMap.get("params");
                    if (params != null) {
                        Condition condition = createConditionFromParams(meta.getCondition(), params);
                        if (condition != null) {
                            meta = new ConditionMeta(meta.getMetaId(), meta.getMetaType(), meta.getDescription(), condition);
                        }
                    }
                    conditions.add(meta);
                } else {
                    System.err.println("[LanConnect] 未知的条件指令元: " + metaId);
                }
            }
        }
        return conditions;
    }

    /**
     * 解析动作列表
     */
    @SuppressWarnings("unchecked")
    private List<ActionMeta> parseActions(List<Map<String, Object>> actionList) {
        List<ActionMeta> actions = new ArrayList<>();
        if (actionList == null) return actions;

        for (Map<String, Object> actionMap : actionList) {
            String metaId = (String) actionMap.get("meta_id");
            if (metaId != null) {
                ActionMeta meta = metaRegistry.getActionMeta(metaId);
                if (meta != null) {
                    // 解析参数并创建新的动作实例
                    Map<String, Object> params = (Map<String, Object>) actionMap.get("params");
                    if (params != null) {
                        Action action = createActionFromParams(meta.getAction(), params);
                        if (action != null) {
                            meta = new ActionMeta(meta.getMetaId(), meta.getMetaType(), meta.getDescription(), action);
                        }
                    }
                    actions.add(meta);
                } else {
                    System.err.println("[LanConnect] 未知的动作指令元: " + metaId);
                }
            }
        }
        return actions;
    }

    /**
     * 根据参数创建条件实例（支持 MovementSync 参数化配置）
     * 使用类名字符串判断，避免 MovementSync 未加载时触发类加载
     */
    private Condition createConditionFromParams(Condition base, Map<String, Object> params) {
        String className = base.getClass().getName();
        if (className.endsWith("MSPositionCondition")) {
            String axisStr = ((String) params.getOrDefault("axis", "Y")).toUpperCase();
            String compareStr = ((String) params.getOrDefault("compare", "GREATER")).toUpperCase();
            double value = ((Number) params.getOrDefault("value", 0)).doubleValue();
            double value2 = ((Number) params.getOrDefault("value2", 0)).doubleValue();
            // 通过反射创建，避免直接引用类
            try {
                Class<?> clazz = Class.forName("top.mcocet.meta.movement.MSPositionCondition");
                Class<?> axisEnum = Class.forName("top.mcocet.meta.movement.MSPositionCondition$Axis");
                Class<?> compareEnum = Class.forName("top.mcocet.meta.movement.MSPositionCondition$CompareType");
                Object axis = Enum.valueOf((Class<Enum>) axisEnum, axisStr);
                Object compare = Enum.valueOf((Class<Enum>) compareEnum, compareStr);
                return (Condition) clazz.getConstructor(axisEnum, compareEnum, double.class, double.class)
                        .newInstance(axis, compare, value, value2);
            } catch (Exception e) {
                System.err.println("[LanConnect] 创建 MSPositionCondition 失败: " + e.getMessage());
                return null;
            }
        }
        if (className.endsWith("MSEntityCondition")) {
            String checkTypeStr = ((String) params.getOrDefault("check_type", "DISTANCE")).toUpperCase();
            String entityType = (String) params.getOrDefault("entity_type", "PLAYER");
            double maxDistance = ((Number) params.getOrDefault("max_distance", 10.0)).doubleValue();
            try {
                Class<?> clazz = Class.forName("top.mcocet.meta.movement.MSEntityCondition");
                Class<?> checkEnum = Class.forName("top.mcocet.meta.movement.MSEntityCondition$EntityCheckType");
                Object checkType = Enum.valueOf((Class<Enum>) checkEnum, checkTypeStr);
                return (Condition) clazz.getConstructor(checkEnum, String.class, double.class)
                        .newInstance(checkType, entityType, maxDistance);
            } catch (Exception e) {
                System.err.println("[LanConnect] 创建 MSEntityCondition 失败: " + e.getMessage());
                return null;
            }
        }
        if (className.endsWith("MSBlockCondition")) {
            String checkTypeStr = ((String) params.getOrDefault("check_type", "IS_SOLID")).toUpperCase();
            String blockType = (String) params.getOrDefault("block_type", null);
            try {
                Class<?> clazz = Class.forName("top.mcocet.meta.movement.MSBlockCondition");
                Class<?> checkEnum = Class.forName("top.mcocet.meta.movement.MSBlockCondition$BlockCheckType");
                Object checkType = Enum.valueOf((Class<Enum>) checkEnum, checkTypeStr);
                return (Condition) clazz.getConstructor(checkEnum, String.class)
                        .newInstance(checkType, blockType);
            } catch (Exception e) {
                System.err.println("[LanConnect] 创建 MSBlockCondition 失败: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * 根据参数创建动作实例（支持 MovementSync 参数化配置）
     * 使用类名字符串判断，避免 MovementSync 未加载时触发类加载
     */
    private Action createActionFromParams(Action base, Map<String, Object> params) {
        String className = base.getClass().getName();
        if (className.endsWith("MSGotoAction")) {
            int x = ((Number) params.getOrDefault("x", 0)).intValue();
            int y = ((Number) params.getOrDefault("y", 64)).intValue();
            int z = ((Number) params.getOrDefault("z", 0)).intValue();
            try {
                Class<?> clazz = Class.forName("top.mcocet.meta.movement.MSGotoAction");
                return (Action) clazz.getConstructor(int.class, int.class, int.class).newInstance(x, y, z);
            } catch (Exception e) {
                System.err.println("[LanConnect] 创建 MSGotoAction 失败: " + e.getMessage());
                return null;
            }
        }
        if (className.endsWith("MSWalkAction")) {
            String directionStr = ((String) params.getOrDefault("direction", "FRONT")).toUpperCase();
            long time = ((Number) params.getOrDefault("time", 1000)).longValue();
            double speed = ((Number) params.getOrDefault("speed", -1)).doubleValue();
            try {
                Class<?> clazz = Class.forName("top.mcocet.meta.movement.MSWalkAction");
                Class<?> dirEnum = Class.forName("top.mcocet.meta.movement.MSWalkAction$WalkDirection");
                Object direction = Enum.valueOf((Class<Enum>) dirEnum, directionStr);
                return (Action) clazz.getConstructor(dirEnum, long.class).newInstance(direction, time);
            } catch (Exception e) {
                System.err.println("[LanConnect] 创建 MSWalkAction 失败: " + e.getMessage());
                return null;
            }
        }
        if (className.endsWith("MSLookAtAction")) {
            double x = ((Number) params.getOrDefault("x", 0)).doubleValue();
            double y = ((Number) params.getOrDefault("y", 64)).doubleValue();
            double z = ((Number) params.getOrDefault("z", 0)).doubleValue();
            try {
                Class<?> clazz = Class.forName("top.mcocet.meta.movement.MSLookAtAction");
                return (Action) clazz.getConstructor(double.class, double.class, double.class).newInstance(x, y, z);
            } catch (Exception e) {
                System.err.println("[LanConnect] 创建 MSLookAtAction 失败: " + e.getMessage());
                return null;
            }
        }
        if (className.endsWith("MSDigBlockAction")) {
            int x = ((Number) params.getOrDefault("x", 0)).intValue();
            int y = ((Number) params.getOrDefault("y", 64)).intValue();
            int z = ((Number) params.getOrDefault("z", 0)).intValue();
            try {
                Class<?> clazz = Class.forName("top.mcocet.meta.movement.MSDigBlockAction");
                return (Action) clazz.getConstructor(int.class, int.class, int.class).newInstance(x, y, z);
            } catch (Exception e) {
                System.err.println("[LanConnect] 创建 MSDigBlockAction 失败: " + e.getMessage());
                return null;
            }
        }
        if (className.endsWith("MSInteractEntityAction")) {
            int entityId = ((Number) params.getOrDefault("entity_id", 0)).intValue();
            String interactTypeStr = ((String) params.getOrDefault("interact_type", "ATTACK")).toUpperCase();
            try {
                Class<?> clazz = Class.forName("top.mcocet.meta.movement.MSInteractEntityAction");
                Class<?> typeEnum = Class.forName("top.mcocet.meta.movement.MSInteractEntityAction$InteractType");
                Object interactType = Enum.valueOf((Class<Enum>) typeEnum, interactTypeStr);
                return (Action) clazz.getConstructor(int.class, typeEnum).newInstance(entityId, interactType);
            } catch (Exception e) {
                System.err.println("[LanConnect] 创建 MSInteractEntityAction 失败: " + e.getMessage());
                return null;
            }
        }
        if (className.endsWith("SendChatAction")) {
            String message = (String) params.getOrDefault("message", "Hello!");
            return new SendChatAction(message);
        }
        return null;
    }
}
