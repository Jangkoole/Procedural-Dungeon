package com.dungeon.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 并发与会话管理集成测试 —— 覆盖多会话、并发操作和会话生命周期。
 *
 * <p>测试策略：
 * <ul>
 *   <li>使用 {@link ExecutorService} 模拟并发请求</li>
 *   <li>验证多个独立会话互不干扰</li>
 *   <li>验证会话列表和删除操作的正确性</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
class DungeonConcurrencyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ==================== 多会话隔离 ====================

    @Test
    @DisplayName("多个独立会话应互不干扰")
    void multipleSessions_ShouldBeIsolated() {
        // 创建 3 个独立会话
        String[] sessionIds = new String[3];
        for (int i = 0; i < 3; i++) {
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    url("/api/dungeon/generate"),
                    Map.of("width", 40, "height", 20), Map.class);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            sessionIds[i] = (String) resp.getBody().get("sessionId");
        }

        // 验证每个会话独立
        for (String sid : sessionIds) {
            ResponseEntity<Map> resp = restTemplate.getForEntity(
                    url("/api/dungeon/" + sid + "/state"), Map.class);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            assertEquals(sid, resp.getBody().get("sessionId"));
        }

        // 在会话 1 中执行操作，不影响其他会话
        restTemplate.postForEntity(url("/api/dungeon/" + sessionIds[0] + "/wait"),
                null, Map.class);

        ResponseEntity<Map> s1 = restTemplate.getForEntity(
                url("/api/dungeon/" + sessionIds[0] + "/state"), Map.class);
        ResponseEntity<Map> s2 = restTemplate.getForEntity(
                url("/api/dungeon/" + sessionIds[1] + "/state"), Map.class);

        int turn1 = (Integer) s1.getBody().get("turn");
        int turn2 = (Integer) s2.getBody().get("turn");

        assertEquals(1, turn1, "Session 1 should have 1 turn");
        assertEquals(0, turn2, "Session 2 should have 0 turns (untouched)");
    }

    // ==================== 会话列表 ====================

    @Test
    @DisplayName("GET /api/sessions - 会话列表应包含所有活跃会话")
    void listSessions_ShouldContainAllActiveSessions() {
        // 创建 2 个新会话
        String sid1 = createSession();
        String sid2 = createSession();

        ResponseEntity<Map> listResp = restTemplate.getForEntity(
                url("/api/sessions"), Map.class);

        List<Map<String, Object>> sessions = (List<Map<String, Object>>) listResp.getBody().get("sessions");
        int count = (Integer) listResp.getBody().get("count");

        assertTrue(count >= 2, "Should have at least 2 sessions");

        Set<String> ids = new HashSet<>();
        for (Map<String, Object> s : sessions) {
            ids.add((String) s.get("sessionId"));
            assertNotNull(s.get("createdAt"));
            assertNotNull(s.get("lastActiveAt"));
            assertNotNull(s.get("turn"));
            assertNotNull(s.get("status"));
        }

        assertTrue(ids.contains(sid1), "Session list should contain sid1");
        assertTrue(ids.contains(sid2), "Session list should contain sid2");
    }

    // ==================== 并发创建 ====================

    @Test
    @DisplayName("并发创建多个会话应全部成功")
    void concurrentCreate_AllShouldSucceed() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    ResponseEntity<Map> resp = restTemplate.postForEntity(
                            url("/api/dungeon/generate"),
                            Map.of("width", 40, "height", 20), Map.class);
                    if (resp.getStatusCode() == HttpStatus.OK) {
                        return (String) resp.getBody().get("sessionId");
                    }
                    return "FAILED:" + resp.getStatusCode();
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Set<String> sessionIds = new HashSet<>();
        for (Future<String> f : futures) {
            String result = f.get();
            assertFalse(result.startsWith("FAILED"), "Concurrent create should not fail: " + result);
            sessionIds.add(result);
        }

        assertEquals(threadCount, sessionIds.size(), "All session IDs should be unique");
    }

    // ==================== 并发操作同一会话 ====================

    @Test
    @DisplayName("并发操作同一会话不应导致数据损坏")
    void concurrentOperations_SameSession_ShouldNotCorrupt() throws Exception {
        String sid = createSession();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    // 混合不同类型的请求
                    String[] dirs = {"up", "down", "left", "right"};
                    String dir = dirs[ThreadLocalRandom.current().nextInt(4)];

                    ResponseEntity<Map> resp = restTemplate.postForEntity(
                            url("/api/dungeon/" + sid + "/move"),
                            Map.of("direction", dir), Map.class);

                    // 任何响应都可以（OK 或 400），只要不抛异常
                    return resp.getStatusCode() == HttpStatus.OK
                            || resp.getStatusCode() == HttpStatus.BAD_REQUEST;
                } catch (Exception e) {
                    return false;
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证所有请求都正常完成
        for (Future<Boolean> f : futures) {
            assertTrue(f.get(), "Concurrent operation should complete without exception");
        }

        // 验证会话状态仍然可读
        ResponseEntity<Map> stateResp = restTemplate.getForEntity(
                url("/api/dungeon/" + sid + "/state"), Map.class);
        assertEquals(HttpStatus.OK, stateResp.getStatusCode());
        assertNotNull(stateResp.getBody().get("player"));
        assertNotNull(stateResp.getBody().get("dungeon"));
    }

    // ==================== 会话清理 ====================

    @Test
    @DisplayName("删除会话后不应出现在会话列表中")
    void deleteSession_ShouldNotAppearInList() {
        String sid = createSession();

        // 删除
        restTemplate.exchange(url("/api/sessions/" + sid),
                HttpMethod.DELETE, HttpEntity.EMPTY, Map.class);

        // 验证不在列表中
        ResponseEntity<Map> listResp = restTemplate.getForEntity(
                url("/api/sessions"), Map.class);
        List<Map<String, Object>> sessions = (List<Map<String, Object>>) listResp.getBody().get("sessions");

        boolean found = sessions.stream().anyMatch(s -> sid.equals(s.get("sessionId")));
        assertFalse(found, "Deleted session should not appear in list");
    }

    // ==================== 大量会话 ====================

    @Test
    @DisplayName("创建大量会话应全部成功")
    void bulkCreateSessions_AllShouldSucceed() {
        int count = 10;
        List<String> ids = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String sid = createSession();
            ids.add(sid);
        }

        // 验证所有会话都可访问
        for (String sid : ids) {
            ResponseEntity<Map> resp = restTemplate.getForEntity(
                    url("/api/dungeon/" + sid + "/state"), Map.class);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
        }

        // 清理
        for (String sid : ids) {
            restTemplate.exchange(url("/api/sessions/" + sid),
                    HttpMethod.DELETE, HttpEntity.EMPTY, Map.class);
        }
    }

    // ==================== 工具方法 ====================

    private String createSession() {
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                url("/api/dungeon/generate"),
                Map.of("width", 40, "height", 20), Map.class);
        return (String) resp.getBody().get("sessionId");
    }
}
