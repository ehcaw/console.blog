package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine.Command;
import ryans.blog.app.AppGlobalState;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.ConsoleTheme;

@Command(name = "create", description = "Create a new blog post")
public class CreateCommand implements Runnable {

    AppGlobalState appGlobalState = AppGlobalState.getInstance();

    @Override
    public void run() {
        try {
            System.setProperty("org.jline.terminal.dumb", "true");
            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

            // Check login status
            if (appGlobalState.getCurrentUser() == null) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "Please login before you attempt to post. Run the login command to get started"
                    )
                );
                return;
            }

            // Get Post Title
            System.out.println(
                ConsoleTheme.formatResponse("\nCreating new post")
            );
            System.out.println(ConsoleTheme.formatResponse("----------------"));
            String title = reader.readLine(
                ConsoleTheme.formatResponse("Title: ")
            );

            // Get post description
            System.out.println(ConsoleTheme.formatResponse("----------------"));
            String description = reader.readLine(
                ConsoleTheme.formatResponse("Description: ")
            );

            // Get post content
            System.out.println(
                ConsoleTheme.formatResponse(
                    "\nEnter your post content (Type 'END' on a new line to finish):"
                )
            );
            System.out.println(ConsoleTheme.formatResponse("----------------"));

            StringBuilder content = new StringBuilder();
            String line;
            while (!(line = reader.readLine("> ")).equals("END")) {
                content.append(line).append("\n");
            }

            // Get tags
            System.out.println(
                ConsoleTheme.formatResponse(
                    "\nEnter tags (comma-separated, e.g.: java,programming,tutorial):"
                )
            );
            String tagsInput = reader.readLine(
                ConsoleTheme.formatResponse("Tags: ")
            );
            List<String> tags = Arrays.asList(
                tagsInput.toLowerCase().split(",")
            )
                .stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());

            // Preview
            System.out.println(ConsoleTheme.formatResponse("\nPreview:"));
            System.out.println(
                ConsoleTheme.formatPost(title, content.toString())
            );
            System.out.println(
                ConsoleTheme.formatResponse("Tags: " + String.join(", ", tags))
            );

            String confirm = reader.readLine(
                ConsoleTheme.formatResponse("Save this post? (y/n): ")
            );

            if (confirm.toLowerCase().startsWith("y")) {
                int postId = savePost(
                    title,
                    description,
                    content.toString().trim(),
                    tags
                );
                System.out.println(
                    ConsoleTheme.formatResponse("Post saved successfully!")
                );
            } else {
                System.out.println(
                    ConsoleTheme.formatResponse("Post discarded.")
                );
            }
        } catch (Exception e) {
            System.out.println(
                ConsoleTheme.formatError(
                    "Error creating post: " + e.getMessage()
                )
            );
        }
    }

    private int savePost(
        String title,
        String description,
        String content,
        List<String> tags
    ) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert post
            int postId;
            String postQuery =
                "INSERT INTO Posts (title, description, content, user_id) VALUES (?, ?, ?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(postQuery)) {
                stmt.setString(1, title);
                stmt.setString(2, description);
                stmt.setString(3, content);
                stmt.setInt(4, appGlobalState.getCurrentUser().getUserId());
                ResultSet rs = stmt.executeQuery();
                rs.next();
                postId = rs.getInt(1);
            }

            // Process tags
            for (String tagName : tags) {
                // Get or create tag
                int tagId = getOrCreateTag(conn, tagName);

                // Link tag to post
                String linkQuery =
                    "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)";
                try (
                    PreparedStatement stmt = conn.prepareStatement(linkQuery)
                ) {
                    stmt.setInt(1, postId);
                    stmt.setInt(2, tagId);
                    stmt.executeUpdate();
                }
            }

            conn.commit(); // Commit transaction
            return postId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    throw new SQLException(
                        "Error rolling back transaction",
                        ex
                    );
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private int getOrCreateTag(Connection conn, String tagName)
        throws SQLException {
        // Try to find existing tag
        String selectQuery = "SELECT id FROM tags WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
            stmt.setString(1, tagName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // Create new tag if it doesn't exist
        String insertQuery = "INSERT INTO tags (name) VALUES (?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, tagName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt("id");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    throw new SQLException(
                        "Error rolling back transaction",
                        ex
                    );
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
