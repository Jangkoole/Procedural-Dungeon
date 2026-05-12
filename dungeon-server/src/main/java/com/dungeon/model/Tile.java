package com.dungeon.model;

public class Tile {
    private TileType type;
    private boolean visible;
    private boolean explored;

    public Tile() {}

    public Tile(TileType type, boolean visible, boolean explored) {
        this.type = type;
        this.visible = visible;
        this.explored = explored;
    }

    public boolean isWalkable() {
        return type == TileType.FLOOR || type == TileType.CORRIDOR;
    }

    public TileType getType() { return type; }
    public void setType(TileType type) { this.type = type; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isExplored() { return explored; }
    public void setExplored(boolean explored) { this.explored = explored; }
}
