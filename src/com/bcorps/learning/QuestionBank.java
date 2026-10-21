package com.bcorps.learning;

import static com.bcorps.problems.Difficulty.EASY;
import static com.bcorps.problems.Difficulty.HARD;
import static com.bcorps.problems.Difficulty.MEDIUM;

import com.bcorps.problems.Difficulty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Demo exam questions: 20 MCQs for each language and 20 Olympiad-style
 * reasoning questions (140 in total). They are copied into the
 * exam_questions table on first start (see ExamRepository.insertMissing).
 *
 * Format of q(...): track, difficulty, question, code snippet (or null),
 * index of the correct option (0-3), explanation, then the four options.
 */
public final class QuestionBank {

    private static final List<McqQuestion> ALL = new ArrayList<>();

    private QuestionBank() { }

    public static List<McqQuestion> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static List<McqQuestion> forTrack(Track track) {
        return ALL.stream().filter(q -> q.getTrack() == track).toList();
    }

    private static void q(Track track, Difficulty level, String question, String code, int correct,
                          String explanation, String a, String b, String c, String d) {
        ALL.add(new McqQuestion(0, track, level, question, code, List.of(a, b, c, d), correct, explanation));
    }

    static {
        java();
        python();
        c();
        cpp();
        javascript();
        sql();
        olympiad();
    }

    // ------------------------------------------------------------------ Java
    private static void java() {
        Track t = Track.JAVA;
        q(t, EASY, "What is the size of an int in Java?", null, 1,
                "An int is always 32 bits in Java, on every platform. That is part of 'write once, run anywhere'.",
                "16 bits", "32 bits", "64 bits", "It depends on the computer");
        q(t, EASY, "Which is the correct signature of the main method?", null, 0,
                "The JVM looks for a public static void method called main that takes a String array.",
                "public static void main(String[] args)", "public void main(String args)",
                "static int main(String[] args)", "public static main(String args[])");
        q(t, EASY, "What does this print?", "System.out.println(10 + 20 + \"Java\");", 2,
                "Java works left to right: 10 + 20 is 30 first, then \"Java\" is joined to it.",
                "1020Java", "Java30", "30Java", "Compilation error");
        q(t, EASY, "What does this print?", "System.out.println(\"Java\" + 10 + 20);", 0,
                "Once a String comes first, every + after it joins text, so 10 and 20 are added as text.",
                "Java1020", "Java30", "30Java", "Compilation error");
        q(t, EASY, "Which keyword makes one class inherit from another?", null, 1,
                "A class extends another class. implements is used for interfaces.",
                "implements", "extends", "inherits", "super");
        q(t, EASY, "Which of these is NOT a primitive type?", null, 3,
                "String is a class (an object type). byte, char and boolean are among the 8 primitives.",
                "byte", "char", "boolean", "String");
        q(t, MEDIUM, "What is the default value of an int field (instance variable) that is never assigned?", null, 0,
                "Fields get default values: 0 for numbers, false for boolean, null for objects. Local variables do not.",
                "0", "null", "-1", "It causes a compilation error");
        q(t, MEDIUM, "For two String objects, what is the difference between == and equals()?", null, 2,
                "== checks if both variables point to the same object; equals() checks if the text is the same.",
                "Both compare the text", "== compares the text, equals() compares references",
                "== compares references, equals() compares the text", "Both compare references");
        q(t, MEDIUM, "What does this print?", "int x = 5;\nSystem.out.println(x++ + ++x);", 2,
                "x++ gives 5 and then x becomes 6; ++x makes x 7 and gives 7. 5 + 7 = 12.",
                "10", "11", "12", "13");
        q(t, EASY, "Which collection does NOT allow duplicate elements?", null, 1,
                "A Set stores each element only once. Lists such as ArrayList allow duplicates.",
                "ArrayList", "HashSet", "LinkedList", "Vector");
        q(t, EASY, "What does the final keyword mean on a variable?", null, 0,
                "A final variable can be assigned only once; after that it cannot be changed.",
                "Its value cannot be reassigned", "It is deleted after use",
                "It can only be used inside main", "It is shared by all threads");
        q(t, MEDIUM, "Which of these is a checked exception?", null, 2,
                "IOException must be caught or declared with throws. The others are unchecked RuntimeExceptions.",
                "NullPointerException", "ArithmeticException", "IOException", "ArrayIndexOutOfBoundsException");
        q(t, EASY, "What does this print?", "int[] a = {1, 2, 3};\nSystem.out.println(a.length);", 1,
                "length is a field of every array and gives the number of elements: 3.",
                "2", "3", "4", "Compilation error");
        q(t, EASY, "Which special method runs automatically when an object is created with new?", null, 3,
                "A constructor has the same name as the class and runs when the object is created.",
                "A static method", "finalize()", "main()", "Constructor");
        q(t, MEDIUM, "Which statement about classes and interfaces in Java is true?", null, 0,
                "Java avoids multiple inheritance of classes, but a class may implement any number of interfaces.",
                "A class can implement many interfaces but extend only one class",
                "A class can extend many classes", "An interface can have constructors",
                "A class cannot implement more than one interface");
        q(t, MEDIUM, "What does this print?", "String s = \"hello\";\ns.toUpperCase();\nSystem.out.println(s);", 1,
                "Strings are immutable. toUpperCase() returns a new String, and the result was not saved.",
                "HELLO", "hello", "Hello", "Compilation error");
        q(t, EASY, "What does the javac compiler produce?", null, 2,
                "javac turns .java source into bytecode (.class files) that the JVM runs.",
                "A machine-code .exe file", "Another .java file", "Bytecode in a .class file", "Assembly code");
        q(t, MEDIUM, "What does this print?",
                "for (int i = 0; i < 3; i++) {\n    if (i == 1) continue;\n    System.out.print(i);\n}", 3,
                "continue skips the rest of the loop when i is 1, so only 0 and 2 are printed.",
                "012", "01", "12", "02");
        q(t, EASY, "Which access modifier is the most restrictive?", null, 0,
                "private members are visible only inside their own class.",
                "private", "protected", "default (no modifier)", "public");
        q(t, EASY, "What does this print?", "System.out.println(7 / 2);", 1,
                "Both numbers are ints, so Java does integer division and drops the .5.",
                "3.5", "3", "4", "3.0");
    }

