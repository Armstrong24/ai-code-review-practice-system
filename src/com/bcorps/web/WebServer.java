package com.bcorps.web;

import com.bcorps.app.Platform;
import jakarta.servlet.SessionCookieConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

/**
 * Web version of the system: Jakarta Servlets on an embedded Jetty server.
 * Serves the browser UI from the web/ folder and the JSON API under /api.
 *
 * Environment variables:
 *   PORT            port to listen on (default 8080; set automatically by Render)
 *   GEMINI_API_KEY  enables Gemini AI review (otherwise the offline checker is used)
 *   ADMIN_EMAILS    comma-separated emails allowed to add/delete problems
 *                   (if empty, every logged-in user can - fine for local use)
 */
public final class WebServer {

    private WebServer() { }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(env("PORT", "8080"));
        Set<String> admins = Arrays.stream(env("ADMIN_EMAILS", "").split(","))
                .map(s -> s.strip().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());

        Path webRoot = Path.of(env("WEB_ROOT", "web")).toAbsolutePath();
        if (!Files.isRegularFile(webRoot.resolve("index.html"))) {
            throw new IllegalStateException("Cannot find web/index.html. Run from the project folder.");
        }

        Platform platform = Platform.openDefault();
        DemoData.ensureDemoAccount(platform);

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setBaseResource(ResourceFactory.of(server).newResource(webRoot));

        SessionCookieConfig cookie = context.getSessionHandler().getSessionCookieConfig();
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setSecure("true".equalsIgnoreCase(env("SECURE_COOKIES", "false")));

        context.addServlet(new ServletHolder(new AuthServlet(platform, admins)), "/api/auth/*");
        context.addServlet(new ServletHolder(new ProblemServlet(platform, admins)), "/api/problems/*");
        context.addServlet(new ServletHolder(new SubmissionServlet(platform, admins)), "/api/submissions/*");
        context.addServlet(new ServletHolder(new StatsServlet(platform, admins)), "/api/stats/*");
        context.addServlet(new ServletHolder(new LearnServlet(platform, admins)), "/api/learn/*");

        ServletHolder files = new ServletHolder("static", DefaultServlet.class);
        files.setInitParameter("dirAllowed", "false");
        context.addServlet(files, "/");

        server.setHandler(context);
        server.start();
        System.out.println("AI Code Review web app running at http://localhost:" + port);
        System.out.println("Code review: " + platform.submissionService().reviewerName());
        server.join();
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.strip();
    }
}
