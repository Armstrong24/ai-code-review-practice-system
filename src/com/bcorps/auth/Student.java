package com.bcorps.auth;

import java.time.LocalDateTime;

/** A registered student (Member 1). The password itself is never stored, only its hash. */
public final class Student {
    private final long id;
    private final String name;
    private final String email;
    private final LocalDateTime createdAt;

    public Student(long id, String name, String email, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
