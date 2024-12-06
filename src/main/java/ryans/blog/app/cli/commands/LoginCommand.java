package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.SQLException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ryans.blog.app.AppGlobalState;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.ConsoleTheme;
import ryans.blog.dao.UserDAO;
import ryans.blog.model.User;

@Command(
    name = "login",
    description = "Login to your account or create a new one"
)
public class LoginCommand implements Runnable {

    @Option(names = "--create", description = "Create a new account")
    private boolean createAccount = false;

    private Connection conn;
    private UserDAO userDao;
    private LineReader reader;

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
            // Initialize database connection and UserDAO
            Connection conn = Database.getConnection();
            this.userDao = new UserDAO(conn);

            // Setup terminal
            System.setProperty("org.jline.terminal.dumb", "true");
            Terminal terminal = TerminalBuilder.terminal();
            reader = LineReaderBuilder.builder().terminal(terminal).build();

            if (createAccount) {
                handleCreateAccount();
            } else {
                handleLogin();
            }
        } catch (SQLException e) {
            System.out.println(
                ConsoleTheme.formatError("Database error: " + e.getMessage())
            );
        } catch (Exception e) {
            System.out.println(
                ConsoleTheme.formatError("Error: " + e.getMessage())
            );
        }
    }

    private void handleLogin() throws SQLException { // Add throws declaration
        System.out.println(
            ConsoleTheme.formatResponse("\nLogin to your account")
        );
        System.out.println(ConsoleTheme.formatResponse("----------------"));

        User user = null;
        while (user == null) {
            String usernameInput = reader.readLine(
                ConsoleTheme.formatResponse("Username: ")
            );

            // Check if user wants to create account instead
            if (usernameInput.equalsIgnoreCase("create")) {
                handleCreateAccount();
                return;
            }

            // Check if user wants to exit
            if (usernameInput.equalsIgnoreCase("exit")) {
                return;
            }

            String passwordInput = reader.readLine(
                ConsoleTheme.formatResponse("Password: "),
                '*'
            );

            try {
                user = userDao.findByUsernameAndPassword(
                    usernameInput,
                    passwordInput
                );

                if (user == null) {
                    System.out.println(
                        ConsoleTheme.formatError(
                            "Invalid username or password\n" +
                            "Type 'create' to create a new account or 'exit' to cancel"
                        )
                    );
                } else {
                    System.out.println(
                        ConsoleTheme.formatResponse(
                            String.format(
                                "Login successful! Welcome %s",
                                user.getUsername()
                            )
                        )
                    );
                    AppGlobalState.getInstance().setCurrentUser(user);
                }
            } catch (SQLException e) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "Database error during login: " + e.getMessage()
                    )
                );
                return; // Exit the login process on database error
            }
        }
    }

    private void handleCreateAccount() {
        System.out.println(
            ConsoleTheme.formatResponse("\nCreate a new account")
        );
        System.out.println(ConsoleTheme.formatResponse("------------------"));

        while (true) {
            String username = reader.readLine(
                ConsoleTheme.formatResponse("Choose username (min 3 chars): ")
            );

            // Allow user to cancel account creation
            if (username.equalsIgnoreCase("exit")) {
                System.out.println(
                    ConsoleTheme.formatResponse("Account creation cancelled")
                );
                return;
            }

            // Validate username
            if (username.length() < 3) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "Username must be at least 3 characters long"
                    )
                );
                continue;
            }

            try {
                // Check if username already exists
                if (userDao.findByUsername(username) != null) {
                    System.out.println(
                        ConsoleTheme.formatError(
                            "Username already exists. Please choose another one\n" +
                            "Type 'exit' to cancel"
                        )
                    );
                    continue;
                }

                // Get and validate password
                String password = reader.readLine(
                    ConsoleTheme.formatResponse(
                        "Choose password (min 6 chars): "
                    ),
                    '*'
                );

                if (password.length() < 6) {
                    System.out.println(
                        ConsoleTheme.formatError(
                            "Password must be at least 6 characters long"
                        )
                    );
                    continue;
                }

                // Confirm password
                String confirmPassword = reader.readLine(
                    ConsoleTheme.formatResponse("Confirm password: "),
                    '*'
                );

                if (!password.equals(confirmPassword)) {
                    System.out.println(
                        ConsoleTheme.formatError("Passwords don't match")
                    );
                    continue;
                }

                // Create the user
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);

                User createdUser = userDao.create(newUser);
                System.out.println(
                    ConsoleTheme.formatResponse(
                        "\nAccount created successfully!"
                    )
                );

                // Automatically log in the new user
                AppGlobalState.getInstance().setCurrentUser(createdUser);
                System.out.println(
                    ConsoleTheme.formatResponse(
                        String.format("Welcome %s!", username)
                    )
                );
                return;
            } catch (SQLException e) {
                System.out.println(
                    ConsoleTheme.formatError(
                        "Error creating account: " + e.getMessage()
                    )
                );
                return;
            }
        }
    }
}
