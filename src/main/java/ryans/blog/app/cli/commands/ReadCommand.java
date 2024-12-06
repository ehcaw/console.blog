package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;
import ryans.blog.dao.CommentDAO;
import ryans.blog.dao.UserDAO;
import ryans.blog.model.Comment;

@Command(
    name = "read",
    description = "Read a blog post by adding the post id after read"
)
public class ReadCommand implements Runnable {

    @Parameters(
        index = "0",
        paramLabel = "ID",
        description = "The id of the post you want to read"
    )
    private int postId;

    private UserDAO userDao;
    private CommentDAO commentDao;

    public ReadCommand() {
        try {
            Connection conn = Database.getConnection();
            this.userDao = new UserDAO(conn);
            this.commentDao = new CommentDAO(conn);
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
            String query =
                "SELECT p.*, u.username FROM Posts p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE p.id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, postId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        displayPost(rs);
                        displayTags();
                        displayComments();
                    } else {
                        System.out.println(
                            ConsoleTheme.formatError(
                                "Post not found with ID: " + postId
                            )
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(
                ConsoleTheme.formatError(
                    "Error reading post: " + e.getMessage()
                )
            );
        }
    }

    private void displayPost(ResultSet rs) throws SQLException {
        // Header
        System.out.println(
            "\n" +
            ConsoleColors.CYAN_BOLD +
            "╔═══════════════════════════════════════════════════════════════╗" +
            ConsoleColors.RESET
        );

        // Title
        String title = rs.getString("title");
        System.out.println(
            ConsoleColors.YELLOW_BOLD + "  " + title + ConsoleColors.RESET
        );

        // Meta information
        System.out.printf(
            ConsoleColors.CYAN +
            "  By: %s | Posted on: %s" +
            ConsoleColors.RESET +
            "\n",
            rs.getString("username"),
            rs.getString("created_at")
        );

        // Description
        System.out.println(
            ConsoleColors.BLUE +
            "\n  " +
            rs.getString("description") +
            ConsoleColors.RESET
        );

        // Divider
        System.out.println(
            "\n" +
            ConsoleColors.WHITE +
            "  " +
            "─".repeat(60) +
            ConsoleColors.RESET +
            "\n"
        );

        // Content
        String content = rs.getString("content");
        String[] paragraphs = content.split("\n");
        for (String paragraph : paragraphs) {
            System.out.println("  " + paragraph + "\n");
        }

        // Footer
        System.out.println(
            ConsoleColors.CYAN_BOLD +
            "╚═══════════════════════════════════════════════════════════════╝" +
            ConsoleColors.RESET
        );
    }

    private void displayTags() throws SQLException {
        String query =
            "SELECT t.name FROM tags t " +
            "JOIN post_tags pt ON t.id = pt.tag_id " +
            "WHERE pt.post_id = ? " +
            "ORDER BY t.name";

        List<String> tags = new ArrayList<>();
        try (
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("name"));
                }
            }
        }

        if (!tags.isEmpty()) {
            System.out.println(
                ConsoleColors.BLUE_BOLD +
                "\n📌 Tags: " +
                ConsoleColors.BLUE +
                String.join(", ", tags) +
                ConsoleColors.RESET +
                "\n"
            );
        }
    }

    private void displayComments() throws SQLException {
        List<Comment> comments = commentDao.findByPostId(postId);

        if (!comments.isEmpty()) {
            System.out.println(
                ConsoleColors.GREEN_BOLD +
                "\n💬 Comments (" +
                comments.size() +
                "):" +
                ConsoleColors.RESET
            );

            for (Comment comment : comments) {
                System.out.println(
                    ConsoleColors.CYAN +
                    "  ┌─────────────────────" +
                    ConsoleColors.RESET
                );
                System.out.printf(
                    "  │ %s%s%s on %s%n",
                    ConsoleColors.YELLOW_BOLD,
                    userDao.findById(comment.getUserId()).getUsername(),
                    ConsoleColors.RESET,
                    comment.getCreatedAt()
                );
                System.out.println("  │ " + comment.getContent());
                System.out.println(
                    ConsoleColors.CYAN +
                    "  └─────────────────────" +
                    ConsoleColors.RESET +
                    "\n"
                );
            }
        }
    }
}
