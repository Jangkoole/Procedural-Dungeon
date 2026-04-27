# CIVC 工程自评审计报告

**项目**：Deep Dungeon（Roguelike 地牢游戏）  
**语言**：C++17  
**评估日期**：2026年4月27日  
**评估范围**：AI 辅助开发的约束、信息流、验证机制、纠正能力  

---

## (a) Constraint（约束）

### 当前 AI 可访问的文件范围

✅ **开放访问**：
- `include/*.h` - 所有头文件（接口定义）
- `src/*.cpp` - 所有源文件（实现）
- `api/openapi.yaml` - API 规范文档
- `CMakeLists.txt` - 构建配置
- `README.md` - 项目文档

✅ **应授予访问权限**（当前可能缺失）：
- `test/` 目录（单元测试框架）
- `.github/workflows/` - CI/CD 配置
- `build/` - 编译输出（用于调试）

❌ **禁止访问**（应隔离）：
- 系统级敏感文件（无）
- 凭证文件（无）
- 用户数据（无）

### 权限隔离问题分析

#### 问题 1：缺乏测试隔离机制
**现状**：
- 项目结构中没有独立的 `test/` 目录
- 缺少 Google Test 框架集成

**风险等级**：🟡 中等
- AI 无法独立编写和验证单元测试
- 容易生成基于不安全的 mock 对象的伪测试

**改进方案**：
```bash
# 步骤 1：创建测试目录结构
mkdir -p DeepDungeon/test
touch DeepDungeon/test/CMakeLists.txt

# 步骤 2：在 test/CMakeLists.txt 中配置 Google Test
cmake_minimum_required(VERSION 3.16)

# Fetch Google Test
include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/refs/tags/v1.13.0.zip
)
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

# 步骤 3：创建示例测试文件
cat > DeepDungeon/test/entity_test.cpp << 'EOF'
#include <gtest/gtest.h>
#include "../include/Entity.h"
using namespace dungeon;

TEST(EntityTest, TakeDamageReducesHP) {
    Entity ent('g', 5, 5, EntityType::Monster);
    int initial_hp = ent.hp();
    ent.takeDamage(10);
    ASSERT_LT(ent.hp(), initial_hp);
}
EOF
```

#### 问题 2：主源文件无读写权限分级
**现状**：
- Game.cpp、Entity.cpp 等核心文件均可被 AI 修改
- 缺少"只读文件清单"

**风险等级**：🔴 高
- AI 可能意外破坏关键函数（如 tryMovePlayer）
- 难以追踪不当修改

**改进方案**：
在项目根目录创建 `.ai-policy.yaml`
```yaml
# .ai-policy.yaml - AI 操作策略
read_only_files:
  - "include/Constants.h"           # 参数定义，仅增加，不删除
  - "include/Entity.h"              # 接口合约
  - "src/main.cpp"                  # 入口逻辑
  - "include/Game.h"                # 游戏状态机接口

modifiable_with_review:
  - "src/Game.cpp"                  # 可修改，但需单元测试验证
  - "src/Player.cpp"                # 可修改
  - "src/Map.cpp"                   # 可修改

never_delete:
  - "test/*"                        # 测试用例不可删除
  - "README.md"                     # 文档不可删除
  - "include/Constants.h"           # 参数表

strict_validation:
  - "src/Game.cpp::tryMovePlayer()" # 修改需 6+ 单元测试验证
```

#### 问题 3：依赖管理缺乏透明度
**现状**：
- CMakeLists.txt 中的外部依赖不清晰
- 缺少 `vcpkg.json` 或 `conanfile.txt`

**风险等级**：🟡 中等
- 难以跟踪依赖版本
- 不同环境可能编译不同的代码

**改进方案**：
创建 `DeepDungeon/conanfile.txt` 或 `vcpkg.json`
```json
{
  "name": "deep-dungeon",
  "version": "1.0.0",
  "dependencies": [
    {
      "name": "gtest",
      "version": ">=1.13.0",
      "type": "dev"
    }
  ]
}
```