    // ------------------------------------------------------------------ Python
    private static void python() {
        Track t = Track.PYTHON;
        q(t, EASY, "What does this print?", "print(type(5 / 2))", 1,
                "In Python 3 the / operator always gives a float: 5 / 2 is 2.5.",
                "<class 'int'>", "<class 'float'>", "<class 'str'>", "<class 'complex'>");
        q(t, EASY, "What does this print?", "print(7 // 2)", 0,
                "// is floor division: it divides and rounds down to a whole number.",
                "3", "3.5", "4", "3.0");
        q(t, EASY, "What does this print?", "print(2 ** 3)", 2,
                "** is the power operator: 2 to the power 3 is 8.",
                "6", "9", "8", "5");
        q(t, EASY, "Which of these types is mutable (can be changed after creation)?", null, 3,
                "Lists can be changed in place. tuple, str and int are immutable.",
                "tuple", "str", "int", "list");
        q(t, EASY, "What does this print?", "x = [1, 2, 3]\nprint(x[-1])", 1,
                "Negative indexes count from the end, so x[-1] is the last element.",
                "1", "3", "IndexError", "-1");
        q(t, EASY, "What does this print?", "print(\"ab\" * 3)", 0,
                "Multiplying a string by a number repeats it.",
                "ababab", "ab3", "aaabbb", "TypeError");
        q(t, EASY, "Which keyword is used to define a function?", null, 2,
                "Functions are defined with def, for example def greet():",
                "function", "func", "def", "define");
        q(t, MEDIUM, "What does this print?", "print(len({1, 2, 2, 3}))", 1,
                "A set keeps only unique values, so {1, 2, 2, 3} becomes {1, 2, 3}.",
                "4", "3", "2", "TypeError");
        q(t, MEDIUM, "What does this print?", "print([i * i for i in range(4)])", 3,
                "range(4) is 0, 1, 2, 3, so the squares are 0, 1, 4, 9.",
                "[1, 4, 9, 16]", "[0, 1, 4, 9, 16]", "[1, 4, 9]", "[0, 1, 4, 9]");
        q(t, MEDIUM, "What does this print?", "s = \"python\"\nprint(s[1:4])", 2,
                "Slicing takes index 1 up to but not including 4: y, t, h.",
                "pyt", "ytho", "yth", "pyth");
        q(t, MEDIUM, "What does this print?", "d = {\"a\": 1}\nprint(d.get(\"z\"))", 0,
                "get() returns None for a missing key instead of raising KeyError.",
                "None", "KeyError", "0", "An empty string");
        q(t, EASY, "How do you write a single-line comment in Python?", null, 1,
                "Everything after # on a line is a comment.",
                "// comment", "# comment", "/* comment */", "-- comment");
        q(t, MEDIUM, "What does this print?", "print(bool(\"\"))", 2,
                "Empty values such as \"\", 0, [] and None are falsy.",
                "True", "None", "False", "Error");
        q(t, EASY, "What does a function return if it has no return statement?", null, 0,
                "A Python function without return gives back None.",
                "None", "0", "An empty string", "It causes an error");
        q(t, MEDIUM, "What does this print?", "a, b = 1, 2\na, b = b, a\nprint(a, b)", 3,
                "Tuple unpacking swaps the two values in one line.",
                "1 2", "1 1", "2 2", "2 1");
        q(t, MEDIUM, "Which of these creates a tuple with one element?", null, 1,
                "The comma makes the tuple. (5) is just the number 5 in brackets.",
                "(5)", "(5,)", "[5]", "{5}");
        q(t, EASY, "Which keywords are used to handle exceptions?", null, 2,
                "Python uses try and except (Java and C++ use try and catch).",
                "try / catch", "do / except", "try / except", "catch / finally");
        q(t, EASY, "What does this print?", "print(10 % 3)", 0,
                "% gives the remainder: 10 = 3 x 3 + 1.",
                "1", "3", "0", "3.33");
        q(t, MEDIUM, "What does this print?", "for i in range(1, 10, 3):\n    print(i, end=\" \")", 3,
                "range(1, 10, 3) starts at 1 and adds 3 each time while below 10: 1, 4, 7.",
                "1 3 6 9", "1 4 7 10", "3 6 9", "1 4 7");
        q(t, EASY, "What does Python use to mark a block of code (for example the body of an if)?", null, 1,
                "Python uses indentation instead of curly braces.",
                "Curly braces { }", "Indentation", "begin / end keywords", "Semicolons");
    }

