#pragma once

#include "Entity.h"
#include "Map.h"
#include <vector>

namespace dungeon {

class Player;

// 怪物 AI 系统 - 负责怪物行为逻辑
class MonsterAI {
public:
    MonsterAI();

    // 更新所有怪物
    void updateMonsters(std::vector<std::unique_ptr<Entity>>& entities,
                        Player& player,
                        const Map& map,
                        std::vector<std::string>& log,
                        bool& allMonstersDefeated);

private:
    // 单个怪物的 AI 决策
    void processMonster(Monster* monster,
                        const Player& player,
                        const Map& map,
                        std::vector<std::string>& log,
                        std::vector<Entity*>& defeatedMonsters,
                        const std::vector<std::unique_ptr<Entity>>& allEntities);

    // 检查是否有怪物在指定位置（排除指定实体）
    bool hasMonsterAt(int x, int y, 
                      const std::vector<std::unique_ptr<Entity>>& entities,
                      const Entity* excludeEntity) const;
};

} // namespace dungeon
