package dev.learninglogistics.infrai;

import java.util.LinkedHashMap;
import java.util.Map;

final class Json {
    private Json() {}

    static String object(Map<String, ?> values) {
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (!first) out.append(',');
            first = false;
            out.append(quote(entry.getKey())).append(':');
            Object value = entry.getValue();
            if (value == null) out.append("null");
            else if (value instanceof Boolean || value instanceof Number) out.append(value);
            else out.append(quote(value.toString()));
        }
        return out.append('}').toString();
    }

    static Map<String, String> topLevelFields(String json) {
        Map<String, String> fields = new LinkedHashMap<>();
        int index = skip(json, 0);
        if (index >= json.length() || json.charAt(index++) != '{') return fields;
        while (index < json.length()) {
            index = skip(json, index);
            if (index < json.length() && json.charAt(index) == '}') break;
            Parsed key = string(json, index);
            index = skip(json, key.next());
            if (index >= json.length() || json.charAt(index++) != ':') break;
            index = skip(json, index);
            Parsed value = value(json, index);
            fields.put(key.text(), value.text());
            index = skip(json, value.next());
            if (index < json.length() && json.charAt(index) == ',') index++;
        }
        return fields;
    }

    private static Parsed value(String json, int start) {
        if (json.charAt(start) == '"') return string(json, start);
        int depth = 0;
        boolean quoted = false;
        int i = start;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) quoted = !quoted;
            if (!quoted && (c == '{' || c == '[')) depth++;
            if (!quoted && (c == '}' || c == ']')) {
                if (depth == 0) break;
                depth--;
            }
            if (!quoted && depth == 0 && c == ',') break;
        }
        String raw = json.substring(start, i).trim();
        return new Parsed(raw, i);
    }

    private static Parsed string(String json, int start) {
        StringBuilder value = new StringBuilder();
        int i = start + 1;
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '"') break;
            if (c == '\\' && i < json.length()) {
                char escaped = json.charAt(i++);
                value.append(escaped == 'n' ? '\n' : escaped);
            } else value.append(c);
        }
        return new Parsed(value.toString(), i);
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static int skip(String value, int index) {
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) index++;
        return index;
    }

    private record Parsed(String text, int next) {}
}
