package com.dungeon.service;

import com.dungeon.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DungeonService {

    private final Random random = new Random();

    public Dungeon generate(int width, int height, int monsterCount, int itemCount) {
        Dungeon dungeon = new Dungeon(width, height);
        Tile[][] tiles = new Tile[height][width];

        // Fill all with walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(TileType.WALL, false, false);
            }
        }

        // Build BSP tree
        List<BspNode> leafNodes = new ArrayList<>();
        BspNode root = new BspNode(1, 1, width - 2, height - 2);
        splitBsp(root, leafNodes, 0);

        // Create rooms in leaf nodes
        boolean anyRoomCreated = false;
        for (BspNode leaf : leafNodes) {
            if (leaf.w < 4 || leaf.h < 4) continue;
            int roomW = random.nextInt(Math.max(1, leaf.w - 4)) + 3;
            int roomH = random.nextInt(Math.max(1, leaf.h - 4)) + 3;
            int roomX = leaf.x + random.nextInt(Math.max(1, leaf.w - roomW));
            int roomY = leaf.y + random.nextInt(Math.max(1, leaf.h - roomH));
            leaf.roomX = roomX; leaf.roomY = roomY;
            leaf.roomW = roomW; leaf.roomH = roomH;
            leaf.hasRoom = true;
            anyRoomCreated = true;

            // Carve room
            for (int y = roomY; y < roomY + roomH; y++) {
                for (int x = roomX; x < roomX + roomW; x++) {
                    tiles[y][x].setType(TileType.FLOOR);
                }
            }
        }

        // Fallback: if BSP produced no rooms, carve a guaranteed room at center
        if (!anyRoomCreated) {
            int roomW = Math.min(width - 2, 8);
            int roomH = Math.min(height - 2, 6);
            int roomX = (width - roomW) / 2;
            int roomY = (height - roomH) / 2;
            for (int y = roomY; y < roomY + roomH; y++) {
                for (int x = roomX; x < roomX + roomW; x++) {
                    tiles[y][x].setType(TileType.FLOOR);
                }
            }
        }

        // Connect siblings with corridors
        connectSiblings(root, tiles);

        // Add water tiles (10% of floor tiles randomly)
        List<int[]> floorPositions = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x].getType() == TileType.FLOOR) {
                    floorPositions.add(new int[]{x, y});
                }
            }
        }
        Collections.shuffle(floorPositions, random);
        int waterCount = floorPositions.size() / 10;
        for (int i = 0; i < waterCount; i++) {
            int[] pos = floorPositions.get(i);
            tiles[pos[1]][pos[0]].setType(TileType.WATER);
        }

        dungeon.setTiles(tiles);
        return dungeon;
    }

    private void splitBsp(BspNode node, List<BspNode> leaves, int depth) {
        if (depth > 4) {
            leaves.add(node);
            return;
        }

        boolean splitH = (node.w > node.h * 1.2) || (node.w <= node.h && random.nextBoolean());
        int minSize = 6;
        int maxSplit;

        if (splitH) {
            maxSplit = node.w - minSize;
            if (maxSplit < minSize) {
                leaves.add(node);
                return;
            }
            int split = minSize + random.nextInt(maxSplit - minSize + 1);
            node.left = new BspNode(node.x, node.y, split, node.h);
            node.right = new BspNode(node.x + split, node.y, node.w - split, node.h);
        } else {
            maxSplit = node.h - minSize;
            if (maxSplit < minSize) {
                leaves.add(node);
                return;
            }
            int split = minSize + random.nextInt(maxSplit - minSize + 1);
            node.left = new BspNode(node.x, node.y, node.w, split);
            node.right = new BspNode(node.x, node.y + split, node.w, node.h - split);
        }

        splitBsp(node.left, leaves, depth + 1);
        splitBsp(node.right, leaves, depth + 1);
    }

    private void connectSiblings(BspNode node, Tile[][] tiles) {
        if (node.left == null || node.right == null) return;

        connectSiblings(node.left, tiles);
        connectSiblings(node.right, tiles);

        if (node.left.hasRoom && node.right.hasRoom) {
            // L-shaped corridor connecting room centers
            int x1 = node.left.roomX + node.left.roomW / 2;
            int y1 = node.left.roomY + node.left.roomH / 2;
            int x2 = node.right.roomX + node.right.roomW / 2;
            int y2 = node.right.roomY + node.right.roomH / 2;

            // Horizontal then vertical
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                if (tiles[y1][x].getType() == TileType.WALL) {
                    tiles[y1][x].setType(TileType.CORRIDOR);
                }
            }
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                if (tiles[y][x2].getType() == TileType.WALL) {
                    tiles[y][x2].setType(TileType.CORRIDOR);
                }
            }
        }
    }

    public int[] findFirstFloor(Dungeon dungeon) {
        for (int y = 0; y < dungeon.getHeight(); y++) {
            for (int x = 0; x < dungeon.getWidth(); x++) {
                Tile t = dungeon.getTile(x, y);
                if (t != null && t.isWalkable()) {
                    return new int[]{x, y};
                }
            }
        }
        throw new IllegalStateException("No walkable floor tile found in dungeon.");
    }

    public int[] randomFloorPosition(Dungeon dungeon) {
        List<int[]> floors = new ArrayList<>();
        for (int y = 0; y < dungeon.getHeight(); y++) {
            for (int x = 0; x < dungeon.getWidth(); x++) {
                Tile t = dungeon.getTile(x, y);
                if (t != null && t.isWalkable()) {
                    floors.add(new int[]{x, y});
                }
            }
        }
        if (floors.isEmpty()) throw new IllegalStateException("No floor tiles available.");
        return floors.get(random.nextInt(floors.size()));
    }

    public void spawnMonsters(Dungeon dungeon, int count, Player player) {
        for (int i = 0; i < count; i++) {
            int[] pos = randomFloorPosition(dungeon);
            // Don't spawn on player
            int attempts = 0;
            while (pos[0] == player.getX() && pos[1] == player.getY() && attempts < 50) {
                pos = randomFloorPosition(dungeon);
                attempts++;
            }
            Monster m = new Monster(i + 1, pos[0], pos[1], 30, 8, 1, "G");
            dungeon.getMonsters().add(m);
        }
    }

    public void spawnItems(Dungeon dungeon, int count, Player player) {
        String[] weaponNames = {"Iron Sword", "Steel Axe", "Magic Staff"};
        for (int i = 0; i < count; i++) {
            int[] pos = randomFloorPosition(dungeon);
            int attempts = 0;
            while (pos[0] == player.getX() && pos[1] == player.getY() && attempts < 50) {
                pos = randomFloorPosition(dungeon);
                attempts++;
            }
            Item.ItemType type = (i == 0 || i == 2) ? Item.ItemType.WEAPON : Item.ItemType.POTION;
            String name = type == Item.ItemType.WEAPON ? weaponNames[i] : "Health Potion";
            String symbol = type == Item.ItemType.WEAPON ? "/" : "!";
            Item item = new Item(i + 1, pos[0], pos[1], type, name, symbol);
            dungeon.getItems().add(item);
        }
    }

    public void updateFOV(Dungeon dungeon, Player player, int fovRadius) {
        // Reset visibility
        for (int y = 0; y < dungeon.getHeight(); y++) {
            for (int x = 0; x < dungeon.getWidth(); x++) {
                Tile t = dungeon.getTile(x, y);
                if (t != null) t.setVisible(false);
            }
        }

        int px = player.getX();
        int py = player.getY();

        // Simple circular FOV with ray casting
        for (int y = py - fovRadius; y <= py + fovRadius; y++) {
            for (int x = px - fovRadius; x <= px + fovRadius; x++) {
                if (x < 0 || x >= dungeon.getWidth() || y < 0 || y >= dungeon.getHeight()) continue;
                double dist = Math.sqrt((x - px) * (x - px) + (y - py) * (y - py));
                if (dist <= fovRadius) {
                    // Simple ray: check if line from player to (x,y) is blocked by walls
                    if (hasLineOfSight(dungeon, px, py, x, y)) {
                        Tile t = dungeon.getTile(x, y);
                        if (t != null) {
                            t.setVisible(true);
                            t.setExplored(true);
                        }
                    }
                }
            }
        }
    }

    private static class BspNode {
        int x, y, w, h;
        BspNode left, right;
        int roomX, roomY, roomW, roomH;
        boolean hasRoom;

        BspNode(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    private boolean hasLineOfSight(Dungeon dungeon, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0, y = y0;
        while (x != x1 || y != y1) {
            Tile t = dungeon.getTile(x, y);
            if (t != null && t.getType() == TileType.WALL && (x != x0 || y != y0)) {
                return false;
            }
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
        return true;
    }
}
