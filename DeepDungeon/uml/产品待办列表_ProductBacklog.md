# 产品待办列表 (Product Backlog)
## Product Backlog - DeepDungeon

| 项目 | 信息 |
|------|------|
| 项目名称 | DeepDungeon (深度地牢) |
| 版本 | v1.0 |
| 创建日期 | 2026年4月9日 |
| 最后更新 | 2026年4月9日 |
| Product Owner | 项目团队 |
| Scrum Master | 待指定 |

---

## 目录

1. [待办列表总览](#1-待办列表总览)
2. [高优先级任务 (Sprint 3)](#2-高优先级任务-sprint-3)
3. [中优先级任务 (Sprint 4)](#3-中优先级任务-sprint-4)
4. [低优先级任务 (Sprint 5+)](#4-低优先级任务-sprint-5)
5. [技术债务](#5-技术债务)
6. [重构难点分析](#6-重构难点分析)
7. [版本规划](#7-版本规划)

---

## 1. 待办列表总览

### 1.1 任务统计

| 优先级 | 数量 | 预估工时 | 状态 |
|--------|------|----------|------|
| 🔴 高 | 7 | 28h | 未开始 |
| 🟡 中 | 8 | 32h | 未开始 |
| 🟢 低 | 4 | 12h | 未开始 |
| **总计** | **19** | **72h** | - |

### 1.2 任务分类

| 类别 | 数量 | 占比 |
|------|------|------|
| 重构 | 6 | 32% |
| Bug修复 | 3 | 16% |
| 功能开发 | 4 | 21% |
| 测试 | 4 | 21% |
| 文档 | 2 | 10% |

### 1.3 优先级排序矩阵

| 顺序 | 任务ID | 任务名称 | 优先级 | 类型 | 预估工时 | Sprint |
|------|--------|----------|--------|------|----------|--------|
| 1 | BUG-01 | 修复 setAtk 自赋值 Bug | 🔴 高 | Bug修复 | 0.5h | Sprint 3 |
| 2 | BUG-02 | 修复 UsePotion 命令未实现 | 🟡 中 | Bug修复 | 1h | Sprint 3 |
| 3 | TEST-01 | 建立单元测试框架 | 🔴 高 | 测试 | 4h | Sprint 3 |
| 4 | REFAC-05 | 消除魔法数字 | 🟡 中 | 重构 | 2h | Sprint 3 |
| 5 | BUG-03 | 固定随机数种子可配置 | 🟡 中 | Bug修复 | 1.5h | Sprint 3 |
| 6 | REFAC-02 | 消除 static_cast 类型转换 | 🔴 高 | 重构 | 4h | Sprint 4 |
| 7 | REFAC-01 | Monster 类充血模型 | 🔴 高 | 重构 | 6h | Sprint 4 |
| 8 | REFAC-03 | pickupItem 符合开闭原则 | 🔴 高 | 重构 | 3h | Sprint 4 |
| 9 | TEST-02 | 实体类单元测试 | 🟡 中 | 测试 | 4h | Sprint 4 |
| 10 | FEAT-01 | 战争迷雾算法改进 | 🟡 中 | 功能 | 6h | Sprint 4 |
| 11 | FEAT-02 | 怪物AI寻路改进 | 🟡 中 | 功能 | 6h | Sprint 4 |
| 12 | REFAC-04 | 拆分 Game 上帝类 | 🔴 高 | 重构 | 8h | Sprint 5 |
| 13 | TEST-03 | 地图生成测试 | 🟡 中 | 测试 | 3h | Sprint 5 |
| 14 | TEST-04 | 战斗系统测试 | 🟡 中 | 测试 | 3h | Sprint 5 |
| 15 | FEAT-03 | 环境互动系统 | 🟡 中 | 功能 | 6h | Sprint 5 |
| 16 | FEAT-04 | 回放系统 | 🟢 低 | 功能 | 8h | Sprint 6 |
| 17 | DOC-01 | 补充代码注释 | 🟢 低 | 文档 | 4h | Sprint 6 |
| 18 | DOC-02 | 更新 README 开发计划 | 🟢 低 | 文档 | 1h | Sprint 6 |
| 19 | BUG-04 | 怪物死亡后碰撞检测 | 🟡 中 | Bug修复 | 1.5h | Sprint 4 |

---

## 2. 高优先级任务 (Sprint 3)

### BUG-01: 修复 setAtk 自赋值 Bug

| 属性 | 描述 |
|------|------|
| **任务ID** | BUG-01 |
| **优先级** | 🔴 高 |
| **类型** | Bug 修复 |
| **预估工时** | 0.5h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 3 |

**问题描述：**

`Entity.h` 第 52 行存在自赋值 Bug，参数 `atk` 未被使用，而是将 `atk_` 赋值给自己。

**当前代码：**
```cpp
void setAtk(int atk) { atk_ = atk_; }  // Bug: 自赋值
```

**修复方案：**
```cpp
void setAtk(int atk) { atk_ = atk; }  // 正确赋值
```

**相关文件：**
- `include/Entity.h` (第 52 行)

**验收标准：**
- [ ] 修复自赋值 Bug
- [ ] 验证 setAtk 方法正常工作

---

### TEST-01: 建立单元测试框架

| 属性 | 描述 |
|------|------|
| **任务ID** | TEST-01 |
| **优先级** | 🔴 高 |
| **类型** | 测试基础设施 |
| **预估工时** | 4h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 3 |

**问题描述：**

项目当前测试覆盖率为 0%，无任何测试框架。在进行大规模重构前，必须建立测试保护网。

**任务详情：**

1. 选择测试框架（推荐 Google Test 或 Catch2）
2. 配置 CMakeLists.txt 支持测试
3. 创建测试目录结构
4. 编写第一个测试用例（示例）
5. 确保测试可以运行

**目录结构：**
```
DeepDungeon/
├── tests/
│   ├── CMakeLists.txt
│   ├── test_entity.cpp
│   ├── test_player.cpp
│   ├── test_map.cpp
│   └── test_game.cpp
└── CMakeLists.txt (更新)
```

**验收标准：**
- [ ] CMake 配置支持测试 (`cmake --build . --target test`)
- [ ] 至少 1 个测试用例可以运行
- [ ] 测试输出清晰，易于阅读

**建议测试框架：**

| 框架 | 优点 | 缺点 |
|------|------|------|
| Google Test | 功能强大，广泛使用 | 需要编译 |
| Catch2 | 单头文件，易于集成 | 编译较慢 |
| doctest | 轻量，快速 | 功能较少 |

---

### REFAC-02: 消除 static_cast 类型转换

| 属性 | 描述 |
|------|------|
| **任务ID** | REFAC-02 |
| **优先级** | 🔴 高 |
| **类型** | 重构（降低耦合） |
| **预估工时** | 4h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 4 |

**问题描述：**

代码中存在 3 处 `static_cast`，违反类型安全原则，增加运行时错误风险。

**当前代码位置：**

| 位置 | 代码 | 行号 |
|------|------|------|
| `Game::updateMonsters()` | `static_cast<Monster*>(entity.get())` | 133 |
| `Game::getMonsterAt()` | `static_cast<Monster*>(entity.get())` | 236 |
| `Game::getItemAt()` | `static_cast<Item*>(entity.get())` | 245 |

**重构方案：**

**方案 A：虚函数多态（推荐）**
```cpp
// Entity 基类
class Entity {
public:
    virtual bool isMonster() const { return false; }
    virtual bool isItem() const { return false; }
    virtual Monster* asMonster() { return nullptr; }
    virtual Item* asItem() { return nullptr; }
};

// Monster 类
class Monster : public Entity {
public:
    bool isMonster() const override { return true; }
    Monster* asMonster() override { return this; }
};

// 使用
Monster* monster = entity->asMonster();
if (monster) { /* ... */ }
```

**方案 B：Visitor Pattern**
```cpp
class EntityVisitor {
public:
    virtual void visit(Monster* m) = 0;
    virtual void visit(Item* i) = 0;
    virtual void visit(Player* p) = 0;
};

class Entity {
public:
    virtual void accept(EntityVisitor* visitor) = 0;
};
```

**验收标准：**
- [ ] 消除所有 static_cast
- [ ] 使用虚函数多态或 Visitor Pattern
- [ ] 代码编译通过，无警告
- [ ] 现有功能不受影响

---

### REFAC-01: Monster 类充血模型

| 属性 | 描述 |
|------|------|
| **任务ID** | REFAC-01 |
| **优先级** | 🔴 高 |
| **类型** | 重构（消除贫血模型） |
| **预估工时** | 6h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 4 |

**问题描述：**

Monster 类仅有构造函数，无任何行为方法。AI 追踪逻辑、攻击逻辑全部在 `Game::updateMonsters()` 中实现，形成贫血模型。

**当前代码：**
```cpp
class Monster : public Entity {
public:
    Monster(int x, int y);  // 仅构造函数
};
```

**重构后代码：**
```cpp
class Monster : public Entity {
public:
    Monster(int x, int y);
    
    // AI 行为
    void updateAI(GameState& state);
    void chase(Entity* target, Map* map);
    bool canSee(Entity* target, Map* map);
    
    // 战斗行为
    void attack(Entity* target);
    int calculateDamageTo(Entity* target);
    
private:
    int findPathTo(Entity* target, Map* map);  // 寻路
};
```

**迁移逻辑：**

| 当前在 Game 中 | 迁移到 Monster |
|---------------|---------------|
| `updateMonsters()` 中的追踪逻辑 | `Monster::chase()` |
| 伤害计算 | `Monster::calculateDamageTo()` |
| 攻击玩家 | `Monster::attack()` |
| 视野检测 | `Monster::canSee()` |

**验收标准：**
- [ ] Monster 类有独立的 AI 和战斗方法
- [ ] Game::updateMonsters() 简化为调用 monster->updateAI()
- [ ] 消除 Game 对 Monster 内部状态的直接操作
- [ ] 测试覆盖 Monster 的所有新方法

---

### REFAC-03: pickupItem 符合开闭原则

| 属性 | 描述 |
|------|------|
| **任务ID** | REFAC-03 |
| **优先级** | 🔴 高 |
| **类型** | 重构（符合 OCP） |
| **预估工时** | 3h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 4 |

**问题描述：**

`Game::pickupItem()` 使用 switch 语句判断物品类型，新增物品类型需修改现有代码，违反开闭原则。

**当前代码：**
```cpp
void Game::pickupItem() {
    Item* item = getItemAt(player_->x(), player_->y());
    if (item) {
        switch (item->itemType()) {
            case ItemType::Weapon:
                player_->equipWeapon(5);
                addLog("You found a sword! ATK +5");
                break;
            case ItemType::Potion:
                player_->addPotion();
                addLog("You found a potion! Press P to use");
                break;
        }
        // 移除物品...
    }
}
```

**重构方案：虚函数多态**
```cpp
// Item 基类
class Item : public Entity {
public:
    virtual void onPickup(Player* player, GameLog* log) = 0;
};

// 武器类
class Weapon : public Item {
public:
    void onPickup(Player* player, GameLog* log) override {
        player->equipWeapon(5);
        log->add("You found a sword! ATK +5");
    }
};

// 药水类
class Potion : public Item {
public:
    void onPickup(Player* player, GameLog* log) override {
        player->addPotion();
        log->add("You found a potion! Press P to use");
    }
};

// Game 中简化
void Game::pickupItem() {
    Item* item = getItemAt(player_->x(), player_->y());
    if (item) {
        item->onPickup(player_.get(), this);
        // 移除物品...
    }
}
```

**验收标准：**
- [ ] 消除 switch 语句
- [ ] 每种物品类型有独立的 onPickup 实现
- [ ] 新增物品类型无需修改 Game 类
- [ ] 现有物品拾取功能正常

---

## 3. 中优先级任务 (Sprint 4)

### BUG-02: 修复 UsePotion 命令未实现

| 属性 | 描述 |
|------|------|
| **任务ID** | BUG-02 |
| **优先级** | 🟡 中 |
| **类型** | Bug 修复 |
| **预估工时** | 1h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 3 |

**问题描述：**

`main.cpp` 第 47 行 `Command::UsePotion` 的 case 分支为空，玩家按 P 键无任何效果。

**当前代码：**
```cpp
case Command::UsePotion:
    // handled in update
    break;
```

**修复方案：**

在 main.cpp 中调用 `player_->usePotion()` 或在 Game 中添加 `usePlayerPotion()` 方法。

**相关文件：**
- `src/main.cpp` (第 47 行)
- `src/Player.cpp` (usePotion 方法已实现)

**验收标准：**
- [ ] 按 P 键可以使用药水
- [ ] 药水库存减少
- [ ] 玩家 HP 恢复 25 点

---

### REFAC-05: 消除魔法数字

| 属性 | 描述 |
|------|------|
| **任务ID** | REFAC-05 |
| **优先级** | 🟡 中 |
| **类型** | 重构（代码质量） |
| **预估工时** | 2h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 3 |

**问题描述：**

多处使用硬编码数字，降低代码可读性和可维护性。

**魔法数字清单：**

| 位置 | 魔法数字 | 含义 | 建议常量名 |
|------|----------|------|-----------|
| `Player::addExp()` | `50` | 升级阈值 | `EXP_PER_LEVEL` |
| `Player::addExp()` | `10` | 升级 HP 增量 | `HP_PER_LEVEL` |
| `Player::addExp()` | `2` | 升级 ATK 增量 | `ATK_PER_LEVEL` |
| `Player::addExp()` | `1` | 升级 DEF 增量 | `DEF_PER_LEVEL` |
| `Player::usePotion()` | `25` | 治疗量 | `POTION_HEAL_AMOUNT` |
| `Game::updateMonsters()` | `5` | 怪物数量 | `MONSTER_COUNT` |
| `Game::init()` | `3` | 物品数量 | `ITEM_COUNT` |
| `Map::generateRooms()` | `6` | 房间数量 | `ROOM_COUNT` |
| `Map::generateRooms()` | `4, 8` | 房间大小范围 | `ROOM_MIN_SIZE`, `ROOM_MAX_SIZE` |
| `Map::addWaterFeatures()` | `10` | 水生成概率 10% | `WATER_CHANCE_PERCENT` |
| `Game::attackMonster()` | `20` | 击杀经验值 | `EXP_PER_KILL` |

**修复方案：**

将所有魔法数字提取为常量，添加到 `Constants.h` 中。

```cpp
namespace dungeon {
    // 升级相关
    constexpr int EXP_PER_LEVEL = 50;
    constexpr int HP_PER_LEVEL = 10;
    constexpr int ATK_PER_LEVEL = 2;
    constexpr int DEF_PER_LEVEL = 1;
    
    // 物品相关
    constexpr int POTION_HEAL_AMOUNT = 25;
    constexpr int EXP_PER_KILL = 20;
    
    // 生成相关
    constexpr int MONSTER_COUNT = 5;
    constexpr int ITEM_COUNT = 3;
    constexpr int ROOM_COUNT = 6;
    constexpr int ROOM_MIN_SIZE = 4;
    constexpr int ROOM_MAX_SIZE = 8;
    constexpr int WATER_CHANCE_PERCENT = 10;
}
```

**验收标准：**
- [ ] 所有魔法数字提取为常量
- [ ] 常量命名符合规范（全大写，下划线分隔）
- [ ] 代码编译通过，功能不变

---

### BUG-03: 固定随机数种子可配置

| 属性 | 描述 |
|------|------|
| **任务ID** | BUG-03 |
| **优先级** | 🟡 中 |
| **类型** | Bug 修复 / 重构 |
| **预估工时** | 1.5h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 3 |

**问题描述：**

`Map.cpp` 和 `Game.cpp` 使用固定随机数种子，导致每次游戏地图和怪物位置完全相同。

**当前代码：**
```cpp
// Map.cpp 第 68 行
std::mt19937 rng(42);  // 固定种子

// Game.cpp 第 29 行
std::mt19937 rng(456);  // 固定种子
```

**修复方案：**

提供可选种子参数，默认使用随机种子（基于时间）。

```cpp
// Map::generate
void Map::generate(int seed = -1) {
    if (seed == -1) {
        seed = std::random_device{}();  // 随机种子
    }
    std::mt19937 rng(seed);
    // ...
}
```

**验收标准：**
- [ ] 默认情况下每次游戏地图不同
- [ ] 可传入固定种子用于调试和测试
- [ ] 向后兼容（现有调用不受影响）

---

### TEST-02: 实体类单元测试

| 属性 | 描述 |
|------|------|
| **任务ID** | TEST-02 |
| **优先级** | 🟡 中 |
| **类型** | 测试 |
| **预估工时** | 4h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 4 |

**测试清单：**

| 测试用例 | 测试内容 | 预期结果 |
|----------|----------|----------|
| `Entity_Constructor` | 构造 Entity | 属性初始化正确 |
| `Entity_Move` | 移动实体 | 位置更新 |
| `Entity_TakeDamage` | 受到伤害 | HP 减少 |
| `Entity_Heal` | 治疗 | HP 增加，不超过 maxHp |
| `Entity_IsAlive` | 存活检查 | HP > 0 返回 true |
| `Player_AddExp` | 获得经验 | exp 增加 |
| `Player_LevelUp` | 升级 | 等级和属性提升 |
| `Player_UsePotion` | 使用药水 | 药水减少，HP 恢复 |
| `Monster_Constructor` | 构造 Monster | 继承 Entity 属性 |
| `Item_Constructor` | 构造 Item | itemType 正确 |

**验收标准：**
- [ ] 至少 10 个测试用例
- [ ] 测试覆盖率 > 60%（实体类）
- [ ] 所有测试通过

---

### FEAT-01: 战争迷雾算法改进

| 属性 | 描述 |
|------|------|
| **任务ID** | FEAT-01 |
| **优先级** | 🟡 中 |
| **类型** | 功能开发 |
| **预估工时** | 6h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 4 |

**问题描述：**

当前 FOV 为简单圆形遍历，无视线阻挡计算，墙壁无法遮挡视野。

**改进方向：**

| 算法 | 复杂度 | 效果 | 推荐度 |
|------|--------|------|--------|
| 射线投射 (Raycasting) | O(R×N) | 良好 | ⭐⭐⭐⭐ |
| 递归阴影 (Recursive Shadowcasting) | O(N) | 优秀 | ⭐⭐⭐⭐⭐ |
| 精确视野 (Permissive FOV) | O(N²) | 最佳 | ⭐⭐⭐ |

**推荐实现：** 递归阴影算法（平衡性能和效果）

**验收标准：**
- [ ] 墙壁可以遮挡视野
- [ ] 角落可见性符合 Roguelike 惯例
- [ ] 性能不受明显影响

---

### FEAT-02: 怪物AI寻路改进

| 属性 | 描述 |
|------|------|
| **任务ID** | FEAT-02 |
| **优先级** | 🟡 中 |
| **类型** | 功能开发 + 重构 |
| **预估工时** | 6h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 4 |

**问题描述：**

当前 AI 仅做简单的坐标差追踪，无路径规划，会被墙壁卡住。

**改进方向：**

| 算法 | 复杂度 | 效果 | 推荐度 |
|------|--------|------|--------|
| A* 寻路 | O(N log N) | 最优路径 | ⭐⭐⭐⭐⭐ |
| Dijkstra | O(N²) | 最优路径 | ⭐⭐⭐ |
| 贪心最佳优先 | O(N) | 次优但快 | ⭐⭐⭐ |

**推荐实现：** A* 寻路算法

**验收标准：**
- [ ] 怪物可以绕过墙壁到达玩家位置
- [ ] 寻路性能可接受（< 10ms/回合）
- [ ] 加入视野检测（canSee）

---

### BUG-04: 怪物死亡后碰撞检测

| 属性 | 描述 |
|------|------|
| **任务ID** | BUG-04 |
| **优先级** | 🟡 中 |
| **类型** | Bug 修复 |
| **预估工时** | 1.5h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 4 |

**问题描述：**

`getMonsterAt()` 没有检查怪物是否存活，可能导致攻击已死亡怪物。

**当前代码：**
```cpp
Monster* Game::getMonsterAt(int x, int y) {
    for (auto& entity : entities_) {
        if (entity->type() == EntityType::Monster &&
            entity->x() == x && entity->y() == y) {
            return static_cast<Monster*>(entity.get());
        }
    }
    return nullptr;
}
```

**修复方案：**

添加 `isAlive()` 检查。

```cpp
Monster* Game::getMonsterAt(int x, int y) {
    for (auto& entity : entities_) {
        if (entity->type() == EntityType::Monster &&
            entity->isAlive() &&  // 添加此行
            entity->x() == x && entity->y() == y) {
            return static_cast<Monster*>(entity.get());
        }
    }
    return nullptr;
}
```

**验收标准：**
- [ ] getMonsterAt 不返回死亡怪物
- [ ] 玩家无法攻击已死亡的怪物

---

## 4. 低优先级任务 (Sprint 5+)

### REFAC-04: 拆分 Game 上帝类

| 属性 | 描述 |
|------|------|
| **任务ID** | REFAC-04 |
| **优先级** | 🔴 高 |
| **类型** | 重构（架构级） |
| **预估工时** | 8h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 5 |
| **依赖** | REFAC-01, REFAC-02, REFAC-03, TEST-02 |

**问题描述：**

Game 类承担过多职责，违反单一职责原则。必须在完成前置重构后才能安全拆分。

**拆分方案：**

```
Game (重构后)
├── GameState (数据容器)
│   ├── Player player
│   ├── Map map
│   └── vector<Entity> entities
├── CombatSystem (战斗系统)
│   ├── attack(attacker, defender)
│   └── calculateDamage(attacker, defender)
├── MovementSystem (移动系统)
│   ├── tryMove(entity, dx, dy, map)
│   └── checkCollisions(entity, entities)
├── AISystem (AI 系统)
│   └── updateAI(entities, gameState)
├── FOVSystem (视野系统)
│   └── calculateFOV(player, map)
└── RenderSystem (渲染系统)
    └── render(gameState)
```

**验收标准：**
- [ ] Game 类方法数 < 8
- [ ] Game 类行数 < 150
- [ ] 每个系统类职责单一
- [ ] 所有测试通过

---

### TEST-03: 地图生成测试

| 属性 | 描述 |
|------|------|
| **任务ID** | TEST-03 |
| **优先级** | 🟡 中 |
| **类型** | 测试 |
| **预估工时** | 3h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 5 |

**测试清单：**

| 测试用例 | 测试内容 | 预期结果 |
|----------|----------|----------|
| `Map_Generate_NotEmpty` | 生成地图 | 至少有一个 Floor |
| `Map_Generate_PlayerSpawn` | 玩家出生点 | 出生在 Floor 上 |
| `Map_Generate_Connectivity` | 连通性 | 所有房间可达 |
| `Map_Generate_WaterFeatures` | 水域生成 | 有 Water 单元格 |
| `Map_IsWalkable_Boundary` | 边界检查 | 越界返回 false |

**验收标准：**
- [ ] 至少 5 个测试用例
- [ ] 测试覆盖率 > 60%（地图类）

---

### TEST-04: 战斗系统测试

| 属性 | 描述 |
|------|------|
| **任务ID** | TEST-04 |
| **优先级** | 🟡 中 |
| **类型** | 测试 |
| **预估工时** | 3h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 5 |

**测试清单：**

| 测试用例 | 测试内容 | 预期结果 |
|----------|----------|----------|
| `Combat_Damage_Calculation` | 伤害计算 | damage = max(1, atk - def) |
| `Combat_MonsterDeath` | 怪物死亡 | 怪物 HP 为 0 时死亡 |
| `Combat_PlayerDeath` | 玩家死亡 | 玩家 HP 为 0 时游戏结束 |
| `Combat_ExpGain` | 经验获取 | 击杀怪物获得 20 Exp |
| `Combat_LevelUp` | 升级 | 达到阈值升级 |
| `Combat_VictoryCondition` | 胜利条件 | 所有怪物死亡时胜利 |

**验收标准：**
- [ ] 至少 6 个测试用例
- [ ] 战斗逻辑完全正确

---

### FEAT-03: 环境互动系统

| 属性 | 描述 |
|------|------|
| **任务ID** | FEAT-03 |
| **优先级** | 🟡 中 |
| **类型** | 功能开发 |
| **预估工时** | 6h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 5 |

**功能清单：**

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 水导电机制 | 在水上使用闪电攻击 | 🟡 中 |
| 陷阱系统 | 踩陷阱受到伤害 | 🟡 中 |
| 可破坏墙壁 | 使用炸弹炸开墙壁 | 🟢 低 |
| 门系统 | 打开/关闭门 | 🟢 低 |

**验收标准：**
- [ ] 至少实现水导电机制
- [ ] 符合开闭原则（新增环境类型无需修改 Game）

---

### FEAT-04: 回放系统

| 属性 | 描述 |
|------|------|
| **任务ID** | FEAT-04 |
| **优先级** | 🟢 低 |
| **类型** | 功能开发 |
| **预估工时** | 8h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 6 |

**功能描述：**

记录玩家输入和游戏状态变化，支持回放游戏过程。

**实现方案：**

1. 引入命令模式（Command Pattern）
2. 记录命令序列到文件
3. 支持重放命令序列

**验收标准：**
- [ ] 可以录制游戏过程
- [ ] 可以回放游戏过程
- [ ] 回放结果与原游戏一致

---

### DOC-01: 补充代码注释

| 属性 | 描述 |
|------|------|
| **任务ID** | DOC-01 |
| **优先级** | 🟢 低 |
| **类型** | 文档 |
| **预估工时** | 4h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 6 |

**任务详情：**

为所有代码文件添加 Doxygen 风格的函数注释。

**注释模板：**
```cpp
/**
 * @brief 函数简要说明
 * @param param1 参数说明
 * @return 返回值说明
 * @note 注意事项
 */
```

**验收标准：**
- [ ] 所有公有方法有完整注释
- [ ] 关键私有方法有简要注释
- [ ] 符合 Doxygen 格式

---

### DOC-02: 更新 README 开发计划

| 属性 | 描述 |
|------|------|
| **任务ID** | DOC-02 |
| **优先级** | 🟢 低 |
| **类型** | 文档 |
| **预估工时** | 1h |
| **状态** | 未开始 |
| **负责人** | 待分配 |
| **Sprint** | Sprint 6 |

**任务详情：**

更新 README.md 中的开发计划，反映实际进度。

**验收标准：**
- [ ] Sprint 2/3/4 状态更新
- [ ] 新增功能列表更新

---

## 5. 技术债务

### 5.1 技术债务清单

| 债务ID | 债务描述 | 影响 | 利息 | 偿还成本 | 优先级 |
|--------|----------|------|------|----------|--------|
| TD-01 | Game 上帝类 | 高 | 高 | 8h | 🔴 高 |
| TD-02 | 贫血模型 | 高 | 高 | 6h | 🔴 高 |
| TD-03 | static_cast 类型转换 | 中 | 中 | 4h | 🔴 高 |
| TD-04 | switch 语句违反 OCP | 中 | 中 | 3h | 🔴 高 |
| TD-05 | 魔法数字 | 低 | 低 | 2h | 🟡 中 |
| TD-06 | 固定随机数种子 | 中 | 低 | 1.5h | 🟡 中 |
| TD-07 | 无测试覆盖 | 高 | 高 | 4h | 🔴 高 |
| TD-08 | 无代码注释 | 低 | 低 | 4h | 🟢 低 |

### 5.2 技术债务趋势

```
当前技术债务总量: ~32.5h
偿还计划: 
  - Sprint 3: 消除 TD-05, TD-06, TD-07 (7.5h)
  - Sprint 4: 消除 TD-02, TD-03, TD-04 (13h)
  - Sprint 5: 消除 TD-01 (8h)
  - Sprint 6: 消除 TD-08 (4h)
```

---

## 6. 重构难点分析

### 难点 1：Game 类拆分的高风险性

| 属性 | 描述 |
|------|------|
| **风险等级** | 高 |
| **原因** | Game 类是所有逻辑的中心枢纽，Map、Player、entities_ 等成员被多个私有方法共享 |
| **缓解措施** | 先建立测试保护网，再逐步提取子系统 |
| **依赖** | 必须先完成 REFAC-01, REFAC-02, REFAC-03, TEST-02 |

### 难点 2：static_cast 的消除需要架构级改动

| 属性 | 描述 |
|------|------|
| **风险等级** | 高 |
| **原因** | 当前使用 `vector<unique_ptr<Entity>>` 存储所有实体，需要类型转换才能访问派生类特有方法 |
| **缓解措施** | 使用虚函数多态或 Visitor Pattern 替代 |
| **长期方案** | 考虑 ECS (Entity-Component-System) 架构 |

### 难点 3：无测试保护网

| 属性 | 描述 |
|------|------|
| **风险等级** | 高 |
| **原因** | 0% 测试覆盖率意味着任何重构都可能引入回归 bug 而无法自动检测 |
| **缓解措施** | 先写集成测试（测试游戏端到端行为），再做内部重构 |
| **优先级** | TEST-01 必须在所有重构之前完成 |

### 难点 4：命令模式与现有输入处理的兼容

| 属性 | 描述 |
|------|------|
| **风险等级** | 中 |
| **原因** | 当前输入处理在 Game::handleInput() 中直接返回 Command 枚举，main.cpp 直接调用 tryMovePlayer |
| **缓解措施** | 渐进式重构，先提取 Command 类，再逐步迁移 |
| **相关任务** | FEAT-04 (回放系统) |

---

## 7. 版本规划

### 7.1 版本路线图

| 版本 | 目标 | 预计完成 | 关键任务 |
|------|------|----------|----------|
| v0.1 | 基础框架 | ✅ 已完成 | Sprint 1 |
| v0.2 | 架构重构 | Sprint 4-5 | REFAC-01, REFAC-02, REFAC-03 |
| v0.3 | 功能完善 | Sprint 5 | FEAT-01, FEAT-02, FEAT-03 |
| v1.0 | 正式发布 | Sprint 6+ | 所有高优先级任务完成 |

### 7.2 Sprint 计划

#### Sprint 3 (本周)

| 任务ID | 任务名称 | 负责人 | 状态 |
|--------|----------|--------|------|
| BUG-01 | 修复 setAtk Bug | 待分配 | ⬜ 未开始 |
| BUG-02 | 修复 UsePotion 命令 | 待分配 | ⬜ 未开始 |
| TEST-01 | 建立单元测试框架 | 待分配 | ⬜ 未开始 |
| REFAC-05 | 消除魔法数字 | 待分配 | ⬜ 未开始 |
| BUG-03 | 随机数种子可配置 | 待分配 | ⬜ 未开始 |

**Sprint 3 目标：** 修复关键 Bug，建立测试基础，提升代码质量

---

#### Sprint 4

| 任务ID | 任务名称 | 负责人 | 状态 |
|--------|----------|--------|------|
| REFAC-02 | 消除 static_cast | 待分配 | ⬜ 未开始 |
| REFAC-01 | Monster 充血模型 | 待分配 | ⬜ 未开始 |
| REFAC-03 | pickupItem 符合 OCP | 待分配 | ⬜ 未开始 |
| TEST-02 | 实体类单元测试 | 待分配 | ⬜ 未开始 |
| FEAT-01 | 战争迷雾改进 | 待分配 | ⬜ 未开始 |
| FEAT-02 | 怪物AI寻路改进 | 待分配 | ⬜ 未开始 |
| BUG-04 | 怪物死亡碰撞检测 | 待分配 | ⬜ 未开始 |

**Sprint 4 目标：** 核心架构重构，消除贫血模型，改进 AI 和 FOV

---

#### Sprint 5

| 任务ID | 任务名称 | 负责人 | 状态 |
|--------|----------|--------|------|
| REFAC-04 | 拆分 Game 上帝类 | 待分配 | ⬜ 未开始 |
| TEST-03 | 地图生成测试 | 待分配 | ⬜ 未开始 |
| TEST-04 | 战斗系统测试 | 待分配 | ⬜ 未开始 |
| FEAT-03 | 环境互动系统 | 待分配 | ⬜ 未开始 |

**Sprint 5 目标：** 拆分上帝类，完善测试覆盖，新增环境互动

---

#### Sprint 6

| 任务ID | 任务名称 | 负责人 | 状态 |
|--------|----------|--------|------|
| FEAT-04 | 回放系统 | 待分配 | ⬜ 未开始 |
| DOC-01 | 补充代码注释 | 待分配 | ⬜ 未开始 |
| DOC-02 | 更新 README | 待分配 | ⬜ 未开始 |

**Sprint 6 目标：** 功能完善，文档补充，准备 v1.0 发布

---

## 附录

### 附录 A: 任务状态图例

| 符号 | 含义 |
|------|------|
| ⬜ | 未开始 |
| 🟡 | 进行中 |
| ✅ | 已完成 |
| ❌ | 已取消 |

### 附录 B: 优先级定义

| 优先级 | 描述 | 响应时间 |
|--------|------|----------|
| 🔴 高 | 核心功能/严重问题，必须在本 Sprint 完成 | 立即 |
| 🟡 中 | 重要功能/问题，应该在本 Sprint 完成 | 1-2 天 |
| 🟢 低 | 增强功能，可以在后续 Sprint 完成 | 本周内 |

### 附录 C: 任务类型定义

| 类型 | 描述 |
|------|------|
| Bug 修复 | 修复代码错误或功能缺陷 |
| 重构 | 改善代码结构，不改变外部行为 |
| 功能开发 | 新增功能或改进现有功能 |
| 测试 | 编写测试用例，提升测试覆盖率 |
| 文档 | 补充代码注释或项目文档 |

---

**文档结束**

*本文档由项目团队编写，经 Product Owner 确认*

*最后更新：2026年4月9日*