    // ------------------------------------------------------------------ C
    private static void c() {
        Track t = Track.C;
        q(t, EASY, "What does this print?", "printf(\"%d\", (int) sizeof(char));", 0,
                "The C standard defines sizeof(char) as exactly 1.",
                "1", "2", "4", "It depends on the compiler");
        q(t, EASY, "Which format specifier prints a float?", null, 2,
                "%f prints float and double values; %d is for int.",
                "%d", "%c", "%f", "%s");
        q(t, EASY, "Which operator gives the address of a variable?", null, 1,
                "&x is the address of x. * goes the other way and reads the value at an address.",
                "*", "&", "->", "#");
        q(t, MEDIUM, "What does this print?", "int a = 10;\nint *p = &a;\n*p = 20;\nprintf(\"%d\", a);", 3,
                "p points to a, so writing through *p changes a itself.",
                "10", "The address of a", "Compilation error", "20");
        q(t, EASY, "Which character marks the end of a string in C?", null, 0,
                "C strings are char arrays ending with the null character '\\0'.",
                "'\\0'", "'\\n'", "'.'", "EOF");
        q(t, EASY, "Which header file declares printf and scanf?", null, 2,
                "Standard input and output functions live in stdio.h.",
                "conio.h", "stdlib.h", "stdio.h", "string.h");
        q(t, MEDIUM, "What does this print?", "int i = 0;\nwhile (i < 5) i += 2;\nprintf(\"%d\", i);", 1,
                "i goes 0, 2, 4, 6. The loop stops when i is 6.",
                "5", "6", "4", "7");
        q(t, MEDIUM, "What type does malloc() return?", null, 3,
                "malloc returns a void pointer, which can be assigned to any pointer type.",
                "int *", "char *", "Always NULL", "void *");
        q(t, EASY, "For int arr[5]; what is the index of the last element?", null, 1,
                "Indexes start at 0, so five elements use 0 to 4.",
                "5", "4", "6", "1");
        q(t, MEDIUM, "What does this print?", "printf(\"%d\", 5 > 3 && 2 > 4);", 0,
                "5 > 3 is true but 2 > 4 is false, and true && false is 0 in C.",
                "0", "1", "true", "false");
        q(t, EASY, "Which loop always runs its body at least once?", null, 2,
                "do-while checks the condition after running the body.",
                "for", "while", "do-while", "None of them");
        q(t, HARD, "What does this print?",
                "int x = 2;\nswitch (x) {\n    case 1: printf(\"A\");\n    case 2: printf(\"B\");\n    case 3: printf(\"C\");\n}", 3,
                "Without break, execution falls through from case 2 into case 3.",
                "B", "A", "ABC", "BC");
        q(t, EASY, "Which function releases memory that was allocated with malloc?", null, 1,
                "free() gives dynamically allocated memory back. delete is C++.",
                "delete", "free()", "remove()", "dealloc()");
        q(t, EASY, "What does strlen(\"hello\") return?", null, 0,
                "strlen counts characters before '\\0', so the answer is 5.",
                "5", "6", "4", "Error");
        q(t, MEDIUM, "What does this print?", "int a = 5;\nprintf(\"%d\", a << 1);", 2,
                "Shifting left by 1 bit multiplies by 2.",
                "5", "2", "10", "25");
        q(t, MEDIUM, "How do you reach a struct member through a pointer p?", null, 1,
                "p->member is short for (*p).member.",
                "p.member", "p->member", "p::member", "*p.member");
        q(t, MEDIUM, "In call by value, what happens when the function changes its parameter?", null, 0,
                "The function gets a copy, so the caller's variable stays the same.",
                "The caller's variable does not change", "The caller's variable changes too",
                "The program crashes", "It causes a compilation error");
        q(t, HARD, "What does this print?", "char s[] = \"abc\";\nprintf(\"%d\", (int) sizeof(s));", 1,
                "The array holds a, b, c and the hidden '\\0', so it is 4 bytes.",
                "3", "4", "1", "8");
        q(t, MEDIUM, "Which storage class keeps a local variable's value between function calls?", null, 3,
                "A static local variable is created once and keeps its value.",
                "auto", "register", "extern", "static");
        q(t, MEDIUM, "What does this print?", "int x = 7 / 2 * 2;\nprintf(\"%d\", x);", 2,
                "7 / 2 is 3 in integer division, and 3 * 2 is 6.",
                "7", "7.0", "6", "3");
    }

