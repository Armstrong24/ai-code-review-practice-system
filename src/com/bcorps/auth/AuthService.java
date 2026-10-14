package com.bcorps.auth;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/** Registration and login rules (Member 1). */
public final class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final StudentRepository repository;

    public AuthService(StudentRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository is required.");
        }
        this.repository = repository;
    }

    public Student register(String name, String email, char[] password) throws SQLException {
        try {
            String cleanName = name == null ? "" : name.strip();
            String cleanEmail = normalizeEmail(email);
            if (cleanName.isEmpty() || cleanName.length() > 100) {
                throw new IllegalArgumentException("Enter your full name (up to 100 characters).");
            }
            if (!EMAIL.matcher(cleanEmail).matches()) {
                throw new IllegalArgumentException("Enter a valid email address.");
            }
            if (password == null || password.length < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters.");
            }
            if (repository.emailExists(cleanEmail)) {
                throw new IllegalArgumentException("This email is already registered.");
            }
            String salt = PasswordHasher.newSalt();
            long id = repository.create(cleanName, cleanEmail,
                    PasswordHasher.hash(password, salt), salt);
            return repository.findById(id).orElseThrow();
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    /** Returns the student when the email and password match; empty otherwise. */
    public Optional<Student> login(String email, char[] password) throws SQLException {
        try {
            if (password == null || password.length == 0) {
                return Optional.empty();
            }
            return repository.findCredentials(normalizeEmail(email))
                    .filter(c -> PasswordHasher.matches(password, c.salt(), c.passwordHash()))
                    .map(StudentRepository.StoredCredentials::student);
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
