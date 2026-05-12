package com.dungeon.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DungeonControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static String sessionId;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/dungeon/generate - should create a new game session")
    void generateDungeon_ShouldCreateSession() {
        Map<String, Object> body = Map.of("width", 40, "height", 20);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/generate"), body, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("sessionId"));
        assertEquals("PLAYING", response.getBody().get("status"));

        sessionId = (String) response.getBody().get("sessionId");
        assertNotNull(sessionId, "sessionId should not be null");

        Map<String, Object> player = (Map<String, Object>) response.getBody().get("player");
        assertNotNull(player);
        assertEquals(100, player.get("hp"));

        Map<String, Object> dungeon = (Map<String, Object>) response.getBody().get("dungeon");
        assertNotNull(dungeon);
        assertEquals(40, dungeon.get("width"));
        assertEquals(20, dungeon.get("height"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/dungeon/{id}/state - should return current game state")
    void getState_ShouldReturnGameState() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/dungeon/" + sessionId + "/state"), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sessionId, response.getBody().get("sessionId"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/dungeon/{id}/move - should move player")
    void movePlayer_ShouldMovePlayer() {
        Map<String, Object> body = Map.of("direction", "right");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/move"), body, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("events"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/dungeon/{id}/move - should reject invalid direction")
    void movePlayer_InvalidDirection_ShouldReturn400() {
        Map<String, Object> body = Map.of("direction", "north");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/move"), body, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/dungeon/{id}/wait - should pass one turn")
    void waitTurn_ShouldPassTurn() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/wait"), null, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Map<String, Object>> events = (List<Map<String, Object>>) response.getBody().get("events");
        assertNotNull(events);
        assertTrue(events.stream().anyMatch(e -> "WAIT".equals(e.get("type"))));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/dungeon/{id}/pickup - should return 400 when no item at location")
    void pickup_NoItem_ShouldReturn400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/pickup"), null, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("NO_ITEM", response.getBody().get("error"));
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/dungeon/{id}/use-potion - should return 400 when no potions in inventory")
    void usePotion_NoPotions_ShouldReturn400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/use-potion"), null, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("NO_POTIONS", response.getBody().get("error"));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/sessions - should list active sessions")
    void listSessions_ShouldReturnSessions() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/sessions"), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("sessions"));
        assertTrue((Integer) response.getBody().get("count") >= 1);
    }

    @Test
    @Order(9)
    @DisplayName("API endpoint for non-existent session should return 404")
    void nonExistentSession_ShouldReturn404() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/dungeon/non-existent-id/state"), Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("SESSION_NOT_FOUND", response.getBody().get("error"));
    }

    @Test
    @Order(10)
    @DisplayName("DELETE /api/sessions/{id} - should remove session")
    void deleteSession_ShouldRemoveSession() {
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/sessions/" + sessionId),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify session is gone
        ResponseEntity<Map> followUp = restTemplate.getForEntity(
                url("/api/dungeon/" + sessionId + "/state"), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, followUp.getStatusCode());
    }
}