    // ------------------------------------------------------------------ C++
    private static void cpp() {
        Track t = Track.CPP;
        q(t, EASY, "Which operator is used with cout to print?", null, 0,
                "cout << value sends output. cin >> value reads input.",
                "<<", ">>", "->", "::");
        q(t, EASY, "What is :: called in C++?", null, 2,
                ":: is the scope resolution operator, as in std::cout.",
                "Pointer operator", "Member access operator", "Scope resolution operator", "Ternary operator");
        q(t, MEDIUM, "What does this print?", "int a = 5;\nint &r = a;\nr = 10;\ncout << a;", 1,
                "r is a reference (another name) for a, so changing r changes a.",
                "5", "10", "An address", "Compilation error");
        q(t, EASY, "Which operator allocates memory dynamically in C++?", null, 3,
                "new allocates memory and calls the constructor; delete frees it.",
                "alloc", "create", "make", "new");
        q(t, EASY, "What is function overloading?", null, 1,
                "Overloading means several functions share a name but take different parameters.",
                "Two functions with the same name and same parameters",
                "Functions with the same name but different parameters",
                "A function calling itself", "A function inside a class");
        q(t, MEDIUM, "What do virtual functions make possible?", null, 2,
                "A virtual function call is decided at run time from the real object type.",
                "Faster code", "Multiple inheritance", "Run-time polymorphism", "Operator overloading");
        q(t, EASY, "What is the default access level of members in a class?", null, 0,
                "Class members are private by default; struct members are public by default.",
                "private", "public", "protected", "There is no default");
        q(t, EASY, "Which statement about constructors is true?", null, 3,
                "A constructor has the same name as its class and no return type.",
                "It must return int", "It must be called by hand",
                "It can have any name", "It has the class name and no return type");
        q(t, MEDIUM, "What does this print?", "vector<int> v = {1, 2, 3};\nv.push_back(4);\ncout << v.size();", 2,
                "push_back adds one element at the end, so there are now 4.",
                "3", "5", "4", "Compilation error");
        q(t, EASY, "Which symbol starts the name of a destructor?", null, 1,
                "A destructor is written ~ClassName() and runs when the object is destroyed.",
                "!", "~", "#", "&");
        q(t, MEDIUM, "Which STL container stores key-value pairs sorted by key?", null, 0,
                "std::map keeps keys in sorted order (unordered_map does not).",
                "map", "vector", "stack", "queue");
        q(t, MEDIUM, "What does this print?", "int x = 10;\nint *p = &x;\ncout << *p + 1;", 3,
                "*p reads the value 10, then 1 is added.",
                "10", "The address plus 1", "Compilation error", "11");
        q(t, EASY, "How does class B publicly inherit from class A?", null, 1,
                "The syntax is class B : public A { ... };",
                "class B extends A", "class B : public A", "class B inherits A", "class B(A)");
        q(t, HARD, "How do you declare a pure virtual function?", null, 2,
                "Writing = 0 makes the function pure virtual and the class abstract.",
                "virtual void f() {}", "void f() = virtual;", "virtual void f() = 0;", "pure void f();");
        q(t, EASY, "Which of these is NOT one of the pillars of object-oriented programming?", null, 3,
                "The pillars are encapsulation, inheritance, polymorphism and abstraction.",
                "Encapsulation", "Inheritance", "Polymorphism", "Compilation");
        q(t, MEDIUM, "What does this print?", "string s = \"code\";\ns += \"r\";\ncout << s.length();", 0,
                "s becomes \"coder\", which has 5 characters.",
                "5", "4", "6", "Compilation error");
        q(t, MEDIUM, "What does endl do?", null, 1,
                "endl writes a newline and flushes the output buffer.",
                "Ends the program", "Prints a newline and flushes the output",
                "Clears the screen", "Ends the loop");
        q(t, EASY, "What does this print?", "int sum = 0;\nfor (int i = 1; i <= 4; i++) sum += i;\ncout << sum;", 2,
                "1 + 2 + 3 + 4 = 10.",
                "4", "9", "10", "15");
        q(t, MEDIUM, "What are templates used for?", null, 0,
                "Templates let one function or class work with many types (generic programming).",
                "Generic programming", "Exception handling", "File handling", "Memory management");
        q(t, MEDIUM, "Which declaration receives an int by reference?", null, 3,
                "int &x makes x a reference to the caller's variable.",
                "void f(int x)", "void f(const int x)", "void f(int x[])", "void f(int &x)");
    }