---

## (b) Information（信息）

### AI 获取的上下文充分性

#### 信息维度 1：架构理解
**当前获得**：
- ✅ 完整的类图（Entity、Player、Map、Game）
- ✅ 函数签名和文档注释
- ✅ Constants.h 中的参数表

**信息盲区**：
- ❌ 缺少架构设计文档（为何采用 ECS-轻量而非完整 ECS？）
- ❌ 缺少性能约束文档（地图大小为何是 40x20？FOV 为何是 5？）
- ❌ 缺少交互流程图（输入→命令解析→逻辑更新→渲染的完整链路）

**改进方案**：
创建 `DeepDungeon/ARCHITECTURE.md`
```markdown
# 架构设计

## 为何采用轻量 ECS？
- 项目规模：~1000 行代码
- 完整 ECS 开销过大（>500 行框架）
- 轻量版本（std::vector<Entity>) 足以满足

## 性能约束
- 地图：40x20 = 800 格（每帧 O(n) 遍历可接受）
- 怪物数：5 个（暴力碰撞检测 O(5) 可接受）
- FOV：5 格（Bresenham 线算法，<50ms）

## 关键流程
```
User Input (W/A/S/D)
    ↓
handleInput() → Command enum
    ↓
Game::update()
  ├─ tryMovePlayer()
  ├─ updateMonsters()
  ├─ checkCollisions()
  └─ updateFOV()
    ↓
render() → std::cout
```
```

#### 信息维度 2：依赖关系透明度
**当前获得**：
- ✅ #include 语句清晰
- ✅ 公开 API 接口完整

**信息盲区**：
- ❌ 循环依赖检查缺失（Game.h 是否包含 Map.h？→ 是，会导致编译时间长）
- ❌ 缺少调用图（哪个函数调用哪个？）
- ❌ 缺少数据流文档

**改进方案**：
```bash
# 步骤 1：使用 Graphviz 生成依赖图
# 安装 Graphviz（Windows: choco install graphviz）

# 步骤 2：编写脚本分析依赖
cat > analyze_deps.py << 'EOF'
import os, re

files = []
deps = {}

for file in os.listdir("include"):
    if file.endswith(".h"):
        with open(f"include/{file}") as f:
            includes = re.findall(r'#include [<"]([^>"]+)[>"]', f.read())
            deps[file] = includes

# 输出 Graphviz DOT 格式
print("digraph {")
for src, targets in deps.items():
    for tgt in targets:
        if tgt.endswith(".h"):
            print(f'  "{src}" -> "{tgt}"')
print("}")
EOF

python analyze_deps.py > deps.dot
dot -Tpng deps.dot -o deps.png
```

#### 信息维度 3：测试覆盖情况
**当前获得**：
- ❌ 0 个测试文件（无法从代码库推断测试策略）
- ❌ 无代码覆盖率指标

**信息盲区**：
- ❌ 不知道哪些函数已测试、哪些未测试
- ❌ 缺少边界情况的已知漏洞列表
- ❌ 无性能基准

**改进方案**：
```bash
# 步骤 1：集成 gcov（代码覆盖率）
# 在 CMakeLists.txt 中添加：
if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
  add_compile_options(--coverage)
  link_libraries(gcov)
endif()

# 步骤 2：运行测试并生成覆盖率报告
cmake -DCMAKE_BUILD_TYPE=Debug ..
make
ctest
gcov src/*.cpp
lcov --capture --directory . --output-file coverage.info
genhtml coverage.info --output-directory coverage_html

# 步骤 3：在 CI 中自动生成报告
# 见下文 (c) Verification 部分
```

---

## (c) Verification（验证）

### 当前验证机制

#### 现状 1：无自动化测试框架
**状态**：🔴 缺失
- ❌ 项目无 test/ 目录
- ❌ 无 CMake test 集成
- ❌ 无单元测试代码

