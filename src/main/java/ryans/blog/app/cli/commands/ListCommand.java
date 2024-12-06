package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.Command;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.AsciiArt;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;
import ryans.blog.dao.TagsDAO;
import ryans.blog.dao.UserDAO;

@Command(name = "list", description = "List all blog posts")
public class ListCommand implements Runnable {

    private Connection conn;
    private UserDAO userDao;
    private TagsDAO tagsDao;

    public ListCommand() {
        try {
            this.conn = Database.getConnection();
            this.userDao = new UserDAO(conn);
            this.tagsDao = new TagsDAO(conn);
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
            String query = "SELECT * FROM Posts ORDER BY created_at DESC";
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
                    int postId = rs.getInt("id");
                    // Get tags for this post
                    List<String> tagNames = getTagsForPost(postId);
                    String tagDisplay = tagNames.isEmpty()
                        ? "No tags"
                        : String.join(", ", tagNames);

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
                        "\n" +
                        ConsoleColors.BLUE +
                        "Tags: %s" +
                        ConsoleColors.RESET +
                        "%n",
                        postId,
                        userDao.findById(rs.getInt("user_id")).getUsername(),
                        rs.getString("title"),
                        rs.getString("description"),
                        tagDisplay
                    );
                    System.out.println(AsciiArt.DIVIDER);
                }
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

    private List<String> getTagsForPost(int postId) throws SQLException {
        List<String> tagNames = new ArrayList<>();
        String query =
            "SELECT t.name FROM tags t " +
            "JOIN post_tags pt ON t.id = pt.tag_id " +
            "WHERE pt.post_id = ? " +
            "ORDER BY t.name";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tagNames.add(rs.getString("name"));
                }
            }
        }
        return tagNames;
    }
}
