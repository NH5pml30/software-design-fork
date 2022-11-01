package ru.akirakozov.sd.refactoring.util;

import java.sql.*;

public class SQLAccessor {
    public interface SQLAction<T> {
        void accept(T c) throws SQLException;
    }

    public SQLAccessor(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void withConnection(SQLAction<Connection> action) throws SQLException {
        try (Connection c = DriverManager.getConnection(dbUrl)) {
            action.accept(c);
        }
    }

    public void withStatement(Connection c, SQLAction<Statement> action) throws SQLException {
        try (var s = c.createStatement()) {
            action.accept(s);
        }
    }

    public void withStatement(SQLAction<Statement> action) throws SQLException {
        withConnection(c -> withStatement(c, action));
    }

    public void withQuery(Statement stmt, String query, SQLAction<ResultSet> action) throws SQLException {
        try (var rs = stmt.executeQuery(query)) {
            action.accept(rs);
        }
    }

    private final String dbUrl;
}
