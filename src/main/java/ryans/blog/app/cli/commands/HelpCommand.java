package ryans.blog.app.cli.commands;

import picocli.CommandLine.Command;
import ryans.blog.app.cli.utils.ConsoleTheme;

@Command(name = "help")
public class HelpCommand implements Runnable {

    public static final String HELP_HEADER =
        ConsoleTheme.DIM_GREEN +
        "create: Use this to initialize creating a post\n" +
        "list: Use this to list all the posts in the blog\n" +
        "read: Use this with a -p id parameter to read a post\n" +
        "clear: Clear the console\n" +
        "exit: Exit the cli" +
        ConsoleTheme.RESET;

    @Override
    public void run() {
        System.out.println(HELP_HEADER);
    }
}
