package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine.Command;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.AsciiArt;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;

@Command(name = "clear")
public class ClearCommand implements Runnable {

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    @Override
    public void run() {
        clearScreen();
        System.out.println(ConsoleTheme.CONSOLE_HEADER);
    }
}
