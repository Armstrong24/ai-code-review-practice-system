package com.bcorps.learning;

import com.bcorps.learning.TutorialNote.Link;
import com.bcorps.learning.TutorialNote.Section;
import com.bcorps.learning.TutorialNote.Video;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Short revision notes for every track, plus "one shot" YouTube full courses
 * (one Hindi and one English where possible) for a quick revision.
 * All video ids were checked against YouTube's oEmbed service on 2026-10-21.
 */
public final class TutorialData {

    private static final Map<Track, TutorialNote> NOTES = new EnumMap<>(Track.class);

    private TutorialData() { }

    public static TutorialNote forTrack(Track track) {
        return NOTES.get(track);
    }

    private static Section s(String title, String markdown) {
        return new Section(title, markdown.strip());
    }

    private static Video v(String title, String channel, String language, String duration, String id) {
        return new Video(title, channel, language, duration, id);
    }

    static {
        NOTES.put(Track.JAVA, new TutorialNote(Track.JAVA,
                "Java is compiled to bytecode and runs on the JVM, so the same program runs on any computer.",
                List.of(
                        s("Program structure", """
                                Every program needs a class and a `main` method.
                                ```
                                public class Main {
                                    public static void main(String[] args) {
                                        System.out.println("Hello");
                                    }
                                }
                                ```
                                - `javac Main.java` compiles to `Main.class` (bytecode)
                                - `java Main` runs it on the JVM
                                """),
                        s("Data types and variables", """
                                - 8 primitives: `byte, short, int, long, float, double, char, boolean`
                                - `int` is always 32 bits, `long` 64 bits, `double` 64 bits
                                - `String` is a class, and strings are **immutable**
                                - `7 / 2` is `3` (integer division); `7 / 2.0` is `3.5`
                                - `x++` uses the value, then adds 1; `++x` adds 1 first
                                """),
                        s("Input with Scanner", """
                                ```
                                Scanner sc = new Scanner(System.in);
                                int n = sc.nextInt();
                                String word = sc.next();
                                String line = sc.nextLine();
                                ```
                                """),
                        s("OOP in one minute", """
                                - **Class**: a blueprint. **Object**: made with `new`
                                - **Constructor**: same name as the class, no return type
                                - **Encapsulation**: `private` fields with getters and setters
                                - **Inheritance**: `class Dog extends Animal` (one parent only)
                                - **Interfaces**: `class A implements X, Y` (many allowed)
                                - **Polymorphism**: overloading (compile time), overriding (run time)
                                """),
                        s("Collections", """
                                | Need | Use |
                                |---|---|
                                | Ordered list, duplicates OK | `ArrayList` |
                                | No duplicates | `HashSet` |
                                | Key to value | `HashMap` |
                                | Sorted keys | `TreeMap` |
                                """),
                        s("Exceptions", """
                                - **Checked** (must handle): `IOException`, `SQLException`
                                - **Unchecked**: `NullPointerException`, `ArithmeticException`
                                ```
                                try { risky(); } catch (IOException e) { ... } finally { cleanUp(); }
                                ```
                                """),
                        s("Exam traps", """
                                - `10 + 20 + "Java"` is `30Java`, but `"Java" + 10 + 20` is `Java1020`
                                - Compare strings with `equals()`, not `==`
                                - `s.toUpperCase()` does not change `s`; save the result
                                - Array size is `a.length`, String size is `s.length()`
                                """)),
                List.of(v("Java Tutorial for Beginners | Learn Java in 2 Hours", "Apna College", "Hindi", "2 h", "UmnCZ7-9yDY"),
                        v("Java Full Course for free", "Bro Code", "English", "12 h", "xk4_1vDrzzo"),
                        v("Java Programming for Beginners – Full Course", "freeCodeCamp.org", "English", "4 h", "A74TOX803D0")),
                List.of(new Link("Official Java tutorials (Oracle)", "https://docs.oracle.com/javase/tutorial/"))));

        NOTES.put(Track.PYTHON, new TutorialNote(Track.PYTHON,
                "Python is interpreted and easy to read. Blocks are marked by indentation, not braces.",
                List.of(
                        s("Basics", """
                                ```
                                name = input("Your name: ")
                                age = int(input("Age: "))
                                print(f"Hi {name}, next year you'll be {age + 1}")
                                ```
                                - No type declarations: `x = 5` then `x = "five"` is fine
                                - Comments start with `#`
                                """),
                        s("Operators", """
                                - `/` always gives a float: `5 / 2` is `2.5`
                                - `//` floor division: `7 // 2` is `3`
                                - `%` remainder, `**` power: `2 ** 3` is `8`
                                - `and`, `or`, `not` instead of `&&`, `||`, `!`
                                """),
                        s("Data structures", """
                                | Type | Example | Mutable? |
                                |---|---|---|
                                | list | `[1, 2, 3]` | yes |
                                | tuple | `(1, 2)` or `(5,)` | no |
                                | set | `{1, 2}` (unique) | yes |
                                | dict | `{"a": 1}` | yes |
                                | str | `"hi"` | no |
                                - Negative index: `x[-1]` is the last item
                                - Slicing: `s[1:4]` takes indexes 1, 2, 3
                                - `d.get("k")` returns `None` if the key is missing
                                """),
                        s("Loops and comprehensions", """
                                ```
                                for i in range(1, 10, 3):   # 1 4 7
                                    print(i)
                                squares = [i * i for i in range(4)]   # [0, 1, 4, 9]
                                ```
                                """),
                        s("Functions", """
                                ```
                                def area(w, h=1):
                                    return w * h
                                ```
                                - A function with no `return` gives `None`
                                - `lambda x: x * 2` is a small anonymous function
                                """),
                        s("Exceptions and classes", """
                                ```
                                try:
                                    n = int("abc")
                                except ValueError:
                                    print("Not a number")

                                class Dog:
                                    def __init__(self, name):
                                        self.name = name
                                ```
                                """),
                        s("Exam traps", """
                                - Falsy values: `0`, `""`, `[]`, `{}`, `None`
                                - `"ab" * 3` is `"ababab"`
                                - Swap in one line: `a, b = b, a`
                                """)),
                List.of(v("Python Tutorial for Beginners - Full Course (with Notes & Practice Questions)", "Apna College", "Hindi", "11 h", "ERCMXc8x7mc"),
                        v("Learn Python - Full Course for Beginners", "freeCodeCamp.org", "English", "4.5 h", "rfscVS0vtbw"),
                        v("Python Full Course for free", "Bro Code", "English", "12 h", "ix9cRaBkVe0")),
                List.of(new Link("Official Python tutorial", "https://docs.python.org/3/tutorial/"))));

        NOTES.put(Track.C, new TutorialNote(Track.C,
                "C is a small, fast language close to the hardware. You manage memory yourself.",
                List.of(
                        s("Program structure", """
                                ```
                                #include <stdio.h>
                                int main(void) {
                                    int n;
                                    scanf("%d", &n);
                                    printf("%d\\n", n * 2);
                                    return 0;
                                }
                                ```
                                """),
                        s("Format specifiers", """
                                | Type | printf / scanf |
                                |---|---|
                                | int | `%d` |
                                | float / double | `%f` / `%lf` |
                                | char | `%c` |
                                | string | `%s` |
                                - `scanf` needs the address: `&n` (but not for char arrays)
                                """),
                        s("Pointers", """
                                ```
                                int a = 10;
                                int *p = &a;   // p holds the address of a
                                *p = 20;       // a is now 20
                                ```
                                - `&` gives an address, `*` reads or writes the value there
                                - Struct through a pointer: `p->field` is `(*p).field`
                                """),
                        s("Arrays and strings", """
                                - `int arr[5];` uses indexes 0 to 4
                                - Strings end with `'\\0'`: `char s[] = "abc";` is 4 bytes
                                - `strlen` (in string.h) does not count the `'\\0'`
                                """),
                        s("Dynamic memory", """
                                ```
                                int *a = malloc(n * sizeof(int));   // returns void *
                                if (a == NULL) return 1;
                                free(a);
                                ```
                                - Always `free` what you `malloc`
                                """),
                        s("Control flow and storage", """
                                - `do { } while (cond);` runs at least once
                                - `switch` falls through without `break`
                                - `static` local variables keep their value between calls
                                - Arguments are passed **by value** (pass a pointer to change the caller's variable)
                                """)),
                List.of(v("C Language Tutorial for Beginners (with Notes & Practice Questions)", "Apna College", "Hindi", "10 h", "irqbmMNs2Bo"),
                        v("C Language Tutorial For Beginners In Hindi (With Notes)", "CodeWithHarry", "Hindi", "15 h", "ZSPZob_1TOk"),
                        v("C Programming Full Course for free", "Bro Code", "English", "7 h", "87SH2Cn0s9A")),
                List.of(new Link("C reference (cppreference)", "https://en.cppreference.com/w/c"))));

        NOTES.put(Track.CPP, new TutorialNote(Track.CPP,
                "C++ adds classes, references and the Standard Template Library (STL) to C.",
                List.of(
                        s("Program structure", """
                                ```
                                #include <bits/stdc++.h>
                                using namespace std;
                                int main() {
                                    int n;
                                    cin >> n;
                                    cout << n * 2 << endl;
                                }
                                ```
                                - `cout <<` prints, `cin >>` reads, `::` is scope resolution
                                - `endl` prints a newline and flushes; `"\\n"` is faster
                                """),
                        s("References and pointers", """
                                ```
                                int a = 5;
                                int &r = a;   // r is another name for a
                                r = 10;       // a is now 10
                                void inc(int &x) { x++; }   // pass by reference
                                ```
                                - `new` / `delete` replace `malloc` / `free`
                                """),
                        s("OOP", """
                                - Class members are `private` by default (struct: `public`)
                                - Constructor: class name, no return type. Destructor: `~Name()`
                                - Inheritance: `class B : public A { };`
                                - `virtual` functions give run-time polymorphism
                                - Pure virtual: `virtual void draw() = 0;` makes the class abstract
                                - Overloading: same name, different parameters
                                """),
                        s("STL cheat sheet", """
                                | Container | Use for |
                                |---|---|
                                | `vector<int>` | dynamic array, `push_back`, `size()` |
                                | `map<string,int>` | sorted key to value |
                                | `unordered_map` | fast key to value |
                                | `set<int>` | sorted unique values |
                                | `stack`, `queue`, `priority_queue` | LIFO, FIFO, max-heap |
                                - `sort(v.begin(), v.end());`
                                """),
                        s("Templates", """
                                ```
                                template <typename T>
                                T biggest(T a, T b) { return a > b ? a : b; }
                                ```
                                One function, any type: generic programming.
                                """)),
                List.of(v("C++ in One Shot | Complete C++ Programming Tutorial for Beginners", "College Wallah", "Hindi", "One shot", "e7sAf4SbS_g"),
                        v("C++ Tutorial for Beginners - Full Course", "freeCodeCamp.org", "English", "4 h", "vLnPwxZdW4Y"),
                        v("C++ Full Course for free", "Bro Code", "English", "6 h", "-TkoO8Z07hI")),
                List.of(new Link("C++ reference (cppreference)", "https://en.cppreference.com/w/cpp"))));

        NOTES.put(Track.JAVASCRIPT, new TutorialNote(Track.JAVASCRIPT,
                "JavaScript runs in every browser and on servers with Node.js. It is dynamically typed.",
                List.of(
                        s("Variables", """
                                - `let`: block-scoped, can be reassigned
                                - `const`: block-scoped, cannot be reassigned (object properties can still change)
                                - `var`: old style, function-scoped and hoisted (`undefined` before its line)
                                """),
                        s("Types and coercion", """
                                - `1 + "2"` is `"12"` (joins text), `"5" - 2` is `3` (converts to number)
                                - `===` checks value and type; avoid `==`
                                - `typeof null` is `"object"`, `typeof []` is `"object"`
                                - Falsy: `0, "", null, undefined, NaN, false`
                                - `NaN === NaN` is `false`; use `Number.isNaN()`
                                """),
                        s("Functions", """
                                ```
                                function add(a, b) { return a + b; }
                                const add2 = (a, b) => a + b;
                                ```
                                """),
                        s("Arrays", """
                                ```
                                const nums = [1, 2, 3, 4];
                                nums.push(5);                        // add at the end
                                nums.map(n => n * 2);                // [2, 4, 6, 8, 10]
                                nums.filter(n => n % 2 === 0);       // [2, 4]
                                nums.reduce((sum, n) => sum + n, 0); // 15
                                ```
                                """),
                        s("DOM and events", """
                                ```
                                const btn = document.getElementById("save");
                                btn.addEventListener("click", () => alert("Saved"));
                                ```
                                """),
                        s("Async code", """
                                ```
                                async function load() {
                                    const res = await fetch("/api/problems");
                                    const data = await res.json();   // like JSON.parse
                                }
                                ```
                                - `await` works inside `async` functions
                                - `JSON.parse` text to object, `JSON.stringify` object to text
                                """)),
                List.of(v("JavaScript Tutorial (2024) for Beginners to Pro (with Notes, Projects & Practice Questions)", "Apna College", "Hindi", "12 h", "VlPiVmYuoqw"),
                        v("Javascript in 1 shot in Hindi | part 1", "Chai aur Code", "Hindi", "9 h", "sscX432bMZo"),
                        v("Learn JavaScript - Full Course for Beginners", "freeCodeCamp.org", "English", "3.5 h", "PkZNo7MFNFg")),
                List.of(new Link("MDN JavaScript Guide", "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide"))));

        NOTES.put(Track.SQL, new TutorialNote(Track.SQL,
                "SQL asks questions of relational databases (tables of rows and columns). This project itself uses SQL with H2.",
                List.of(
                        s("Reading data", """
                                ```
                                SELECT DISTINCT city FROM students
                                WHERE marks >= 60 AND name LIKE 'A%'
                                ORDER BY marks DESC;
                                ```
                                - `LIKE 'A%'` starts with A, `'%a'` ends with a
                                - `BETWEEN 70 AND 80` includes both ends
                                - Check for missing values with `IS NULL`, never `= NULL`
                                """),
                        s("Order of a query", """
                                `SELECT` columns `FROM` table `WHERE` row filter `GROUP BY` columns `HAVING` group filter `ORDER BY` columns
                                ```
                                SELECT city, COUNT(*) AS total
                                FROM students
                                GROUP BY city
                                HAVING COUNT(*) > 5;
                                ```
                                """),
                        s("Aggregate functions", """
                                `COUNT(*)`, `SUM()`, `AVG()`, `MIN()`, `MAX()` combine many rows into one value.
                                """),
                        s("Changing data", """
                                ```
                                INSERT INTO students (name, marks) VALUES ('Riya', 91);
                                UPDATE students SET marks = 95 WHERE name = 'Riya';
                                DELETE FROM students WHERE marks < 35;
                                ```
                                - `DELETE` removes rows, `TRUNCATE` removes all rows, `DROP` removes the table
                                """),
                        s("Keys and joins", """
                                - **Primary key**: unique and not NULL
                                - **Foreign key**: points at another table's primary key
                                | Join | Returns |
                                |---|---|
                                | `INNER JOIN` | only matching rows |
                                | `LEFT JOIN` | all left rows + matches (NULL if none) |
                                | `RIGHT JOIN` | all right rows + matches |
                                """),
                        s("Command groups", """
                                - **DDL** (structure): `CREATE, ALTER, DROP, TRUNCATE`
                                - **DML** (data): `SELECT, INSERT, UPDATE, DELETE`
                                - **DCL**: `GRANT, REVOKE`. **TCL**: `COMMIT, ROLLBACK`
                                """)),
                List.of(v("SQL - Complete Course in 3 Hours | SQL One Shot using MySQL", "Apna College", "Hindi", "3 h", "hlGoQC332VM"),
                        v("SQL Tutorial - Full Database Course for Beginners", "freeCodeCamp.org", "English", "4 h", "HXV3zeQKqGY"),
                        v("SQL Full Course for free", "Bro Code", "English", "3 h", "5OdVJbNCSso")),
                List.of(new Link("SQLBolt interactive lessons", "https://sqlbolt.com/"))));

        NOTES.put(Track.OLYMPIAD, new TutorialNote(Track.OLYMPIAD,
                "The Indian Computing Olympiad starts with ZIO (pen-and-paper puzzles) and ZCO (coding), then INOI, the training camp, and the IOI.",
                List.of(
                        s("The Olympiad path in India", """
                                1. **ZIO**: written, logic and counting problems, no coding
                                2. **ZCO**: online coding contest
                                3. **INOI**: national round (C++ only)
                                4. **IOITC**: training camp, top students
                                5. **IOI**: four students represent India
                                """),
                        s("Counting tricks", """
                                - Handshakes among n people: `n(n-1)/2`
                                - Subsets of n items: `2^n`
                                - Grid paths with r rights and d downs: `(r+d)! / (r! d!)`
                                - Pigeonhole: n+1 items in n boxes means a box holds two
                                - Sum 1..n: `n(n+1)/2`
                                """),
                        s("Number theory", """
                                - GCD by Euclid: `gcd(a, b) = gcd(b, a % b)`
                                - Trailing zeros of n!: `n/5 + n/25 + n/125 + ...`
                                - Remainders: work with `% m` at every step to keep numbers small
                                - Binary: 13 = 8 + 4 + 1 = `1101`
                                """),
                        s("Recursion and DP", """
                                - Stairs with 1 or 2 steps: `ways(n) = ways(n-1) + ways(n-2)` (Fibonacci)
                                - Write the small cases first, then find the pattern
                                - Dynamic programming = recursion + remembering answers
                                """),
                        s("Data structures and algorithms", """
                                | Topic | Key idea |
                                |---|---|
                                | Stack | last in, first out (DFS, brackets) |
                                | Queue | first in, first out (BFS) |
                                | Binary search | halve the range, O(log n) |
                                | Sorting | bubble O(n^2), merge sort O(n log n) |
                                | Trees | n nodes, n-1 edges |
                                | Greedy | best local choice (works for coins 1, 5, 10) |
                                """),
                        s("Practise here", """
                                - Take the **Olympiad exam** (20 questions, 30 minutes)
                                - Then try the **Hard coding problems** on the Problems page: Longest increasing subsequence, Longest valid parentheses, Count inversions and Shortest path in a grid
                                """)),
                List.of(v("ZIO/ZCO 2026 Complete Guide | Syllabus, Strategy, Selection Process & Benefits", "Vedantu Olympiad School", "Hindi", "Guide", "bjY10W1SCz4"),
                        v("Indian Computing Olympiads | ZCO, ZIO, INOI, and IOI", "Samik Goyal", "English", "Guide", "C5PfBxfzE5A")),
                List.of(new Link("Indian Computing Olympiad (IARCS)", "https://www.iarcs.org.in/inoi/"),
                        new Link("ICO preparatory material (CodeChef)", "https://www.codechef.com/ioi/prepare"))));
    }
}
