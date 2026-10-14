package com.bcorps.web;

import com.bcorps.app.Platform;
import com.bcorps.auth.Student;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Shared helpers for every JSON API servlet. */
abstract class ApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static final String SESSION_STUDENT = "studentId";
    private static final int MAX_BODY = 200_000;

    protected final transient Platform platform;
    private final transient Set<String> adminEmails;

    ApiServlet(Platform platform, Set<String> adminEmails) {
        this.platform = platform;
        this.adminEmails = adminEmails;
    }

    @FunctionalInterface
    interface Handler {
        Object handle() throws Exception;
    }

    /** Runs a handler and writes its result (or a clean error) as JSON. */
    protected void respond(HttpServletResponse response, Handler handler) throws IOException {
        int status = 200;
        Object body;
        try {
            body = handler.handle();
        } catch (ApiError e) {
            status = e.status;
            body = error(e.getMessage());
        } catch (IllegalArgumentException e) {
            status = 400;
            body = error(e.getMessage());
        } catch (Exception e) {
            status = 500;
            body = error("Server error. Please try again.");
            log("API failure", e);
        }
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(Json.write(body));
    }

    protected Map<String, Object> readBody(HttpServletRequest request) throws IOException {
        String type = request.getContentType();
        // Only JSON is accepted: browsers cannot send it cross-site without permission (CSRF guard).
        if (type == null || !type.toLowerCase(Locale.ROOT).startsWith("application/json")) {
            throw new ApiError(415, "Send JSON.");
        }
        byte[] bytes = request.getInputStream().readNBytes(MAX_BODY + 1);
        if (bytes.length > MAX_BODY) {
            throw new ApiError(413, "Request too large.");
        }
        return Json.readObject(new String(bytes, StandardCharsets.UTF_8));
    }

    protected Optional<Student> currentStudent(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(SESSION_STUDENT) instanceof Long id)) {
            return Optional.empty();
        }
        return platform.students().findById(id);
    }

    protected Student requireStudent(HttpServletRequest request) throws Exception {
        return currentStudent(request).orElseThrow(() -> new ApiError(401, "Please log in."));
    }

    protected boolean isAdmin(Student student) {
        return student != null && (adminEmails.isEmpty()
                || adminEmails.contains(student.getEmail().toLowerCase(Locale.ROOT)));
    }

    protected Student requireAdmin(HttpServletRequest request) throws Exception {
        Student student = requireStudent(request);
        if (!isAdmin(student)) {
            throw new ApiError(403, "Only admins can manage problems.");
        }
        return student;
    }

    /** Path after the servlet mapping, e.g. "/12" for /api/problems/12. */
    protected static String subPath(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "/" : path;
    }

    protected static long idFrom(String subPath) {
        try {
            return Long.parseLong(subPath.replace("/", ""));
        } catch (NumberFormatException e) {
            throw new ApiError(404, "Not found.");
        }
    }

    protected static String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : value.toString();
    }

    protected static Map<String, Object> error(String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("error", message);
        return map;
    }

    protected static Map<String, Object> ok() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ok", true);
        return map;
    }

    /** An error with an HTTP status code. */
    static final class ApiError extends RuntimeException {
        private static final long serialVersionUID = 1L;
        final int status;

        ApiError(int status, String message) {
            super(message);
            this.status = status;
        }
    }
}
