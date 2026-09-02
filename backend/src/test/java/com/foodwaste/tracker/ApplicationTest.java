package com.foodwaste.tracker;

import com.foodwaste.tracker.model.FoodWasteEntry;
import com.foodwaste.tracker.model.StatsSummary;
import com.foodwaste.tracker.repository.FoodWasteRepository;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    private static final int TEST_PORT = 18080;
    private static final Gson GSON = new Gson();
    private static HttpServer testServer;

    @BeforeAll
    static void setUp() throws Exception {
        testServer = Application.startServer(TEST_PORT);
    }

    @AfterAll
    static void tearDown() {
        Application.stopServer();
    }

    @Test
    void testHealthEndpoint() throws Exception {
        URL url = URI.create("http://localhost:" + TEST_PORT + "/api/health").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String response = readResponse(conn);
        assertTrue(response.contains("\"status\": \"UP\"") || response.contains("\"status\":\"UP\""));
        assertTrue(response.contains("tracker-backend"));
    }

    @Test
    void testStatsEndpoint() throws Exception {
        URL url = URI.create("http://localhost:" + TEST_PORT + "/api/stats").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String response = readResponse(conn);
        StatsSummary stats = GSON.fromJson(response, StatsSummary.class);
        assertNotNull(stats);
        assertTrue(stats.getTotalEntries() > 0);
        assertNotNull(stats.getTotalWasteLogged());
        assertNotNull(stats.getReductionRate());
    }

    @Test
    void testGetEntries() throws Exception {
        URL url = URI.create("http://localhost:" + TEST_PORT + "/api/entries").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String response = readResponse(conn);
        assertTrue(response.contains("FW-1042") || response.contains("Kitchen A"));
    }

    @Test
    void testCreateAndUpdateAndFilterEntries() throws Exception {
        // 1. Create a new Entry
        URL createUrl = URI.create("http://localhost:" + TEST_PORT + "/api/entries").toURL();
        HttpURLConnection createConn = (HttpURLConnection) createUrl.openConnection();
        createConn.setRequestMethod("POST");
        createConn.setDoOutput(true);
        createConn.setRequestProperty("Content-Type", "application/json");

        FoodWasteEntry newEntry = new FoodWasteEntry(
                null, "Bakery Test", "Bakery", 7.5, "kg", "Pending", "2026-09-01", "Test batch leftovers"
        );
        try (OutputStream os = createConn.getOutputStream()) {
            os.write(GSON.toJson(newEntry).getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(201, createConn.getResponseCode());
        String createResp = readResponse(createConn);
        FoodWasteEntry created = GSON.fromJson(createResp, FoodWasteEntry.class);
        assertNotNull(created.getId());
        assertEquals("Bakery Test", created.getSource());

        // 2. Filter by search
        URL searchUrl = URI.create("http://localhost:" + TEST_PORT + "/api/entries?search=Bakery+Test").toURL();
        HttpURLConnection searchConn = (HttpURLConnection) searchUrl.openConnection();
        searchConn.setRequestMethod("GET");
        assertEquals(200, searchConn.getResponseCode());
        String searchResp = readResponse(searchConn);
        assertTrue(searchResp.contains(created.getId()));

        // 3. Update entry status to Reviewed
        URL updateUrl = URI.create("http://localhost:" + TEST_PORT + "/api/entries/" + created.getId()).toURL();
        HttpURLConnection updateConn = (HttpURLConnection) updateUrl.openConnection();
        updateConn.setRequestMethod("PUT");
        updateConn.setDoOutput(true);
        updateConn.setRequestProperty("Content-Type", "application/json");

        FoodWasteEntry updateData = new FoodWasteEntry();
        updateData.setStatus("Reviewed");
        try (OutputStream os = updateConn.getOutputStream()) {
            os.write(GSON.toJson(updateData).getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(200, updateConn.getResponseCode());
        String updateResp = readResponse(updateConn);
        FoodWasteEntry updated = GSON.fromJson(updateResp, FoodWasteEntry.class);
        assertEquals("Reviewed", updated.getStatus());

        // 4. Delete entry
        URL deleteUrl = URI.create("http://localhost:" + TEST_PORT + "/api/entries/" + created.getId()).toURL();
        HttpURLConnection deleteConn = (HttpURLConnection) deleteUrl.openConnection();
        deleteConn.setRequestMethod("DELETE");
        assertEquals(200, deleteConn.getResponseCode());
    }

    @Test
    void testRepositoryDirectOperations() {
        FoodWasteRepository repo = new FoodWasteRepository();
        assertFalse(repo.findAll().isEmpty());
        StatsSummary stats = repo.calculateStats();
        assertTrue(stats.getTotalWasteKg() > 0);

        List<FoodWasteEntry> produceList = repo.filterAndSearch(null, null, "Produce", null);
        assertFalse(produceList.isEmpty());
        assertTrue(produceList.stream().allMatch(e -> "Produce".equalsIgnoreCase(e.getCategory())));
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
