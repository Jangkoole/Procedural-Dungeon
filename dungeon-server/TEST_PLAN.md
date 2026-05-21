# Procedural Dungeon API — 集成测试方案

> **版本**: 2.0.0  
> **更新日期**: 2026-05-21  
> **对应 API**: `api/openapi.yaml`

---

## 1. 概述

本文档定义 Procedural Dungeon REST API 的集成测试策略、测试用例组织和执行方案。

### 1.1 测试目标

- 验证所有 API 端点按 OpenAPI 规范正确响应
- 覆盖正常业务流程、边界条件、错误路径和并发场景
- 确保游戏状态在每次操作后保持一致
- 验证会话管理（创建、查询、删除、隔离）的正确性

### 1.2 技术栈

| 组件 | 技术 |
|------|------|
| 测试框架 | JUnit 5 (Jupiter) |
| HTTP 客户端 | `TestRestTemplate` (Spring Boot) |
| 断言 | JUnit 5 + Hamcrest |
| 构建工具 | Maven (`mvn test`) |
| 测试隔离 | `@SpringBootTest(webEnvironment = RANDOM_PORT)` |

### 1.3 测试架构

```
src/test/java/com/dungeon/controller/
├── DungeonControllerIntegrationTest.java   # 基础流程（顺序执行）
├── DungeonErrorScenariosTest.java          # 边界与错误场景
├── DungeonStateConsistencyTest.java        # 状态一致性验证
└── DungeonConcurrencyTest.java             # 并发与会话管理
```

---

## 2. 测试分类

### 2.1 基础流程测试 (`DungeonControllerIntegrationTest`)

按 `@Order` 顺序执行，模拟一次完整的游戏会话生命周期。

| # | 测试用例 | 验证点 |
|---|---------|--------|
| 1 | 使用默认参数创建新游戏 | sessionId 非空、status=PLAYING、turn=0、player 属性正确、dungeon 尺寸正确、5 monsters、3 items、messages 非空 |
| 2 | 使用最小/最大边界尺寸 | width=20/80, height=10/40 均成功 |
| 3 | 尺寸超出范围返回 400 | width<20 或 >80 返回 BAD_REQUEST |
| 10 | 获取游戏状态 | 返回的 sessionId 匹配、player/dungeon 非空 |
| 20 | 向四个方向移动 | 至少一个方向可通行、events 非空 |
| 21 | 无效方向返回 400 | "north" 和空字符串均返回 BAD_REQUEST |
| 22 | 移动到墙壁返回 BLOCKED | 返回 BAD_REQUEST + error="BLOCKED" |
| 30 | 等待一回合 | turn 递增 1、events 包含 WAIT |
| 40 | 无物品时拾取返回 400 | error="NO_ITEM" |
| 50 | 无药水时使用返回 400 | error="NO_POTIONS" |
| 60 | 列出活跃会话 | count>=1、当前会话在列表中 |
| 61 | 不存在的会话返回 404 | 所有端点均返回 NOT_FOUND |
| 62 | 删除会话后不可访问 | DELETE 返回 200、GET 返回 404 |

### 2.2 边界与错误场景测试 (`DungeonErrorScenariosTest`)

每个测试用例独立创建会话，覆盖异常路径。

| 测试用例 | 验证点 |
|---------|--------|
| 空请求体生成地牢 | 使用默认值，应成功 |
| 负尺寸生成地牢 | 返回 400 |
| 零尺寸生成地牢 | 返回 400 |
| 缺少 direction 字段移动 | 返回 400 |
| 无效 direction 值移动 | 返回 400 + error 非空 |
| 游戏结束后操作 | 所有操作返回 GAME_OVER |
| HP 满时用药水 | 返回 NO_POTIONS |
| 重复删除会话 | 第二次返回 404 |

### 2.3 状态一致性测试 (`DungeonStateConsistencyTest`)

验证每次操作后游戏状态变化符合预期。

| 测试用例 | 验证点 |
|---------|--------|
| 移动后玩家位置更新 | 成功移动后坐标变化 |
| 移动被阻挡时位置不变 | 失败移动后坐标不变 |
| 攻击怪物后 HP 减少 | 战斗事件后怪物 HP 降低 |
| 每次操作回合数递增 | move/wait 后 turn+1 |
| 拾取物品后从地牢移除 | PICKUP 事件后 items 数量减少 |
| 等待后怪物可能移动 | 怪物 AI 正常运行 |
| 击杀所有怪物后胜利 | 状态变为 VICTORY |

### 2.4 并发与会话管理测试 (`DungeonConcurrencyTest`)

覆盖多会话隔离和并发操作。

