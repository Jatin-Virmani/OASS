package com.oass.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Central JDBC helper used by every DAO. All SQL goes through here.
 * Keeps try-with-resources and PreparedStatement binding in one place.
 * Syllabus: Section C (JDBC, PreparedStatement, try-with-resources).
 */
public final class Db {

    private Db() {}

    /** Functional interface: converts one ResultSet row to an object. */
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Runs a SELECT and returns all matching rows.
     * Wraps SQLException in DataAccessException (unchecked).
     */
    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        List<T> result = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("query failed: " + sql, e);
        }
        return result;
    }

    /**
     * Runs a SELECT and returns the first row, or null if no rows found.
     */
    public static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper.map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("queryOne failed: " + sql, e);
        }
    }

    /**
     * Runs an INSERT/UPDATE/DELETE and returns the number of affected rows.
     */
    public static int update(String sql, Object... params) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("update failed: " + sql, e);
        }
    }

    /**
     * Runs an INSERT and returns the auto-generated key.
     */
    public static long insert(String sql, Object... params) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new DataAccessException("No generated key returned for: " + sql, null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("insert failed: " + sql, e);
        }
    }

    /** Code that runs inside one database transaction. */
    public interface TxWork<T> {
        T run(Tx tx) throws SQLException;
    }

    /**
     * Runs several statements as ONE transaction on the same connection:
     * if anything fails, everything is rolled back; otherwise it is committed.
     * Syllabus: Section C (JDBC transactions: setAutoCommit, commit, rollback).
     */
    public static <T> T transaction(TxWork<T> work) {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                T result = work.run(new Tx(con));
                con.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                if (e instanceof RuntimeException re) throw re;
                throw new DataAccessException("transaction failed", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("transaction failed", e);
        }
    }

    /** Statement helper bound to the connection of one transaction. */
    public static final class Tx {
        private final Connection con;

        private Tx(Connection con) { this.con = con; }

        public int update(String sql, Object... params) throws SQLException {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, params);
                return ps.executeUpdate();
            }
        }

        public long insert(String sql, Object... params) throws SQLException {
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(ps, params);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getLong(1);
                    throw new SQLException("No generated key returned for: " + sql);
                }
            }
        }

        public <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapper.map(rs) : null;
                }
            }
        }
    }

    /** Binds parameters to the PreparedStatement using setObject. */
    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
