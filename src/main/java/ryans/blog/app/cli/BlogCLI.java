package ryans.blog.app.cli;

import static picocli.CommandLine.Command;

import java.io.IOException;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import ryans.blog.app.cli.commands.ClearCommand;
import ryans.blog.app.cli.commands.CommentCommand;
import ryans.blog.app.cli.commands.CreateCommand;
import ryans.blog.app.cli.commands.DeleteCommand;
import ryans.blog.app.cli.commands.EditCommand;
import ryans.blog.app.cli.commands.HelpCommand;
import ryans.blog.app.cli.commands.ListCommand;
import ryans.blog.app.cli.commands.LoginCommand;
import ryans.blog.app.cli.commands.ReadCommand;
import ryans.blog.app.cli.commands.SearchCommand;
import ryans.blog.app.cli.utils.ConsoleTheme;

@Command(
    name = "console.blog",
    subcommands = {
        ListCommand.class,
        CreateCommand.class,
        ReadCommand.class,
        HelpCommand.class,
        ClearCommand.class,
        LoginCommand.class,
        DeleteCommand.class,
        EditCommand.class,
        SearchCommand.class,
        CommentCommand.class,
    },
    description = "Retro-style console blog interface"
)
public class BlogCLI implements Runnable {

    private boolean running = true;
    private Terminal terminal;
    private LineReader lineReader;
    private CommandLine commandLine;

    public static void main(String[] args) {
        BlogCLI cli = new BlogCLI();
        CommandLine cmd = new CommandLine(cli);
        cli.commandLine = cmd;

        if (args.length > 0) {
            // Run in non-interactive mode
            System.exit(cmd.execute(args));
        } else {
            // Run in interactive mode
            cli.run();
        }
    }

    @Override
    public void run() {
        try {
            setupTerminal();
            showBootSequence();
            startInteractiveMode();
        } catch (IOException e) {
            System.err.println(ConsoleTheme.formatError(e.getMessage()));
        }
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void setupTerminal() throws IOException {
        terminal = TerminalBuilder.builder().system(true).build();

        Completer completer = new ArgumentCompleter(
            new StringsCompleter(
                "list",
                "create",
                "read",
                "exit",
                "help",
                "clear",
                "edit",
                "delete"
            )
        );

        lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(completer)
            .parser(new DefaultParser())
            .build();
    }

    private void showBootSequence() {
        clearScreen();
        System.out.println(ConsoleTheme.CONSOLE_HEADER);

        // Simulate boot sequence with delays
        for (String line : ConsoleTheme.BOOT_SEQUENCE.split("\n")) {
            System.out.println(line);
            try {
                Thread.sleep(200); // Add delay for effect
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void startInteractiveMode() {
        while (running) {
            try {
                String line = lineReader.readLine(ConsoleTheme.PROMPT);
                if (line.trim().isEmpty()) continue;

                if ("exit".equalsIgnoreCase(line.trim())) {
                    showShutdownSequence();
                    running = false;
                    continue;
                }

                commandLine.execute(line.split("\\s+"));
            } catch (UserInterruptException | EndOfFileException e) {
                showShutdownSequence();
                running = false;
            } catch (Exception e) {
                System.err.println(ConsoleTheme.formatError(e.getMessage()));
            }
        }
    }

    private void showShutdownSequence() {
        System.out.println(
            ConsoleTheme.formatResponse(
                """

                [SHUTDOWN] Saving system state...
                [SHUTDOWN] Closing database connection...
                [SHUTDOWN] Terminal interface terminated.
                [SHUTDOWN] System shutdown complete.

                C:\\> Goodbye!
                """
            )
        );
    }
}
