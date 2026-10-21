package com.bcorps.web;

import com.bcorps.app.Platform;
import com.bcorps.auth.Student;
import com.bcorps.learning.ExamAttempt;
import com.bcorps.learning.ExamRepository;
import com.bcorps.learning.ExamResult;
import com.bcorps.learning.ExamService;
import com.bcorps.learning.ExamService.ExamSession;
import com.bcorps.learning.McqQuestion;
import com.bcorps.learning.Track;
import com.bcorps.learning.TutorialNote;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Learn & Exams module.
 *   GET  /api/learn/tracks                 every language + Olympiad, with my best score
 *   GET  /api/learn/notes/{track}          revision notes + one-shot YouTube videos
 *   POST /api/learn/exam/{track}/start     starts a timed exam (questions without answers)
 *   POST /api/learn/exam/{track}/submit    {answers: {questionId: optionIndex}} -> score + explanations
 *   GET  /api/learn/attempts               my exam history
 *   GET  /api/learn/leaderboard/{track}    best score per student for a track
 */
public class LearnServlet extends ApiServlet {
    private static final long serialVersionUID = 1L;
    private static final String SESSION_EXAMS = "exams";

    public LearnServlet(Platform platform, Set<String> adminEmails) {
        super(platform, adminEmails);
    }

    private ExamService exams() {
        return platform.exams();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            Student student = requireStudent(req);
            String[] parts = subPath(req).replaceAll("^/+", "").split("/");
            switch (parts[0]) {
                case "tracks":
                    return tracks(student);
                case "notes":
                    return notes(trackFrom(parts));
                case "attempts":
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (ExamAttempt a : exams().history(student.getId())) {
                        rows.add(attemptJson(a));
                    }
                    return rows;
                case "leaderboard":
                    List<Map<String, Object>> board = new ArrayList<>();
                    for (ExamRepository.LeaderRow r : exams().leaderboard(trackFrom(parts))) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("rank", r.rank());
                        row.put("name", r.name());
                        row.put("score", r.score());
                        row.put("timeTaken", r.timeTakenSeconds());
                        row.put("me", r.studentId() == student.getId());
                        board.add(row);
                    }
                    return board;
                default:
                    throw new ApiError(404, "Not found.");
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> {
            Student student = requireStudent(req);
            String[] parts = subPath(req).replaceAll("^/+", "").split("/");
            if (parts.length != 3 || !parts[0].equals("exam")) {
                throw new ApiError(404, "Not found.");
            }
            Track track = trackFrom(parts);
            Map<String, Object> body = readBody(req);
            Map<Track, ExamSession> running = runningExams(req.getSession(true));
            switch (parts[2]) {
                case "start": {
                    ExamSession session = exams().start(track);
                    running.put(track, session);
                    return startJson(session);
                }
                case "submit": {
                    ExamSession session = running.remove(track);
                    ExamResult result = exams().submit(student.getId(), session,
                            answersFrom(body.get("answers")), System.currentTimeMillis());
                    return resultJson(result);
                }
                default:
                    throw new ApiError(404, "Not found.");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static Map<Track, ExamSession> runningExams(HttpSession session) {
        synchronized (session) {
            Object value = session.getAttribute(SESSION_EXAMS);
            if (!(value instanceof Map)) {
                value = new ConcurrentHashMap<Track, ExamSession>();
                session.setAttribute(SESSION_EXAMS, value);
            }
            return (Map<Track, ExamSession>) value;
        }
    }

    private static Track trackFrom(String[] parts) {
        if (parts.length < 2) {
            throw new ApiError(404, "Choose a language.");
        }
        try {
            return Track.parse(parts[1]);
        } catch (IllegalArgumentException e) {
            throw new ApiError(404, e.getMessage());
        }
    }

    private static Map<Long, Integer> answersFrom(Object raw) {
        Map<Long, Integer> answers = new HashMap<>();
        if (raw instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                try {
                    long id = Long.parseLong(String.valueOf(e.getKey()));
                    if (e.getValue() instanceof Number n) {
                        answers.put(id, n.intValue());
                    }
                } catch (NumberFormatException ignored) {
                    // skip keys that are not question ids
                }
            }
        }
        return answers;
    }

    // ---------------------------------------------------------------- JSON shapes
    private List<Map<String, Object>> tracks(Student student) throws Exception {
        Map<Track, ExamAttempt> best = exams().bestByTrack(student.getId());
        List<ExamAttempt> history = exams().history(student.getId());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Track t : Track.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", t.name());
            row.put("name", t.displayName());
            row.put("kind", t.kind());
            row.put("tagline", t.tagline());
            row.put("minutes", t.minutes());
            int count = exams().questionCount(t);
            row.put("questions", Math.min(count, ExamService.QUESTIONS_PER_EXAM));
            row.put("bank", count);
            row.put("attempts", history.stream().filter(a -> a.track() == t).count());
            ExamAttempt b = best.get(t);
            row.put("bestScore", b == null ? null : b.score());
            row.put("passed", b != null && b.passed());
            list.add(row);
        }
        return list;
    }

    private Map<String, Object> notes(Track track) {
        TutorialNote note = exams().notes(track).orElseThrow(() -> new ApiError(404, "No notes yet."));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("track", track.name());
        map.put("name", track.displayName());
        map.put("overview", note.overview());
        List<Map<String, Object>> sections = new ArrayList<>();
        for (TutorialNote.Section s : note.sections()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("title", s.title());
            row.put("markdown", s.markdown());
            sections.add(row);
        }
        map.put("sections", sections);
        List<Map<String, Object>> videos = new ArrayList<>();
        for (TutorialNote.Video v : note.videos()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("title", v.title());
            row.put("channel", v.channel());
            row.put("language", v.language());
            row.put("duration", v.duration());
            row.put("youtubeId", v.youtubeId());
            row.put("url", v.url());
            videos.add(row);
        }
        map.put("videos", videos);
        List<Map<String, Object>> links = new ArrayList<>();
        for (TutorialNote.Link l : note.links()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("title", l.title());
            row.put("url", l.url());
            links.add(row);
        }
        map.put("links", links);
        return map;
    }

    private Map<String, Object> startJson(ExamSession session) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("track", session.track().name());
        map.put("name", session.track().displayName());
        map.put("minutes", session.track().minutes());
        map.put("secondsLeft", Math.max(0, (session.deadlineMillis() - System.currentTimeMillis()) / 1000));
        List<Map<String, Object>> questions = new ArrayList<>();
        for (McqQuestion q : exams().questionsOf(session)) {
            questions.add(questionJson(q, false));
        }
        map.put("questions", questions);
        return map;
    }

