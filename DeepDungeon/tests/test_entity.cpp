#include <gtest/gtest.h>
#include "Entity.h"

using namespace dungeon;

// ============================================================
// 测试套件：Entity::takeDamage() 和 heal()
// ============================================================

// Entity 基类默认属性：HP 50, ATK 5, DEF 2
// takeDamage(damage) 内部计算：actualDamage = max(1, damage - def_)
// 然后 hp_ = max(0, hp_ - actualDamage)

TEST(EntityTest, TakeDamage_ReducesHp) {
    Entity entity('@', 0, 0, EntityType::Player);
    int initialHp = entity.hp();

    entity.takeDamage(10);  // 传入 10，实际伤害 = max(1, 10-2) = 8

    EXPECT_EQ(entity.hp(), initialHp - 8);
    EXPECT_TRUE(entity.isAlive());
}

TEST(EntityTest, TakeDamage_DefenseReduction) {
    Entity entity('@', 0, 0, EntityType::Player);

    entity.takeDamage(5);   // 实际伤害 = max(1, 5-2) = 3
    int hpAfterFirstHit = entity.hp();

    entity.takeDamage(2);   // 实际伤害 = max(1, 2-2) = 1（至少 1 点）
    int hpAfterSecondHit = entity.hp();

    EXPECT_EQ(hpAfterFirstHit, 50 - 3);      // 47
    EXPECT_EQ(hpAfterSecondHit, 47 - 1);     // 46
}

TEST(EntityTest, TakeDamage_MinimumOneDamage) {
    Entity entity('@', 0, 0, EntityType::Player);

    // 即使攻击力 <= 防御力，也至少造成 1 点伤害
    entity.takeDamage(1);   // 实际伤害 = max(1, 1-2) = 1
    entity.takeDamage(0);   // 实际伤害 = max(1, 0-2) = 1
    entity.takeDamage(2);   // 实际伤害 = max(1, 2-2) = 1

    EXPECT_EQ(entity.hp(), 50 - 3);  // 47
}

TEST(EntityTest, TakeDamage_DoesNotGoBelowZero) {
    Entity entity('@', 0, 0, EntityType::Player);

    entity.takeDamage(999);  // 大额伤害

    EXPECT_EQ(entity.hp(), 0);
    EXPECT_FALSE(entity.isAlive());
}

TEST(EntityTest, Heal_RestoresHp) {
    Entity entity('@', 0, 0, EntityType::Player);

    entity.takeDamage(20);   // 实际伤害 = max(1, 20-2) = 18 → HP = 32
    entity.heal(10);         // HP = 42

    EXPECT_EQ(entity.hp(), 42);
}

TEST(EntityTest, Heal_DoesNotExceedMaxHp) {
    Entity entity('@', 0, 0, EntityType::Player);

    entity.heal(100);  // 满血时治疗不应超过 maxHp

    EXPECT_EQ(entity.hp(), entity.maxHp());
}

TEST(EntityTest, Heal_PartialThenFull) {
    Entity entity('@', 0, 0, EntityType::Player);

    entity.takeDamage(30);   // 实际伤害 = max(1, 30-2) = 28 → HP = 22
    entity.heal(20);         // HP = 42
    entity.heal(20);         // HP = 50（不应超过 maxHp=50）

    EXPECT_EQ(entity.hp(), 50);
}

TEST(EntityTest, Move_ChangesPosition) {
    Entity entity('@', 5, 5, EntityType::Player);

    entity.move(1, 0);
    EXPECT_EQ(entity.x(), 6);
    EXPECT_EQ(entity.y(), 5);

    entity.move(-2, 3);
    EXPECT_EQ(entity.x(), 4);
    EXPECT_EQ(entity.y(), 8);
}

TEST(EntityTest, IsAlive_ReturnsCorrectState) {
    Entity entity('@', 0, 0, EntityType::Player);

    EXPECT_TRUE(entity.isAlive());

    entity.takeDamage(999);
    EXPECT_FALSE(entity.isAlive());
}

// ============================================================
// 测试套件：Monster 默认属性
// ============================================================

TEST(MonsterTest, DefaultAttributes) {
    Monster monster(10, 10);

    EXPECT_EQ(monster.hp(), 30);
    EXPECT_EQ(monster.maxHp(), 30);
    EXPECT_EQ(monster.atk(), 8);
    EXPECT_EQ(monster.def(), 1);
    EXPECT_EQ(monster.symbol(), 'g');
    EXPECT_EQ(monster.type(), EntityType::Monster);
}

TEST(MonsterTest, MonsterCanBeDamaged) {
    Monster monster(10, 10);

    monster.takeDamage(15);  // 实际伤害 = max(1, 15-1) = 14 → HP = 16

    EXPECT_EQ(monster.hp(), 16);
    EXPECT_TRUE(monster.isAlive());

    monster.takeDamage(20);  // 实际伤害 = max(1, 20-1) = 19 → HP = 0

    EXPECT_EQ(monster.hp(), 0);
    EXPECT_FALSE(monster.isAlive());
}

// ============================================================
// 测试套件：Item 创建
// ============================================================

TEST(ItemTest, WeaponItem) {
    Item item(ItemType::Weapon, 5, 5);

    EXPECT_EQ(item.itemType(), ItemType::Weapon);
    EXPECT_EQ(item.symbol(), '/');
    EXPECT_EQ(item.type(), EntityType::Item);
}

TEST(ItemTest, PotionItem) {
    Item item(ItemType::Potion, 5, 5);

    EXPECT_EQ(item.itemType(), ItemType::Potion);
    EXPECT_EQ(item.symbol(), '!');
    EXPECT_EQ(item.type(), EntityType::Item);
}
