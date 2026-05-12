# 详细设计说明书 (DDS)
## Detailed Design Specification

| 项目 | 信息 |
|------|------|
| 项目名称 | DeepDungeon (深度地牢) |
| 版本 | v1.0 |
| 创建日期 | 2026年4月9日 |
| 最后更新 | 2026年4月9日 |
| 文档状态 | 草稿 |

---

## 目录

1. [引言](#1-引言)
   - 1.1 目的
   - 1.2 范围
   - 1.3 参考文献
2. [核心控制类设计](#2-核心控制类设计)
   - 2.1 Game 类
   - 2.2 Map 类
   - 2.3 Player 类
3. [关键算法 PAD/N-S 图](#3-关键算法-padns-图)
   - 3.1 地图生成算法 (Map::generate)
   - 3.2 战争迷雾算法 (Game::updateFOV)
   - 3.3 怪物AI追踪算法 (Game::updateMonsters)
   - 3.4 战斗伤害计算算法 (Game::attackMonster)
   - 3.5 玩家升级算法 (Player::addExp)
   - 3.6 物品拾取算法 (Game::pickupItem)
   - 3.7 游戏初始化算法 (Game::init)
4. [跨类调用方法签名规范](#4-跨类调用方法签名规范)
   - 4.1 方法签名总览
   - 4.2 Game 类接口
   - 4.3 Map 类接口
   - 4.4 Player 类接口
   - 4.5 Entity 基类接口
   - 4.6 调用关系图
5. [数据结构设计](#5-数据结构设计)
   - 5.1 枚举类型
   - 5.2 结构体
   - 5.3 类关系
6. [设计约束与注意事项](#6-设计约束与注意事项)

---

## 1. 引言

### 1.1 目的

本文档是 **DeepDungeon (深度地牢)** 游戏的详细设计说明书，旨在：

- 归档核心控制类的关键算法设计（PAD图/N-S盒图）
- 规范化跨类调用的方法签名细节
- 为开发人员提供详细的实现参考
- 为代码审查和维护提供设计基准

### 1.2 范围

本文档覆盖以下核心类的详细设计：

| 类名 | 职责 | 关键算法数量 |
|------|------|------------|
| `Game` | 游戏主控制器，协调所有系统 | 5个 |
| `Map` | 地图生成与管理 | 1个 |
| `Player` | 玩家状态与行为 | 1个 |

**总计：7个关键算法**，每个算法都有对应的PAD图或N-S盒图。

### 1.3 参考文献

| 编号 | 文档名称 | 版本 |
|------|----------|------|
| [1] | 需求规格说明书 (SRS) | v1.0 |
| [2] | 架构审查与重构报告 | v1.0 |
| [3] | UML 类图 | uml/02_Class_Diagram.plantuml |
| [4] | C++ 源代码 | include/*.h, src/*.cpp |

---

## 2. 核心控制类设计

### 2.1 Game 类

**类职责：** 游戏主控制器，负责协调输入处理、状态更新、渲染和所有子系统。

**当前问题：** 🔴 上帝类警告 - 承担了过多职责（输入处理、渲染逻辑、战斗判定、怪物AI、FOV计算、碰撞检测）

**成员变量：**

| 变量名 | 类型 | 可见性 | 描述 |
|--------|------|--------|------|
| map_ | Map | private | 地图实例 |
| player_ | unique_ptr<Player> | private | 玩家实例 |
| entities_ | vector<unique_ptr<Entity>> | private | 实体列表（怪物+物品） |
| gameOver_ | bool | private | 游戏结束标志 |
| gameWon_ | bool | private | 游戏胜利标志 |
| log_ | vector<string> | private | 游戏日志 |

**关键算法：**
1. `init()` - 游戏初始化
2. `updateFOV()` - 战争迷雾计算
3. `updateMonsters()` - 怪物AI追踪
4. `attackMonster()` - 战斗伤害计算
5. `pickupItem()` - 物品拾取

### 2.2 Map 类

**类职责：** 地图生成、管理和查询。

**成员变量：**

| 变量名 | 类型 | 可见性 | 描述 |
|--------|------|--------|------|
| width_ | int | private | 地图宽度 |
| height_ | int | private | 地图高度 |
| tiles_ | vector<vector<Tile>> | private | 地图单元格二维数组 |

**关键算法：**
1. `generate()` - 程序化地图生成

### 2.3 Player 类

**类职责：** 玩家状态管理（HP、经验、等级、物品）。

**成员变量：**

| 变量名 | 类型 | 可见性 | 描述 |
|--------|------|--------|------|
| exp_ | int | private | 经验值 |
| level_ | int | private | 等级 |
| weaponBonus_ | int | private | 武器攻击力加成 |
| potionCount_ | int | private | 药水数量 |

**关键算法：**
1. `addExp()` - 经验值与升级计算

---

## 3. 关键算法 PAD/N-S 图

### 3.1 地图生成算法 (Map::generate)

**算法描述：** 使用房间+走廊的方式生成地牢地图。

**输入：** `seed` (随机种子，默认0)  
**输出：** 完整的地图数据结构 `tiles_`

#### 3.1.1 PAD 图

> **[在此插入 Map::generate PAD图]**
> 
> 文件位置：`uml/06_PAD_MapGenerate.plantuml`

**算法步骤说明：**

```
Map::generate(seed)
├─ 1. 初始化全为墙
│   └─ 遍历所有 tiles_[y][x]
│       ├─ type = Wall
│       ├─ explored = false
│       └─ visible = false
├─ 2. generateRooms()
│   └─ 随机生成 6 个房间
│       ├─ 随机房间大小 (4-8)
│       └─ 挖掘房间（Floor）
├─ 3. generateCorridors()
│   └─ 连接所有房间
│       ├─ 收集所有 Floor 单元格
│       └─ L型走廊连接相邻房间
└─ 4. addWaterFeatures()
    └─ 10% 概率将 Floor 转为 Water
```

#### 3.1.2 N-S 盒图

```
┌─────────────────────────────────────────────────────────┐
│                    Map::generate(seed)                   │
├─────────────────────────────────────────────────────────┤
│              1. 初始化全为墙 (遍历 tiles_)               │
├─────────────────────────────────────────────────────────┤
│                   2. generateRooms()                     │
│  ┌───────────────────────────────────────────────────┐  │
│  │  for i = 0 to 5                                   │  │
│  │  ├─ 随机生成 roomW, roomH, roomX, roomY           │  │
│  │  └─ 挖掘房间（设置 type = Floor）                 │  │
│  └───────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│                 3. generateCorridors()                   │
│  ┌───────────────────────────────────────────────────┐  │
│  │  收集所有 Floor 单元格到 floorTiles               │  │
│  │  ├─ 排序 floorTiles                               │  │
│  │  └─ for i = 0 to size-2                           │  │
│  │      ├─ 水平挖走廊 (x1 to x2, y = y1)             │  │
│  │      └─ 垂直挖走廊 (y1 to y2, x = x2)             │  │
│  └───────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│                 4. addWaterFeatures()                    │
│  ┌───────────────────────────────────────────────────┐  │
│  │  for y = 1 to height_-2                           │  │
│  │  └─ for x = 1 to width_-2                         │  │
│  │      └─ if tile == Floor && rand < 10%            │  │
│  │          └─ type = Water                          │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

### 3.2 战争迷雾算法 (Game::updateFOV)

**算法描述：** 基于玩家位置的圆形视野计算。

**输入：** 玩家当前位置  
**输出：** 更新地图的 `visible` 和 `explored` 标记

#### 3.2.1 PAD 图

> **[在此插入 Game::updateFOV PAD图]**
> 
> 文件位置：`uml/07_PAD_UpdateFOV.plantuml`

**算法步骤说明：**

```
Game::updateFOV()
├─ 1. map_.clearVisibility()
│   └─ 所有 tile.visible = false
├─ 2. 获取玩家位置 (px, py)
└─ 3. 圆形扫描
    └─ for dy = -FOV_RADIUS to FOV_RADIUS
        └─ for dx = -FOV_RADIUS to FOV_RADIUS
            ├─ 计算距离: dist² = dx² + dy²
            ├─ if dist² <= FOV_RADIUS²
            │   ├─ 计算目标位置: x = px + dx, y = py + dy
            │   └─ if map_.isValid(x, y)
            │       └─ map_.setTileVisible(x, y)
            │           ├─ tile.visible = true
            │           └─ tile.explored = true
            └─ else: 跳过
```

#### 3.2.2 N-S 盒图

```
┌─────────────────────────────────────────────────────────┐
│                  Game::updateFOV()                       │
├─────────────────────────────────────────────────────────┤
│              1. map_.clearVisibility()                   │
├─────────────────────────────────────────────────────────┤
│              2. px = player_->x(), py = player_->y()     │
├─────────────────────────────────────────────────────────┤
│                    3. 圆形扫描                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │  for dy = -5 to 5                                 │  │
│  │  └─ for dx = -5 to 5                              │  │
│  │      ├─ dist² = dx*dx + dy*dy                     │  │
│  │      ├─ if dist² <= 25                            │  │
│  │      │   ├─ x = px + dx, y = py + dy              │  │
│  │      │   └─ if map_.isValid(x, y)                 │  │
│  │      │       └─ map_.setTileVisible(x, y)         │  │
│  │      └─ else: 继续下一次循环                      │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

### 3.3 怪物AI追踪算法 (Game::updateMonsters)

**算法描述：** 怪物简单追踪玩家，优先X轴移动，其次Y轴。

**输入：** 玩家位置、怪物位置  
**输出：** 怪物新位置或玩家受到伤害

#### 3.3.1 PAD 图

> **[在此插入 Game::updateMonsters PAD图]**
> 
> 文件位置：`uml/08_PAD_UpdateMonsters.plantuml`

**算法步骤说明：**

```
Game::updateMonsters()
└─ for each entity in entities_
    └─ if entity.type == Monster && entity.isAlive()
        ├─ 获取玩家位置 (px, py)
        ├─ 获取怪物位置 (mx, my)
        ├─ 计算移动方向 (dx, dy)
        │   ├─ if px > mx: dx = 1
        │   ├─ else if px < mx: dx = -1
        │   ├─ else if py > my: dy = 1
        │   └─ else if py < my: dy = -1
        ├─ if dx != 0 || dy != 0
        │   ├─ newX = mx + dx, newY = my + dy
        │   ├─ if newX == px && newY == py (攻击玩家)
        │   │   ├─ damage = max(1, monster.atk - player.def)
        │   │   ├─ player.takeDamage(damage)
        │   │   └─ if !player.isAlive()
        │   │       ├─ gameOver_ = true
        │   │       └─ 添加日志 "You died"
        │   └─ else if map_.isWalkable(newX, newY) 
        │              && getMonsterAt(newX, newY) == nullptr
        │       └─ monster.setPosition(newX, newY)
        └─ else: 怪物不动
```

#### 3.3.2 N-S 盒图

```
┌─────────────────────────────────────────────────────────┐
│                Game::updateMonsters()                    │
├─────────────────────────────────────────────────────────┤
│              for each entity in entities_                │
│  ┌───────────────────────────────────────────────────┐  │
│  │  if type != Monster || !isAlive(): continue       │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  计算移动方向 (dx, dy)                            │  │
│  │  ├─ 优先 X 轴: px > mx ? dx=1 : px < mx ? dx=-1  │  │
│  │  └─ 其次 Y 轴: py > my ? dy=1 : py < my ? dy=-1  │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  if dx == 0 && dy == 0: continue                  │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  newX = mx + dx, newY = my + dy                   │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  if newX == px && newY == py                      │  │
│  │  ├─ 攻击玩家                                      │  │
│  │  │  damage = max(1, monster.atk - player.def)    │  │
│  │  │  player.takeDamage(damage)                    │  │
│  │  │  if !player.isAlive()                         │  │
│  │  │  ├─ gameOver_ = true                          │  │
│  │  │  └─ 添加日志 "You died"                       │  │
│  │  └──────────────────────────────────────────────┤  │
│  │  else if isWalkable && no monster: move          │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

### 3.4 战斗伤害计算算法 (Game::attackMonster)

**算法描述：** 玩家攻击怪物，计算伤害，检查怪物死亡和胜利条件。

**输入：** 怪物指针  
**输出：** 怪物HP减少，玩家获得经验，可能移除怪物

#### 3.4.1 PAD 图

> **[在此插入 Game::attackMonster PAD图]**
> 
> 文件位置：`uml/09_PAD_AttackMonster.plantuml`

**算法步骤说明：**

```
Game::attackMonster(monster)
├─ 1. damage = player_->totalAtk()
├─ 2. monster->takeDamage(damage)
├─ 3. 添加日志 "You attacked for X damage"
└─ 4. if !monster->isAlive()
    ├─ 添加日志 "Monster defeated! +20 exp"
    ├─ player_->addExp(20)
    ├─ 从 entities_ 中移除怪物
    │   └─ erase-remove_if 惯用法
    └─ 5. 检查胜利条件
        └─ if entities_.empty()
            ├─ gameWon_ = true
            ├─ gameOver_ = true
            └─ 添加日志 "Victory!"
```

#### 3.4.2 N-S 盒图

```
┌─────────────────────────────────────────────────────────┐
│               Game::attackMonster(monster)               │
├─────────────────────────────────────────────────────────┤
│  1. damage = player_->totalAtk()                        │
├─────────────────────────────────────────────────────────┤
│  2. monster->takeDamage(damage)                         │
├─────────────────────────────────────────────────────────┤
│  3. 添加日志 "You attacked for X damage"                │
├─────────────────────────────────────────────────────────┤
│  4. if !monster->isAlive()                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │  添加日志 "Monster defeated! +20 exp"             │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  player_->addExp(20)                              │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  从 entities_ 中移除怪物                          │  │
│  │  (erase-remove_if 惯用法)                         │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  if entities_.empty()                             │  │
│  │  ├─ gameWon_ = true                               │  │
│  │  ├─ gameOver_ = true                              │  │
│  │  └─ 添加日志 "Victory!"                           │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

### 3.5 玩家升级算法 (Player::addExp)

**算法描述：** 玩家获得经验值，达到阈值自动升级。

**输入：** `amount` (经验值)  
**输出：** 更新 exp_, level_, 属性提升

#### 3.5.1 PAD 图

> **[在此插入 Player::addExp PAD图]**
> 
> 文件位置：`uml/10_PAD_AddExp.plantuml`

**算法步骤说明：**

```
Player::addExp(amount)
├─ 1. exp_ += amount
├─ 2. newLevel = exp_ / 50 + 1
└─ 3. if newLevel > level_
    ├─ level_ = newLevel
    ├─ maxHp_ += 10
    ├─ hp_ = maxHp_  (升级时满血)
    ├─ atk_ += 2
    └─ def_ += 1
```

#### 3.5.2 N-S 盒图

```
┌─────────────────────────────────────────────────────────┐
│                   Player::addExp(amount)                 │
├─────────────────────────────────────────────────────────┤
│  1. exp_ += amount                                      │
├─────────────────────────────────────────────────────────┤
│  2. newLevel = exp_ / 50 + 1                            │
├─────────────────────────────────────────────────────────┤
│  3. if newLevel > level_                                │
│  ┌───────────────────────────────────────────────────┐  │
│  │  level_ = newLevel                                │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  maxHp_ += 10                                     │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  hp_ = maxHp_  (升级时满血)                       │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  atk_ += 2                                        │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  def_ += 1                                        │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

### 3.6 物品拾取算法 (Game::pickupItem)

**算法描述：** 玩家移动到物品位置时自动拾取并应用效果。

**输入：** 玩家当前位置  
**输出：** 应用物品效果，从地图移除物品

#### 3.6.1 PAD 图

> **[在此插入 Game::pickupItem PAD图]**
> 
> 文件位置：`uml/11_PAD_PickupItem.plantuml`

**算法步骤说明：**

```
Game::pickupItem()
└─ item = getItemAt(player_->x(), player_->y())
    └─ if item != nullptr
        ├─ switch (item->itemType())
        │   ├─ case Weapon:
        │   │   ├─ player_->equipWeapon(5)
        │   │   └─ 添加日志 "Found sword! ATK +5"
        │   └─ case Potion:
        │       ├─ player_->addPotion()
        │       └─ 添加日志 "Found potion! Press P"
        └─ 从 entities_ 中移除物品
            └─ erase-remove_if 惯用法
```

#### 3.6.2 N-S 盒图

```
┌─────────────────────────────────────────────────────────┐
│                   Game::pickupItem()                     │
├─────────────────────────────────────────────────────────┤
│  item = getItemAt(player_->x(), player_->y())           │
├─────────────────────────────────────────────────────────┤
│  if item == nullptr: return                             │
├─────────────────────────────────────────────────────────┤
│  switch (item->itemType())                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │  case Weapon:                                     │  │
│  │  ├─ player_->equipWeapon(5)                       │  │
│  │  └─ 添加日志 "Found sword! ATK +5"                │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  case Potion:                                     │  │
│  │  ├─ player_->addPotion()                          │  │
│  │  └─ 添加日志 "Found potion! Press P"              │  │
│  └───────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  从 entities_ 中移除物品 (erase-remove_if)              │
└─────────────────────────────────────────────────────────┘
```

---

### 3.7 游戏初始化算法 (Game::init)

**算法描述：** 初始化游戏，生成地图、玩家、怪物和物品。

**输入：** 无  
**输出：** 完整的游戏状态

#### 3.7.1 PAD 图

> **[在此插入 Game::init PAD图]**
> 
> 文件位置：`uml/12_PAD_GameInit.plantuml`

**算法步骤说明：**

```
Game::init()
├─ 1. 添加欢迎日志
├─ 2. map_.generate()
├─ 3. 放置玩家
│   └─ 遍历地图找到第一个 Floor
│       └─ player_ = make_unique<Player>(x, y)
├─ 4. 生成 5 个怪物
│   └─ for i = 0 to 4
│       ├─ 随机位置 (x, y)
│       ├─ 检查: isWalkable && no monster
│       └─ entities_.push_back(make_unique<Monster>)
├─ 5. 生成 3 个物品
│   └─ for i = 0 to 2
│       ├─ 随机位置 (x, y)
│       ├─ 检查: isWalkable && no monster && no item
│       ├─ 随机类型 (Weapon 或 Potion)
│       └─ entities_.push_back(make_unique<Item>)
└─ 6. updateFOV()
    └─ 添加日志 "Entered dungeon"
```

#### 3.7.2 N-S 盒图

```
┌─────────────────────────────────────────────────────────┐
│                      Game::init()                        │
├─────────────────────────────────────────────────────────┤
│  1. 添加欢迎日志                                        │
├─────────────────────────────────────────────────────────┤
│  2. map_.generate()                                     │
├─────────────────────────────────────────────────────────┤
│  3. 放置玩家                                            │
│  ┌───────────────────────────────────────────────────┐  │
│  │  for y = 0 to height_-1                           │  │
│  │  └─ for x = 0 to width_-1                         │  │
│  │      └─ if isWalkable(x, y)                       │  │
│  │          ├─ player_ = Player(x, y)                │  │
│  │          └─ break                                 │  │
│  └───────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  4. 生成 5 个怪物                                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │  for i = 0 to 4                                   │  │
│  │  ├─ 随机位置 (最多 100 次尝试)                    │  │
│  │  ├─ 检查: isWalkable && no monster                │  │
│  │  └─ entities_.push_back(Monster)                  │  │
│  └───────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  5. 生成 3 个物品                                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │  for i = 0 to 2                                   │  │
│  │  ├─ 随机位置 (最多 100 次尝试)                    │  │
│  │  ├─ 检查: isWalkable && no monster && no item     │  │
│  │  ├─ 随机类型 (Weapon/Potion)                      │  │
│  │  └─ entities_.push_back(Item)                     │  │
│  └───────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  6. updateFOV() + 添加日志                              │
└─────────────────────────────────────────────────────────┘
```

---

## 4. 跨类调用方法签名规范

### 4.1 方法签名总览

本节规范化所有跨类调用的方法签名，包括：
- 公有接口 (public)
- 保护接口 (protected)
- 友元接口 (friend)

**命名约定：**
- 类名：`PascalCase`
- 公有方法：`camelCase`
- 私有方法：`camelCase` 带下划线前缀（内部使用）
- 成员变量：`snakeCase_` (带尾缀下划线)
- 参数：`camelCase`

### 4.2 Game 类接口

#### 4.2.1 公有方法 (Public)

| 方法签名 | 返回值 | 调用者 | 描述 |
|----------|--------|--------|------|
| `Game()` | 构造函数 | main.cpp | 创建游戏实例 |
| `void init()` | void | main.cpp | 初始化游戏系统 |
| `void run()` | void | main.cpp | 游戏主循环 |
| `Command handleInput(const std::string& input)` | Command | main.cpp | 解析键盘输入 |
| `void update()` | void | run() | 更新游戏状态 |
| `std::string render() const` | string | run() | 渲染游戏画面 |
| `bool isGameOver() const` | bool | main.cpp | 检查游戏是否结束 |
| `const std::vector<std::string>& logs() const` | vector<string>& | render() | 获取游戏日志 |
| `void addLog(const std::string& message)` | void | 多个方法 | 添加日志消息 |
| `void tryMovePlayer(int dx, int dy)` | void | main.cpp | 尝试移动玩家 |

#### 4.2.2 私有方法 (Private)

| 方法签名 | 返回值 | 内部调用者 | 描述 |
|----------|--------|----------|------|
| `void attackMonster(Monster* monster)` | void | tryMovePlayer() | 玩家攻击怪物 |
| `void updateMonsters()` | void | update() | 更新怪物AI |
| `void checkCollisions()` | void | update() | 碰撞检测（预留） |
| `void pickupItem()` | void | tryMovePlayer() | 拾取物品 |
| `void updateFOV()` | void | init(), update() | 更新战争迷雾 |
| `Player* getPlayer()` | Player* | 内部使用 | 获取玩家指针 |
| `Monster* getMonsterAt(int x, int y)` | Monster* | 多个方法 | 查询指定位置的怪物 |
| `Item* getItemAt(int x, int y)` | Item* | pickupItem() | 查询指定位置的物品 |

#### 4.2.3 跨类调用关系

```
Game 调用以下类的方法：

Game::init()
├─ Map::generate()
├─ Map::isWalkable()
├─ Map::isValid()
├─ Map::height()
├─ Map::width()
├─ Player::Player(x, y)  [构造函数]
├─ Monster::Monster(x, y)  [构造函数]
├─ Item::Item(type, x, y)  [构造函数]
├─ Game::getMonsterAt()
├─ Game::getItemAt()
└─ Game::updateFOV()

Game::tryMovePlayer(dx, dy)
├─ Player::x()
├─ Player::y()
├─ Player::move(dx, dy)
├─ Map::isValid()
├─ Map::isWalkable()
├─ Game::getMonsterAt()
├─ Game::attackMonster()
└─ Game::pickupItem()

Game::attackMonster(monster)
├─ Player::totalAtk()
├─ Monster::takeDamage()  [继承自 Entity]
├─ Monster::isAlive()  [继承自 Entity]
├─ Player::addExp()
└─ entities_.erase()

Game::updateMonsters()
├─ Entity::type()
├─ Monster::isAlive()  [继承自 Entity]
├─ Player::x()
├─ Player::y()
├─ Monster::x()  [继承自 Entity]
├─ Monster::y()  [继承自 Entity]
├─ Monster::setPosition()  [继承自 Entity]
├─ Map::isWalkable()
├─ Game::getMonsterAt()
├─ Monster::atk()  [继承自 Entity]
├─ Player::def()  [继承自 Entity]
└─ Player::takeDamage()  [继承自 Entity]

Game::pickupItem()
├─ Player::x()
├─ Player::y()
├─ Game::getItemAt()
├─ Item::itemType()
├─ Player::equipWeapon()
├─ Player::addPotion()
└─ entities_.erase()

Game::updateFOV()
├─ Map::clearVisibility()
├─ Player::x()
├─ Player::y()
├─ Map::isValid()
└─ Map::setTileVisible()

Game::render()
├─ Map::height()
├─ Map::width()
├─ Map::tile()
├─ Tile::visible
├─ Tile::explored
├─ Tile::type
├─ Player::x()
├─ Player::y()
├─ Player::symbol()  [继承自 Entity]
├─ Entity::x()
├─ Entity::y()
├─ Entity::symbol()
├─ Player::hp()  [继承自 Entity]
├─ Player::maxHp()  [继承自 Entity]
├─ Player::totalAtk()
├─ Player::def()  [继承自 Entity]
├─ Player::level()
└─ Player::potionCount()
```

### 4.3 Map 类接口

#### 4.3.1 公有方法 (Public)

| 方法签名 | 返回值 | 调用者 | 描述 |
|----------|--------|--------|------|
| `Map(int width, int height)` | 构造函数 | Game | 创建地图实例 |
| `int width() const` | int | Game, render() | 获取地图宽度 |
| `int height() const` | int | Game, render() | 获取地图高度 |
| `const Tile& tile(int x, int y) const` | Tile& | render() | 访问单元格（只读） |
| `Tile& tile(int x, int y)` | Tile& | 内部使用 | 访问单元格（可写） |
| `bool isWalkable(int x, int y) const` | bool | Game | 检查是否可通行 |
| `bool isWater(int x, int y) const` | bool | 未使用 | 检查是否是水 |
| `void generate(int seed = 0)` | void | Game::init() | 生成地图 |
| `std::string render() const` | string | 未使用 | 渲染地图（独立） |
| `void clearVisibility()` | void | Game::updateFOV() | 清除可见性 |
| `void setTileVisible(int x, int y)` | void | Game::updateFOV() | 设置可见性 |
| `void setTileExplored(int x, int y)` | void | 未使用 | 标记已探索 |

#### 4.3.2 私有方法 (Private)

| 方法签名 | 返回值 | 内部调用者 | 描述 |
|----------|--------|----------|------|
| `bool isValid(int x, int y) const` | bool | 多个方法 | 检查坐标合法性 |
| `void generateRooms()` | void | generate() | 生成房间 |
| `void generateCorridors()` | void | generate() | 生成走廊 |
| `void addWaterFeatures()` | void | generate() | 添加水域 |

#### 4.3.3 友元声明

```cpp
friend class Game;  // Game 可以访问 Map 的所有成员
```

#### 4.3.4 跨类调用关系

```
Map 被以下类调用：

Game::init()
├─ Map::generate()
├─ Map::isWalkable()
├─ Map::height()
└─ Map::width()

Game::tryMovePlayer()
├─ Map::isValid()
└─ Map::isWalkable()

Game::updateMonsters()
└─ Map::isWalkable()

Game::updateFOV()
├─ Map::clearVisibility()
├─ Map::isValid()
└─ Map::setTileVisible()

Game::render()
├─ Map::height()
├─ Map::width()
└─ Map::tile()
```

### 4.4 Player 类接口

#### 4.4.1 公有方法 (Public)

| 方法签名 | 返回值 | 调用者 | 描述 |
|----------|--------|--------|------|
| `Player(int x, int y)` | 构造函数 | Game::init() | 创建玩家实例 |
| `int exp() const` | int | render() | 获取经验值 |
| `int level() const` | int | render() | 获取等级 |
| `void addExp(int amount)` | void | Game::attackMonster() | 增加经验值 |
| `int weaponAttackBonus() const` | int | 未使用 | 获取武器加成 |
| `int potionCount() const` | int | render() | 获取药水数量 |
| `void equipWeapon(int bonus)` | void | Game::pickupItem() | 装备武器 |
| `void addPotion()` | void | Game::pickupItem() | 添加药水 |
| `void usePotion()` | void | main.cpp (P键) | 使用药水 |
| `int totalAtk() const` | int | Game::attackMonster(), render() | 获取总攻击力 |

#### 4.4.2 继承自 Entity 的方法

| 方法签名 | 返回值 | 调用者 | 描述 |
|----------|--------|--------|------|
| `int x() const` | int | 多个方法 | 获取X坐标 |
| `int y() const` | int | 多个方法 | 获取Y坐标 |
| `void move(int dx, int dy)` | void | Game::tryMovePlayer() | 移动玩家 |
| `char symbol() const` | char | render() | 获取显示字符 |
| `int hp() const` | int | render() | 获取当前HP |
| `int maxHp() const` | int | render() | 获取最大HP |
| `int atk() const` | int | 未直接调用 | 获取基础攻击力 |
| `int def() const` | int | Game::updateMonsters() | 获取防御力 |
| `bool isAlive() const` | bool | Game::updateMonsters() | 检查是否存活 |
| `void takeDamage(int damage)` | void | Game::updateMonsters() | 受到伤害 |
| `void heal(int amount)` | void | Player::usePotion() | 治疗 |

#### 4.4.3 跨类调用关系

```
Player 被以下类调用：

Game::init()
└─ Player::Player(x, y)  [构造函数]

Game::tryMovePlayer()
├─ Player::x()
├─ Player::y()
└─ Player::move()

Game::attackMonster()
├─ Player::totalAtk()
└─ Player::addExp()

Game::updateMonsters()
├─ Player::x()
├─ Player::y()
├─ Player::def()  [Entity]
├─ Player::takeDamage()  [Entity]
└─ Player::isAlive()  [Entity]

Game::pickupItem()
├─ Player::x()
├─ Player::y()
├─ Player::equipWeapon()
└─ Player::addPotion()

Game::render()
├─ Player::x()
├─ Player::y()
├─ Player::hp()  [Entity]
├─ Player::maxHp()  [Entity]
├─ Player::totalAtk()
├─ Player::def()  [Entity]
├─ Player::level()
└─ Player::potionCount()

Player::usePotion()
└─ Entity::heal()
```

### 4.5 Entity 基类接口

#### 4.5.1 公有方法 (Public)

| 方法签名 | 返回值 | 调用者 | 描述 |
|----------|--------|--------|------|
| `Entity(char symbol, int x, int y, EntityType type)` | 构造函数 | Player/Monster/Item | 创建实体 |
| `virtual ~Entity()` | 析构函数 | - | 虚析构函数 |
| `int x() const` | int | 多个方法 | 获取X坐标 |
| `int y() const` | int | 多个方法 | 获取Y坐标 |
| `void setPosition(int x, int y)` | void | Game::updateMonsters() | 设置位置 |
| `void move(int dx, int dy)` | void | Game::tryMovePlayer() | 移动 |
| `char symbol() const` | char | render() | 获取显示字符 |
| `void setSymbol(char symbol)` | void | 未使用 | 设置显示字符 |
| `EntityType type() const` | EntityType | 多个方法 | 获取实体类型 |
| `int hp() const` | int | render() | 获取HP |
| `int maxHp() const` | int | render() | 获取最大HP |
| `int atk() const` | int | Game::updateMonsters() | 获取攻击力 |
| `int def() const` | int | Game::updateMonsters() | 获取防御力 |
| `void setHp(int hp)` | void | 未使用 | 设置HP |
| `void setMaxHp(int hp)` | void | 未使用 | 设置最大HP |
| `void setAtk(int atk)` | void | 未使用 | 设置攻击力 |
| `void setDef(int def)` | void | 未使用 | 设置防御力 |
| `bool isAlive() const` | bool | 多个方法 | 检查是否存活 |
| `virtual void takeDamage(int damage)` | void | 多个方法 | 受到伤害 |
| `void heal(int amount)` | void | Player::usePotion() | 治疗 |

#### 4.5.2 保护成员 (Protected)

| 成员名 | 类型 | 描述 |
|--------|------|------|
| x_ | int | X坐标 |
| y_ | int | Y坐标 |
| symbol_ | char | 显示字符 |
| type_ | EntityType | 实体类型 |
| hp_ | int | 当前HP |
| maxHp_ | int | 最大HP |
| atk_ | int | 攻击力 |
| def_ | int | 防御力 |

### 4.6 调用关系图

#### 4.6.1 类依赖关系矩阵

| 调用方 \ 被调用方 | Game | Map | Player | Entity | Monster | Item |
|-------------------|------|-----|--------|--------|---------|------|
| **Game** | - | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Map** | - | - | - | - | - | - |
| **Player** | - | - | - | ✅ (继承) | - | - |
| **Monster** | - | - | - | ✅ (继承) | - | - |
| **Item** | - | - | - | ✅ (继承) | - | - |

#### 4.6.2 主要调用链

```
main.cpp
  └─ Game::run()
      ├─ Game::handleInput() → Command
      ├─ Game::tryMovePlayer()
      │   ├─ Map::isValid()
      │   ├─ Map::isWalkable()
      │   ├─ Player::x(), Player::y()
      │   ├─ Player::move()
      │   ├─ Game::getMonsterAt()
      │   │   └─ Entity::type(), Entity::x(), Entity::y()
      │   ├─ Game::attackMonster()
      │   │   ├─ Player::totalAtk()
      │   │   ├─ Entity::takeDamage()
      │   │   ├─ Entity::isAlive()
      │   │   └─ Player::addExp()
      │   │       └─ 升级逻辑
      │   └─ Game::pickupItem()
      │       ├─ Game::getItemAt()
      │       ├─ Item::itemType()
      │       ├─ Player::equipWeapon()
      │       └─ Player::addPotion()
      ├─ Game::update()
      │   └─ Game::updateMonsters()
      │       ├─ Entity::type(), Entity::isAlive()
      │       ├─ Player::x(), Player::y()
      │       ├─ Entity::x(), Entity::y()
      │       ├─ Map::isWalkable()
      │       ├─ Entity::setPosition()
      │       ├─ Entity::atk()
      │       ├─ Player::def()
      │       └─ Player::takeDamage()
      │           └─ Entity::takeDamage()
      └─ Game::render()
          ├─ Map::height(), Map::width(), Map::tile()
          ├─ Player::x(), Player::y()
          ├─ Entity::x(), Entity::y(), Entity::symbol()
          └─ Player::hp(), Player::maxHp(), Player::totalAtk(), ...
```

---

## 5. 数据结构设计

### 5.1 枚举类型

#### 5.1.1 Command

```cpp
enum class Command {
    MoveUp,      // 向上移动 (W)
    MoveDown,    // 向下移动 (S)
    MoveLeft,    // 向左移动 (A)
    MoveRight,   // 向右移动 (D)
    Wait,        // 等待一回合 (Space)
    UsePotion,   // 使用药水 (P)
    Quit,        // 退出游戏 (Q)
    Unknown      // 未知命令
};
```

#### 5.1.2 EntityType

```cpp
enum class EntityType {
    Player,   // 玩家
    Monster,  // 怪物
    Item      // 物品
};
```

#### 5.1.3 ItemType

```cpp
enum class ItemType {
    Weapon,  // 武器
    Potion,  // 药水
    None     // 无
};
```

#### 5.1.4 TileType

```cpp
enum class TileType {
    Wall,   // 墙壁 (#)
    Floor,  // 地板 (.)
    Water   // 水域 (~)
};
```

### 5.2 结构体

#### 5.2.1 Tile

```cpp
struct Tile {
    TileType type;       // 图块类型
    bool explored;       // 是否已探索（用于战争迷雾）
    bool visible;        // 当前是否可见

    Tile() : type(TileType::Wall), explored(false), visible(false) {}
};
```

**内存布局：**

| 字段 | 类型 | 大小 (字节) | 偏移 |
|------|------|------------|------|
| type | TileType (enum) | 4 | 0 |
| explored | bool | 1 | 4 |
| visible | bool | 1 | 5 |
| (padding) | - | 2 | 6 |
| **总计** | - | **8** | - |

### 5.3 类关系

#### 5.3.1 继承关系

```
Entity (基类)
├── Player    ← 添加 exp, level, weaponBonus, potionCount
├── Monster   ← 无额外字段
└── Item      ← 添加 itemType
```

#### 5.3.2 组合关系

```
Game
├── Map map_                    (值类型，组合)
├── unique_ptr<Player> player_  (智能指针，独占)
└── vector<unique_ptr<Entity>> entities_  (智能指针数组，独占)
    ├── Monster
    └── Item
```

#### 5.3.3 友元关系

```
Map <──friend── Game  (Game 可访问 Map 的私有成员)
```

---

## 6. 设计约束与注意事项

### 6.1 当前设计约束

| 约束 | 描述 | 影响 |
|------|------|------|
| 固定地图尺寸 | 40x20 | 无法动态调整 |
| 固定随机种子 | Map::generateRooms 使用 seed=42 | 地图可重复但固定 |
| 固定怪物数量 | 5个 | 无法动态调整难度 |
| 固定物品数量 | 3个 | 无法动态调整 |
| 固定升级阈值 | 50 Exp | 无法动态调整 |
| 固定武器加成 | +5 ATK | 无法扩展 |
| 固定药水治疗效果 | +25 HP | 无法扩展 |

### 6.2 代码坏味道 (待重构)

| 问题 | 位置 | 严重性 | 描述 |
|------|------|--------|------|
| 🔴 上帝类 | Game | 严重 | 承担过多职责 |
| 🔴 贫血模型 | Monster, Item | 严重 | 无独立行为 |
| 🟡 static_cast | Game::updateMonsters() | 中等 | 类型不安全 |
| 🟡 switch语句 | Game::pickupItem() | 中等 | 违反开闭原则 |
| 🟡 魔法数字 | 多处 | 中等 | 50, 5, 6, 10, 20, 25 |
| 🟢 硬编码日志 | Game::addLog() | 轻微 | 无法国际化 |

### 6.3 性能注意事项

| 算法 | 时间复杂度 | 空间复杂度 | 备注 |
|------|-----------|-----------|------|
| Map::generate() | O(W×H) | O(W×H) | W=宽度, H=高度 |
| Game::updateFOV() | O(R²) | O(1) | R=FOV半径 |
| Game::updateMonsters() | O(N×M) | O(1) | N=怪物数, M=查询 |
| Game::attackMonster() | O(N) | O(1) | N=实体数 (erase) |
| Game::pickupItem() | O(N) | O(1) | N=实体数 (erase) |
| Game::render() | O(W×H×N) | O(W×H) | N=实体数 |

### 6.4 安全注意事项

| 风险 | 位置 | 缓解措施 |
|------|------|----------|
| 数组越界 | Map::tile() | Map::isValid() 检查 |
| 空指针解引用 | Game::attackMonster() | 调用前检查 monster != nullptr |
| 整数溢出 | Player::addExp() | 当前 exp 值较小，无风险 |
| 内存泄漏 | entities_ 管理 | 使用 unique_ptr 自动管理 |

---

## 附录

### 附录 A: PAD图文件索引

| 算法 | PAD图文件 | N-S盒图位置 |
|------|----------|------------|
| Map::generate | `uml/06_PAD_MapGenerate.plantuml` | 本文档 3.1.2 |
| Game::updateFOV | `uml/07_PAD_UpdateFOV.plantuml` | 本文档 3.2.2 |
| Game::updateMonsters | `uml/08_PAD_UpdateMonsters.plantuml` | 本文档 3.3.2 |
| Game::attackMonster | `uml/09_PAD_AttackMonster.plantuml` | 本文档 3.4.2 |
| Player::addExp | `uml/10_PAD_AddExp.plantuml` | 本文档 3.5.2 |
| Game::pickupItem | `uml/11_PAD_PickupItem.plantuml` | 本文档 3.6.2 |
| Game::init | `uml/12_PAD_GameInit.plantuml` | 本文档 3.7.2 |

### 附录 B: 缩略语

| 缩略语 | 全称 |
|--------|------|
| DDS | Detailed Design Specification |
| PAD | Problem Analysis Diagram |
| N-S | Nassi-Shneiderman (盒图) |
| FOV | Field of View |
| HP | Hit Points |
| ATK | Attack |
| DEF | Defense |
| Exp | Experience |

### 附录 C: 设计模式应用建议

| 模式 | 应用场景 | 当前状态 |
|------|----------|----------|
| Command Pattern | 输入处理 | ❌ 未实现 |
| Strategy Pattern | 物品效果 | ❌ 未实现 |
| Factory Pattern | 实体创建 | ❌ 未实现 |
| Observer Pattern | 事件系统 | ❌ 未实现 |
| State Pattern | 游戏状态管理 | ❌ 未实现 |

---

**文档结束**

*本文档由项目团队编写，经审查确认*

*最后更新：2026年4月9日*
