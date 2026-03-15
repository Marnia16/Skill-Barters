package com.skillbarter.util;

import com.skillbarter.exception.DatabaseException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * CONCEPT 11: Raw JDBC Connection utility.
 * Reads credentials from db.properties → connects to AWS RDS MySQL.
 */
public class DBConnection {

    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    static {
        try (InputStream is = DBConnection.class
                .getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(is);
            url      = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            driver   = props.getProperty("db.driver");
            Class.forName(driver);
        } catch (Exception e) {
            throw new DatabaseException("Failed to load db.properties", e);
        }
    }

    /** Returns a new JDBC connection to AWS RDS. Always close after use. */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new DatabaseException("Cannot connect to AWS RDS", e);
        }
    }
}
