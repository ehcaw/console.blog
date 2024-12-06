package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.AsciiArt;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;
import ryans.blog.dao.TagsDAO;
import ryans.blog.dao.UserDAO;

@Command(
    name = "search",
    description = "Search posts by title, content, tags, or author"
)
public class SearchCommand implements Runnable {

    @Parameters(index = "0", description = "Search term", defaultValue = "")
    private String searchTerm;

    @Option(names = "--title", description = "Search only in titles")
    private boolean titleOnly = false;

    @Option(names = "--content", description = "Search only in content")
    private boolean contentOnly = false;

    @Option(names = "--tag", description = "Search only in tags")
    private boolean tagOnly = false;

    @Option(names = "--author", description = "Search only by author")
    private boolean authorOnly = false;

    private Connection conn;
    private UserDAO userDao;
    private TagsDAO tagsDao;

    public SearchCommand() {
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
        if (searchTerm.trim().isEmpty()) {
            System.out.println(
                ConsoleTheme.formatError("Please provide a search term")
            );
            return;
        }

        try {
            // If no specific search type is selected, search everywhere
            if (!titleOnly && !contentOnly && !tagOnly && !authorOnly) {
                searchAll();
            } else {
                if (titleOnly) searchByTitle();
                if (contentOnly) searchByContent();
                if (tagOnly) searchByTag();
                if (authorOnly) searchByAuthor();
            }
        } catch (SQLException e) {
            System.err.println(
                ConsoleTheme.formatError(
                    "Error during search: " + e.getMessage()
                )
            );
        }
    }

    private void searchAll() throws SQLException {
        String query =
            "SELECT DISTINCT p.*, u.username FROM Posts p " +
            "JOIN users u ON p.user_id = u.id " +
            "LEFT JOIN post_tags pt ON p.id = pt.post_id " +
            "LEFT JOIN tags t ON pt.tag_id = t.id " +
            "WHERE LOWER(p.title) LIKE ? " +
            "OR LOWER(p.content) LIKE ? " +
            "OR LOWER(p.description) LIKE ? " +
            "OR LOWER(t.name) LIKE ? " +
            "OR LOWER(u.username) LIKE ? " +
            "ORDER BY p.created_at DESC";

        displayResults(query, 5);
    }

    private void searchByTitle() throws SQLException {
        String query =
            "SELECT DISTINCT p.*, u.username FROM Posts p " +
            "JOIN users u ON p.user_id = u.id " +
            "WHERE LOWER(p.title) LIKE ? " +
            "ORDER BY p.created_at DESC";

        displayResults(query, 1);
    }

    private void searchByContent() throws SQLException {
        String query =
            "SELECT DISTINCT p.*, u.username FROM Posts p " +
            "JOIN users u ON p.user_id = u.id " +
            "WHERE LOWER(p.content) LIKE ? " +
            "OR LOWER(p.description) LIKE ? " +
            "ORDER BY p.created_at DESC";

        displayResults(query, 2);
    }

    private void searchByTag() throws SQLException {
        String query =
            "SELECT DISTINCT p.*, u.username FROM Posts p " +
            "JOIN users u ON p.user_id = u.id " +
            "JOIN post_tags pt ON p.id = pt.post_id " +
            "JOIN tags t ON pt.tag_id = t.id " +
            "WHERE LOWER(t.name) LIKE ? " +
            "ORDER BY p.created_at DESC";

        displayResults(query, 1);
    }

    private void searchByAuthor() throws SQLException {
        String query =
            "SELECT DISTINCT p.*, u.username FROM Posts p " +
            "JOIN users u ON p.user_id = u.id " +
            "WHERE LOWER(u.username) LIKE ? " +
            "ORDER BY p.created_at DESC";

        displayResults(query, 1);
    }

    private void displayResults(String query, int paramCount)
        throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            for (int i = 1; i <= paramCount; i++) {
                stmt.setString(i, searchPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println(
                    ConsoleColors.CYAN_BOLD +
                    "\nSearch Results:" +
                    ConsoleColors.RESET
                );
                System.out.println(AsciiArt.DIVIDER);

                boolean foundResults = false;
                while (rs.next()) {
                    foundResults = true;
                    int postId = rs.getInt("id");
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
                        rs.getString("username"),
                        rs.getString("title"),
                        rs.getString("description"),
                        tagDisplay
                    );
                    System.out.println(AsciiArt.DIVIDER);
                }

                if (!foundResults) {
                    System.out.println(
                        ConsoleTheme.formatResponse("No results found.")
                    );
                    System.out.println(AsciiArt.DIVIDER);
                }
            }
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
