package com.dungeon.model;

public class Player {
    private int x;
    private int y;
    private int hp;
    private int maxHp;
    private int atk;
    private int def;
    private int level;
    private int exp;
    private int expToNext;
    private int potions;
    private String weaponName;

    public Player() {}

    public Player(int x, int y, int hp, int atk, int def) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.atk = atk;
        this.def = def;
        this.level = 1;
        this.exp = 0;
        this.expToNext = 50;
        this.potions = 0;
    }

    public int totalAtk() {
        return atk + (weaponName != null ? 5 : 0);
    }

    public int actualDamage(int attackerAtk) {
        return Math.max(1, attackerAtk - def);
    }

    public void takeDamage(int attackerAtk) {
        hp -= actualDamage(attackerAtk);
        if (hp < 0) hp = 0;
    }

    public void heal(int amount) {
        hp = Math.min(hp + amount, maxHp);
    }

    public void addExp(int amount) {
        exp += amount;
        while (exp >= expToNext) {
            exp -= expToNext;
            expToNext += 10;
            level++;
            maxHp += 10;
            atk += 2;
            def += 1;
            hp = maxHp;
        }
    }

    public void addPotion() { potions++; }

    public boolean usePotion() {
        if (potions <= 0 || hp >= maxHp) return false;
        potions--;
        heal(25);
        return true;
    }

    public boolean isAlive() { return hp > 0; }

    // Getters and setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }
    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public int getExpToNext() { return expToNext; }
    public void setExpToNext(int expToNext) { this.expToNext = expToNext; }
    public int getPotions() { return potions; }
    public void setPotions(int potions) { this.potions = potions; }
    public String getWeaponName() { return weaponName; }
    public void setWeaponName(String weaponName) { this.weaponName = weaponName; }
}
