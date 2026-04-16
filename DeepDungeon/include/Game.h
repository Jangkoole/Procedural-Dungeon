#pragma once

#include <vector>
#include <memory>
#include <string>
#include "Map.h"
#include "Player.h"
#include "Entity.h"
#include "MonsterAI.h"
#include "Renderer.h"

namespace dungeon {

// 游戏输入命令
enum class Command {
    MoveUp,
    MoveDown,
    MoveLeft,
    MoveRight,
    Wait,
    UsePotion,
    Quit,
    Unknown
};

// 游戏主类
class Game {
public:
    Game();

    // 初始化游戏
    void init();

    // 游戏主循环
    void run();

    // 处理输入
    Command handleInput(const std::string& input) const;

    // 更新游戏状态
    void update();

    // 渲染游戏
    std::string render() const;

    // 游戏是否结束
    bool isGameOver() const { return gameOver_; }

    // 获取游戏日志
    const std::vector<std::string>& logs() const { return log_; }

    // 添加日志
    void addLog(const std::string& message);

    // 玩家移动
    void tryMovePlayer(int dx, int dy);

    // 使用药水
    void usePlayerPotion();

private:
    Map map_;
    std::unique_ptr<Player> player_;
    std::vector<std::unique_ptr<Entity>> entities_;

    bool gameOver_;
    bool gameWon_;
    std::vector<std::string> log_;

    // 子系统
    MonsterAI monsterAI_;
    Renderer renderer_;

    // 游戏逻辑
    void attackMonster(Monster* monster);
    void checkCollisions();
    void pickupItem();
    void collectItem(Item* item);
    void removeEntity(Entity* entity);

    // FOV 计算
    void updateFOV();

    // 辅助函数
    Monster* getMonsterAt(int x, int y) const;
    Item* getItemAt(int x, int y) const;
    bool isValidSpawnPosition(int x, int y) const;

    // 实体生成
    template<typename T, typename Factory>
    void spawnEntities(int count, Factory factory) {
        std::mt19937 rng(456);
        std::uniform_int_distribution<int> posDist(0, map_.width() - 1);

        for (int i = 0; i < count; i++) {
            int x, y;
            int attempts = 0;
            do {
                x = posDist(rng) % map_.width();
                y = posDist(rng) % map_.height();
                attempts++;
            } while (!isValidSpawnPosition(x, y) && attempts < 100);

            if (attempts < 100) {
                entities_.push_back(factory(x, y));
            }
        }
    }
};

} // namespace dungeon
