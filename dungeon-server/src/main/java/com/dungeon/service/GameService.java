package com.dungeon.service;

import com.dungeon.dto.GameEvent;
import com.dungeon.exception.InvalidActionException;
import com.dungeon.exception.SessionNotFoundException;
import com.dungeon.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    private final SessionManager sessionManager;
    private final DungeonService dungeonService;

    public GameService(SessionManager sessionManager, DungeonService dungeonService) {
        this.sessionManager = sessionManager;
        this.dungeonService = dungeonService;
    }

    public GameSession newGame(int width, int height, int monsterCount, int itemCount) {
        Dungeon dungeon = dungeonService.generate(width, height, monsterCount, itemCount);
        int[] spawn = dungeonService.findFirstFloor(dungeon);
        Player player = new Player(spawn[0], spawn[1], 100, 10, 5);

        dungeonService.spawnMonsters(dungeon, monsterCount, player);
        dungeonService.spawnItems(dungeon, itemCount, player);
        dungeonService.updateFOV(dungeon, player, 5);

        GameSession session = new GameSession(sessionManager.createId(), dungeon, player);
        session.addMessage("Welcome to the dungeon...");
        session.addMessage("You see " + monsterCount + " monsters lurking in the dark.");
        sessionManager.put(session);
        return session;
    }

    public GameSession getSession(String sessionId) {
        GameSession session = sessionManager.get(sessionId);
        if (session == null) throw new SessionNotFoundException(sessionId);
        return session;
    }

    public GameStateResult move(String sessionId, Direction dir) {
        GameSession session = getSession(sessionId);
        checkPlaying(session);
        List<GameEvent> events = new ArrayList<>();

        Player p = session.getPlayer();
        Dungeon d = session.getDungeon();
        int nx = p.getX() + dir.dx;
        int ny = p.getY() + dir.dy;

        // Boundary check
        if (nx < 0 || nx >= d.getWidth() || ny < 0 || ny >= d.getHeight()) {
            throw new InvalidActionException("BLOCKED", "Cannot move: out of bounds.");
        }

        // Terrain check
        if (!d.isWalkable(nx, ny)) {
            throw new InvalidActionException("BLOCKED", "Cannot move: " + d.getTile(nx, ny).getType().name().toLowerCase() + " in the way.");
        }

        // Combat check
        Monster monster = d.getMonsterAt(nx, ny);
        if (monster != null) {
            int dmg = monster.actualDamage(p.totalAtk());
            monster.takeDamage(p.totalAtk());
            events.add(new GameEvent("COMBAT", "You attack the " + monster.getSymbol() + " for " + dmg + " damage!"));

            if (!monster.isAlive()) {
                d.removeMonster(monster);
                events.add(new GameEvent("KILL", "You defeated the " + monster.getSymbol() + "!"));
                p.addExp(20);
                if (p.getExp() == 0 && p.getLevel() > 1) {
                    events.add(new GameEvent("LEVEL_UP", "Level up! You are now level " + p.getLevel() + "."));
                }
            }
        } else {
            p.setX(nx);
            p.setY(ny);
            events.add(new GameEvent("MOVE", "You move " + dir.name().toLowerCase() + "."));
        }

        // Item pickup
        Item item = d.getItemAt(p.getX(), p.getY());
        if (item != null) {
            if (item.getType() == Item.ItemType.WEAPON) {
                p.setWeaponName(item.getName());
                events.add(new GameEvent("PICKUP", "You picked up " + item.getName() + " (+5 ATK)!"));
            } else {
                p.addPotion();
                events.add(new GameEvent("PICKUP", "You found a Health Potion!"));
            }
            d.removeItem(item);
        }

        // Monster AI turn
        updateMonsters(session, events);

        // Update FOV
        dungeonService.updateFOV(d, p, 5);

        // Check game over
        session.setTurn(session.getTurn() + 1);

        if (d.allMonstersDead()) {
            session.setStatus(GameStatus.VICTORY);
            events.add(new GameEvent("VICTORY", "You have defeated all monsters! The dungeon is clear."));
        }
        if (!p.isAlive()) {
            session.setStatus(GameStatus.DEFEAT);
            events.add(new GameEvent("DEFEAT", "You have been slain... Game over."));
        }

        for (GameEvent ev : events) {
            session.addMessage(ev.getMessage());
        }
        session.touch();

        return new GameStateResult(session, events);
    }

    public GameStateResult pickup(String sessionId) {
        GameSession session = getSession(sessionId);
        checkPlaying(session);
        List<GameEvent> events = new ArrayList<>();

        Player p = session.getPlayer();
        Dungeon d = session.getDungeon();
        Item item = d.getItemAt(p.getX(), p.getY());

        if (item == null) {
            throw new InvalidActionException("NO_ITEM", "No item to pick up at this location.");
        }

        if (item.getType() == Item.ItemType.WEAPON) {
            p.setWeaponName(item.getName());
            events.add(new GameEvent("PICKUP", "You picked up " + item.getName() + " (+5 ATK)!"));
        } else {
            p.addPotion();
            events.add(new GameEvent("PICKUP", "You found a Health Potion!"));
        }
        d.removeItem(item);

        // Monster turn
        updateMonsters(session, events);
        dungeonService.updateFOV(d, p, 5);
        session.setTurn(session.getTurn() + 1);

        for (GameEvent ev : events) {
            session.addMessage(ev.getMessage());
        }
        session.touch();
        return new GameStateResult(session, events);
    }

    public GameStateResult usePotion(String sessionId) {
        GameSession session = getSession(sessionId);
        checkPlaying(session);
        List<GameEvent> events = new ArrayList<>();

        Player p = session.getPlayer();
        if (p.getPotions() <= 0) {
            throw new InvalidActionException("NO_POTIONS", "You don't have any potions.");
        }
        if (p.getHp() >= p.getMaxHp()) {
            throw new InvalidActionException("HP_FULL", "Your HP is already full.");
        }

        p.usePotion();
        events.add(new GameEvent("USE_POTION", "You drink a potion and restore 25 HP."));

        // Monster turn
        updateMonsters(session, events);
        dungeonService.updateFOV(session.getDungeon(), p, 5);
        session.setTurn(session.getTurn() + 1);

        for (GameEvent ev : events) {
            session.addMessage(ev.getMessage());
        }
        session.touch();
        return new GameStateResult(session, events);
    }

    public GameStateResult wait(String sessionId) {
        GameSession session = getSession(sessionId);
        checkPlaying(session);
        List<GameEvent> events = new ArrayList<>();

        events.add(new GameEvent("WAIT", "You wait one turn."));

        // Monster turn
        updateMonsters(session, events);
        dungeonService.updateFOV(session.getDungeon(), session.getPlayer(), 5);
        session.setTurn(session.getTurn() + 1);

        for (GameEvent ev : events) {
            session.addMessage(ev.getMessage());
        }
        session.touch();
        return new GameStateResult(session, events);
    }

    private void checkPlaying(GameSession session) {
        if (session.getStatus() != GameStatus.PLAYING) {
            throw new InvalidActionException("GAME_OVER", "Game is already over. Status: " + session.getStatus().name().toLowerCase());
        }
    }

    private void updateMonsters(GameSession session, List<GameEvent> events) {
        Player p = session.getPlayer();
        Dungeon d = session.getDungeon();

        for (Monster m : d.getMonsters()) {
            if (!m.isAlive()) continue;

            int dx = Integer.compare(p.getX(), m.getX());
            int dy = Integer.compare(p.getY(), m.getY());

            int nx = m.getX() + dx;
            int ny = m.getY() + dy;

            if (nx == p.getX() && ny == p.getY()) {
                int dmg = p.actualDamage(m.getAtk());
                p.takeDamage(m.getAtk());
                events.add(new GameEvent("MONSTER_ATTACK", "The " + m.getSymbol() + " attacks you for " + dmg + " damage!"));
            } else if (d.isWalkable(nx, ny) && d.getMonsterAt(nx, ny) == null
                    && !(nx == p.getX() && ny == p.getY())) {
                m.setX(nx);
                m.setY(ny);
                if (Math.abs(nx - p.getX()) <= 1 && Math.abs(ny - p.getY()) <= 1) {
                    events.add(new GameEvent("MONSTER_TURN", "The " + m.getSymbol() + " moves closer."));
                }
            }
        }
    }

    public record GameStateResult(GameSession session, List<GameEvent> events) {}
}