    // ------------------------------------------------------------------ JavaScript
    private static void javascript() {
        Track t = Track.JAVASCRIPT;
        q(t, MEDIUM, "What does this print?", "console.log(typeof null);", 2,
                "A long-standing quirk of JavaScript: typeof null is \"object\".",
                "\"null\"", "\"undefined\"", "\"object\"", "\"number\"");
        q(t, EASY, "What does this print?", "console.log(1 + \"2\");", 0,
                "With a string involved, + joins text, so the result is the string \"12\".",
                "\"12\"", "3", "NaN", "TypeError");
        q(t, MEDIUM, "What does this print?", "console.log(\"5\" - 2);", 1,
                "- only works on numbers, so \"5\" is converted to 5.",
                "\"52\"", "3", "NaN", "\"3\"");
        q(t, EASY, "What does === check?", null, 3,
                "=== is strict equality: same value and same type, with no conversion.",
                "Only the value", "Only the type", "If both are objects", "The value and the type");
        q(t, EASY, "Which keyword declares a block-scoped variable that can be reassigned?", null, 1,
                "let is block-scoped and can be reassigned. var is function-scoped.",
                "var", "let", "function", "global");
        q(t, EASY, "What does this print?", "console.log([1, 2, 3].map(x => x * 2));", 2,
                "map builds a new array by applying the function to each element.",
                "[1, 2, 3]", "6", "[2, 4, 6]", "[1, 4, 9]");
        q(t, HARD, "What does this print?", "console.log(a);\nvar a = 5;", 0,
                "var declarations are hoisted but not their values, so a exists and is undefined.",
                "undefined", "5", "ReferenceError", "null");
        q(t, EASY, "Which method adds an element to the end of an array?", null, 3,
                "push adds at the end; unshift adds at the start; pop and shift remove.",
                "pop()", "shift()", "unshift()", "push()");
        q(t, MEDIUM, "What does this print?", "console.log(0.1 + 0.2 === 0.3);", 1,
                "Floating-point maths gives 0.30000000000000004, which is not exactly 0.3.",
                "true", "false", "undefined", "NaN");
        q(t, EASY, "Which function turns a JSON string into an object?", null, 2,
                "JSON.parse reads JSON text; JSON.stringify does the opposite.",
                "JSON.stringify()", "JSON.toObject()", "JSON.parse()", "Object.fromJSON()");
        q(t, EASY, "What does this print?", "const add = (a, b) => a + b;\nconsole.log(add(2, 3));", 0,
                "An arrow function with a single expression returns it automatically.",
                "5", "\"23\"", "undefined", "SyntaxError");
        q(t, EASY, "How do you select an HTML element by its id?", null, 3,
                "document.getElementById(\"name\") returns the element with that id.",
                "document.getElement(\"id\")", "document.id(\"name\")",
                "window.findById(\"name\")", "document.getElementById(\"name\")");
        q(t, MEDIUM, "What is true about an object declared with const?", null, 1,
                "const stops the variable being reassigned, but the object's properties can still change.",
                "Nothing about it can change", "Its properties can change, but it cannot be reassigned",
                "It can be reassigned", "It is copied every time it is used");
        q(t, EASY, "What does this print?", "console.log([1, 2, 3, 4].filter(n => n % 2 === 0));", 0,
                "filter keeps only elements for which the function returns true: the even ones.",
                "[2, 4]", "[1, 3]", "[true, false, true, false]", "2");
        q(t, MEDIUM, "Where can the await keyword be used (outside modules)?", null, 2,
                "await pauses an async function until a Promise settles.",
                "Inside any loop", "Only in the global scope",
                "Inside an async function", "Only inside a constructor");
        q(t, EASY, "What does this print?", "console.log(Boolean(0));", 3,
                "0 is one of the falsy values, so Boolean(0) is false.",
                "true", "0", "undefined", "false");
        q(t, EASY, "Which method attaches a click handler to an element?", null, 0,
                "element.addEventListener(\"click\", fn) runs fn on every click.",
                "addEventListener()", "onEvent()", "attachClick()", "listen()");
        q(t, EASY, "What does this print?", "let s = \"hello\";\nconsole.log(s.toUpperCase());", 1,
                "toUpperCase returns the text in capital letters.",
                "hello", "HELLO", "Hello", "undefined");
        q(t, MEDIUM, "What does this print?", "console.log(typeof []);", 2,
                "Arrays are objects in JavaScript. Use Array.isArray() to test for one.",
                "\"array\"", "\"list\"", "\"object\"", "\"undefined\"");
        q(t, HARD, "What does this print?", "console.log(NaN === NaN);", 0,
                "NaN is the only value not equal to itself. Use Number.isNaN() to test it.",
                "false", "true", "undefined", "TypeError");
    }

