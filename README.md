# AI-Powered Code Review & Programming Practice System

A Java platform where students register, solve programming problems in Java,
get their code compiled and graded against hidden test cases, and receive AI feedback
on their code. Progress, scores and a leaderboard are stored in an H2 database.

**🌐 Live demo: https://ai-code-review-practice-system.onrender.com**
(demo login `demo@codereview.app` / `demo1234`; free hosting, so the first visit may take about a minute to wake up)

It runs two ways:

- **Website**: Jakarta Servlets on an embedded Jetty server with a browser UI (`run.bat web`)
- **Desktop app**: Swing (`run.bat`)

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/Armstrong24/ai-code-review-practice-system)

Group project by five members. Each module lives in its own package.

## How it works

```
Student (website or Swing app, Member 1)
   │  logs in, picks a problem, writes Java code, presses Submit
   ▼
Problem bank (Member 2) ── gives the problem + sample AND hidden test cases
   ▼
Execution engine (Member 3) ── compile → run each test (5 s limit) → compare → score
   ▼
AI review (Member 4) ── Groq or Gemini explains errors and suggests improvements
   ▼                     (offline rule checker if no API key / no internet)
Progress & database (Member 5) ── saves the submission, updates dashboard + leaderboard
```

All modules share one H2 database file: `data/codereview.mv.db` (created on first run).

## Modules

| Member | Module | Package | Main classes |
|---|---|---|---|
| 1 | User & Authentication, student UI | `com.bcorps.auth`, `com.bcorps.ui` | `AuthService`, `StudentRepository`, `PasswordHasher`, `AICodeReviewSystem` (Swing) |
| 2 | Programming Problems & Test Cases | `com.bcorps.problems` | `Problem`, `TestCase`, `ProblemService`, `JdbcProblemRepository`, `ProblemFileService`, `Member2Main` |
| 3 | Code Execution & Evaluation Engine | `com.bcorps.execution` | `CodeExecutor`, `TestCaseRunner`, `EvaluationEngine`, `EvaluationResult` |
| 4 | AI Code Review & Feedback | `com.bcorps.review` | `CodeReviewer` (interface), `GroqCodeReviewer`, `AICodeReviewer` (Gemini), `RuleBasedCodeReviewer`, `ReviewService` |
| 5 | Progress, Database & Integration | `com.bcorps.progress`, `com.bcorps.database`, `com.bcorps.app`, `com.bcorps.web` | `SubmissionDAO`, `ProgressService`, `DashboardService`, `LeaderboardService`, `DatabaseManager`, `Platform`, `SubmissionService`, `WebServer` + servlets |

### Database tables (`DatabaseManager`)

- `students` – id, name, email (unique), password_hash, password_salt, created_at
- `problems` – id, title, description, input_format, output_format, constraints_text, difficulty (EASY/MEDIUM/HARD), category
- `test_cases` – id, problem_id → problems, input_data, expected_output, is_sample
- `submissions` – id, student_id → students, problem_id → problems, source_code, status, score, passed_tests, total_tests, execution_time_ms, ai_review, submitted_at

## Requirements

- **JDK 17 or newer** (a full JDK, not just a JRE: the engine needs `javac` to compile submissions)
- The jars in `lib/` (included): H2 database, Jetty 12 + Jakarta Servlet 6 API, SLF4J

## Run it

**Windows**
```
run.bat web       website  ->  open http://localhost:8080
run.bat           student desktop app
run.bat admin     problem & test-case admin console (Member 2)
run.bat test      run every self-test
```

**macOS / Linux**
```
sh run.sh web
sh run.sh
sh run.sh admin
sh run.sh test
```

**Eclipse:** File → Import → Existing Projects into Workspace → select this folder.
Run `com.bcorps.app.Main` (add `admin` or `test` as a program argument if needed).
If Eclipse shows a JRE error, right-click the project → Properties → Java Build Path → Libraries and choose your installed JDK (17+).

On first start the app adds 15 demo problems (5 easy, 6 medium, 4 hard) with 87 test cases,
and the website adds a
demo login: **demo@codereview.app / demo1234** (the "Try the demo account" button).
If you already have a database from an older version, any missing demo problems are added
automatically on the next start.

### Demo problem bank

| # | Problem | Difficulty | Category | What it practises |
|---|---------|-----------|----------|-------------------|
| 1 | Largest element in an array | Easy | Arrays | loops, max |
| 2 | Palindrome string | Easy | Strings | two pointers |
| 3 | Square of a number | Easy | Math | input/output |
| 4 | Count vowels | Easy | Strings | characters, nextLine |
| 5 | Sum of digits | Easy | Math | `long`, % and / |
| 6 | Binary search | Medium | Searching | binary search |
| 7 | Check prime number | Medium | Math | loop up to √n |
| 8 | Second largest element | Medium | Arrays | distinct values, edge cases |
| 9 | Sort students by marks | Medium | Sorting | `Comparator`, tie-breaking |
| 10 | Anagram check | Medium | Strings | counting / sorting chars |
| 11 | Maximum subarray sum | Medium | Arrays | Kadane's algorithm, overflow |
| 12 | Longest increasing subsequence | Hard | Searching | O(n log n) with binary search |
| 13 | Longest valid parentheses | Hard | Strings | stack |
| 14 | Count inversions | Hard | Sorting | merge sort |
| 15 | Shortest path in a grid | Hard | Other | breadth-first search |

The large hidden tests (up to 100,000 characters or 50,000 numbers) are generated from a fixed
seed, so they are the same every run, and their answers come from reference solutions in
`SampleData`. Every problem was checked against an independent working solution.

## Website