**风险**：
- AI 无法独立验证代码正确性
- 手动测试易遗漏边界情况
- 无法防止回归（修改 A 功能会破坏 B）

**改进方案（可执行步骤）**：

**步骤 1：创建 test 目录和 CMakeLists.txt**
```bash
mkdir -p DeepDungeon/test

cat > DeepDungeon/test/CMakeLists.txt << 'EOF'
cmake_minimum_required(VERSION 3.16)

# Fetch Google Test
include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/refs/tags/v1.13.0.zip
)
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

# 启用测试
enable_testing()

# 添加 Player 测试
add_executable(
  player_test
  player_test.cpp
  ../src/Player.cpp
  ../src/Entity.cpp
)
target_include_directories(player_test PRIVATE ../include)
target_link_libraries(player_test PRIVATE gtest gtest_main)
add_test(NAME PlayerTest COMMAND player_test)

# 添加 Game 移动测试
add_executable(
  game_move_test
  game_move_test.cpp
  ../src/Game.cpp
  ../src/Map.cpp
  ../src/Player.cpp
  ../src/Entity.cpp
)
target_include_directories(game_move_test PRIVATE ../include)
target_link_libraries(game_move_test PRIVATE gtest gtest_main)
add_test(NAME GameMoveTest COMMAND game_move_test)

# 运行所有测试
add_custom_target(
  run_tests
  COMMAND ctest --output-on-failure
  DEPENDS player_test game_move_test
)
EOF
```

**步骤 2：创建测试文件（使用前文的测试代码）**
```bash
# 创建 player_test.cpp（内容见 PROMPT_EVOLUTION_EXPERIMENT.md）
cp PROMPT_EVOLUTION_EXPERIMENT.md test/player_test.cpp

# 创建 game_move_test.cpp（内容见 PROMPT_EVOLUTION_EXPERIMENT.md）
cp PROMPT_EVOLUTION_EXPERIMENT.md test/game_move_test.cpp
```

**步骤 3：修改主 CMakeLists.txt**
```cmake
# DeepDungeon/CMakeLists.txt
cmake_minimum_required(VERSION 3.16)
project(DeepDungeon)

set(CMAKE_CXX_STANDARD 17)

add_subdirectory(src)
add_subdirectory(test)  # 新增

enable_testing()
```

**步骤 4：运行测试**
```bash
cd DeepDungeon/build
cmake ..
cmake --build .
ctest --output-on-failure
```

#### 现状 2：无 CI/CD 流水线
**状态**：🟡 部分
- ✅ 有 `.github/workflows/ci.yml`
- ❌ 内容未知（需查看）
- ❌ 缺少测试、覆盖率检查步骤

**改进方案（完整 CI/CD）**：

创建或更新 `.github/workflows/ci.yml`
```yaml
name: CI/CD Pipeline

on: [push, pull_request]

jobs:
  build-and-test:
    runs-on: ${{ matrix.os }}
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
        compiler: [g++, clang++]
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Install dependencies (Ubuntu)
        if: runner.os == 'Linux'
        run: |
          sudo apt-get update
          sudo apt-get install -y cmake g++ clang
      
      - name: Install dependencies (Windows)
        if: runner.os == 'Windows'
        run: |
          choco install cmake --yes
      
      - name: Configure CMake
        run: |
          cd DeepDungeon
          mkdir build
          cd build
          cmake -DCMAKE_BUILD_TYPE=Release ..
      
      - name: Build
        run: |
          cd DeepDungeon/build
          cmake --build . --config Release
      
      - name: Run Unit Tests
        run: |
          cd DeepDungeon/build
          ctest --output-on-failure
      
      - name: Generate Coverage Report
        if: runner.os == 'Linux'
        run: |
          cd DeepDungeon/build
          gcov src/*.cpp
          lcov --capture --directory . --output-file coverage.info
          lcov --remove coverage.info '/usr/*' '*/test/*' -o coverage.info
      
      - name: Upload Coverage to Codecov
        if: runner.os == 'Linux'
        uses: codecov/codecov-action@v3
        with:
          files: ./DeepDungeon/build/coverage.info
      
      - name: Static Analysis (clang-tidy)
        if: runner.os == 'Linux'
        run: |
          sudo apt-get install -y clang-tools
          cd DeepDungeon
          clang-tidy src/*.cpp -- -I./include
      
      - name: Build Game (Final)
        run: |
          cd DeepDungeon/build
          cmake --build . --config Release
      
      - name: Artifact Upload
        uses: actions/upload-artifact@v3
        with:
          name: game-binary-${{ matrix.os }}
          path: DeepDungeon/build/**/DeepDungeon*
```

