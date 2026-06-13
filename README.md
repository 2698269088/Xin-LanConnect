# LanConnect 插件使用文档

LanConnect 是一个基于**规则链（RuleChain）**系统的 XinBot 元插件，用于自动化处理各种游戏场景。规则链支持**顺序执行**、**步骤间延迟**和**条件等待**，非常适合登录、注册、加入游戏等需要按顺序执行的任务。

## 目录

- [快速开始](#快速开始)
- [配置文件](#配置文件)
- [规则链系统](#规则链系统)
- [条件指令元](#条件指令元)
- [动作指令元](#动作指令元)
- [规则链示例](#规则链示例)
- [XinMetaPlugin 等效配置](#xinmetaplugin-等效配置)
- [MovementSync 集成](#movementsync-集成)
- [常见问题](#常见问题)

---

## 快速开始

1. 将插件 JAR 文件放入 Bot 框架的插件目录
2. 首次运行时会自动在插件目录下创建 `LanConnect/` 文件夹
3. 编辑 `LanConnect/LanConnect.yml` 配置服务器地址（密码从 Bot 配置自动读取）
4. 在 `LanConnect/rules/` 目录中编写规则链文件
5. 重启 Bot 或重新加载插件使规则生效

---

## 配置文件

配置文件路径：`LanConnect/LanConnect.yml`

```yaml
# 服务器配置
server:
  host: "2b2t.xin"          # 服务器地址
  port: 25565               # 服务器端口
  loginGameModes:           # 哪些游戏模式表示登录服
    - "ADVENTURE"

# 登录配置
login:
  enabled: true             # 是否启用自动登录
  password: ""              # 登录密码（空则自动使用 Bot 配置中的密码）
  cooldown: 2000            # 登录命令发送间隔（毫秒）

# 重连配置
reconnect:
  enabled: true             # 是否启用自动重连
  maxAttempts: 10           # 最大重连次数
  delaySeconds: 5           # 重连间隔（秒）
  exponentialBackoff: true  # 是否使用指数退避

# 调试开关
debug: false                # 开启后输出更多调试日志
```

> **密码优先级**：`LanConnect.yml` 中的 `login.password` > Bot 全局配置中的密码。如果插件配置为空，自动使用 Bot 配置。

---

## 规则链系统

LanConnect 的核心是**规则链（RuleChain）**系统。规则链将多个步骤组织成一条链，**按顺序执行**，支持步骤间延迟和条件等待。

### 规则链格式

```yaml
chain:
  id: "login_flow"              # 链的唯一标识
  name: "登录并进入游戏流程"      # 链的名称
  enabled: true                 # 是否启用
  repeat: false                 # 是否可重复触发
  cooldown: 5000                # 触发冷却时间（毫秒）
  triggerConditions:            # 触发链执行的条件
    - meta_id: "cond.title.login"
  steps:                        # 步骤列表，按顺序执行
    - id: "send_login"          # 步骤ID
      name: "发送登录命令"       # 步骤名称
      conditions: []            # 步骤执行条件（空则立即执行）
      actions:                  # 步骤动作
        - meta_id: "action.cmd.login"
      delayAfter: 500           # 执行完后等待500ms再执行下一步
      timeout: 0                # 等待条件的超时时间（0=无限等待）
```

### 规则链字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | 字符串 | 是 | 链的唯一标识 |
| `name` | 字符串 | 是 | 链的名称 |
| `enabled` | 布尔值 | 否 | 是否启用，默认 `true` |
| `repeat` | 布尔值 | 否 | 是否可重复触发，默认 `false` |
| `cooldown` | 整数 | 否 | 触发冷却时间（毫秒），默认 `0` |
| `triggerConditions` | 列表 | 是 | 触发链的条件，所有条件满足时启动链 |
| `steps` | 列表 | 是 | 步骤列表，按顺序执行 |

### 步骤字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | 字符串 | 否 | 步骤ID，默认自动生成 |
| `name` | 字符串 | 否 | 步骤名称 |
| `conditions` | 列表 | 否 | 执行条件，空则立即执行 |
| `actions` | 列表 | 否 | 动作列表，条件满足后执行 |
| `delayAfter` | 整数 | 否 | 执行完后延迟（毫秒），默认 `0` |
| `timeout` | 整数 | 否 | 等待条件的超时时间（毫秒），默认 `0`（无限） |
| `enabled` | 布尔值 | 否 | 是否启用，默认 `true` |

### 条件/动作配置格式

```yaml
conditions:
  - meta_id: "条件指令元ID"
    params:                      # 可选，参数化配置
      key1: "value1"
      key2: 123

actions:
  - meta_id: "动作指令元ID"
    params:                      # 可选，参数化配置
      key1: "value1"
      key2: 123
```

---

## 条件指令元

条件指令元用于检测游戏中的特定事件。

### 标题检测

| 指令元ID | 说明 | 默认检测关键词 |
|----------|------|---------------|
| `cond.title.register` | 检测标题包含注册关键词 | 注册、register |
| `cond.title.login` | 检测标题包含登录关键词 | 登陆、登录、login |
| `cond.title.success` | 检测标题包含成功关键词 | 成功、success |
| `cond.title.question` | 检测标题包含答题关键词 | 题、question |
| `cond.title.queue` | 检测标题包含队列关键词 | 队列、queue |
| `cond.title.queue_end` | 检测队列结束 | 无（通过聊天消息检测） |

### 服务器状态

| 指令元ID | 说明 |
|----------|------|
| `cond.server.login` | 检测当前为登录服（游戏模式为 ADVENTURE） |
| `cond.server.game` | 检测当前为游戏服 |

### 聊天消息检测

| 指令元ID | 说明 | 默认检测关键词 |
|----------|------|---------------|
| `cond.chat.question` | 检测聊天包含问题分隔符 | 丨 |
| `cond.chat.hello` | 检测聊天包含问候关键词 | hello、hi、你好、在吗 |
| `cond.chat.queue_position` | 检测队列位置消息 | 队列、queue、位置 |
| `cond.chat.private_message` | 检测私聊消息 | 来自 |
| `cond.chat.public_message` | 检测公共聊天消息 | <、[、说: |

### 登录流状态

| 指令元ID | 说明 |
|----------|------|
| `cond.login.completed` | 检测登录已完成（登录流程成功结束） |
| `cond.login.not_completed` | 检测登录未完成 |

### 物品检测

| 指令元ID | 说明 | 默认检测关键词 |
|----------|------|---------------|
| `cond.item.join_button` | 检测加入游戏物品 | 加入游戏、Game |
| `cond.item.join_button_record` | 检测加入游戏物品（记录模式） | 加入游戏、Game |

### 实体检测（需要 MovementSync）

| 指令元ID | 说明 | 参数 |
|----------|------|------|
| `cond.ms.entity.player_near` | 检测附近是否有玩家 | `entity_type`: PLAYER, `max_distance`: 10 |
| `cond.ms.entity.hostile` | 检测附近是否有敌对生物 | `entity_type`: ZOMBIE, `max_distance`: 15 |

### 位置检测（需要 MovementSync）

| 指令元ID | 说明 | 参数 |
|----------|------|------|
| `cond.ms.pos.x_range` | 检测 X 坐标范围 | `axis`: X, `compare`: RANGE, `value`: 0, `value2`: 100 |
| `cond.ms.pos.y_above` | 检测 Y 坐标高于某值 | `axis`: Y, `compare`: GREATER/LESS, `value`: 64 |
| `cond.ms.pos.z_equal` | 检测 Z 坐标等于某值 | `axis`: Z, `compare`: EQUAL, `value`: 0 |

---

## 动作指令元

动作指令元用于执行游戏中的操作。

### 发送命令

| 指令元ID | 说明 | 默认命令 |
|----------|------|----------|
| `action.cmd.register` | 发送注册命令 | `/reg {password} {password}` |
| `action.cmd.login` | 发送登录命令 | `/l {password}` |
| `action.cmd.say_hello` | 发送打招呼命令 | `/say Hello!` |

> **模板变量**：`{password}` 会自动替换为配置文件中设置的密码。优先级：插件配置 > Bot 全局配置。

### 发送聊天消息

| 指令元ID | 说明 | 参数 |
|----------|------|------|
| `action.chat.reply` | 发送聊天回复（非命令） | `message` - 回复内容 |

### 使用物品

| 指令元ID | 说明 |
|----------|------|
| `action.item.use_slot2` | 切换到物品栏2号位并使用 |
| `action.item.use_slot4` | 切换到物品栏4号位并使用 |
| `action.inv.use_join_item` | 使用加入游戏物品（通过记录的位置） |

### 移动与旋转（需要 MovementSync）

| 指令元ID | 说明 | 参数 |
|----------|------|------|
| `action.ms.goto` | 寻路移动到指定坐标 | `x`, `y`, `z` |
| `action.ms.walk_front` | 向前行走 | `direction`: FRONT, `time`: 1000 |
| `action.ms.walk_back` | 向后行走 | `direction`: BACK, `time`: 1000 |
| `action.ms.jump` | 跳跃 | - |
| `action.ms.lookat` | 看向指定坐标 | `x`, `y`, `z` |
| `action.ms.digblock` | 挖掘指定方块 | `x`, `y`, `z` |
| `action.ms.attack_entity` | 攻击实体 | `entity_id`, `interact_type`: ATTACK |

### 答题与日志

| 指令元ID | 说明 | 参数 |
|----------|------|------|
| `action.cmd.answer_question` | 自动答题（从题库匹配答案） | `question_file` - 题库文件路径 |
| `action.log.queue_position` | 记录队列位置到日志 | - |
| `action.log.private_chat` | 记录私聊消息到日志 | - |
| `action.log.public_chat` | 记录公聊消息到日志 | - |
| `action.log.record_join_slot` | 记录加入游戏物品槽位到日志 | - |

### 登录流控制

| 指令元ID | 说明 |
|----------|------|
| `action.cmd.reset_loginflow` | 重置登录流程状态 |
| `action.cmd.refresh_auth` | 刷新微软认证（断开连接时触发） |

---

## 规则链示例

### 示例1：登录并进入游戏流程

```yaml
chain:
  id: "login_flow"
  name: "登录并进入游戏流程"
  enabled: true
  repeat: false
  cooldown: 5000
  triggerConditions:
    - meta_id: "cond.title.login"
  steps:
    - id: "send_login"
      name: "发送登录命令"
      conditions: []
      actions:
        - meta_id: "action.cmd.login"
      delayAfter: 500

    - id: "wait_login_success"
      name: "等待登录成功"
      conditions:
        - meta_id: "cond.title.success"
      actions: []
      timeout: 10000
      delayAfter: 500

    - id: "record_join_button"
      name: "记录加入游戏按钮"
      conditions:
        - meta_id: "cond.item.join_button_record"
      actions:
        - meta_id: "action.log.record_join_slot"
      delayAfter: 200

    - id: "click_join_item"
      name: "点击加入游戏物品"
      conditions:
        - meta_id: "cond.item.join_button"
      actions:
        - meta_id: "action.inv.use_join_item"
      delayAfter: 1000

    - id: "wait_game_server"
      name: "等待进入游戏服"
      conditions:
        - meta_id: "cond.server.game"
      actions: []
      timeout: 15000
```

**执行流程**：
1. 检测到登录标题 → 触发链
2. 立即发送登录命令
3. 等待10秒，直到检测到"成功"标题
4. 记录加入游戏物品位置
5. 等待检测到加入游戏物品，点击它
6. 等待进入游戏服（最多15秒）

### 示例2：注册流程

```yaml
chain:
  id: "register_flow"
  name: "注册流程"
  enabled: true
  repeat: false
  cooldown: 5000
  triggerConditions:
    - meta_id: "cond.title.register"
  steps:
    - id: "send_register"
      name: "发送注册命令"
      conditions: []
      actions:
        - meta_id: "action.cmd.register"
      delayAfter: 500

    - id: "wait_register_success"
      name: "等待注册成功"
      conditions:
        - meta_id: "cond.title.success"
      actions: []
      timeout: 10000
```

### 示例3：自动回复

```yaml
chain:
  id: "auto_reply"
  name: "自动回复"
  enabled: true
  repeat: true
  cooldown: 1000
  triggerConditions:
    - meta_id: "cond.chat.private_message"
  steps:
    - id: "reply"
      name: "发送回复"
      conditions: []
      actions:
        - meta_id: "action.chat.reply"
      delayAfter: 0
```

### 示例4：队列等待

```yaml
chain:
  id: "queue_wait"
  name: "队列等待"
  enabled: true
  repeat: false
  cooldown: 0
  triggerConditions:
    - meta_id: "cond.title.queue"
  steps:
    - id: "wait_queue"
      name: "等待队列结束"
      conditions:
        - meta_id: "cond.title.queue_end"
      actions: []
      timeout: 300000
      delayAfter: 500

    - id: "send_login_after_queue"
      name: "队列结束后登录"
      conditions: []
      actions:
        - meta_id: "action.cmd.login"
      delayAfter: 500
```

### 示例5：自动答题

```yaml
chain:
  id: "answer_question"
  name: "自动答题"
  enabled: true
  repeat: false
  cooldown: 0
  triggerConditions:
    - meta_id: "cond.title.question"
  steps:
    - id: "answer"
      name: "发送答案"
      conditions: []
      actions:
        - meta_id: "action.cmd.answer_question"
      delayAfter: 500
```

### 示例6：攻击敌对生物（需要 MovementSync）

```yaml
chain:
  id: "attack_hostile"
  name: "攻击附近敌对生物"
  enabled: false
  repeat: true
  cooldown: 500
  triggerConditions:
    - meta_id: "cond.ms.entity.hostile"
  steps:
    - id: "goto_target"
      name: "移动到目标"
      conditions: []
      actions:
        - meta_id: "action.ms.goto"
      delayAfter: 100

    - id: "attack_target"
      name: "攻击目标"
      conditions:
        - meta_id: "cond.ms.pos.y_above"
          params:
            axis: "Y"
            compare: "GREATER"
            value: 0
      actions:
        - meta_id: "action.ms.attack_entity"
      delayAfter: 500
```

---

## XinMetaPlugin 等效配置

LanConnect 提供了与 XinMetaPlugin 等效的功能，用于 2b2t.xin 等需要登录流程的服务器。

### 完整登录流程

1. **进入登录服** → `LoginFlowListener` 检测标题 → 自动发送注册/登录命令
2. **登录成功** → `loginFlow` 状态变为 `COMPLETED`
3. **检测加入游戏物品** + **登录已完成** → `AutoJoinListener` 点击加入游戏物品
4. **进入游戏服** → 重置登录流状态

### 等效规则链文件

插件首次运行时会自动释放以下规则链文件到 `LanConnect/rules/` 目录：

| 文件名 | 功能 |
|--------|------|
| `login_flow.yml` | 完整的登录并进入游戏流程 |
| `register_flow.yml` | 注册流程 |
| `auto_reply.yml` | 自动回复 |
| `queue_position.yml` | 队列等待 |
| `answer_question.yml` | 自动答题 |

---

## MovementSync 集成

当 MovementSync 插件加载时，LanConnect 会自动注册以下额外的指令元。

### 位置条件

**参数说明**：
- `axis`: 坐标轴（X/Y/Z）
- `compare`: 比较类型（EQUAL/GREATER/LESS/GREATER_EQUAL/LESS_EQUAL/RANGE）
- `value`: 比较值
- `value2`: 范围结束值（仅 RANGE 时使用）

### 实体条件

**参数说明**：
- `check_type`: 检测类型（DISTANCE/EXISTS/LOOKING_AT）
- `entity_type`: 实体类型（PLAYER/ZOMBIE/CREEPER/SKELETON 等）
- `max_distance`: 最大检测距离

### 方块条件

**参数说明**：
- `check_type`: 检测类型（IS_SOLID/IS_PASSABLE/IS_DIGGABLE/MATCH_TYPE）
- `block_type`: 方块类型（仅 MATCH_TYPE 时使用）

### 移动动作

**参数说明**：
- `x`, `y`, `z`: 目标坐标
- `direction`: 方向（FRONT/BACK/LEFT/RIGHT/NORTH/SOUTH/EAST/WEST）
- `time`: 行走时间（毫秒）
- `speed`: 速度（可选，默认使用 MovementSync 配置的速度）

### MovementSync 示例规则链

```yaml
chain:
  id: "goto_example"
  name: "Y坐标低于60时寻路到安全位置"
  enabled: true
  repeat: true
  cooldown: 5000
  triggerConditions:
    - meta_id: "cond.ms.pos.y_above"
      params:
        axis: "Y"
        compare: "LESS"
        value: 60
  steps:
    - id: "goto_safe"
      name: "寻路到安全位置"
      conditions: []
      actions:
        - meta_id: "action.ms.goto"
          params:
            x: 0
            y: 64
            z: 0
      delayAfter: 0
```

---

## 常见问题

### Q: 规则链文件放在哪里？

规则链文件放在 `LanConnect/rules/` 目录下，使用 `.yml` 后缀。插件首次运行时会自动从资源文件释放示例规则链。

### Q: 如何禁用某个规则链？

将规则链文件中的 `enabled: true` 改为 `enabled: false`，或者直接删除/重命名该文件。

### Q: 修改规则后需要重启吗？

目前需要重启 Bot 或重新加载插件。未来版本可能会支持热重载。

### Q: 为什么登录命令没有发送？

1. 检查 `LanConnect.yml` 中 `login.enabled: true`
2. 检查密码是否配置（或 Bot 全局配置中有密码）
3. 开启 `debug: true` 查看详细日志

### Q: 步骤的 `timeout` 是什么？

`timeout` 指定等待条件满足的最大时间（毫秒）。如果超时，步骤会跳过并执行下一步。`timeout: 0` 表示无限等待。

### Q: `delayAfter` 和 `cooldown` 有什么区别？

- `delayAfter`：**步骤间**的延迟，控制链内步骤的执行节奏
- `cooldown`：**链触发**的冷却时间，防止链被频繁触发

### Q: MovementSync 指令元显示"未知"怎么办？

MovementSync 指令元只在 MovementSync 插件加载后才可用。如果看到"未知的条件指令元"或"未知的动作指令元"，请检查：
1. MovementSync 插件是否正确安装
2. 查看启动日志中是否有 "[LanConnect] MovementSync 已加载，已注册 MS 指令元"

### Q: 如何添加自定义的检测关键词？

目前条件指令元的关键词是硬编码的。如果需要自定义关键词，可以通过复制条件类并修改关键词，或者等待未来版本支持参数化关键词配置。

---

## 文件结构

```
LanConnect/
├── LanConnect.yml          # 主配置文件
├── questions.json          # 答题题库（可选）
└── rules/                  # 规则链文件夹
    ├── login_flow.yml      # 登录并进入游戏流程
    ├── register_flow.yml   # 注册流程
    ├── auto_reply.yml      # 自动回复
    ├── queue_position.yml  # 队列等待
    ├── answer_question.yml # 自动答题
    └── chain_examples.yml  # 更多示例
```
