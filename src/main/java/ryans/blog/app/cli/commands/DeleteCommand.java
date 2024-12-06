package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jline.reader.LineReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ryans.blog.app.AppGlobalState;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.ConsoleTheme;
import ryans.blog.app.cli.utils.TerminalUtil;

@Command(name = "delete", description = "Delete an existing blog post")
public class DeleteCommand implements Runnable {

    @Parameters(index = "0", description = "Post ID to delete")
    private int postId;

    private AppGlobalState appGlobalState = AppGlobalState.getInstance();

    @Override
    public void run() {
        try {
            // Check if user is logged in
            if (appGlobalState.getCurrentUser() == null) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "Please login first to delete posts"
                    )
                );
                return;
            }

            // Fetch the post to confirm it exists and belongs to user
            String[] postDetails = getPost(postId);
            if (postDetails == null) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "Post not found or you don't have permission to delete it"
                    )
                );
                return;
            }

            // Show post to be deleted
            System.out.println(
                ConsoleTheme.formatResponse("\nPost to delete:")
            );
            System.out.println(
                ConsoleTheme.formatPost(postDetails[0], postDetails[1])
            );

            // Get confirmation
            LineReader reader = TerminalUtil.getLineReader();
            String confirm = reader.readLine(
                ConsoleTheme.formatResponse(
                    "\nAre you sure you want to delete this post? (y/n): "
                )
            );

            if (confirm.toLowerCase().startsWith("y")) {
                deletePost(postId);
                System.out.println(
                    ConsoleTheme.formatResponse("Post deleted successfully!")
                );
            } else {
                System.out.println(
                    ConsoleTheme.formatResponse(
                        "Delete cancelled. No changes were made."
                    )
                );
            }
        } catch (Exception e) {
            System.out.println(
                ConsoleTheme.formatError(
                    "Error deleting post: " + e.getMessage()
                )
            );
        }
    }

    private String[] getPost(int postId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String query =
                "SELECT title, content FROM Posts WHERE id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, postId);
                stmt.setLong(2, appGlobalState.getCurrentUser().getUserId());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new String[] {
                            rs.getString("title"),
                            rs.getString("content"),
                        };
                    }
                }
            }
        }
        return null;
    }

    private void deletePost(int postId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String query = "DELETE FROM Posts WHERE id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, postId);
                stmt.setLong(2, appGlobalState.getCurrentUser().getUserId());
                stmt.executeUpdate();
            }
        }
    }
}
