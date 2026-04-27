#include "Player.h"

namespace dungeon {

Player::Player(int x, int y)
    : Entity(dungeon::CHAR_PLAYER, x, y, EntityType::Player),
      exp_(0), level_(1), weaponBonus_(0), potionCount_(0) {
    hp_ = dungeon::PLAYER_BASE_HP;
    maxHp_ = dungeon::PLAYER_BASE_HP;
    atk_ = dungeon::PLAYER_BASE_ATK;
    def_ = dungeon::PLAYER_BASE_DEF;
}

void Player::addExp(int amount) {
    int oldLevel = level_;
    exp_ += amount;
    // 简单升级逻辑：每 50 exp 升一级
    int newLevel = exp_ / 50 + 1;
    if (newLevel > level_) {
        int levelsGained = newLevel - level_;
        level_ = newLevel;
        maxHp_ += 10 * levelsGained;
        hp_ = maxHp_;  // 升级时满血
        atk_ += 2 * levelsGained;
        def_ += 1 * levelsGained;
    }
}

void Player::usePotion() {
    if (potionCount_ > 0) {
        potionCount_--;
        heal(25);
    }
}

} // namespace dungeon
