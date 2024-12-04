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
import ryans.blog.dao.UserDAO;
import ryans.blog.model.User;

@Command(
    name = "login",
    description = "Login to your account to post, comment, etc"
)
public class LoginCommand implements Runnable {

    private Connection conn;
    private UserDAO userDao;

    public LoginCommand() {
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
        try {
            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

            System.out.println(
                ConsoleTheme.formatResponse("\nLogin to your account")
            );
            System.out.println(ConsoleTheme.formatResponse("----------------"));

            User user = null;
            while (user == null) {
                String usernameInput = reader.readLine(
                    ConsoleTheme.formatResponse("Username: ")
                );
                String passwordInput = reader.readLine(
                    ConsoleTheme.formatResponse("Password: "),
                    '*'
                );
                user = userDao.findByUsernameAndPassword(
                    usernameInput,
                    passwordInput
                );
                if (user == null) {
                    System.out.println(
                        ConsoleTheme.formatError("Invalid username or password")
                    );
                } else {
                    System.out.println(
                        ConsoleTheme.formatResponse("Login successful!")
                    );
                    // Set the logged-in user in AppState
                    AppGlobalState.getInstance().setCurrentUser(user);
                }
            }
        } catch (Exception e) {
            System.out.println(
                ConsoleTheme.formatError(
                    "Error during login: " + e.getMessage()
                )
            );
        }
    }
}
