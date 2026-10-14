# AI-Powered Code Review & Programming Practice System

A Java desktop platform where students register, solve programming problems in Java,
get their code compiled and graded against hidden test cases, and receive AI feedback
on their code. Progress, scores and a leaderboard are stored in an H2 database.

Group project by five members. Each module lives in its own package.

## How it works

```
Student (Swing app, Member 1)
   │  logs in, picks a problem, writes Java code, presses Submit
   ▼
Problem bank (Member 2) ── gives the problem + sample AND hidden test cases
   ▼
Execution engine (Member 3) ── compile → run each test (5 s limit) → compare → score
   ▼
AI review (Member 4) ── Gemini explains errors and suggests improvements
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
| 4 | AI Code Review & Feedback | `com.bcorps.review` | `CodeReviewer` (interface), `AICodeReviewer` (Gemini), `RuleBasedCodeReviewer`, `ReviewService` |
| 5 | Progress, Database & Integration | `com.bcorps.progress`, `com.bcorps.database`, `com.bcorps.app` | `SubmissionDAO`, `ProgressService`, `DashboardService`, `LeaderboardService`, `DatabaseManager`, `Platform`, `SubmissionService` |

### Database tables (`DatabaseManager`)

- `students` – id, name, email (unique), password_hash, password_salt, created_at
- `problems` – id, title, description, input_format, output_format, constraints_text, difficulty (EASY/MEDIUM/HARD), category
- `test_cases` – id, problem_id → problems, input_data, expected_output, is_sample
- `submissions` – id, student_id → students, problem_id → problems, source_code, status, score, passed_tests, total_tests, execution_time_ms, ai_review, submitted_at

## Requirements

- **JDK 17 or newer** (a full JDK, not just a JRE: the engine needs `javac` to compile submissions)
- `lib/h2-2.5.252.jar` (included)

## Run it

**Windows**
```
run.bat           student app
run.bat admin     problem & test-case admin console (Member 2)
run.bat test      run every self-test
```

**macOS / Linux**
```
./run.sh
./run.sh admin
./run.sh test
```

**Eclipse:** File → Import → Existing Projects into Workspace → select this folder.
Run `com.bcorps.app.Main` (add `admin` or `test` as a program argument if needed).
If Eclipse shows a JRE error, right-click the project → Properties → Java Build Path → Libraries and choose your installed JDK (17+).

On first start the app adds 5 demo problems with 24 test cases.

## AI review setup (optional)

The app works without a key, using the offline rule checker. For Gemini feedback:

1. Get a free API key from Google AI Studio.
2. Set it as an environment variable before starting the app:
   - Windows: `setx GEMINI_API_KEY "your-key"` (then open a new terminal)
   - macOS/Linux: `export GEMINI_API_KEY="your-key"`
3. Optional: `GEMINI_MODEL` (default `gemini-3.5-flash-lite`).

**Never commit your API key.** `ModelChecker` lists the models your key can use.

**What is sent to the AI** (`ReviewRequest`): the student's code, the problem title and
description, the compiler error (if any) and a summary of test verdicts. Hidden test
inputs/outputs, student names and emails are never sent.

## Self-tests

`run.bat test` / `./run.sh test` runs:

- `ModuleSelfTest` – 27 checks for problems, test cases, filters, transactions, cascade delete, file export/import
- `ReviewSelfTest` – 6 checks for the Gemini response parser and offline fallback
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
- **Package names:** everything moved under `com.bcorps.*` (Member 4's and Member 5's `com.codereview` packages collided).
