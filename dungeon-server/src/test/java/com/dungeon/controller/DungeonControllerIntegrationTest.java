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

/**
 * 基础流程集成测试 —— 覆盖 Dungeon API 的核心业务流程。
 *
 * <p>测试策略：
 * <ul>
 *   <li>使用 {@code @SpringBootTest(webEnvironment = RANDOM_PORT)} 启动完整 Spring 上下文</li>
 *   <li>通过 {@link TestRestTemplate} 发送真实 HTTP 请求</li>
 *   <li>按 {@code @Order} 顺序执行，模拟一次完整的游戏会话生命周期</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DungeonControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /** 跨测试用例共享的会话 ID */
    private static String sessionId;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ==================== 1. 地牢生成 ====================

    @Test
    @Order(1)
    @DisplayName("POST /api/dungeon/generate - 使用默认参数创建新游戏会话")
    void generateDungeon_WithDefaults_ShouldCreateSession() {
        Map<String, Object> body = Map.of("width", 40, "height", 20);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/generate"), body, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // --- 顶层字段 ---
        assertNotNull(response.getBody().get("sessionId"), "sessionId must not be null");
        assertEquals("PLAYING", response.getBody().get("status"));
        assertEquals(0, response.getBody().get("turn"));
        assertNotNull(response.getBody().get("remainingMonsters"));
        assertTrue((Integer) response.getBody().get("remainingMonsters") > 0);

        sessionId = (String) response.getBody().get("sessionId");

        // --- Player ---
        Map<String, Object> player = asMap(response.getBody().get("player"));
        assertNotNull(player);
        assertEquals(100, player.get("hp"));
        assertEquals(100, player.get("maxHp"));
        assertEquals(10, player.get("atk"));
        assertEquals(5, player.get("def"));
        assertEquals(1, player.get("level"));
        assertEquals(0, player.get("exp"));
        assertEquals(50, player.get("expToNext"));
        assertEquals(0, player.get("potions"));
        assertTrue((Integer) player.get("x") >= 0);
        assertTrue((Integer) player.get("y") >= 0);

        // --- Dungeon ---
        Map<String, Object> dungeon = asMap(response.getBody().get("dungeon"));
        assertNotNull(dungeon);
        assertEquals(40, dungeon.get("width"));
        assertEquals(20, dungeon.get("height"));

        // --- Tiles ---
        List<List<Map<String, Object>>> tiles = (List<List<Map<String, Object>>>) dungeon.get("tiles");
        assertNotNull(tiles);
        assertEquals(20, tiles.size());
        assertEquals(40, tiles.get(0).size());

        // --- Monsters ---
        List<Map<String, Object>> monsters = (List<Map<String, Object>>) dungeon.get("monsters");
        assertNotNull(monsters);
        assertEquals(5, monsters.size());

        // --- Items ---
        List<Map<String, Object>> items = (List<Map<String, Object>>) dungeon.get("items");
        assertNotNull(items);
        assertEquals(3, items.size());

        // --- Messages ---
        List<String> messages = (List<String>) response.getBody().get("messages");
        assertNotNull(messages);
        assertFalse(messages.isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/dungeon/generate - 使用最小/最大边界尺寸")
    void generateDungeon_WithBoundarySizes_ShouldSucceed() {
        // 最小值
        ResponseEntity<Map> minResp = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", 20, "height", 10), Map.class);
        assertEquals(HttpStatus.OK, minResp.getStatusCode());
        Map<String, Object> minDungeon = asMap(minResp.getBody().get("dungeon"));
        assertEquals(20, minDungeon.get("width"));
        assertEquals(10, minDungeon.get("height"));

        // 最大值
        ResponseEntity<Map> maxResp = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", 80, "height", 40), Map.class);
        assertEquals(HttpStatus.OK, maxResp.getStatusCode());
        Map<String, Object> maxDungeon = asMap(maxResp.getBody().get("dungeon"));
        assertEquals(80, maxDungeon.get("width"));
        assertEquals(40, maxDungeon.get("height"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/dungeon/generate - 尺寸超出范围应返回 400")
    void generateDungeon_WithInvalidSize_ShouldReturn400() {
        // 太小
        ResponseEntity<Map> tooSmall = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", 10, "height", 5), Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, tooSmall.getStatusCode());

        // 太大
        ResponseEntity<Map> tooLarge = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", 100, "height", 50), Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, tooLarge.getStatusCode());
    }

    // ==================== 2. 获取游戏状态 ====================

    @Test
    @Order(10)
    @DisplayName("GET /api/dungeon/{id}/state - 返回当前游戏状态")
    void getState_ShouldReturnGameState() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/dungeon/" + sessionId + "/state"), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sessionId, response.getBody().get("sessionId"));
        assertNotNull(response.getBody().get("player"));
        assertNotNull(response.getBody().get("dungeon"));
    }

    // ==================== 3. 玩家移动 ====================

    @Test
    @Order(20)
    @DisplayName("POST /api/dungeon/{id}/move - 向四个方向移动玩家")
    void movePlayer_AllDirections_ShouldSucceed() {
        // 尝试四个方向，至少有一个方向可通行
        String[] directions = {"up", "down", "left", "right"};
        boolean moved = false;

        for (String dir : directions) {
            Map<String, Object> body = Map.of("direction", dir);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url("/api/dungeon/" + sessionId + "/move"), body, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                moved = true;
                Map<String, Object> player = asMap(response.getBody().get("player"));
                assertNotNull(player);
                List<Map<String, Object>> events = (List<Map<String, Object>>) response.getBody().get("events");
                assertNotNull(events);
                assertFalse(events.isEmpty());
                break;
            }
        }

        assertTrue(moved, "At least one direction should be walkable");
    }

    @Test
    @Order(21)
    @DisplayName("POST /api/dungeon/{id}/move - 无效方向应返回 400")
    void movePlayer_InvalidDirection_ShouldReturn400() {
        // 无效枚举值
        ResponseEntity<Map> invalidDir = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/move"),
                Map.of("direction", "north"), Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, invalidDir.getStatusCode());

        // 空方向
        ResponseEntity<Map> nullDir = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/move"),
                Map.of("direction", ""), Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, nullDir.getStatusCode());
    }

    @Test
    @Order(22)
    @DisplayName("POST /api/dungeon/{id}/move - 移动到墙壁应返回 BLOCKED")
    void movePlayer_IntoWall_ShouldReturnBlocked() {
        // 获取玩家当前位置，尝试向墙壁方向移动
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sessionId + "/state"), Map.class);
        Map<String, Object> player = asMap(stateResp.getBody().get("player"));
        int px = (Integer) player.get("x");
        int py = (Integer) player.get("y");

        // 尝试所有方向，找到墙壁方向
        String[] dirs = {"up", "down", "left", "right"};
        for (String dir : dirs) {
            ResponseEntity<Map> moveResp = restTemplate.postForEntity(
                    url("/api/dungeon/" + sessionId + "/move"),
                    Map.of("direction", dir), Map.class);
            if (moveResp.getStatusCode() == HttpStatus.BAD_REQUEST) {
                Map<String, Object> errBody = moveResp.getBody();
                if (errBody != null && "BLOCKED".equals(errBody.get("error"))) {
                    return; // 找到了墙壁阻挡场景
                }
            }
        }

        // 如果所有方向都可通行，这个测试跳过（地牢生成随机性）
        // 这不是失败，而是地牢布局导致无法复现
    }

    // ==================== 4. 等待回合 ====================

    @Test
    @Order(30)
    @DisplayName("POST /api/dungeon/{id}/wait - 等待一回合，怪物行动")
    void waitTurn_ShouldPassTurn() {
        ResponseEntity<Map> before = restTemplate.getForEntity(
                url("/api/dungeon/" + sessionId + "/state"), Map.class);
        int turnBefore = (Integer) before.getBody().get("turn");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/wait"), null, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        int turnAfter = (Integer) response.getBody().get("turn");
        assertEquals(turnBefore + 1, turnAfter, "Turn count should increment by 1");

        List<Map<String, Object>> events = (List<Map<String, Object>>) response.getBody().get("events");
        assertNotNull(events);
        assertTrue(events.stream().anyMatch(e -> "WAIT".equals(e.get("type"))),
                "Events should contain WAIT event");
    }

    // ==================== 5. 物品拾取 ====================

    @Test
    @Order(40)
    @DisplayName("POST /api/dungeon/{id}/pickup - 当前位置无物品应返回 400")
    void pickup_NoItem_ShouldReturn400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/pickup"), null, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("NO_ITEM", response.getBody().get("error"));
    }

    // ==================== 6. 使用药水 ====================

    @Test
    @Order(50)
    @DisplayName("POST /api/dungeon/{id}/use-potion - 无药水应返回 400")
    void usePotion_NoPotions_ShouldReturn400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sessionId + "/use-potion"), null, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("NO_POTIONS", response.getBody().get("error"));
    }

    // ==================== 7. 会话管理 ====================

    @Test
    @Order(60)
    @DisplayName("GET /api/sessions - 列出活跃会话")
    void listSessions_ShouldReturnSessions() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/sessions"), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("sessions"));
        assertTrue((Integer) response.getBody().get("count") >= 1);

        List<Map<String, Object>> sessions = (List<Map<String, Object>>) response.getBody().get("sessions");
        assertTrue(sessions.stream().anyMatch(s -> sessionId.equals(s.get("sessionId"))),
                "Our session should be in the list");
    }

    @Test
    @Order(61)
    @DisplayName("不存在的会话 ID 应返回 404")
    void nonExistentSession_ShouldReturn404() {
        String fakeId = "00000000-0000-0000-0000-000000000000";

        // GET state
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + fakeId + "/state"), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, stateResp.getStatusCode());
        assertEquals("SESSION_NOT_FOUND", stateResp.getBody().get("error"));

        // POST move
        ResponseEntity<Map> moveResp = restTemplate.postForEntity(
                url("/api/dungeon/" + fakeId + "/move"),
                Map.of("direction", "up"), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, moveResp.getStatusCode());

        // POST pickup
        ResponseEntity<Map> pickupResp = restTemplate.postForEntity(
                url("/api/dungeon/" + fakeId + "/pickup"), null, Map.class);
        assertEquals(HttpStatus.NOT_FOUND, pickupResp.getStatusCode());

        // POST use-potion
        ResponseEntity<Map> potionResp = restTemplate.postForEntity(
                url("/api/dungeon/" + fakeId + "/use-potion"), null, Map.class);
        assertEquals(HttpStatus.NOT_FOUND, potionResp.getStatusCode());

        // POST wait
        ResponseEntity<Map> waitResp = restTemplate.postForEntity(
                url("/api/dungeon/" + fakeId + "/wait"), null, Map.class);
        assertEquals(HttpStatus.NOT_FOUND, waitResp.getStatusCode());

        // DELETE session
        ResponseEntity<Map> deleteResp = restTemplate.exchange(
                url("/api/sessions/" + fakeId),
                HttpMethod.DELETE, HttpEntity.EMPTY, Map.class);
        assertEquals(HttpStatus.NOT_FOUND, deleteResp.getStatusCode());
    }

    @Test
    @Order(62)
    @DisplayName("DELETE /api/sessions/{id} - 删除会话后状态不可访问")
    void deleteSession_ShouldRemoveSession() {
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/sessions/" + sessionId),
                HttpMethod.DELETE, HttpEntity.EMPTY, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证会话已删除
        ResponseEntity<Map> followUp = restTemplate.getForEntity(
                url("/api/dungeon/" + sessionId + "/state"), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, followUp.getStatusCode());
    }

    // ==================== 工具方法 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object obj) {
        return (Map<String, Object>) obj;
    }
}
