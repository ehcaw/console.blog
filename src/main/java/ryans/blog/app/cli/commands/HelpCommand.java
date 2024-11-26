package ryans.blog.app.cli.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import picocli.CommandLine.Command;
import ryans.blog.app.Database;
import ryans.blog.app.cli.utils.AsciiArt;
import ryans.blog.app.cli.utils.ConsoleColors;
import ryans.blog.app.cli.utils.ConsoleTheme;

@Command(name = "help")
public class HelpCommand implements Runnable {

    public static final String HELP_HEADER =
        ConsoleTheme.DIM_GREEN +
        "create: Use this to initialize creating a post\n" +
        "list: Use this to list all the posts in the blog\n" +
        "read: Use this with a -p id parameter to read a post\n" +
        "clear: Clear the console" +
        ConsoleTheme.RESET;

    @Override
    public void run() {
        System.out.println(HELP_HEADER);
    }
}
