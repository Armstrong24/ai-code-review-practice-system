package com.bcorps.web;

import com.bcorps.app.Platform;
import com.bcorps.auth.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Member 1: registration, login, logout and "who am I".
 *   POST /api/auth/register  {name, email, password}
 *   POST /api/auth/login     {email, password}
 *   POST /api/auth/logout
 *   GET  /api/auth/me
 */
public class AuthServlet extends ApiServlet {
    private static final long serialVersionUID = 1L;

    public AuthServlet(Platform platform, Set<String> adminEmails) {
        super(platform, adminEmails);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            if (!"/me".equals(subPath(req))) {
                throw new ApiError(404, "Not found.");
            }
            Optional<Student> student = currentStudent(req);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("student", student.map(this::describe).orElse(null));
            map.put("reviewer", platform.submissionService().reviewerName());
            return map;
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            switch (subPath(req)) {
                case "/register": {
                    Map<String, Object> body = readBody(req);
                    Student student = platform.auth().register(text(body, "name"),
                            text(body, "email"), text(body, "password").toCharArray());
                    startSession(req, student);
                    return Map.of("student", describe(student));
                }
                case "/login": {
                    Map<String, Object> body = readBody(req);
                    Student student = platform.auth()
                            .login(text(body, "email"), text(body, "password").toCharArray())
                            .orElseThrow(() -> new ApiError(401, "Invalid email or password."));
                    startSession(req, student);
                    return Map.of("student", describe(student));
                }
                case "/logout": {
                    HttpSession session = req.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    return ok();
                }
                default:
                    throw new ApiError(404, "Not found.");
            }
        });
    }

    private void startSession(HttpServletRequest req, Student student) {
        HttpSession old = req.getSession(false);
        if (old != null) {
            old.invalidate(); // new session ID after login (prevents session fixation)
        }
        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(60 * 60 * 4);
        session.setAttribute(SESSION_STUDENT, student.getId());
    }

    private Map<String, Object> describe(Student student) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", student.getId());
        map.put("name", student.getName());
        map.put("email", student.getEmail());
        map.put("admin", isAdmin(student));
        return map;
    }
}
