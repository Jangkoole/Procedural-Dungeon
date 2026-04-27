#include <gtest/gtest.h>
#include "Player.h"

using namespace dungeon;

// ============================================================
// 测试套件：Player 默认属性
// ============================================================

TEST(PlayerTest, DefaultAttributes) {
    Player player(5, 5);

    EXPECT_EQ(player.hp(), 100);
    EXPECT_EQ(player.maxHp(), 100);
    EXPECT_EQ(player.atk(), 10);
    EXPECT_EQ(player.def(), 5);
    EXPECT_EQ(player.symbol(), '@');
    EXPECT_EQ(player.type(), EntityType::Player);

    EXPECT_EQ(player.exp(), 0);
    EXPECT_EQ(player.level(), 1);
    EXPECT_EQ(player.weaponAttackBonus(), 0);
    EXPECT_EQ(player.potionCount(), 0);
    EXPECT_EQ(player.totalAtk(), 10);  // 基础 ATK 10 + 武器加成 0
}

// ============================================================
// 测试套件：Player::addExp() 升级逻辑
// 升级规则：每 50 exp 升一级
// 升级效果：HP+10（满血）、ATK+2、DEF+1
// ============================================================

TEST(PlayerTest, AddExp_NoLevelUp) {
    Player player(5, 5);

    player.addExp(30);  // 不足 50，不升级

    EXPECT_EQ(player.exp(), 30);
    EXPECT_EQ(player.level(), 1);
    // 属性不变
    EXPECT_EQ(player.maxHp(), 100);
    EXPECT_EQ(player.atk(), 10);
    EXPECT_EQ(player.def(), 5);
}

TEST(PlayerTest, AddExp_LevelUpOnce) {
    Player player(5, 5);

    player.addExp(50);  // 刚好 50，升到 2 级

    EXPECT_EQ(player.exp(), 50);
    EXPECT_EQ(player.level(), 2);
    EXPECT_EQ(player.maxHp(), 110);  // HP +10
    EXPECT_EQ(player.hp(), 110);     // 升级满血
    EXPECT_EQ(player.atk(), 12);     // ATK +2
    EXPECT_EQ(player.def(), 6);      // DEF +1
}

TEST(PlayerTest, AddExp_LevelUpMultipleTimes) {
    Player player(5, 5);

    player.addExp(120);  // 120 / 50 = 2 次升级（余 20），升到 3 级

    EXPECT_EQ(player.exp(), 120);
    EXPECT_EQ(player.level(), 3);
    EXPECT_EQ(player.maxHp(), 120);  // 100 + 10*2
    EXPECT_EQ(player.hp(), 120);     // 升级满血
    EXPECT_EQ(player.atk(), 14);     // 10 + 2*2
    EXPECT_EQ(player.def(), 7);      // 5 + 1*2
}

TEST(PlayerTest, AddExp_ExactLevelBoundary) {
    Player player(5, 5);

    player.addExp(49);   // 49 exp，不升级
    EXPECT_EQ(player.level(), 1);

    player.addExp(1);    // 50 exp，升到 2 级
    EXPECT_EQ(player.level(), 2);
    EXPECT_EQ(player.exp(), 50);
}

TEST(PlayerTest, AddExp_LevelUpHealsToFull) {
    Player player(5, 5);

    // 先受伤
    player.takeDamage(30);  // 实际伤害 = max(1, 30-5) = 25 → HP = 75
    ASSERT_EQ(player.hp(), 75);

    // 升级应满血
    player.addExp(50);
    EXPECT_EQ(player.hp(), player.maxHp());  // HP = 110
    EXPECT_EQ(player.hp(), 110);
}

TEST(PlayerTest, AddExp_MultipleLevelsInOneCall) {
    Player player(5, 5);

    // 一次性获得大量经验，跨越多个等级
    player.addExp(200);  // 200 / 50 = 4 次升级，升到 5 级

    EXPECT_EQ(player.level(), 5);
    EXPECT_EQ(player.maxHp(), 140);  // 100 + 10*4
    EXPECT_EQ(player.atk(), 18);     // 10 + 2*4
    EXPECT_EQ(player.def(), 9);      // 5 + 1*4
}

// ============================================================
// 测试套件：Player::usePotion() 药水逻辑
// 药水效果：回复 25 HP，消耗一瓶药水
// ============================================================

TEST(PlayerTest, UsePotion_NoPotions) {
    Player player(5, 5);

    // 没有药水时使用不应有任何效果
    player.usePotion();

    EXPECT_EQ(player.potionCount(), 0);
    EXPECT_EQ(player.hp(), 100);
}

TEST(PlayerTest, UsePotion_HealsHp) {
    Player player(5, 5);

    player.addPotion();          // 获得一瓶药水
    ASSERT_EQ(player.potionCount(), 1);

    player.takeDamage(30);       // 实际伤害 = max(1, 30-5) = 25 → HP = 75
    ASSERT_EQ(player.hp(), 75);

    player.usePotion();          // 使用药水，回复 25 HP

    EXPECT_EQ(player.potionCount(), 0);
    EXPECT_EQ(player.hp(), 100); // 75 + 25 = 100
}

TEST(PlayerTest, UsePotion_DoesNotExceedMaxHp) {
    Player player(5, 5);

    player.addPotion();
    player.takeDamage(10);       // 实际伤害 = max(1, 10-5) = 5 → HP = 95
    ASSERT_EQ(player.hp(), 95);

    player.usePotion();          // 回复 25，但 95+25=120 > maxHp=100

    EXPECT_EQ(player.hp(), 100); // 不应超过 maxHp
    EXPECT_EQ(player.potionCount(), 0);
}

TEST(PlayerTest, UsePotion_MultiplePotions) {
    Player player(5, 5);

    player.addPotion();
    player.addPotion();
    player.addPotion();
    ASSERT_EQ(player.potionCount(), 3);

    player.takeDamage(60);       // 实际伤害 = max(1, 60-5) = 55 → HP = 45
    ASSERT_EQ(player.hp(), 45);

    player.usePotion();          // HP = 70
    EXPECT_EQ(player.potionCount(), 2);
    EXPECT_EQ(player.hp(), 70);

    player.usePotion();          // HP = 95
    EXPECT_EQ(player.potionCount(), 1);
    EXPECT_EQ(player.hp(), 95);

    player.usePotion();          // HP = 100（满血，不超过 maxHp）
    EXPECT_EQ(player.potionCount(), 0);
    EXPECT_EQ(player.hp(), 100);
}

// ============================================================
// 测试套件：Player 武器加成
// ============================================================

TEST(PlayerTest, EquipWeapon_IncreasesTotalAtk) {
    Player player(5, 5);

    EXPECT_EQ(player.totalAtk(), 10);  // 基础 ATK

    player.equipWeapon(5);
    EXPECT_EQ(player.weaponAttackBonus(), 5);
    EXPECT_EQ(player.totalAtk(), 15);  // 10 + 5

    player.equipWeapon(3);             // 替换武器
    EXPECT_EQ(player.weaponAttackBonus(), 3);
    EXPECT_EQ(player.totalAtk(), 13);  // 10 + 3
}

TEST(PlayerTest, TotalAtk_WithLevelUpAndWeapon) {
    Player player(5, 5);

    player.equipWeapon(5);             // 武器 +5
    player.addExp(100);                // 升 2 级：ATK +4

    EXPECT_EQ(player.totalAtk(), 19);  // 10(基础) + 4(升级) + 5(武器)
}