| 测试用例 | 验证点 |
|---------|--------|
| 多会话隔离 | 3 个独立会话互不干扰，操作一个不影响其他 |
| 会话列表完整性 | 列表包含所有活跃会话、字段完整 |
| 并发创建 5 个会话 | 全部成功、sessionId 唯一 |
| 并发操作同一会话 | 10 个并发请求不抛异常、状态可读 |
| 删除后不在列表中 | 删除后列表查询不包含 |
| 批量创建 10 个会话 | 全部成功、全部可访问 |

---

## 3. API 端点覆盖矩阵

| 端点 | 方法 | 基础流程 | 错误场景 | 状态一致性 | 并发 |
|------|------|---------|---------|-----------|------|
| `/api/dungeon/generate` | POST | ✅ | ✅ | — | ✅ |
| `/api/dungeon/{id}/state` | GET | ✅ | ✅ | ✅ | ✅ |
| `/api/dungeon/{id}/move` | POST | ✅ | ✅ | ✅ | ✅ |
| `/api/dungeon/{id}/pickup` | POST | ✅ | ✅ | — | — |
| `/api/dungeon/{id}/use-potion` | POST | ✅ | ✅ | — | — |
| `/api/dungeon/{id}/wait` | POST | ✅ | ✅ | ✅ | — |
| `/api/sessions` | GET | ✅ | — | — | ✅ |
| `/api/sessions/{id}` | DELETE | ✅ | ✅ | — | ✅ |

---

## 4. 测试数据设计

### 4.1 地牢生成参数

| 参数 | 正常值 | 边界值 | 无效值 |
|------|-------|--------|--------|
| width | 40 | 20, 80 | 19, 81, -1, 0 |
| height | 20 | 10, 40 | 9, 41, -1, 0 |

### 4.2 移动方向

| 有效值 | 无效值 |
|--------|--------|
| up, down, left, right | north, south, east, west, 空字符串, null |

### 4.3 游戏状态枚举

| 状态 | 触发条件 |
|------|---------|
| PLAYING | 游戏进行中 |
| VICTORY | 所有怪物被击杀 |
| DEFEAT | 玩家 HP 归零 |

---

## 5. 执行指南

### 5.1 运行所有测试

```bash
cd dungeon-server
mvn test
```

### 5.2 运行特定测试类

```bash
# 基础流程测试
mvn test -Dtest=DungeonControllerIntegrationTest

# 错误场景测试
mvn test -Dtest=DungeonErrorScenariosTest

# 状态一致性测试
mvn test -Dtest=DungeonStateConsistencyTest

# 并发测试
mvn test -Dtest=DungeonConcurrencyTest
```

### 5.3 运行单个测试方法

```bash
mvn test -Dtest=DungeonControllerIntegrationTest#generateDungeon_ShouldCreateSession
```

### 5.4 生成测试报告

```bash
mvn test site
# 报告位于 target/site/index.html
```

### 5.5 CI 集成

测试已配置在 GitHub Actions 中自动运行（参见 `.github/workflows/`）。

---

## 6. 测试质量指标

| 指标 | 目标 | 当前 |
|------|------|------|
| 测试用例总数 | ≥ 30 | 38 |
| API 端点覆盖率 | 100% | 100% |
| 错误路径覆盖率 | ≥ 80% | ✅ |
| 并发场景覆盖 | ≥ 3 场景 | ✅ |
| 状态一致性验证 | 核心操作 | ✅ |

---

## 7. 扩展指南

### 7.1 添加新测试

1. 确定测试类别（基础流程/错误场景/状态一致性/并发）
2. 在对应测试类中添加 `@Test` 方法
3. 遵循命名规范：`{method}_{scenario}_{expectedResult}`
4. 使用 `@DisplayName` 提供中文描述
5. 独立测试用例应自行创建会话

### 7.2 测试命名规范

```
# 成功路径
{method}_{scenario}_Should{ExpectedResult}

# 错误路径
{method}_{condition}_ShouldReturn{StatusCode}

# 示例
generateDungeon_WithDefaults_ShouldCreateSession
movePlayer_InvalidDirection_ShouldReturn400
```

### 7.3 新增 API 端点时的测试清单

- [ ] 正常请求返回 200
- [ ] 缺少必需参数返回 400
- [ ] 无效参数值返回 400
- [ ] 不存在的资源返回 404
- [ ] 状态一致性验证
- [ ] 并发安全（如适用）

---

## 8. 已知限制

1. **地牢随机性**：BSP 生成算法具有随机性，某些测试（如"移动到墙壁"）可能因地牢布局无法稳定复现，测试中已做容错处理。
2. **游戏结束测试**：依赖怪物 AI 攻击玩家，可能需要多回合等待，设置了最大尝试次数防止死循环。
3. **无持久化层**：会话存储在内存 `ConcurrentHashMap` 中，重启后丢失。测试不依赖持久化。
4. **无认证/授权**：当前 API 无认证机制，未覆盖安全相关测试。
