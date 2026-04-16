#include "Game.h"
#include "InputHandler.h"
#include <iostream>
#include <algorithm>
#include <cmath>
#include <random>

namespace dungeon {

Game::Game() : gameOver_(false), gameWon_(false) {}

void Game::init() {
    addLog("=== Welcome to Deep Dungeon ===");
    addLog("Use W/A/S/D to move, Q to quit");

    // 生成地图
    map_.generate();

    // 找到玩家的出生点
    bool playerPlaced = false;
    for (int y = 0; y < map_.height() && !playerPlaced; y++) {
        for (int x = 0; x < map_.width() && !playerPlaced; x++) {
            if (map_.isWalkable(x, y)) {
                player_ = std::make_unique<Player>(x, y);
                playerPlaced = true;
            }
        }
    }

    // 生成怪物
    spawnEntities<Monster>(5, [](int x, int y) {
        return std::make_unique<Monster>(x, y);
    });

    // 生成物品
    spawnEntities<Item>(3, [](int x, int y) {
        std::mt19937 rng(789);
        std::uniform_int_distribution<int> dist(0, 1);
        ItemType type = (dist(rng) == 0) ? ItemType::Weapon : ItemType::Potion;
        return std::make_unique<Item>(type, x, y);
    });

    updateFOV();
    addLog("You entered a mysterious dungeon...");
}

bool Game::isValidSpawnPosition(int x, int y) const {
    if (!map_.isWalkable(x, y)) return false;
    if (getMonsterAt(x, y) != nullptr) return false;
    if (getItemAt(x, y) != nullptr) return false;
    return true;
}

Command Game::handleInput(const std::string& input) const {
    InputHandler handler;
    return handler.parseCommand(input);
}

void Game::update() {
    if (gameOver_) return;

    // 更新怪物 AI
    bool allMonstersDefeated = false;
    monsterAI_.updateMonsters(entities_, *player_, map_, log_, allMonstersDefeated);

    // 检查胜利条件
    if (allMonstersDefeated) {
        gameWon_ = true;
        gameOver_ = true;
        addLog("=== You cleared all monsters, Victory! ===");
    }

    // 更新视野
    updateFOV();
}

void Game::tryMovePlayer(int dx, int dy) {
    int newX = player_->x() + dx;
    int newY = player_->y() + dy;

    // 检查边界
    if (!map_.isValid(newX, newY)) {
        addLog("That's a wall.");
        return;
    }

    // 检查是否有怪物
    Monster* monster = getMonsterAt(newX, newY);
    if (monster) {
        attackMonster(monster);
        return;
    }

    // 检查是否可通行
    if (!map_.isWalkable(newX, newY)) {
        addLog("That's an obstacle.");
        return;
    }

    // 移动
    player_->move(dx, dy);
    addLog("You moved.");

    // 检查拾取
    pickupItem();
}

void Game::attackMonster(Monster* monster) {
    int damage = player_->totalAtk();
    monster->takeDamage(damage);
    addLog("You attacked the monster for " + std::to_string(damage) + " damage.");

    if (!monster->isAlive()) {
        addLog("Monster defeated! Gained 20 exp.");
        player_->addExp(20);

        // 移除怪物
        removeEntity(monster);
    }
}

void Game::removeEntity(Entity* entity) {
    entities_.erase(
        std::remove_if(entities_.begin(), entities_.end(),
            [entity](const std::unique_ptr<Entity>& e) {
                return e.get() == entity;
            }),
        entities_.end()
    );
}

void Game::checkCollisions() {
    // 预留用于其他碰撞检测
}

void Game::pickupItem() {
    Item* item = getItemAt(player_->x(), player_->y());
    if (item) {
        collectItem(item);
        removeEntity(item);
    }
}

void Game::collectItem(Item* item) {
    switch (item->itemType()) {
        case ItemType::Weapon:
            player_->equipWeapon(5);
            addLog("You found a sword! ATK +5");
            break;
        case ItemType::Potion:
            player_->addPotion();
            addLog("You found a potion! Press P to use");
            break;
        default:
            break;
    }
}

void Game::usePlayerPotion() {
    if (player_->potionCount() > 0) {
        player_->usePotion();
        addLog("You used a potion! Healed 25 HP.");
    } else {
        addLog("No potions available.");
    }
}

void Game::updateFOV() {
    map_.clearVisibility();

    int px = player_->x(), py = player_->y();

    // 简单圆形 FOV
    for (int dy = -FOV_RADIUS; dy <= FOV_RADIUS; dy++) {
        for (int dx = -FOV_RADIUS; dx <= FOV_RADIUS; dx++) {
            if (dx * dx + dy * dy <= FOV_RADIUS * FOV_RADIUS) {
                int x = px + dx;
                int y = py + dy;
                if (map_.isValid(x, y)) {
                    map_.setTileVisible(x, y);
                }
            }
        }
    }
}

std::string Game::render() const {
    return renderer_.renderGame(map_, *player_, entities_, log_);
}

void Game::addLog(const std::string& message) {
    log_.push_back(message);
    // 限制日志数量
    if (log_.size() > 20) {
        log_.erase(log_.begin());
    }
}

Monster* Game::getMonsterAt(int x, int y) const {
    for (const auto& entity : entities_) {
        if (entity->type() == EntityType::Monster &&
            entity->x() == x && entity->y() == y) {
            return static_cast<Monster*>(entity.get());
        }
    }
    return nullptr;
}

Item* Game::getItemAt(int x, int y) const {
    for (const auto& entity : entities_) {
        if (entity->type() == EntityType::Item &&
            entity->x() == x && entity->y() == y) {
            return static_cast<Item*>(entity.get());
        }
    }
    return nullptr;
}

} // namespace dungeon
