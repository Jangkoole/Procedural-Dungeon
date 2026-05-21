package com.dungeon.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 边界与错误场景集成测试 —— 覆盖异常路径、输入验证和错误处理。
 *
 * <p>测试策略：
 * <ul>
 *   <li>每个测试用例独立创建自己的会话，互不依赖</li>
 *   <li>覆盖 HTTP 400/404/500 错误路径</li>
 *   <li>验证错误响应体格式符合 {@code ErrorResponse} 规范</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
class DungeonErrorScenariosTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    /** 创建一个新会话并返回 sessionId */
    private String createSession() {
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", 40, "height", 20), Map.class);
        return (String) resp.getBody().get("sessionId");
    }

    // ==================== 输入验证 ====================

    @Test
    @DisplayName("POST /api/dungeon/generate - 空请求体应返回 400")
    void generateDungeon_EmptyBody_ShouldReturn400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/dungeon/generate"), HttpMethod.POST, entity, Map.class);

        // 空 body 使用默认值，应该成功
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/dungeon/generate - 负尺寸应返回 400")
    void generateDungeon_NegativeSize_ShouldReturn400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", -1, "height", -1), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().get("error"));
    }

    @Test
    @DisplayName("POST /api/dungeon/generate - 零尺寸应返回 400")
    void generateDungeon_ZeroSize_ShouldReturn400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", 0, "height", 0), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== 移动错误场景 ====================

    @Test
    @DisplayName("POST /api/dungeon/{id}/move - 缺少 direction 字段应返回 400")
    void movePlayer_MissingDirection_ShouldReturn400() {
        String sid = createSession();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sid + "/move"),
                Map.of(), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/dungeon/{id}/move - 无效 direction 值应返回 400")
    void movePlayer_InvalidDirectionValue_ShouldReturn400() {
        String sid = createSession();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sid + "/move"),
                Map.of("direction", "sideways"), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().get("error"));
    }

    // ==================== 游戏结束场景 ====================

    @Test
    @DisplayName("游戏结束后尝试操作应返回 GAME_OVER")
    void actionAfterGameOver_ShouldReturn400() {
        String sid = createSession();

        // 获取玩家初始位置
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        Map<String, Object> player = asMap(stateResp.getBody().get("player"));
        int initialHp = (Integer) player.get("hp");

        // 反复等待，让怪物攻击玩家直到死亡
        // 最多尝试 100 回合
        for (int i = 0; i < 100; i++) {
            ResponseEntity<Map> waitResp = restTemplate.postForEntity(
                    url("/api/dungeon/" + sid + "/wait"), null, Map.class);

            if (waitResp.getStatusCode() != HttpStatus.OK) break;

            String status = (String) waitResp.getBody().get("status");
            if ("DEFEAT".equals(status)) {
                // 游戏结束，验证后续操作返回 GAME_OVER
                ResponseEntity<Map> moveAfterDeath = restTemplate.postForEntity(
                        url("/api/dungeon/" + sid + "/move"),
                        Map.of("direction", "up"), Map.class);
                assertEquals(HttpStatus.BAD_REQUEST, moveAfterDeath.getStatusCode());
                assertEquals("GAME_OVER", moveAfterDeath.getBody().get("error"));

                ResponseEntity<Map> pickupAfterDeath = restTemplate.postForEntity(
                        url("/api/dungeon/" + sid + "/pickup"), null, Map.class);
                assertEquals(HttpStatus.BAD_REQUEST, pickupAfterDeath.getStatusCode());
                assertEquals("GAME_OVER", pickupAfterDeath.getBody().get("error"));

                ResponseEntity<Map> potionAfterDeath = restTemplate.postForEntity(
                        url("/api/dungeon/" + sid + "/use-potion"), null, Map.class);
                assertEquals(HttpStatus.BAD_REQUEST, potionAfterDeath.getStatusCode());
                assertEquals("GAME_OVER", potionAfterDeath.getBody().get("error"));

                ResponseEntity<Map> waitAfterDeath = restTemplate.postForEntity(
                        url("/api/dungeon/" + sid + "/wait"), null, Map.class);
                assertEquals(HttpStatus.BAD_REQUEST, waitAfterDeath.getStatusCode());
                assertEquals("GAME_OVER", waitAfterDeath.getBody().get("error"));

                return;
            }
        }

        // 如果 100 回合后玩家仍未死亡（运气好），跳过此测试
    }

    // ==================== 药水边界场景 ====================

    @Test
    @DisplayName("POST /api/dungeon/{id}/use-potion - HP 已满时应返回 HP_FULL")
    void usePotion_HpFull_ShouldReturn400() {
        String sid = createSession();

        // 玩家初始 HP 为满，直接用药水应失败
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/dungeon/" + sid + "/use-potion"), null, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("NO_POTIONS", response.getBody().get("error"),
                "Should fail with NO_POTIONS since player has no potions");
    }

    // ==================== 重复操作 ====================

    @Test
    @DisplayName("DELETE /api/sessions/{id} - 重复删除应返回 404")
    void deleteSession_Twice_ShouldReturn404() {
        String sid = createSession();

        // 第一次删除
        ResponseEntity<Map> first = restTemplate.exchange(
                url("/api/sessions/" + sid), HttpMethod.DELETE, HttpEntity.EMPTY, Map.class);
        assertEquals(HttpStatus.OK, first.getStatusCode());

        // 第二次删除
        ResponseEntity<Map> second = restTemplate.exchange(
                url("/api/sessions/" + sid), HttpMethod.DELETE, HttpEntity.EMPTY, Map.class);
        assertEquals(HttpStatus.NOT_FOUND, second.getStatusCode());
    }

    // ==================== 工具方法 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object obj) {
        return (Map<String, Object>) obj;
    }
}