    // ------------------------------------------------------------------ SQL
    private static void sql() {
        Track t = Track.SQL;
        q(t, EASY, "Which command reads data from a table?", null, 0,
                "SELECT reads rows, for example SELECT * FROM students;",
                "SELECT", "GET", "READ", "FETCH ALL");
        q(t, EASY, "Which clause keeps only the rows that match a condition?", null, 2,
                "WHERE filters rows before any grouping.",
                "ORDER BY", "GROUP BY", "WHERE", "HAVING");
        q(t, EASY, "Which keyword removes duplicate rows from a result?", null, 1,
                "SELECT DISTINCT city FROM students; lists each city once.",
                "UNIQUE", "DISTINCT", "DIFFERENT", "SINGLE");
        q(t, EASY, "How do you sort employees from highest to lowest salary?", null, 3,
                "DESC means descending. Ascending (ASC) is the default.",
                "SORT BY salary", "ORDER salary DOWN", "ORDER BY salary ASC", "ORDER BY salary DESC");
        q(t, EASY, "Which function counts the rows in a table?", null, 0,
                "COUNT(*) counts every row.",
                "COUNT(*)", "SUM(*)", "TOTAL()", "LEN()");
        q(t, MEDIUM, "Which clause filters groups after GROUP BY?", null, 2,
                "HAVING works on groups, such as HAVING COUNT(*) > 5. WHERE works on single rows.",
                "WHERE", "ORDER BY", "HAVING", "LIMIT");
        q(t, EASY, "What is true about a PRIMARY KEY column?", null, 1,
                "A primary key identifies each row, so it must be unique and cannot be NULL.",
                "It can repeat", "It is unique and not NULL", "It must be text", "A table can have many");
        q(t, MEDIUM, "What does a FOREIGN KEY do?", null, 3,
                "A foreign key links a column to the primary key of another table.",
                "Encrypts a column", "Sorts the table", "Makes the table faster",
                "Refers to the primary key of another table");
        q(t, MEDIUM, "Which rows does an INNER JOIN return?", null, 0,
                "INNER JOIN returns only the rows that have a match in both tables.",
                "Rows that match in both tables", "All rows from both tables",
                "All rows from the left table", "Rows that match in neither table");
        q(t, MEDIUM, "Which rows does a LEFT JOIN return?", null, 2,
                "Every row from the left table, with NULLs where the right table has no match.",
                "Only matching rows", "All rows from the right table",
                "All rows from the left table plus the matches from the right", "No rows");
        q(t, EASY, "Which names does this find?", "SELECT * FROM students WHERE name LIKE 'A%';", 1,
                "% means any characters, so 'A%' matches names that start with A.",
                "Names ending in A", "Names starting with A", "Names containing only A", "Exactly 'A%'");
        q(t, MEDIUM, "Which command removes a table and its structure completely?", null, 3,
                "DROP TABLE deletes the table itself. DELETE and TRUNCATE only remove rows.",
                "DELETE", "TRUNCATE", "REMOVE", "DROP");
        q(t, EASY, "Which command adds a new row?", null, 0,
                "INSERT INTO table (cols) VALUES (...) adds a row.",
                "INSERT INTO", "ADD ROW", "UPDATE", "CREATE ROW");
        q(t, EASY, "Which command changes data in existing rows?", null, 2,
                "UPDATE table SET col = value WHERE ... changes rows. Always use WHERE.",
                "ALTER", "MODIFY", "UPDATE", "CHANGE");
        q(t, MEDIUM, "How do you find rows where email has no value?", null, 1,
                "NULL is not a value, so = NULL never matches. Use IS NULL.",
                "WHERE email = NULL", "WHERE email IS NULL", "WHERE email == ''", "WHERE email NULL");
        q(t, EASY, "Which of these is NOT an aggregate function?", null, 3,
                "UPPER() works on one value at a time. SUM, AVG and MAX combine many rows.",
                "SUM()", "AVG()", "MAX()", "UPPER()");
        q(t, EASY, "Table marks has scores 80, 90 and 70. What does this return?",
                "SELECT MAX(score) FROM marks;", 0,
                "MAX returns the largest value: 90.",
                "90", "80", "70", "240");
        q(t, MEDIUM, "Table marks has scores 80, 90 and 70. What does this return?",
                "SELECT COUNT(*) FROM marks WHERE score > 75;", 2,
                "Only 80 and 90 are greater than 75.",
                "1", "3", "2", "0");
        q(t, MEDIUM, "Does score BETWEEN 70 AND 80 include 70 and 80?", null, 1,
                "BETWEEN is inclusive at both ends.",
                "Neither", "Yes, both", "Only 70", "Only 80");
        q(t, EASY, "Which of these is a DDL (Data Definition Language) command?", null, 3,
                "CREATE, ALTER and DROP define structure. SELECT, INSERT and UPDATE work on data.",
                "SELECT", "INSERT", "UPDATE", "CREATE");
    }

