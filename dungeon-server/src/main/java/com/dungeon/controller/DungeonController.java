package com.dungeon.controller;

import com.dungeon.dto.*;
import com.dungeon.model.Direction;
import com.dungeon.model.GameSession;
import com.dungeon.service.GameService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dungeon")
public class DungeonController {

    private final GameService gameService;

    @Value("${game.default-dungeon-width:40}")
    private int defaultWidth;

    @Value("${game.default-dungeon-height:20}")
    private int defaultHeight;

    @Value("${game.monster-count:5}")
    private int monsterCount;

    @Value("${game.item-count:3}")
    private int itemCount;

    public DungeonController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GameStateResponse> generate(@RequestBody GenerateRequest request) {
        int w = request.getWidth() > 0 ? request.getWidth() : defaultWidth;
        int h = request.getHeight() > 0 ? request.getHeight() : defaultHeight;

        if (w < 20 || w > 80) throw new IllegalArgumentException("Width must be between 20 and 80.");
        if (h < 10 || h > 40) throw new IllegalArgumentException("Height must be between 10 and 40.");

        GameSession session = gameService.newGame(w, h, monsterCount, itemCount);
        return ResponseEntity.ok(GameStateResponse.from(session, List.of()));
    }

    @GetMapping("/{id}/state")
    public ResponseEntity<GameStateResponse> getState(@PathVariable String id) {
        GameSession session = gameService.getSession(id);
        return ResponseEntity.ok(GameStateResponse.from(session, List.of()));
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<GameStateResponse> move(@PathVariable String id, @RequestBody MoveRequest request) {
        Direction dir = parseDirection(request.getDirection());
        GameService.GameStateResult result = gameService.move(id, dir);
        return ResponseEntity.ok(GameStateResponse.from(result.session(), result.events()));
    }

    @PostMapping("/{id}/pickup")
    public ResponseEntity<GameStateResponse> pickup(@PathVariable String id) {
        GameService.GameStateResult result = gameService.pickup(id);
        return ResponseEntity.ok(GameStateResponse.from(result.session(), result.events()));
    }

    @PostMapping("/{id}/use-potion")
    public ResponseEntity<GameStateResponse> usePotion(@PathVariable String id) {
        GameService.GameStateResult result = gameService.usePotion(id);
        return ResponseEntity.ok(GameStateResponse.from(result.session(), result.events()));
    }

    @PostMapping("/{id}/wait")
    public ResponseEntity<GameStateResponse> waitTurn(@PathVariable String id) {
        GameService.GameStateResult result = gameService.wait(id);
        return ResponseEntity.ok(GameStateResponse.from(result.session(), result.events()));
    }

    private Direction parseDirection(String d) {
        if (d == null) throw new IllegalArgumentException("Direction is required.");
        return switch (d.toLowerCase()) {
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            case "left" -> Direction.LEFT;
            case "right" -> Direction.RIGHT;
            default -> throw new IllegalArgumentException("Direction must be one of: up, down, left, right.");
        };
    }
}
