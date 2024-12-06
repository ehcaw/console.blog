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

@Command(name = "edit", description = "Edit an existing blog post")
public class EditCommand implements Runnable {

    @Parameters(index = "0", description = "Post ID to edit")
    private int postId;

    private AppGlobalState appGlobalState = AppGlobalState.getInstance();

    @Override
    public void run() {
        try {
            // Check if user is logged in
            if (appGlobalState.getCurrentUser() == null) {
                System.out.println(
                    ConsoleTheme.formatError("Please login first to edit posts")
                );
                return;
            }

            // Fetch the post
            String[] postDetails = getPost(postId);
            if (postDetails == null) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "Post not found or you don't have permission to edit it"
                    )
                );
                return;
            }

            String currentTitle = postDetails[0];
            String currentContent = postDetails[1];
            String currentDescription = postDetails[2];

            // Get LineReader for input
            LineReader reader = TerminalUtil.getLineReader();

            // Show current post
            System.out.println(ConsoleTheme.formatResponse("\nCurrent post:"));
            System.out.println(
                ConsoleTheme.formatResponse(
                    "Description: " + currentDescription
                )
            );
            System.out.println(
                ConsoleTheme.formatPost(currentTitle, currentContent)
            );

            // Edit title
            System.out.println(
                ConsoleTheme.formatResponse(
                    "\nPress Enter to keep current title, or type new title:"
                )
            );
            String titleInput = reader.readLine(
                ConsoleTheme.formatResponse("Title [" + currentTitle + "]: ")
            );
            String newTitle = titleInput.isEmpty() ? currentTitle : titleInput;

            System.out.println(
                ConsoleTheme.formatResponse(
                    "\nPress Enter to keep current description, or type new description:"
                )
            );
            String descriptionInput = reader.readLine(
                ConsoleTheme.formatResponse(
                    "Description [" + currentDescription + "]: "
                )
            );
            String newDescription = descriptionInput.isEmpty()
                ? currentDescription
                : descriptionInput;

            // Edit content
            System.out.println(
                ConsoleTheme.formatResponse(
                    "\nCurrent content shown above." +
                    "\nPress Enter to keep current content, or type 'edit' to modify:"
                )
            );
            String contentChoice = reader.readLine(
                ConsoleTheme.formatResponse("Choice [keep/edit]: ")
            );

            String newContent = currentContent;
            if (contentChoice.toLowerCase().equals("edit")) {
                System.out.println(
                    ConsoleTheme.formatResponse(
                        "\nEnter new content (Type 'END' on a new line to finish):"
                    )
                );
                StringBuilder content = new StringBuilder();
                String line;
                while (!(line = reader.readLine("> ")).equals("END")) {
                    content.append(line).append("\n");
                }
                newContent = content.toString().trim();
            }

            // Show preview
            System.out.println(
                ConsoleTheme.formatResponse("\nPreview of changes:")
            );
            System.out.println(
                ConsoleTheme.formatResponse("Description: " + newDescription)
            );
            System.out.println(ConsoleTheme.formatPost(newTitle, newContent));

            // Confirm changes
            String confirm = reader.readLine(
                ConsoleTheme.formatResponse("Save these changes? (y/n): ")
            );

            if (confirm.toLowerCase().startsWith("y")) {
                updatePost(postId, newTitle, newContent, newDescription);
                System.out.println(
                    ConsoleTheme.formatResponse("Post updated successfully!")
                );
            } else {
                System.out.println(
                    ConsoleTheme.formatResponse(
                        "Edit cancelled. No changes were saved."
                    )
                );
            }
        } catch (Exception e) {
            System.out.println(
                ConsoleTheme.formatError(
                    "Error editing post: " + e.getMessage()
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
                            rs.getString("description"),
                            rs.getString("content"),
                        };
                    }
                }
            }
        }
        return null;
    }

    private void updatePost(
        int postId,
        String title,
        String content,
        String description
    ) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String query =
                "UPDATE Posts SET title = ?, content = ?, description = ? WHERE id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, title);
                stmt.setString(2, content);
                stmt.setString(3, description);
                stmt.setInt(4, postId);
                stmt.setLong(5, appGlobalState.getCurrentUser().getUserId());
                stmt.executeUpdate();
            }
        }
    }
}
