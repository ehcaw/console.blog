package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.ConsoleTheme;

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

    @Override
    public void run() {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT * FROM Posts WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query);) {
                stmt.setInt(1, postId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(
                            ConsoleTheme.formatPost(
                                rs.getString("title"),
                                rs.getString("content")
                            )
                        );
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
}
