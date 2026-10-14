package com.bcorps.problems;

public final class Problem {
    private final long id;
    private final String title;
    private final String description;
    private final String inputFormat;
    private final String outputFormat;
    private final String constraints;
    private final Difficulty difficulty;
    private final Category category;

    public Problem(long id, String title, String description,
                   String inputFormat, String outputFormat, String constraints,
                   Difficulty difficulty, Category category) {
        if (id < 0) {
            throw new IllegalArgumentException("Problem ID cannot be negative.");
        }
        this.id = id;
        this.title = requireText(title, "Title", 200);
        this.description = requireText(description, "Description", 100000);
        this.inputFormat = requireText(inputFormat, "Input format", 100000);
        this.outputFormat = requireText(outputFormat, "Output format", 100000);
        this.constraints = requireText(constraints, "Constraints", 100000);
        if (difficulty == null || category == null) {
            throw new IllegalArgumentException("Difficulty and category are required.");
        }
        this.difficulty = difficulty;
        this.category = category;
    }

    private static String requireText(String value, String name, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank.");
        }
        String cleaned = value.strip();
        if (cleaned.length() > maxLength) {
            throw new IllegalArgumentException(name + " exceeds " + maxLength + " characters.");
        }
        return cleaned;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInputFormat() { return inputFormat; }
    public String getOutputFormat() { return outputFormat; }
    public String getConstraints() { return constraints; }
    public Difficulty getDifficulty() { return difficulty; }
    public Category getCategory() { return category; }
}
