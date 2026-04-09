#include "MonsterAI.h"
#include "Player.h"
#include <algorithm>

namespace dungeon {

MonsterAI::MonsterAI() {}

void MonsterAI::updateMonsters(std::vector<std::unique_ptr<Entity>>& entities,
                                Player& player,
                                const Map& map,
                                std::vector<std::string>& log,
                                bool& allMonstersDefeated) {
    std::vector<Entity*> defeatedMonsters;

    // 更新每个怪物
    for (auto& entity : entities) {
        if (entity->type() == EntityType::Monster && entity->isAlive()) {
            Monster* monster = static_cast<Monster*>(entity.get());
            processMonster(monster, player, map, log, defeatedMonsters, entities);
            
            // 检查玩家是否死亡
            if (!player.isAlive()) {
                return;
            }
        }
    }

    // 移除死亡的怪物
    entities.erase(
        std::remove_if(entities.begin(), entities.end(),
            [&defeatedMonsters](const std::unique_ptr<Entity>& e) {
                return std::find(defeatedMonsters.begin(), defeatedMonsters.end(), e.get()) 
                       != defeatedMonsters.end();
            }),
        entities.end()
    );

    // 检查是否所有怪物被击败
    allMonstersDefeated = true;
    for (const auto& entity : entities) {
        if (entity->type() == EntityType::Monster) {
            allMonstersDefeated = false;
            break;
        }
    }
}

void MonsterAI::processMonster(Monster* monster,
                                const Player& player,
                                const Map& map,
                                std::vector<std::string>& log,
                                std::vector<Entity*>& defeatedMonsters,
                                const std::vector<std::unique_ptr<Entity>>& allEntities) {
    int px = player.x(), py = player.y();
    int mx = monster->x(), my = monster->y();

    // 计算移动方向
    int dx = 0, dy = 0;
    monster->calculateMoveTowards(px, py, dx, dy);

    if (dx != 0 || dy != 0) {
        int newX = mx + dx;
        int newY = my + dy;

        // 检查是否是玩家位置（攻击）
        if (newX == px && newY == py) {
            monster->attackPlayer(player, log);
        }
        // 检查是否是可通行的空地
        else if (map.isWalkable(newX, newY) && 
                 !hasMonsterAt(newX, newY, allEntities, monster)) {
            monster->setPosition(newX, newY);
        }
    }
}

bool MonsterAI::hasMonsterAt(int x, int y, 
                              const std::vector<std::unique_ptr<Entity>>& entities,
                              const Entity* excludeEntity) const {
    for (const auto& entity : entities) {
        if (entity.get() == excludeEntity) continue;
        
        if (entity->type() == EntityType::Monster &&
            entity->x() == x && entity->y() == y) {
            return true;
        }
    }
    return false;
}

} // namespace dungeon
