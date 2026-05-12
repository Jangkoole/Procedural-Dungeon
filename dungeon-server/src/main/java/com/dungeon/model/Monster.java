package com.dungeon.model;

public class Monster {
    private int id;
    private int x;
    private int y;
    private int hp;
    private int maxHp;
    private int atk;
    private int def;
    private String symbol;

    public Monster() {}

    public Monster(int id, int x, int y, int hp, int atk, int def, String symbol) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.atk = atk;
        this.def = def;
        this.symbol = symbol;
    }

    public int actualDamage(int attackerAtk) {
        return Math.max(1, attackerAtk - def);
    }

    public void takeDamage(int attackerAtk) {
        hp -= actualDamage(attackerAtk);
        if (hp < 0) hp = 0;
    }

    public boolean isAlive() { return hp > 0; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}
