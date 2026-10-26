package com.findr.web;

import com.findr.exception.*;
import com.findr.model.*;
import com.findr.service.ItemService;
import com.findr.service.SearchCriteria;
import com.findr.util.Json;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * REST API + web server using only the JDK (no Maven, no Spring).
 * It is a THIN layer: it reads the request, calls ItemService, and writes JSON back.
 *
 *   GET    /api/state                  -> { items:[...], matches:[...] }
 *   GET    /api/items?type=&category=&location=&keyword=&date=
 *   POST   /api/items                  -> report a lost or found item
 *   PUT    /api/items/{id}/status      -> ACTIVE | CLAIMED | RETURNED
 *   DELETE /api/items/{id}
 *   GET    /api/matches[?lostId=L1]
 *   anything else                      -> files from the web/ folder (the website)
 */
public class ApiServer {
    private final ItemService service;                 // COMPOSITION: the server has-a service
    private final int port;
    private final Path webRoot = Paths.get("web").toAbsolutePath().normalize();

    public ApiServer(ItemService service, int port) {
        this.service = service;
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.start();
        System.out.println("Lost & Found backend running.");
        System.out.println("Open the website:  http://localhost:" + port + "/");
        System.out.println("Press Ctrl+C to stop.");
    }

    // ---------- router ----------
    private synchronized void handle(HttpExchange ex) throws IOException {
        try {
            addCors(ex);
            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();
            if (method.equals("OPTIONS")) { send(ex, 204, "text/plain", ""); return; }
            if (!path.startsWith("/api/")) { serveFile(ex, path); return; }

            if (path.equals("/api/state") && method.equals("GET")) {
                send(ex, 200, "application/json",
                        "{\"items\":" + jsonArray(service.getAllItems()) + ",\"matches\":" + jsonArray(service.findMatches()) + "}");
            } else if (path.equals("/api/items") && method.equals("GET")) {
                Map<String, String> q = query(ex);
                SearchCriteria criteria = new SearchCriteria(q.get("type"), q.get("category"), q.get("location"), q.get("keyword"), q.get("date"));
                send(ex, 200, "application/json", jsonArray(service.search(criteria)));
            } else if (path.equals("/api/items") && method.equals("POST")) {
                addItem(ex);
            } else if (path.equals("/api/matches") && method.equals("GET")) {
                String lostId = query(ex).get("lostId");
                List<MatchResult> res;
                if (lostId == null) res = service.findMatches();
                else {
                    Item item = service.findById(lostId);
                    if (!(item instanceof LostItem)) throw new InvalidInputException(lostId + " is not a lost item.");
                    res = service.findMatches((LostItem) item);
                }
                send(ex, 200, "application/json", jsonArray(res));
            } else if (path.startsWith("/api/items/") && method.equals("PUT") && path.endsWith("/status")) {
                String id = path.substring("/api/items/".length(), path.length() - "/status".length());
                Map<String, String> body = Json.parseObject(readBody(ex));
                Item item = service.updateStatus(id, body.get("status"));
                send(ex, 200, "application/json", item.toJson());
            } else if (path.startsWith("/api/items/") && method.equals("DELETE")) {
                String id = path.substring("/api/items/".length());
                service.delete(id);
                send(ex, 200, "application/json", "{\"deleted\":" + Json.quote(id) + "}");
            } else {
                send(ex, 404, "application/json", error("Unknown API route: " + method + " " + path));
            }
        } catch (ItemNotFoundException e) {
            send(ex, 404, "application/json", error(e.getMessage()));
        } catch (InvalidInputException e) {
            send(ex, 400, "application/json", error(e.getMessage()));
        } catch (FindrException e) {
            send(ex, 500, "application/json", error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, "application/json", error("Server error: " + e.getMessage()));
        }
    }

    private void addItem(HttpExchange ex) throws IOException, FindrException {
        Map<String, String> b = Json.parseObject(readBody(ex));
        String type = b.getOrDefault("type", "").toLowerCase();
        Item item;
        if (type.equals("lost"))
            item = service.reportLost(b.get("name"), b.get("cat"), b.get("desc"), b.get("loc"), b.get("date"), b.get("p1"), b.get("p2"));
        else if (type.equals("found"))
            item = service.reportFound(b.get("name"), b.get("cat"), b.get("desc"), b.get("loc"), b.get("date"), b.get("p1"), b.get("p2"));
        else
            throw new InvalidInputException("type must be 'lost' or 'found'.");
        send(ex, 201, "application/json", item.toJson());
    }

    // ---------- helpers ----------
    /** GENERICS + INTERFACE: works for a list of Items or a list of MatchResults, as both are JsonSerializable. */
    private static String jsonArray(List<? extends JsonSerializable> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) { if (i > 0) sb.append(','); sb.append(list.get(i).toJson()); }
        return sb.append(']').toString();
    }

    private static String error(String msg) { return "{\"error\":" + Json.quote(msg) + "}"; }

    private static void addCors(HttpExchange ex) {
        Headers h = ex.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> query(HttpExchange ex) {
        Map<String, String> map = new HashMap<>();
        String q = ex.getRequestURI().getRawQuery();
        if (q == null) return map;
        for (String pair : q.split("&")) {
            String[] kv = pair.split("=", 2);
            map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return map;
    }

    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        if (code == 204) { ex.sendResponseHeaders(204, -1); ex.close(); return; }
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    /** Serves the website from the web/ folder. */
    private void serveFile(HttpExchange ex, String path) throws IOException {
        if (path.equals("/")) path = "/index.html";
        Path file = webRoot.resolve(path.substring(1)).normalize();
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {      // blocks ../ tricks
            send(ex, 404, "text/plain", "Not found");
            return;
        }
        String name = file.getFileName().toString().toLowerCase();
        String type = name.endsWith(".html") ? "text/html" : name.endsWith(".css") ? "text/css"
                : name.endsWith(".js") ? "application/javascript" : name.endsWith(".svg") ? "image/svg+xml"
                : name.endsWith(".png") ? "image/png" : name.endsWith(".ico") ? "image/x-icon" : "application/octet-stream";
        byte[] data = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", type + (type.startsWith("text") ? "; charset=utf-8" : ""));
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }
}
