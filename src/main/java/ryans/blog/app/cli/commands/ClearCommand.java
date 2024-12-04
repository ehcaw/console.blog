package ryans.blog.app.cli.commands;

import picocli.CommandLine.Command;
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
