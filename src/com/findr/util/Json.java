package com.findr.util;

import com.findr.exception.InvalidInputException;
import java.util.*;

/** Tiny JSON helper (no external libraries): builds JSON text and reads a simple flat JSON object. */
public final class Json {
    private Json() { }   // utility class: nobody should create objects of it

    /** STRING HANDLING: wraps text in quotes and escapes special characters. */
    public static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    /** Reads {"name":"Wallet","cat":"Other"} into a Map. */
    public static Map<String, String> parseObject(String text) throws InvalidInputException {
        Map<String, String> map = new HashMap<>();
        int[] pos = {0};
        skipSpaces(text, pos);
        if (pos[0] >= text.length() || text.charAt(pos[0]) != '{') throw new InvalidInputException("Request body must be a JSON object.");
        pos[0]++;
        skipSpaces(text, pos);
        if (pos[0] < text.length() && text.charAt(pos[0]) == '}') return map;
        while (true) {
            skipSpaces(text, pos);
            String key = readString(text, pos);
            skipSpaces(text, pos);
            if (pos[0] >= text.length() || text.charAt(pos[0]) != ':') throw new InvalidInputException("Invalid JSON: expected ':'.");
            pos[0]++;
            skipSpaces(text, pos);
            String value;
            if (pos[0] < text.length() && text.charAt(pos[0]) == '"') value = readString(text, pos);
            else {                                   // numbers, true, false, null
                int start = pos[0];
                while (pos[0] < text.length() && ",}".indexOf(text.charAt(pos[0])) < 0) pos[0]++;
                value = text.substring(start, pos[0]).trim();
            }
            map.put(key, value);
            skipSpaces(text, pos);
            if (pos[0] >= text.length()) throw new InvalidInputException("Invalid JSON: unexpected end.");
            char c = text.charAt(pos[0]++);
            if (c == '}') return map;
            if (c != ',') throw new InvalidInputException("Invalid JSON: expected ',' or '}'.");
        }
    }

    private static void skipSpaces(String t, int[] p) {
        while (p[0] < t.length() && Character.isWhitespace(t.charAt(p[0]))) p[0]++;
    }

    private static String readString(String t, int[] p) throws InvalidInputException {
        if (p[0] >= t.length() || t.charAt(p[0]) != '"') throw new InvalidInputException("Invalid JSON: expected a string.");
        p[0]++;
        StringBuilder sb = new StringBuilder();
        while (p[0] < t.length()) {
            char c = t.charAt(p[0]++);
            if (c == '"') return sb.toString();
            if (c == '\\' && p[0] < t.length()) {
                char e = t.charAt(p[0]++);
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (p[0] + 4 > t.length()) throw new InvalidInputException("Invalid JSON escape.");
                        sb.append((char) Integer.parseInt(t.substring(p[0], p[0] + 4), 16)); p[0] += 4; break;
                    default: sb.append(e);
                }
            } else sb.append(c);
        }
        throw new InvalidInputException("Invalid JSON: unterminated string.");
    }
}