#### 现状 3：代码覆盖率
**当前覆盖率**：
- 🔴 0% - 无单元测试

**目标**：
- 🟢 >= 80% 的代码行覆盖率
- 🟢 >= 70% 的分支覆盖率
- 🟢 核心函数 (tryMovePlayer, addExp) 100% 覆盖

**改进方案**：
参见上文 (c) Verification 步骤 1-4，集成 gcov 后自动生成报告。

---

## (d) Correction（纠正）

### 当前纠正机制

#### 现状 1：版本控制
**状态**：✅ 有 Git 仓库（推测）
- 假设已初始化 `.git`
- 有能力回滚（git revert/reset）

**风险**：
- ❌ 无 branch protection 规则（AI 可能提交到 main）
- ❌ 无 pre-commit hook（代码格式、测试未通过时拒绝提交）

**改进方案**：

**步骤 1：创建 pre-commit hook**
```bash
# .git/hooks/pre-commit
#!/bin/bash

echo "Running pre-commit checks..."

# 检查 C++ 代码格式
cd DeepDungeon/build
cmake --build . --target check-format || exit 1

# 运行单元测试
ctest --output-on-failure || exit 1

echo "✅ All checks passed!"
exit 0
```

**步骤 2：启用 branch protection（GitHub 设置）**
- Settings → Branches → Add rule
- Branch name pattern: `main`
- ✅ Require pull request reviews before merging
- ✅ Require status checks to pass before merging
- ✅ Require branches to be up to date before merging

**步骤 3：强制代码审查工作流**
```yaml
# 在 PR 中强制 CI 检查通过
name: Require CI Checks
on: [pull_request]
jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - run: echo "All CI checks must pass before merging"
```

#### 现状 2：回滚和版本管理
**状态**：✅ Git 支持回滚
- `git revert <commit>` - 安全回滚（新增 commit）
- `git reset --hard <commit>` - 强制回滚（危险，需权限）

**流程**：
```
Bug 发现 → 运行 ctest 失败 → git bisect 定位 → git revert 或修复
```

**改进方案**：
创建 `ROLLBACK.md`，文档化回滚流程
```markdown
# 紧急回滚指南

## 场景 1：新提交引入 bug
\`\`\`bash
git log --oneline | head -10
# 假设 abc1234 是有问题的 commit
git revert abc1234
git push origin main
\`\`\`

## 场景 2：需要回滚多个 commit
\`\`\`bash
git reset --soft HEAD~3  # 回滚最近 3 个，保留代码
git commit -m "Fix: revert broken commits"
\`\`\`

## 场景 3：测试失败，回滚并通知
\`\`\`bash
ctest || {
  echo "❌ Tests failed! Rolling back..."
  git revert HEAD
  echo "⚠️ Rollback complete. Please fix and resubmit."
  exit 1
}
\`\`\`
```

#### 现状 3：错误检测和修复
**当前机制**：❌ 缺失

**改进方案（完整的错误检测流水线）**：

