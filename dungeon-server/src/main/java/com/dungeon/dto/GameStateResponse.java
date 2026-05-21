package com.dungeon.dto;

import com.dungeon.model.*;

import java.util.ArrayList;
import java.util.List;

public class GameStateResponse {
    private String sessionId;
    private GameStatus status;
    private int turn;
    private int remainingMonsters;
    private PlayerDTO player;
    private DungeonDTO dungeon;
    private List<GameEvent> events;
    private List<String> messages;

    public static GameStateResponse from(com.dungeon.model.GameSession session, List<GameEvent> events) {
        GameStateResponse r = new GameStateResponse();
        r.sessionId = session.getSessionId();
        r.status = session.getStatus();
        r.turn = session.getTurn();
        r.remainingMonsters = session.remainingMonsters();
        r.player = PlayerDTO.from(session.getPlayer());
        r.dungeon = DungeonDTO.from(session.getDungeon());
        r.events = events;
        r.messages = session.getMessages();
        return r;
    }

    // Inner DTOs for JSON serialization
    public static class PlayerDTO {
        public int x, y, hp, maxHp, atk, def, level, exp, expToNext, potions;
        public String weaponName;

        static PlayerDTO from(Player p) {
            PlayerDTO d = new PlayerDTO();
            d.x = p.getX(); d.y = p.getY();
            d.hp = p.getHp(); d.maxHp = p.getMaxHp();
            d.atk = p.getAtk(); d.def = p.getDef();
            d.level = p.getLevel(); d.exp = p.getExp();
            d.expToNext = p.getExpToNext(); d.potions = p.getPotions();
            d.weaponName = p.getWeaponName();
            return d;
        }
    }

    public static class DungeonDTO {
        public int width, height;
        public List<List<TileDTO>> tiles;
        public List<MonsterDTO> monsters;
        public List<ItemDTO> items;

        static DungeonDTO from(Dungeon d) {
            DungeonDTO dto = new DungeonDTO();
            dto.width = d.getWidth();
            dto.height = d.getHeight();
            dto.tiles = new ArrayList<>();
            for (int y = 0; y < d.getHeight(); y++) {
                List<TileDTO> row = new ArrayList<>();
                for (int x = 0; x < d.getWidth(); x++) {
                    Tile t = d.getTile(x, y);
                    if (t != null) {
                        row.add(new TileDTO(t.getType().name(), t.isVisible(), t.isExplored()));
                    } else {
                        row.add(new TileDTO("WALL", false, false));
                    }
                }
                dto.tiles.add(row);
            }
            dto.monsters = d.getMonsters().stream()
                    .filter(Monster::isAlive)
                    .map(MonsterDTO::from).toList();
            dto.items = d.getItems().stream()
                    .map(ItemDTO::from).toList();
            return dto;
        }
    }

    public static class TileDTO {
        public String type;
        public boolean visible;
        public boolean explored;

        public TileDTO() {}
        public TileDTO(String type, boolean visible, boolean explored) {
            this.type = type; this.visible = visible; this.explored = explored;
        }
    }

    public static class MonsterDTO {
        public int id, x, y, hp, maxHp;
        public String symbol;

        static MonsterDTO from(Monster m) {
            MonsterDTO d = new MonsterDTO();
            d.id = m.getId(); d.x = m.getX(); d.y = m.getY();
            d.hp = m.getHp(); d.maxHp = m.getMaxHp(); d.symbol = m.getSymbol();
            return d;
        }
    }

    public static class ItemDTO {
        public int id, x, y;
        public String type, name, symbol;

        static ItemDTO from(Item i) {
            ItemDTO d = new ItemDTO();
            d.id = i.getId(); d.x = i.getX(); d.y = i.getY();
            d.type = i.getType().name(); d.name = i.getName(); d.symbol = i.getSymbol();
            return d;
        }
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public GameStatus getStatus() { return status; }
    public int getTurn() { return turn; }
    public int getRemainingMonsters() { return remainingMonsters; }
    public PlayerDTO getPlayer() { return player; }
    public DungeonDTO getDungeon() { return dungeon; }
    public List<GameEvent> getEvents() { return events; }
    public List<String> getMessages() { return messages; }
}
