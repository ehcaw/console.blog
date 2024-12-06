package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.SQLException;
import org.jline.reader.LineReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ryans.blog.app.AppGlobalState;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;
import ryans.blog.app.cli.utils.TerminalUtil;
import ryans.blog.dao.CommentDAO;
import ryans.blog.dao.PostDAO;
import ryans.blog.model.Comment;
import ryans.blog.model.Post;

@Command(
    name = "comment",
    description = "Add a comment to the post you're currently reading"
)
public class CommentCommand implements Runnable {

    private static Integer currentlyReadingPostId = null;

    // Method to set the current post being read
    public static void setCurrentPost(int postId) {
        currentlyReadingPostId = postId;
    }

    private Connection conn;
    private CommentDAO commentDao;
    private PostDAO postDao;
    private AppGlobalState appState;

    public CommentCommand() {
        try {
            this.conn = Database.getConnection();
            this.commentDao = new CommentDAO(conn);
            this.postDao = new PostDAO(conn);
            this.appState = AppGlobalState.getInstance();
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
        try {
            // Check if user is logged in
            if (appState.getCurrentUser() == null) {
                System.out.println(
                    ConsoleTheme.formatError("Please login first to comment")
                );
                return;
            }

            // Check if user is currently reading a post
            if (currentlyReadingPostId == null) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "You must be reading a post to comment on it.\n" +
                        "Use 'read <post-id>' command first."
                    )
                );
                return;
            }

            // Verify the post exists
            Post post = postDao.findById(currentlyReadingPostId.intValue());
            if (post == null) {
                System.out.println(ConsoleTheme.formatError("Post not found"));
                currentlyReadingPostId = null;
                return;
            }

            // Get comment content from user
            LineReader reader = TerminalUtil.getLineReader();
            System.out.println(
                ConsoleTheme.formatResponse(
                    "\nAdding comment to post #" + currentlyReadingPostId
                )
            );
            System.out.println(
                ConsoleTheme.formatResponse(
                    "Enter your comment (Type 'END' on a new line to finish):"
                )
            );

            StringBuilder content = new StringBuilder();
            String line;
            while (!(line = reader.readLine("> ")).equals("END")) {
                content.append(line).append("\n");
            }

            if (content.length() == 0) {
                System.out.println(
                    ConsoleTheme.formatError("Comment cannot be empty")
                );
                return;
            }

            // Create and save the comment
            Comment comment = new Comment();
            comment.setContent(content.toString().trim());
            comment.setPostId(currentlyReadingPostId);
            comment.setUserId(appState.getCurrentUser().getUserId());

            Comment createdComment = commentDao.create(comment);

            System.out.println(
                ConsoleTheme.formatResponse("\nComment added successfully!")
            );

            // Display the comment
            System.out.println(
                ConsoleColors.CYAN +
                "  ┌─────────────────────" +
                ConsoleColors.RESET
            );
            System.out.printf(
                "  │ %s%s%s\n",
                ConsoleColors.YELLOW_BOLD,
                appState.getCurrentUser().getUsername(),
                ConsoleColors.RESET
            );
            System.out.println("  │ " + createdComment.getContent());
            System.out.println(
                ConsoleColors.CYAN +
                "  └─────────────────────" +
                ConsoleColors.RESET
            );
        } catch (Exception e) {
            System.out.println(
                ConsoleTheme.formatError(
                    "Error adding comment: " + e.getMessage()
                )
            );
        }
    }
}
