package com.bcorps.problems;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProblemRepository {
    long create(Problem problem, List<TestCase> testCases) throws SQLException;
    Optional<Problem> findById(long id) throws SQLException;
    List<Problem> findAll(Difficulty difficulty, Category category) throws SQLException;
    boolean update(Problem problem) throws SQLException;
    boolean delete(long id) throws SQLException;
    long addTestCase(long problemId, TestCase testCase) throws SQLException;
    List<TestCase> findTestCases(long problemId, boolean includeHidden) throws SQLException;
}