**步骤 1：静态分析（clang-tidy）**
```bash
# DeepDungeon/CMakeLists.txt 添加
find_program(CLANG_TIDY "clang-tidy")
if(CLANG_TIDY)
  set(CMAKE_CXX_CLANG_TIDY ${CLANG_TIDY} -checks=*)
endif()

# 运行
cd DeepDungeon/build
clang-tidy ../src/*.cpp -- -I../include
```

**步骤 2：动态检查（AddressSanitizer）**
```cmake
# CMakeLists.txt
if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
  add_compile_options(-fsanitize=address -fsanitize=undefined)
  link_libraries(-fsanitize=address -fsanitize=undefined)
endif()
```

**步骤 3：自动化检测脚本**
```bash
#!/bin/bash
# scripts/validate.sh

set -e

echo "🔍 Validation Pipeline"

# 编译
echo "  1. Building..."
cd DeepDungeon/build
cmake --build . || exit 1

# 测试
echo "  2. Running tests..."
ctest --output-on-failure || exit 1

# 覆盖率
echo "  3. Checking coverage..."
MIN_COVERAGE=80
COVERAGE=$(gcov_extract_coverage)
if [ $COVERAGE -lt $MIN_COVERAGE ]; then
  echo "❌ Coverage $COVERAGE% < target $MIN_COVERAGE%"
  exit 1
fi

# 静态分析
echo "  4. Static analysis..."
clang-tidy ../src/*.cpp -- -I../include || echo "⚠️ Warnings found"

echo "✅ All validation passed!"
```

**步骤 4：在 CI 中自动运行**
```yaml
# .github/workflows/ci.yml - validate job
- name: Run Validation
  run: bash DeepDungeon/scripts/validate.sh
```

---

## 总体风险评分

| 维度 | 评分 | 状态 | 优先级 |
|------|------|------|--------|
| Constraint（约束） | 🟡 60/100 | 缺少文件隔离策略 | 🔴 高 |
| Information（信息） | 🟡 55/100 | 缺少架构和设计文档 | 🟡 中 |
| Verification（验证） | 🔴 10/100 | 无测试框架 | 🔴 最高 |
| Correction（纠正） | 🟡 50/100 | 缺少 CI/CD 和 pre-commit | 🔴 高 |

**综合分数**：**43/100** - **需立即改进**

---

## 执行优先级和时间表

### 第 1 阶段（第 1 周）- 关键
- [ ] 创建 test/ 目录和 Google Test 集成（2h）
- [ ] 添加 player_test.cpp 和 game_move_test.cpp（3h）
- [ ] 创建 AGENTS.md 和 .ai-policy.yaml（1h）

**完成后得分**：70/100

### 第 2 阶段（第 2 周）- 重要
- [ ] 配置 GitHub Actions CI/CD（2h）
- [ ] 集成 gcov 代码覆盖率（1h）
- [ ] 集成 clang-tidy 静态分析（1h）

**完成后得分**：85/100

### 第 3 阶段（第 3 周）- 优化
- [ ] 编写 ARCHITECTURE.md 和 ROLLBACK.md（1h）
- [ ] 创建 pre-commit hook（0.5h）
- [ ] 设置 GitHub branch protection（0.5h）

**完成后得分**：95/100

---

## 附件 A：快速启用 Google Test

```bash
# 一键启用脚本
#!/bin/bash
set -e

echo "Setting up Google Test..."

mkdir -p DeepDungeon/test

# 下载 Google Test
cd DeepDungeon/test
wget https://github.com/google/googletest/archive/refs/tags/v1.13.0.zip
unzip v1.13.0.zip
mv googletest-1.13.0 gtest

# 创建 CMakeLists.txt（见前文）
cat > CMakeLists.txt << 'EOF'
# ... 内容见前文 ...
EOF

# 编译
cd ../build
cmake -DCMAKE_BUILD_TYPE=Debug ..
cmake --build .

# 运行测试
ctest --output-on-failure

echo "✅ Google Test setup complete!"
```

---

**报告终结**

