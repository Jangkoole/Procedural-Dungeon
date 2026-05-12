package com.dungeon.model;

public class Item {
    public enum ItemType { WEAPON, POTION }

    private int id;
    private int x;
    private int y;
    private ItemType type;
    private String name;
    private String symbol;

    public Item() {}

    public Item(int id, int x, int y, ItemType type, String name, String symbol) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.name = name;
        this.symbol = symbol;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public ItemType getType() { return type; }
    public void setType(ItemType type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}
