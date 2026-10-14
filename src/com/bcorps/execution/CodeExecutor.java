package com.bcorps.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Compiles and runs submitted Java code in a separate process (Member 3).
 *
 * Uses processes (ProcessBuilder), multithreading (stdout and stderr are read
 * on their own threads so a chatty program cannot block), a time limit, and
 * file handling (each submission gets its own temporary folder).
 */
public class CodeExecutor {

    private static final long TIME_LIMIT_SECONDS = 5;
    private static final long COMPILE_LIMIT_SECONDS = 30;
    private static final int MAX_OUTPUT_CHARS = 100_000;
    private static final Pattern PUBLIC_CLASS =
            Pattern.compile("public\\s+(?:final\\s+)?class\\s+([A-Za-z_$][A-Za-z0-9_$]*)");
    private static final Pattern ANY_CLASS =
            Pattern.compile("\\bclass\\s+([A-Za-z_$][A-Za-z0-9_$]*)");
    private static final Pattern PACKAGE =
            Pattern.compile("^\\s*package\\s+[\\w.]+\\s*;", Pattern.MULTILINE);

    /** Finds the class to run: the public class, else the first class, else Main. */
    public static String detectClassName(String sourceCode) {
        Matcher matcher = PUBLIC_CLASS.matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = ANY_CLASS.matcher(sourceCode);
        return matcher.find() ? matcher.group(1) : "Main";
    }

    /**
     * Saves source code into a fresh temporary folder as ClassName.java.
     * The caller compiles it, runs it, then calls {@link #cleanUp(Path)}.
     */
    public Path prepareWorkspace(String sourceCode) throws IOException {
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("Submitted code is empty.");
        }
        if (PACKAGE.matcher(sourceCode).find()) {
            throw new IllegalArgumentException(
                    "Remove the package line: submissions must be in the default package.");
        }
        Path folder = Files.createTempDirectory("submission_");
        Path file = folder.resolve(detectClassName(sourceCode) + ".java");
        Files.writeString(file, sourceCode, StandardCharsets.UTF_8);
        return file;
    }

    /** Compiles one .java file with javac (original Member 3 API, path-based). */
    public CompilationResult compile(String filePath) {

        try {
            File file = new File(filePath);
            File parentFolder = file.getAbsoluteFile().getParentFile();

            ProcessBuilder processBuilder =
                    new ProcessBuilder(tool("javac"), "-encoding", "UTF-8", file.getName());
            processBuilder.directory(parentFolder);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            process.getOutputStream().close();

            ExecutorService reader = Executors.newSingleThreadExecutor();
            try {
                Future<String> output = reader.submit(readAll(process.getInputStream()));
                if (!process.waitFor(COMPILE_LIMIT_SECONDS, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    return new CompilationResult(false, "Compilation took too long.");
                }
                String errors = output.get(5, TimeUnit.SECONDS);
                if (process.exitValue() == 0) {
                    return new CompilationResult(true, "");
                }
                return new CompilationResult(false, errors);
            } finally {
                reader.shutdownNow();
            }

        } catch (IOException e) {
            return new CompilationResult(false,
                    "Could not start javac. Run this app with a JDK, not just a JRE. ("
                    + e.getMessage() + ")");
        } catch (Exception e) {
            return new CompilationResult(false, e.getMessage());
        }
    }

    /** Runs a compiled class with the given standard input (original Member 3 API). */
    public ExecutionResult execute(String className, String folderPath, String input) {

        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    tool("java"), "-Xmx256m", "-Dfile.encoding=UTF-8", "-cp", ".", className);
            processBuilder.directory(new File(folderPath));
            Process process = processBuilder.start();

            // Start the readers BEFORE writing input, so large output cannot deadlock.
            Future<String> outputFuture = executorService.submit(readAll(process.getInputStream()));
            Future<String> errorFuture = executorService.submit(readAll(process.getErrorStream()));

            try (OutputStream stdin = process.getOutputStream()) {
                String data = input == null ? "" : input;
                if (!data.isEmpty() && !data.endsWith("\n")) {
                    data = data + "\n";
                }
                stdin.write(data.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            } catch (IOException closedEarly) {
                // The program exited without reading all input; that is allowed.
            }

            boolean finished = process.waitFor(TIME_LIMIT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                outputFuture.cancel(true);
                errorFuture.cancel(true);
                return new ExecutionResult(false, "", "Time Limit Exceeded",
                        System.currentTimeMillis() - startTime);
            }

            String output = outputFuture.get(2, TimeUnit.SECONDS);
            String errors = errorFuture.get(2, TimeUnit.SECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (process.exitValue() == 0) {
                return new ExecutionResult(true, output, "", executionTime);
            }
            return new ExecutionResult(false, output,
                    errors.isBlank() ? "Program exited with code " + process.exitValue() : errors,
                    executionTime);

        } catch (Exception e) {
            return new ExecutionResult(false, "", e.getMessage(),
                    System.currentTimeMillis() - startTime);
        } finally {
            executorService.shutdownNow();
        }
    }

    /** Deletes a submission's temporary folder. */
    public void cleanUp(Path sourceFile) {
        if (sourceFile == null) {
            return;
        }
        Path folder = sourceFile.getParent();
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignored) {
            // Temporary files; the OS cleans them eventually.
        }
    }

    private static Callable<String> readAll(InputStream stream) {
        return () -> {
            StringBuilder text = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (text.length() < MAX_OUTPUT_CHARS) {
                        text.append(line).append("\n");
                    }
                }
            }
            if (text.length() >= MAX_OUTPUT_CHARS) {
                text.append("...[output truncated]\n");
            }
            return text.toString();
        };
    }

    /** Uses javac/java from the JDK running this app, falling back to the PATH. */
    private static String tool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        Path candidate = Path.of(System.getProperty("java.home"), "bin",
                windows ? name + ".exe" : name);
        return Files.isExecutable(candidate) ? candidate.toString() : name;
    }
}
