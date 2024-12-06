package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.AsciiArt;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;
import ryans.blog.dao.UserDAO;

@Command(
    name = "search",
    description = "Search blog posts by tags, title, or author. Use --tag, --title, --author to search."
)
public class SearchCommand implements Runnable {

    @Option(names = "--tag", description = "Search by tag")
    private String tag;

    @Option(names = "--title", description = "Search by title")
    private String title;

    @Option(names = "--author", description = "Search by author")
    private String author;

    @Parameters(index = "0", description = "Search term", defaultValue = "")
    private String searchTerm;

    private Connection conn;
    private UserDAO userDao;

    public SearchCommand() {
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
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT DISTINCT p.* FROM Posts p ");
            queryBuilder.append("LEFT JOIN post_tags pt ON p.id = pt.post_id ");
            queryBuilder.append("LEFT JOIN tags t ON pt.tag_id = t.id ");
            queryBuilder.append("LEFT JOIN users u ON p.user_id = u.id ");
            queryBuilder.append("WHERE 1=1 ");

            if (tag != null) {
                queryBuilder.append("AND t.name LIKE ? ");
            }
            if (title != null) {
                queryBuilder.append("AND p.title LIKE ? ");
            }
            if (author != null) {
                queryBuilder.append("AND u.username LIKE ? ");
            }
            if (!searchTerm.isEmpty()) {
                queryBuilder.append(
                    "AND (p.title LIKE ? OR p.content LIKE ? OR p.description LIKE ?) "
                );
            }

            try (
                PreparedStatement stmt = conn.prepareStatement(
                    queryBuilder.toString()
                )
            ) {
                int paramIndex = 1;

                if (tag != null) {
                    stmt.setString(paramIndex++, "%" + tag + "%");
                }
                if (title != null) {
                    stmt.setString(paramIndex++, "%" + title + "%");
                }
                if (author != null) {
                    stmt.setString(paramIndex++, "%" + author + "%");
                }
                if (!searchTerm.isEmpty()) {
                    stmt.setString(paramIndex++, "%" + searchTerm + "%");
                    stmt.setString(paramIndex++, "%" + searchTerm + "%");
                    stmt.setString(paramIndex++, "%" + searchTerm + "%");
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
                            userDao
                                .findById(rs.getInt("user_id"))
                                .getUsername(),
                            rs.getString("title"),
                            rs.getString("description")
                        );
                    }

                    if (!foundResults) {
                        System.out.println(
                            ConsoleTheme.formatResponse("No results found.")
                        );
                    }

                    System.out.println(AsciiArt.DIVIDER);
                }
            }
        } catch (Exception e) {
            System.err.println(
                ConsoleColors.RED +
                "Error searching posts: " +
                e.getMessage() +
                ConsoleColors.RESET
            );
        }
    }
}
