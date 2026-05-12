package com.dungeon.model;

import java.util.ArrayList;
import java.util.List;

public class Dungeon {
    private int width;
    private int height;
    private Tile[][] tiles;
    private List<Monster> monsters;
    private List<Item> items;

    public Dungeon() {}

    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width];
        this.monsters = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return tiles[y][x];
    }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        Tile tile = tiles[y][x];
        return tile != null && tile.isWalkable();
    }

    public Monster getMonsterAt(int x, int y) {
        for (Monster m : monsters) {
            if (m.getX() == x && m.getY() == y && m.isAlive()) return m;
        }
        return null;
    }

    public Item getItemAt(int x, int y) {
        for (Item item : items) {
            if (item.getX() == x && item.getY() == y) return item;
        }
        return null;
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void removeMonster(Monster m) {
        monsters.remove(m);
    }

    public boolean allMonstersDead() {
        return monsters.stream().noneMatch(Monster::isAlive);
    }

    // Getters and setters
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public Tile[][] getTiles() { return tiles; }
    public void setTiles(Tile[][] tiles) { this.tiles = tiles; }
    public List<Monster> getMonsters() { return monsters; }
    public void setMonsters(List<Monster> monsters) { this.monsters = monsters; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}
