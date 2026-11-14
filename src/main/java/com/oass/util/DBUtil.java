package com.oass.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens one JDBC connection from config.properties.
 * Called only by Db; not used directly by DAOs.
 * Syllabus: Section C (JDBC, DriverManager).
 */
public final class DBUtil {

    private DBUtil() {}

    /** Returns a fresh connection. Caller must close it (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        String url  = Config.get("db.url");
        String user = Config.get("db.user");
        String pass = Config.get("db.password");
        return DriverManager.getConnection(url, user, pass);
    }
}
