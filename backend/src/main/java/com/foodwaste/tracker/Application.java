package com.foodwaste.tracker;

import com.foodwaste.tracker.handler.CorsHandler;
import com.foodwaste.tracker.model.FoodWasteEntry;
import com.foodwaste.tracker.model.StatsSummary;
import com.foodwaste.tracker.repository.FoodWasteRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application entry point providing REST API services for the Food Waste Tracking Dashboard.
 */
public class Application {
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final FoodWasteRepository repository = new FoodWasteRepository();
    private static final long START_TIME = System.currentTimeMillis();

    private static HttpServer serverInstance;

    public static void main(String[] args) {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid PORT env var. Falling back to default port 8080.");
            }
        }
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException ignored) {}
        }

        try {
            startServer(port);
            LOGGER.info("==================================================");
            LOGGER.info("Food Waste Tracker Backend started on port: " + port);
            LOGGER.info("Healthcheck: http://localhost:" + port + "/api/health");
            LOGGER.info("Stats API:   http://localhost:" + port + "/api/stats");
            LOGGER.info("Entries API: http://localhost:" + port + "/api/entries");
            LOGGER.info("==================================================");

            // Keep main thread alive
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutting down backend server...");
                stopServer();
            }));

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start HTTP server", e);
            System.exit(1);
        }
    }

    public static synchronized HttpServer startServer(int port) throws IOException {
        if (serverInstance != null) {
            return serverInstance;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Root Info
        server.createContext("/", exchange -> {
            if (CorsHandler.handlePreflight(exchange)) return;
            if (exchange.getRequestURI().getPath().equals("/")) {
                Map<String, Object> root = new LinkedHashMap<>();
                root.put("service", "Food Waste Tracking Dashboard Backend");
                root.put("status", "UP");
                root.put("endpoints", List.of("/api/health", "/api/stats", "/api/entries"));
                CorsHandler.sendJsonResponse(exchange, 200, GSON.toJson(root));
            } else if (!exchange.getRequestURI().getPath().startsWith("/api")) {
                CorsHandler.sendTextResponse(exchange, 404, "Not Found");
            }
        });

        // Health Check Endpoint (DevOps / Docker / Jenkins)
        server.createContext("/api/health", exchange -> {
            if (CorsHandler.handlePreflight(exchange)) return;
            long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;
            Runtime runtime = Runtime.getRuntime();
            long freeMem = runtime.freeMemory() / (1024 * 1024);
            long totalMem = runtime.totalMemory() / (1024 * 1024);

            Map<String, Object> health = new LinkedHashMap<>();
            health.put("status", "UP");
            health.put("service", "tracker-backend");
            health.put("version", "0.1.0");
            health.put("timestamp", Instant.now().toString());
            health.put("uptimeSeconds", uptimeSeconds);
            health.put("javaVersion", System.getProperty("java.version"));
            health.put("jvmUptime", ManagementFactory.getRuntimeMXBean().getUptime() + "ms");
            health.put("memory", String.format("%d MB free / %d MB total", freeMem, totalMem));

            CorsHandler.sendJsonResponse(exchange, 200, GSON.toJson(health));
        });

        // Stats Endpoint
        server.createContext("/api/stats", exchange -> {
            if (CorsHandler.handlePreflight(exchange)) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                StatsSummary stats = repository.calculateStats();
                CorsHandler.sendJsonResponse(exchange, 200, GSON.toJson(stats));
            } else {
                CorsHandler.sendTextResponse(exchange, 405, "Method Not Allowed");
            }
        });

        // Entries CRUD Endpoint
        server.createContext("/api/entries", exchange -> {
            if (CorsHandler.handlePreflight(exchange)) return;
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

            // Extract path id if present (e.g. /api/entries/FW-1042)
            String pathId = null;
            if (path.startsWith("/api/entries/") && path.length() > "/api/entries/".length()) {
                pathId = path.substring("/api/entries/".length()).trim();
            } else if (queryParams.containsKey("id")) {
                pathId = queryParams.get("id");
            }

            try {
                switch (method) {
                    case "GET":
                        if (pathId != null && !pathId.isBlank()) {
                            Optional<FoodWasteEntry> entry = repository.findById(pathId);
                            if (entry.isPresent()) {
                                CorsHandler.sendJsonResponse(exchange, 200, GSON.toJson(entry.get()));
                            } else {
                                CorsHandler.sendJsonResponse(exchange, 404, "{\"error\": \"Entry not found\"}");
                            }
                        } else {
                            String search = queryParams.get("search");
                            String status = queryParams.get("status");
                            String category = queryParams.get("category");
                            String source = queryParams.get("source");
                            List<FoodWasteEntry> entries = repository.filterAndSearch(search, status, category, source);
                            CorsHandler.sendJsonResponse(exchange, 200, GSON.toJson(entries));
                        }
                        break;

                    case "POST":
                        String postBody = readRequestBody(exchange);
                        if (postBody.isBlank()) {
                            CorsHandler.sendJsonResponse(exchange, 400, "{\"error\": \"Empty request body\"}");
                            return;
                        }
                        FoodWasteEntry newEntry = GSON.fromJson(postBody, FoodWasteEntry.class);
                        if (newEntry.getSource() == null || newEntry.getSource().isBlank()) {
                            CorsHandler.sendJsonResponse(exchange, 400, "{\"error\": \"Source is required\"}");
                            return;
                        }
                        if (newEntry.getCategory() == null || newEntry.getCategory().isBlank()) {
                            CorsHandler.sendJsonResponse(exchange, 400, "{\"error\": \"Category is required\"}");
                            return;
                        }
                        FoodWasteEntry created = repository.create(newEntry);
                        CorsHandler.sendJsonResponse(exchange, 201, GSON.toJson(created));
                        break;

                    case "PUT":
                    case "PATCH":
                        if (pathId == null || pathId.isBlank()) {
                            CorsHandler.sendJsonResponse(exchange, 400, "{\"error\": \"Entry ID is required\"}");
                            return;
                        }
                        String putBody = readRequestBody(exchange);
                        FoodWasteEntry updatedData = GSON.fromJson(putBody, FoodWasteEntry.class);
                        Optional<FoodWasteEntry> updated = repository.update(pathId, updatedData);
                        if (updated.isPresent()) {
                            CorsHandler.sendJsonResponse(exchange, 200, GSON.toJson(updated.get()));
                        } else {
                            CorsHandler.sendJsonResponse(exchange, 404, "{\"error\": \"Entry not found\"}");
                        }
                        break;

                    case "DELETE":
                        if (pathId == null || pathId.isBlank()) {
                            CorsHandler.sendJsonResponse(exchange, 400, "{\"error\": \"Entry ID is required\"}");
                            return;
                        }
                        boolean deleted = repository.delete(pathId);
                        if (deleted) {
                            CorsHandler.sendJsonResponse(exchange, 200, "{\"message\": \"Deleted successfully\", \"id\": \"" + pathId + "\"}");
                        } else {
                            CorsHandler.sendJsonResponse(exchange, 404, "{\"error\": \"Entry not found\"}");
                        }
                        break;

                    default:
                        CorsHandler.sendTextResponse(exchange, 405, "Method Not Allowed");
                        break;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error handling request: " + exchange.getRequestURI(), e);
                CorsHandler.sendJsonResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
            }
        });

        server.start();
        serverInstance = server;
        return server;
    }

    public static synchronized void stopServer() {
        if (serverInstance != null) {
            serverInstance.stop(0);
            serverInstance = null;
        }
    }

    public static FoodWasteRepository getRepository() {
        return repository;
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                params.put(key, value);
            } else if (pair.length == 1) {
                params.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), "");
            }
        }
        return params;
    }
}
