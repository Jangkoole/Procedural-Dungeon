#pragma once

#include <string>
#include <vector>
#include <memory>
#include "Constants.h"

// 前向声明
namespace dungeon {
class Map;
class Player;
}

namespace dungeon {

// 实体类型
enum class EntityType {
    Player,
    Monster,
    Item
};

// 物品类型
enum class ItemType {
    Weapon,
    Potion,
    None
};

// 实体类 - 所有游戏对象的基础
class Entity {
public:
    Entity(char symbol, int x, int y, EntityType type);
    virtual ~Entity() = default;

    // 位置
    int x() const { return x_; }
    int y() const { return y_; }
    void setPosition(int x, int y) { x_ = x; y_ = y; }
    
    // 移动
    void move(int dx, int dy);
    
    // 渲染字符
    char symbol() const { return symbol_; }
    void setSymbol(char symbol) { symbol_ = symbol; }
    
    // 类型
    EntityType type() const { return type_; }
    
    // 属性
    int hp() const { return hp_; }
    int maxHp() const { return maxHp_; }
    int atk() const { return atk_; }
    int def() const { return def_; }
    
    void setHp(int hp) { hp_ = hp; }
    void setMaxHp(int hp) { maxHp_ = hp; }
    void setAtk(int atk) { atk_ = atk_; }
    void setDef(int def) { def_ = def_; }
    
    // 是否存活
    bool isAlive() const { return hp_ > 0; }
    
    // 受到伤害
    virtual void takeDamage(int damage);
    
    // 治疗
    void heal(int amount);

protected:
    int x_, y_;
    char symbol_;
    EntityType type_;
    
    int hp_;
    int maxHp_;
    int atk_;
    int def_;
};

// 怪物类
class Monster : public Entity {
public:
    Monster(int x, int y);

    // 怪物 AI - 计算移动方向
    void calculateMoveTowards(int targetX, int targetY, int& outDx, int& outDy) const;

    // 攻击玩家
    void attackPlayer(Player& player, std::vector<std::string>& log) const;
};

// 物品类
class Item : public Entity {
public:
    Item(ItemType itemType, int x, int y);
    
    ItemType itemType() const { return itemType_; }
    
private:
    ItemType itemType_;
};

} // namespace dungeon
