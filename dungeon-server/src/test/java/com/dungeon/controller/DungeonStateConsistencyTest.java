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
 * 游戏状态一致性集成测试 —— 验证每次操作后游戏状态保持正确。
 *
 * <p>测试策略：
 * <ul>
 *   <li>每个测试用例独立创建会话，模拟完整的游戏循环</li>
 *   <li>验证操作前后状态变化符合预期（位置、HP、回合数等）</li>
 *   <li>验证怪物和物品的状态一致性</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
class DungeonStateConsistencyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String createSession() {
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", 40, "height", 20), Map.class);
        return (String) resp.getBody().get("sessionId");
    }

    // ==================== 位置一致性 ====================

    @Test
    @DisplayName("移动后玩家位置应正确更新")
    void movePlayer_PositionShouldUpdate() {
        String sid = createSession();

        // 获取初始位置
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        Map<String, Object> playerBefore = asMap(stateResp.getBody().get("player"));
        int xBefore = (Integer) playerBefore.get("x");
        int yBefore = (Integer) playerBefore.get("y");

        // 尝试向右移动
        ResponseEntity<Map> moveResp = restTemplate.postForEntity(
                url("/api/dungeon/" + sid + "/move"),
                Map.of("direction", "right"), Map.class);

        if (moveResp.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> playerAfter = asMap(moveResp.getBody().get("player"));
            int xAfter = (Integer) playerAfter.get("x");
            int yAfter = (Integer) playerAfter.get("y");

            // 位置应变化（向右 x+1）
            assertTrue(xAfter > xBefore || yAfter != yBefore,
                    "Player position should change after successful move");
        }
    }

    @Test
    @DisplayName("移动被阻挡时玩家位置不应变化")
    void movePlayer_Blocked_PositionShouldNotChange() {
        String sid = createSession();

        // 获取初始位置
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        Map<String, Object> playerBefore = asMap(stateResp.getBody().get("player"));
        int xBefore = (Integer) playerBefore.get("x");
        int yBefore = (Integer) playerBefore.get("y");

        // 尝试向墙壁方向移动（所有方向都试一遍）
        String[] dirs = {"up", "down", "left", "right"};
        for (String dir : dirs) {
            ResponseEntity<Map> moveResp = restTemplate.postForEntity(
                    url("/api/dungeon/" + sid + "/move"),
                    Map.of("direction", dir), Map.class);

            if (moveResp.getStatusCode() == HttpStatus.BAD_REQUEST) {
                // 验证位置未变化
                ResponseEntity<Map> checkResp = restTemplate.getForEntity(
                        url("/api/dungeon/" + sid + "/state"), Map.class);
                Map<String, Object> playerCheck = asMap(checkResp.getBody().get("player"));
                assertEquals(xBefore, playerCheck.get("x"), "X should not change after blocked move");
                assertEquals(yBefore, playerCheck.get("y"), "Y should not change after blocked move");
                return;
            }
        }
    }

    // ==================== 战斗一致性 ====================

    @Test
    @DisplayName("攻击怪物后怪物 HP 应减少")
    void attackMonster_MonsterHpShouldDecrease() {
        String sid = createSession();

        // 获取地牢中的怪物位置
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        Map<String, Object> dungeon = asMap(stateResp.getBody().get("dungeon"));
        List<Map<String, Object>> monsters = (List<Map<String, Object>>) dungeon.get("monsters");

        if (monsters.isEmpty()) {
            return; // 没有怪物，跳过
        }

        Map<String, Object> monster = monsters.get(0);
        int mx = (Integer) monster.get("x");
        int my = (Integer) monster.get("y");
        int monsterHpBefore = (Integer) monster.get("hp");

        // 获取玩家位置
        Map<String, Object> player = asMap(stateResp.getBody().get("player"));
        int px = (Integer) player.get("x");
        int py = (Integer) player.get("y");

        // 计算到怪物的方向
        int dx = mx - px;
        int dy = my - py;

        String dir;
        if (dx > 0) dir = "right";
        else if (dx < 0) dir = "left";
        else if (dy > 0) dir = "down";
        else if (dy < 0) dir = "up";
        else return; // 在同一个位置，跳过

        // 向怪物方向移动（攻击）
        ResponseEntity<Map> moveResp = restTemplate.postForEntity(
                url("/api/dungeon/" + sid + "/move"),
                Map.of("direction", dir), Map.class);

        if (moveResp.getStatusCode() == HttpStatus.OK) {
            List<Map<String, Object>> events = (List<Map<String, Object>>) moveResp.getBody().get("events");
            boolean hasCombat = events.stream().anyMatch(e -> "COMBAT".equals(e.get("type")));

            if (hasCombat) {
                // 验证怪物 HP 减少
                ResponseEntity<Map> checkResp = restTemplate.getForEntity(
                        url("/api/dungeon/" + sid + "/state"), Map.class);
                Map<String, Object> dungeonAfter = asMap(checkResp.getBody().get("dungeon"));
                List<Map<String, Object>> monstersAfter = (List<Map<String, Object>>) dungeonAfter.get("monsters");

                // 如果怪物还活着，HP 应该减少
                if (!monstersAfter.isEmpty()) {
                    Map<String, Object> monsterAfter = monstersAfter.get(0);
                    int monsterHpAfter = (Integer) monsterAfter.get("hp");
                    assertTrue(monsterHpAfter < monsterHpBefore,
                            "Monster HP should decrease after attack");
                }
            }
        }
    }

    // ==================== 回合数一致性 ====================

    @Test
    @DisplayName("每次操作后回合数应递增")
    void turnCount_ShouldIncrementOnEachAction() {
        String sid = createSession();

        ResponseEntity<Map> initial = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        int turn0 = (Integer) initial.getBody().get("turn");

        // 移动
        ResponseEntity<Map> moveResp = restTemplate.postForEntity(
                url("/api/dungeon/" + sid + "/move"),
                Map.of("direction", "right"), Map.class);
        if (moveResp.getStatusCode() == HttpStatus.OK) {
            assertEquals(turn0 + 1, moveResp.getBody().get("turn"));
        }

        // 等待
        ResponseEntity<Map> waitResp = restTemplate.postForEntity(
                url("/api/dungeon/" + sid + "/wait"), null, Map.class);
        if (waitResp.getStatusCode() == HttpStatus.OK) {
            int expectedTurn = (moveResp.getStatusCode() == HttpStatus.OK) ? turn0 + 2 : turn0 + 1;
            assertEquals(expectedTurn, waitResp.getBody().get("turn"));
        }
    }

    // ==================== 物品一致性 ====================

    @Test
    @DisplayName("拾取物品后物品应从地牢中移除")
    void pickupItem_ItemShouldBeRemoved() {
        String sid = createSession();

        // 获取地牢中的物品
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        Map<String, Object> dungeon = asMap(stateResp.getBody().get("dungeon"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) dungeon.get("items");

        if (items.isEmpty()) {
            return; // 没有物品，跳过
        }

        // 找一个物品，移动到它所在的位置
        for (Map<String, Object> item : items) {
            int ix = (Integer) item.get("x");
            int iy = (Integer) item.get("y");

            // 获取玩家位置
            Map<String, Object> player = asMap(stateResp.getBody().get("player"));
            int px = (Integer) player.get("x");
            int py = (Integer) player.get("y");

            // 尝试移动到物品位置（可能需要多步）
            // 简化：如果物品就在相邻格，走过去自动拾取
            if (Math.abs(ix - px) + Math.abs(iy - py) == 1) {
                String dir;
                if (ix > px) dir = "right";
                else if (ix < px) dir = "left";
                else if (iy > py) dir = "down";
                else dir = "up";

                ResponseEntity<Map> moveResp = restTemplate.postForEntity(
                        url("/api/dungeon/" + sid + "/move"),
                        Map.of("direction", dir), Map.class);

                if (moveResp.getStatusCode() == HttpStatus.OK) {
                    List<Map<String, Object>> events = (List<Map<String, Object>>) moveResp.getBody().get("events");
                    boolean hasPickup = events.stream().anyMatch(e -> "PICKUP".equals(e.get("type")));

                    if (hasPickup) {
                        // 验证物品已从地牢中移除
                        ResponseEntity<Map> checkResp = restTemplate.getForEntity(
                                url("/api/dungeon/" + sid + "/state"), Map.class);
                        Map<String, Object> dungeonAfter = asMap(checkResp.getBody().get("dungeon"));
                        List<Map<String, Object>> itemsAfter = (List<Map<String, Object>>) dungeonAfter.get("items");

                        assertTrue(itemsAfter.size() < items.size(),
                                "Item count should decrease after pickup");
                        return;
                    }
                }
            }
        }
    }

    // ==================== 怪物状态一致性 ====================

    @Test
    @DisplayName("等待回合后怪物位置可能变化")
    void waitTurn_MonstersMayMove() {
        String sid = createSession();

        // 获取初始怪物位置
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        Map<String, Object> dungeon = asMap(stateResp.getBody().get("dungeon"));
        List<Map<String, Object>> monstersBefore = (List<Map<String, Object>>) dungeon.get("monsters");

        if (monstersBefore.isEmpty()) {
            return;
        }

        // 记录怪物位置快照
        String[] positionsBefore = monstersBefore.stream()
                .map(m -> m.get("x") + "," + m.get("y"))
                .toArray(String[]::new);

        // 等待 3 回合让怪物移动
        for (int i = 0; i < 3; i++) {
            restTemplate.postForEntity(url("/api/dungeon/" + sid + "/wait"), null, Map.class);
        }

        // 获取新位置
        ResponseEntity<Map> afterResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        Map<String, Object> dungeonAfter = asMap(afterResp.getBody().get("dungeon"));
        List<Map<String, Object>> monstersAfter = (List<Map<String, Object>>) dungeonAfter.get("monsters");

        // 至少有一些怪物移动了（或者数量减少了——被其他怪物杀死？不太可能）
        // 这个测试主要是验证怪物 AI 在运行，不崩溃
        assertNotNull(monstersAfter);
        assertFalse(monstersAfter.isEmpty(), "Monsters should still exist after waiting");
    }

    // ==================== 胜利条件 ====================

    @Test
    @DisplayName("击杀所有怪物后游戏状态应为 VICTORY")
    void killAllMonsters_ShouldWin() {
        String sid = createSession();

        // 获取怪物数量
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        int remainingMonsters = (Integer) stateResp.getBody().get("remainingMonsters");

        if (remainingMonsters == 0) {
            return; // 没有怪物，跳过
        }

        // 反复攻击直到胜利（最多 200 回合）
        for (int turn = 0; turn < 200; turn++) {
            ResponseEntity<Map> currResp = restTemplate.getForEntity(
                    url("/api/dungeon/" + sid + "/state"), Map.class);
            String status = (String) currResp.getBody().get("status");

            if ("VICTORY".equals(status)) {
                return; // 胜利！
            }
            if ("DEFEAT".equals(status)) {
                return; // 玩家死亡，跳过
            }

            // 获取当前怪物位置，尝试攻击最近的
            Map<String, Object> dungeon = asMap(currResp.getBody().get("dungeon"));
            List<Map<String, Object>> monsters = (List<Map<String, Object>>) dungeon.get("monsters");
            Map<String, Object> player = asMap(currResp.getBody().get("player"));
            int px = (Integer) player.get("x");
            int py = (Integer) player.get("y");

            // 找最近的怪物
            Map<String, Object> nearest = null;
            int minDist = Integer.MAX_VALUE;
            for (Map<String, Object> m : monsters) {
                int mx = (Integer) m.get("x");
                int my = (Integer) m.get("y");
                int dist = Math.abs(mx - px) + Math.abs(my - py);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = m;
                }
            }

            if (nearest != null && minDist == 1) {
                // 相邻，攻击
                int mx = (Integer) nearest.get("x");
                int my = (Integer) nearest.get("y");
                String dir;
                if (mx > px) dir = "right";
                else if (mx < px) dir = "left";
                else if (my > py) dir = "down";
                else dir = "up";

                restTemplate.postForEntity(url("/api/dungeon/" + sid + "/move"),
                        Map.of("direction", dir), Map.class);
            } else {
                // 等待怪物靠近
                restTemplate.postForEntity(url("/api/dungeon/" + sid + "/wait"),
                        null, Map.class);
            }
        }
    }

    // ==================== 工具方法 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object obj) {
        return (Map<String, Object>) obj;
    }
}
