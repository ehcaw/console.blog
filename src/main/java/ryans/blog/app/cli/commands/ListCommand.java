package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import picocli.CommandLine.Command;
import ryans.blog.app.Database;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.AsciiArt;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;
import ryans.blog.dao.UserDAO;

@Command(name = "list", description = "List all blog posts")
public class ListCommand implements Runnable {

    private Connection conn;
    private UserDAO userDao;

    public ListCommand() {
        try {
            this.conn = Database.getConnection();
            this.userDao = new UserDAO(conn);
        } catch (SQLException e) {
            System.out.println(
                ConsoleTheme.formatError(
                    "Database connection error: " + e.getMessage()
                )
            );
        }
    }

    @Override
    public void run() {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT * FROM Posts";
            try (
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()
            ) {
                System.out.println(
                    ConsoleColors.CYAN_BOLD +
                    "\nAvailable Posts:" +
                    ConsoleColors.RESET
                );
                System.out.println(AsciiArt.DIVIDER);

                while (rs.next()) {
                    System.out.printf(
                        ConsoleColors.YELLOW +
                        "ID: %d" +
                        ConsoleColors.RESET +
                        " | " +
                        ConsoleColors.CYAN +
                        "Author: %s" +
                        ConsoleColors.RESET +
                        " | " +
                        ConsoleColors.GREEN +
                        "%s" +
                        ConsoleColors.RESET +
                        " | Description: " +
                        ConsoleColors.GREEN +
                        "%s" +
                        ConsoleColors.RESET +
                        "%n",
                        rs.getInt("id"),
                        userDao.findById(rs.getInt("user_id")).getUsername(),
                        rs.getString("title"),
                        rs.getString("description")
                    );
                }
                System.out.println(AsciiArt.DIVIDER);
            }
        } catch (Exception e) {
            System.err.println(
                ConsoleColors.RED +
                "Error listing posts: " +
                e.getMessage() +
                ConsoleColors.RESET
            );
        }
    }
}
