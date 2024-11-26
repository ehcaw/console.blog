package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import picocli.CommandLine.Command;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.AsciiArt;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;

@Command(name = "list", description = "List all blog posts")
public class ListCommand implements Runnable {

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
                        ConsoleColors.GREEN +
                        "%s" +
                        ConsoleColors.RESET +
                        "%n",
                        rs.getInt("id"),
                        rs.getString("title")
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
