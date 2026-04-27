# AGENTS.md - AI 操作手册

## 1. 项目概述

**Deep Dungeon**：一款硬核 Roguelike 地牢游戏（纯 C++17，控制台 UI）。
- **核心机制**：过程化地图生成、基于回合的战斗、经验升级系统、物品拾取
- **架构**：ECS-轻量模式，Game 主类协调 Map、Player、Entity（Monster/Item）三大模块

---

## 2. 目录结构

```
DeepDungeon/
├── include/
│   ├── Constants.h          # 游戏常量：地图尺寸、角色属性基值
│   ├── Entity.h             # 实体基类：Player、Monster、Item 继承
│   ├── Game.h               # 游戏主类：状态机、主循环、事件处理
│   ├── Map.h                # 地图类：BSP 房间+走廊生成、FOV、寻路
│   └── Player.h             # 玩家类：经验升级、物品栏、战斗
├── src/
│   ├── CMakeLists.txt       # 编译配置
│   ├── Entity.cpp           # Entity、Monster、Item 实现
│   ├── Game.cpp             # 游戏逻辑：移动、战斗、物品拾取
│   ├── main.cpp             # 入口：初始化 + REPL 循环
│   ├── Map.cpp              # 地图生成（BSP 分割）
│   └── Player.cpp           # 玩家升级逻辑
└── test/                    # 单元测试（使用 Google Test）
    └── *_test.cpp
```

---

## 3. 核心模块职责

### 3.1 Constants.h（配置层）
**职责**：定义游戏平衡参数和魔数
- `MAP_WIDTH/MAP_HEIGHT`：地图尺寸（40x20）
- `FOV_RADIUS`：视野半径（5 格）
- `PLAYER_BASE_HP/ATK/DEF`：玩家初始属性（100/10/5）
- `CHAR_*`：渲染字符（@ 玩家，g 怪物，# 墙）

**禁止操作**：不得硬编码数值，所有平衡参数必须通过 Constants.h 修改

### 3.2 Entity.h & Entity.cpp（基础实体）
**职责**：定义所有游戏对象的通用接口
- **Entity（基类）**
  - 属性：位置(x,y)、HP、ATK、DEF、符号
  - 行为：move()、takeDamage()、heal()、isAlive()
- **Monster（怪物）**
  - 固定属性：HP=30, ATK=8, DEF=1
  - AI：turnCount 计数（保留扩展），暂时随机移动
- **Item（物品）**
  - 类型：Weapon (+2 ATK)、Potion (+25 HP)
  - 拾取后从游戏中移除

**禁止操作**：不得直接修改 takeDamage() 的防御计算公式（当前：actualDamage = max(1, damage - def)）

### 3.3 Map.h & Map.cpp（地图系统）
**职责**：过程化地图生成与碰撞检测
- **生成**：BSP（二进制空间分割）算法 → 房间 + 走廊
- **访问**：Tile[y][x]，支持查询类型(Wall/Floor/Water)、可见性、已探索标记
- **方法**
  - `generate()` - 创建房间和走廊
  - `isWalkable(x,y)` - 检查是否可通行
  - `setTileVisible()` - 更新 FOV
- **状态**：explored（战争迷雾）、visible（当前可见）

**禁止操作**：不得绕过 isWalkable()，直接设置玩家位置。所有移动必须通过 Game::tryMovePlayer()。

### 3.4 Player.h & Player.cpp（玩家类）
**职责**：经验升级与物品管理
- **属性**：exp, level, weaponBonus, potionCount
- **升级规则**：每 50 exp 升一级
  - Level 1→2→3...：HP +10, ATK +2, DEF +1，升级时 HP 满血
- **物品栏**
  - 武器：equip() 增加 totalAtk()
  - 药水：usePotion() 恢复 25 HP
- **方法**
  - `addExp(amount)` - 增加经验（可能触发升级）
  - `usePotion()` - 使用药水
  - `totalAtk()` - 返回 base + weaponBonus

**禁止操作**：不得直接赋值 level_ 或 hp_。所有状态变化必须通过公开方法。

### 3.5 Game.h & Game.cpp（游戏主类）
**职责**：游戏状态机、事件分发、主循环协调
- **初始化 init()**
  - 生成地图、放置玩家、生成怪物(5个)、生成物品(3个)、计算 FOV
- **输入处理 handleInput()**
  - 'W/A/S/D' → 4 方向移动命令
  - 'P' → 使用药水
  - 'Q' → 退出
- **移动核心 tryMovePlayer(dx, dy)**
  - 验证边界：`map_.isValid()`
  - 检查怪物：若存在则 `attackMonster()`
  - 检查通行：`map_.isWalkable()`
  - 执行移动 + 拾取物品：`pickupItem()`
  - 记录日志：`addLog()`