| Page | Module | What it shows |
|---|---|---|
| Log in / Create account | 1 | Register, log in, demo account |
| Dashboard | 5 | Solved, success rate, average score, level, recent submissions, per-difficulty stats |
| Problems | 2 | Problem bank with difficulty / category filters and your status |
| Problem page | 2, 3, 4 | Statement + sample cases, Java code editor, results per test (hidden ones masked), AI feedback |
| Submissions | 5 | History; click one to see the code and feedback |
| Leaderboard | 5 | Ranking by best score per problem |
| Manage problems | 2 | Admins add problems with sample/hidden test cases, or delete them |
| About the project | all | How the five modules connect |

JSON API (all under `/api`, one servlet per module):

- `AuthServlet` – `POST /auth/register`, `POST /auth/login`, `POST /auth/logout`, `GET /auth/me`
- `ProblemServlet` – `GET /problems`, `GET /problems/{id}` (sample cases only), `POST /problems`, `DELETE /problems/{id}`
- `SubmissionServlet` – `POST /submissions`, `GET /submissions`, `GET /submissions/{id}`
- `StatsServlet` – `GET /stats/dashboard`, `GET /stats/leaderboard`

Website environment variables: `PORT` (default 8080), `GROQ_API_KEY`, `GEMINI_API_KEY`, `ADMIN_EMAILS`
(comma-separated; if empty every user can manage problems, fine locally), `JUDGE_THREADS`
(programs run at once, default 2), `SECURE_COOKIES` (`true` behind HTTPS), `DEMO_ACCOUNT` (`false` to disable).

## Live website (free, on Render)

Running at **https://ai-code-review-practice-system.onrender.com**. Light, dark and system themes (switch at the bottom of the sidebar or top-right of the login page; the choice is remembered).

1. Sign in at render.com with your GitHub account.
2. Click the **Deploy to Render** button at the top of this README (or New → Blueprint → pick this repo).
3. When asked, fill in `ADMIN_EMAILS` with the email you'll register with, and optionally `GROQ_API_KEY` and/or `GEMINI_API_KEY`.
4. Click **Apply**. The first build takes about 5 minutes; Render then gives you a link like
   `https://ai-code-review-practice-system.onrender.com`.

Free-plan notes: the site sleeps after 15 minutes without visitors and takes about a minute
to wake up, so open it once before a demo. The free disk is temporary, so accounts and
submissions reset when the service restarts or redeploys (the demo problems and demo
account are recreated automatically).

**Security note:** submitted code runs in a separate JVM with a time limit, memory limit and
output cap, inside a container as a non-root user, but it is not a full sandbox. That is fine
for a class demo; a production judge would use isolated containers per submission.

## AI review setup (optional)

The app works without a key, using the offline rule checker. With keys, it tries
**Groq first, then Gemini, then the offline checker** (if one fails, the next is used).

**Groq (fastest):**
1. Create a free key at console.groq.com → API Keys.
2. Set it before starting the app:
   - Windows: `setx GROQ_API_KEY "your-key"` (then open a new terminal)
   - macOS/Linux: `export GROQ_API_KEY="your-key"`
3. Optional: `GROQ_MODEL` (default `openai/gpt-oss-120b`).

**Gemini:**
1. Get a free API key from Google AI Studio.
2. Set `GEMINI_API_KEY` the same way.
3. Optional: `GEMINI_MODEL` (default `gemini-3.5-flash-lite`).

**On Render:** service → Environment → add `GROQ_API_KEY` → Save (it redeploys).

**Never commit your API key.** `ModelChecker` lists the models your key can use.

**What is sent to the AI** (`ReviewRequest`): the student's code, the problem title and
description, the compiler error (if any) and a summary of test verdicts. Hidden test
inputs/outputs, student names and emails are never sent.

## Self-tests

`run.bat test` / `sh run.sh test` runs:

- `ModuleSelfTest` – 29 checks for problems, test cases, filters, transactions, cascade delete, file export/import
- `ReviewSelfTest` – 8 checks for the Gemini and Groq response parsers and the fallback chain
- `IntegrationSelfTest` – 19 end-to-end checks: register, login, submit correct / wrong / non-compiling / crashing / infinite-loop code, dashboard, leaderboard, report file

## Changes made while combining the modules

- **Shared database:** Member 5's in-memory `ArrayList` storage and Member 1's in-memory lists now use the H2 database, so data survives restarts.
- **One data model:** Member 1's and Member 5's separate `Problem` classes were replaced by Member 2's `Problem`/`TestCase` (difficulty EASY/MEDIUM/HARD).
- **Real submissions:** Member 1's "Accepted / Attempted" choice was replaced with a code editor; Submit runs Member 3's engine on Member 2's hidden test cases.
- **Passwords:** stored as salted PBKDF2 hashes instead of plain text.
- **Engine (Member 3):** each submission runs in its own temporary folder; class name is detected from the code; output readers start before input is written (no deadlock); Windows line endings and trailing spaces no longer cause false "wrong answer"; uses the same JDK that runs the app.
- **AI review (Member 4):** fixed response parsing that cut feedback short whenever the text contained `",`; requests now escape all characters correctly; added problem/test context, timeouts, and an offline fallback reviewer; API key sent in a header, not the URL.
- **Leaderboard (Member 5):** ranks by best score per problem, so resubmitting the same problem cannot inflate rank. Current level now needs both a good average and enough problems solved.
- **File handling (Member 2):** export/import the whole problem bank to a `.properties` file (admin menu options 9 and 10).
- **Website (new):** servlets for every module, a browser UI, and Docker/Render files for a live link.
- **Package names:** everything moved under `com.bcorps.*` (Member 4's and Member 5's `com.codereview` packages collided).
