package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

            // Get post title
            if (appGlobalState.getCurrentUser() == null) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "Please login before you attempt to post. Run the login command to get started"
                    )
                );
                return; // Return instead of throwing Error
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

            // Get post content with multi-line support
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

            // Confirm before saving
            System.out.println(ConsoleTheme.formatResponse("\nPreview:"));
            System.out.println(
                ConsoleTheme.formatPost(title, content.toString())
            );

            String confirm = reader.readLine(
                ConsoleTheme.formatResponse("Save this post? (y/n): ")
            );

            if (confirm.toLowerCase().startsWith("y")) {
                savePost(title, description, content.toString().trim());
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

    private void savePost(String title, String description, String content)
        throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String query =
                "INSERT INTO Posts (title, description, content, user_id ) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, title);
                stmt.setString(2, description);
                stmt.setString(3, content);
                stmt.setInt(4, appGlobalState.getCurrentUser().getUserId());
                stmt.executeUpdate();
            }
        }
    }
}