    // ------------------------------------------------------------------ Olympiad
    private static void olympiad() {
        Track t = Track.OLYMPIAD;
        q(t, EASY, "What is 13 written in binary?", null, 1,
                "13 = 8 + 4 + 1, so the bits are 1101.",
                "1011", "1101", "1110", "1001");
        q(t, MEDIUM, "You climb 5 stairs, taking 1 or 2 steps at a time. In how many different ways can you reach the top?", null, 2,
                "Ways(n) = Ways(n-1) + Ways(n-2): 1, 2, 3, 5, 8. This is the Fibonacci pattern.",
                "5", "7", "8", "10");
        q(t, MEDIUM, "Each step of binary search halves the range. Starting with 1024 elements, how many halvings until one element is left?", null, 0,
                "1024 = 2^10, so 10 halvings. Binary search is O(log n).",
                "10", "11", "32", "512");
        q(t, EASY, "10 people each shake hands with every other person once. How many handshakes happen?", null, 3,
                "Choose 2 people out of 10: 10 x 9 / 2 = 45.",
                "100", "90", "50", "45");
        q(t, EASY, "What comes next: 2, 6, 12, 20, 30, ?", null, 1,
                "The terms are n(n+1): 1x2, 2x3, 3x4, 4x5, 5x6, then 6x7 = 42.",
                "40", "42", "44", "36");
        q(t, MEDIUM, "What is the worst-case time complexity of bubble sort on n items?", null, 2,
                "It may compare every pair, about n x n / 2 comparisons, which is O(n^2).",
                "O(n)", "O(log n)", "O(n^2)", "O(n log n)");
        q(t, EASY, "A tree has n nodes. How many edges does it have?", null, 0,
                "A connected graph with no cycles always has exactly n - 1 edges.",
                "n - 1", "n", "n + 1", "2n");
        q(t, EASY, "How many subsets does a set with 5 elements have (including the empty set)?", null, 3,
                "Each element is either in or out: 2^5 = 32.",
                "10", "25", "31", "32");
        q(t, MEDIUM, "What is the remainder when 2^10 is divided by 7?", null, 1,
                "2^10 = 1024 and 7 x 146 = 1022, so the remainder is 2.",
                "1", "2", "3", "4");
        q(t, MEDIUM, "On an empty stack: push 1, push 2, pop, push 3, push 4, pop, pop. In what order were the values popped?", null, 2,
                "A stack is last-in first-out: 2 comes off first, then 4, then 3.",
                "2 3 4", "4 3 2", "2 4 3", "1 2 3");
        q(t, EASY, "On an empty queue: enqueue A, B, C, then dequeue, enqueue D, dequeue. Which value comes out second?", null, 0,
                "A queue is first-in first-out: A leaves first, then B.",
                "B", "A", "C", "D");
        q(t, EASY, "What is the greatest common divisor (GCD) of 48 and 18?", null, 3,
                "Euclid: 48 mod 18 = 12, 18 mod 12 = 6, 12 mod 6 = 0, so the GCD is 6.",
                "2", "3", "12", "6");
        q(t, MEDIUM, "On a 3 x 3 grid of squares, you move from the top-left square to the bottom-right square using only right and down moves. How many different paths are there?", null, 1,
                "You need 2 rights and 2 downs in some order: 4! / (2! x 2!) = 6.",
                "4", "6", "8", "9");
        q(t, HARD, "Knights always tell the truth and liars always lie. A says: \"B is a liar.\" B says: \"A and I are the same type.\" What are they?", null, 0,
                "If A were a liar, B would be a knight, but then B's claim would be false. So A is a knight and B a liar.",
                "A is a knight, B is a liar", "A is a liar, B is a knight",
                "Both are knights", "Both are liars");
        q(t, MEDIUM, "f(0) = 0 and f(n) = f(n - 1) + 2n. What is f(4)?", null, 2,
                "f(1) = 2, f(2) = 6, f(3) = 12, f(4) = 20.",
                "16", "18", "20", "24");
        q(t, HARD, "How many zeros are at the end of 25! (25 factorial)?", null, 3,
                "Count the factors of 5: 25/5 = 5, plus 25/25 = 1 more, giving 6.",
                "5", "4", "7", "6");
        q(t, EASY, "How many people must be in a room to be sure that two were born in the same month?", null, 1,
                "Pigeonhole principle: 12 months, so 13 people guarantee a repeat.",
                "12", "13", "24", "366");
        q(t, EASY, "What is 1 + 2 + 3 + ... + 100?", null, 0,
                "Gauss: n(n + 1) / 2 = 100 x 101 / 2 = 5050.",
                "5050", "5000", "10100", "4950");
        q(t, MEDIUM, "Coins of 1, 5 and 10 are available. What is the fewest number of coins that make 28?", null, 2,
                "Greedy works here: 10 + 10 + 5 + 1 + 1 + 1 = 6 coins.",
                "4", "5", "6", "7");
        q(t, MEDIUM, "Breadth-first search (BFS) on a graph uses which data structure?", null, 3,
                "BFS visits nodes level by level, using a queue. DFS uses a stack.",
                "Stack", "Priority queue", "Hash map", "Queue");
    }
}
