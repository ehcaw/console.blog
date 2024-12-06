package ryans.blog.app;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String databaseUrl =
        "jdbc:postgresql://dpg-ct7ir168ii6s7388o250-a.oregon-postgres.render.com/console_blog";
    private static final String USER = dotenv.get("PGUSER");
    private static final String PASSWORD = dotenv.get("PGPASSWORD");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, USER, PASSWORD);
    }

    public static void closeConnection() throws SQLException {}
}
