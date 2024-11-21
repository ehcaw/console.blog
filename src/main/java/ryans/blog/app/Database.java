package main.java.ryans.blog.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String databaseUrl =
        "jdbc:mysql://localhost:3306/blog_db";
    private static final String USER = "dev";
    private static final String PASSWORD = "consoleblog";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, USER, PASSWORD);
    }
}