    private static Map<String, Object> questionJson(McqQuestion q, boolean withAnswer) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", q.getId());
        row.put("difficulty", q.getDifficulty());
        row.put("question", q.getQuestion());
        row.put("code", q.getCode());
        row.put("options", q.getOptions());
        if (withAnswer) {
            row.put("correct", q.getCorrectIndex());
            row.put("explanation", q.getExplanation());
        }
        return row;
    }

    private static Map<String, Object> resultJson(ExamResult result) {
        Map<String, Object> map = attemptJson(result.attempt());
        List<Map<String, Object>> answers = new ArrayList<>();
        for (ExamResult.AnswerReview r : result.answers()) {
            Map<String, Object> row = questionJson(r.question(), true);
            row.put("chosen", r.chosenIndex());
            row.put("isCorrect", r.correct());
            answers.add(row);
        }
        map.put("answers", answers);
        return map;
    }

    private static Map<String, Object> attemptJson(ExamAttempt a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.id());
        map.put("track", a.track().name());
        map.put("name", a.track().displayName());
        map.put("correct", a.correct());
        map.put("total", a.total());
        map.put("score", a.score());
        map.put("passed", a.passed());
        map.put("timeTaken", a.timeTakenSeconds());
        map.put("submittedAt", a.submittedAt().toString());
        return map;
    }
}