- **状态更新 update()**
  - 更新怪物 AI：`updateMonsters()`
  - 碰撞检测：`checkCollisions()`
  - FOV 更新：`updateFOV()`
- **日志系统**：所有游戏事件通过 `addLog()` 记录，日志在渲染时显示

**禁止操作**：
- 不得绕过 tryMovePlayer() 直接修改玩家位置
- 不得删除 addLog() 调用，它是唯一的玩家反馈途径
- 不得在 tryMovePlayer() 中调用 update()（会导致重复更新）

---

## 4. 编码规范

### 4.1 命名规范

| 类别 | 规范 | 示例 |
|------|------|------|
| **类名** | PascalCase | `class Player`, `class Monster` |
| **函数名** | camelCase（公开），snake_case（私有） | `getMonsterAt()`, `updateFOV()` |
| **变量** | camelCase（局部），snake_case_（成员） | `int maxHp` → `int maxHp_` |
| **常量** | UPPER_SNAKE_CASE | `MAP_WIDTH`, `PLAYER_BASE_HP` |
| **枚举值** | PascalCase | `EntityType::Player`, `TileType::Wall` |

**具体规则**：
- 成员变量必须以 `_` 结尾（如 `hp_`, `level_`）
- 私有方法前缀 `_`（如 `_updateFOV()` 建议，非强制）
- 避免单字母变量（`i`, `x` 在循环中可接受）

### 4.2 函数长度限制

- **大函数**：`run()`, `init()`, `generate()` < 50 行可接受
- **核心函数**：`tryMovePlayer()`, `update()` < 30 行
- **工具函数**：< 15 行（可内联）

**拆分原则**：若 if-else 嵌套 > 3 层，提取为私有函数

### 4.3 副作用管理

**允许的副作用**：
- ✅ 修改玩家 HP、位置、经验
- ✅ 修改 Map 的 visibility/explored 标记
- ✅ 向 log_ 添加消息
- ✅ 移除或添加 Entity

**禁止的副作用**：
- ❌ 在 pure check 函数中修改状态（如 isWalkable 不得改变 tile.visited）
- ❌ 隐藏的 I/O（如在 update() 中调用 std::cout，应只在 render() 中）
- ❌ 全局状态修改（GameManager 单例等）

---

## 5. 禁止操作清单

| 操作 | 理由 | 后果 |
|------|------|------|
| 删除或注释 addLog() 调用 | 玩家无法了解游戏事件 | 游戏无法使用 |
| 直接赋值 `player_->x_ = ...` | 破坏 Map 碰撞系统 | 玩家可以穿墙 |
| 在 entity loop 中 erase | 导致迭代器失效 | 数据损坏/Crash |
| 修改 Constants 中的数值而不更新注释 | 失去可维护性 | 他人无法理解参数含义 |
| 在 `isWalkable()` 中调用 update() | 重复执行游戏逻辑 | 逻辑混乱、无法调试 |
| 删除现有的 test_*.cpp | 失去测试覆盖 | 无法验证正确性 |
| 硬编码魔数 | Constants.h 失效 | 无法批量调平衡参数 |
| 不通过 Game 接口操作 Entity | 绕过主循环控制 | 游戏状态不同步 |

---

## 6. AI 操作检查清单

**在修改代码前，检查以下项目**：

- [ ] 所有新函数是否在 Constants.h 声明了 if 需要全局访问的参数？
- [ ] tryMovePlayer() 的改动是否验证了边界检查？
- [ ] 是否通过 addLog() 记录了所有玩家可见的事件？
- [ ] 是否在 Entity loop 中使用了安全的 erase 方式（remove_if + erase）？
- [ ] 新增的成员变量是否以 _ 后缀命名？
- [ ] 测试代码是否覆盖了边界情况（0 值、极值、多步操作）？
- [ ] 是否运行了 Google Test 验证新代码？

---

## 7. 建译和测试命令

```bash
# 构建
cd DeepDungeon && mkdir -p build && cd build
cmake .. && make

# 运行游戏
./DeepDungeon

# 运行单元测试（需要安装 gtest）
ctest

# 清理
rm -rf build/
```

---

## 8. 关键 API 速查

| API | 签名 | 使用场景 |
|-----|------|---------|
| `game.tryMovePlayer(dx, dy)` | void | 玩家移动 |
| `game.update()` | void | 每回合更新 |
| `game.addLog(msg)` | void | 记录事件 |
| `player.addExp(amount)` | void | 增加经验 |
| `map.isWalkable(x, y)` | bool | 碰撞检查 |
| `game.getMonsterAt(x, y)` | Monster* | 获取目标怪物 |
| `game.getItemAt(x, y)` | Item* | 获取目标物品 |

---

**最后一条规则**：当不确定时，查看 Game.cpp 中的 tryMovePlayer() 实现——它是所有游戏逻辑的黄金标准。

