package com.bcorps.web;

import com.bcorps.review.MiniJson;
import java.util.Collection;
import java.util.Map;

/** Turns Maps / Lists / Strings / numbers into JSON text, and reads JSON request bodies. */
public final class Json {

    private Json() { }

    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        append(sb, value);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> readObject(String body) {
        Object value = MiniJson.parse(body == null || body.isBlank() ? "{}" : body);
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("Request body must be a JSON object.");
        }
        return (Map<String, Object>) value;
    }

    private static void append(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String s) {
            sb.append(MiniJson.quote(s));
        } else if (value instanceof Boolean b) {
            sb.append(b);
        } else if (value instanceof Double d) {
            sb.append(d.isNaN() || d.isInfinite() ? "0" : trim(d));
        } else if (value instanceof Number n) {
            sb.append(n);
        } else if (value instanceof Enum<?> e) {
            sb.append(MiniJson.quote(e.name()));
        } else if (value instanceof Map<?, ?> map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(MiniJson.quote(String.valueOf(entry.getKey()))).append(':');
                append(sb, entry.getValue());
            }
            sb.append('}');
        } else if (value instanceof Collection<?> list) {
            sb.append('[');
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                append(sb, item);
            }
            sb.append(']');
        } else {
            sb.append(MiniJson.quote(value.toString()));
        }
    }

    private static String trim(double d) {
        return d == Math.rint(d) && Math.abs(d) < 1e15
                ? String.valueOf((long) d) : String.valueOf(Math.round(d * 100) / 100.0);
    }
}
