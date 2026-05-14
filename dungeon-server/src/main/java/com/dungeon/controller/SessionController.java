package com.dungeon.controller;

import com.dungeon.dto.SessionListResponse;
import com.dungeon.exception.SessionNotFoundException;
import com.dungeon.model.GameSession;
import com.dungeon.service.SessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionManager sessionManager;

    public SessionController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @GetMapping
    public ResponseEntity<SessionListResponse> listSessions() {
        List<SessionListResponse.SessionInfo> infos = sessionManager.getAll().stream()
                .map(SessionListResponse.SessionInfo::from)
                .toList();
        return ResponseEntity.ok(new SessionListResponse(infos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSession(@PathVariable String id) {
        GameSession session = sessionManager.get(id);
        if (session == null) throw new SessionNotFoundException(id);
        sessionManager.remove(id);
        return ResponseEntity.ok(Map.of("message", "Session " + id + " removed."));
    }
}
