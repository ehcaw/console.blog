package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine.Command;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.ConsoleTheme;

@Command(name = "create", description = "Create a new blog post")
public class CreateCommand implements Runnable {

    @Override
    public void run() {
        try {
            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

            // Get post title
            System.out.println(
                ConsoleTheme.formatResponse("\nCreating new post")
            );
            System.out.println(ConsoleTheme.formatResponse("----------------"));
            String title = reader.readLine(
                ConsoleTheme.formatResponse("Title: ")
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
                savePost(title, content.toString().trim());
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

    private void savePost(String title, String content) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String query = "INSERT INTO Posts (title, content) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, title);
                stmt.setString(2, content);
                stmt.executeUpdate();
            }
        }
    }
}
