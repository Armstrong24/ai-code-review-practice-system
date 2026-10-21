package com.bcorps.learning;

/**
 * A subject a student can learn and be examined on: a programming language
 * (Java, Python, C, C++, JavaScript, SQL) or the Olympiad round.
 */
public enum Track {
    JAVA("Java", Kind.LANGUAGE, 20, "Object-oriented language behind Android and big enterprise apps."),
    PYTHON("Python", Kind.LANGUAGE, 20, "Easy to read, used for AI, data science and automation."),
    C("C", Kind.LANGUAGE, 20, "The classic systems language: pointers, memory and speed."),
    CPP("C++", Kind.LANGUAGE, 20, "C with classes and the STL, the favourite for competitive coding."),
    JAVASCRIPT("JavaScript", Kind.LANGUAGE, 20, "The language of the web, running in every browser."),
    SQL("SQL", Kind.LANGUAGE, 20, "Ask questions of databases: select, filter, join and group."),
    OLYMPIAD("Olympiad", Kind.OLYMPIAD, 30, "Logic, maths and algorithm puzzles in the style of ZIO / ZCO.");

    /** Language exams test one language; the Olympiad tests problem solving. */
    public enum Kind { LANGUAGE, OLYMPIAD }

    private final String displayName;
    private final Kind kind;
    private final int minutes;
    private final String tagline;

    Track(String displayName, Kind kind, int minutes, String tagline) {
        this.displayName = displayName;
        this.kind = kind;
        this.minutes = minutes;
        this.tagline = tagline;
    }

    public String displayName() { return displayName; }
    public Kind kind() { return kind; }
    /** Time allowed for one exam attempt. */
    public int minutes() { return minutes; }
    public String tagline() { return tagline; }

    /** Accepts "java", "JAVA", "c++", "cpp", "js" and so on. */
    public static Track parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Choose a language.");
        }
        String v = value.strip().toUpperCase(java.util.Locale.ROOT);
        switch (v) {
            case "C++": return CPP;
            case "JS": return JAVASCRIPT;
            default:
                try {
                    return Track.valueOf(v);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown language: " + value);
                }
        }
    }
}
