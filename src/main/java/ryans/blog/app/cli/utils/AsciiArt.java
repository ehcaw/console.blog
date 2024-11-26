package ryans.blog.app.cli.utils;

public class AsciiArt {

    public static final String BLOG_HEADER =
        ConsoleTheme.BRIGHT_GREEN +
        " .d8888b.                                  888            888888b.   888                   \n" +
        "d88P  Y88b                                 888            888  \"88b  888                   \n" +
        "888    888                                 888            888  .88P  888                   \n" +
        "888         .d88b.  88888b.  .d8888b   .d88888  .d88b.  8888888K.  888  .d88b.   .d88b.  \n" +
        "888        d88\"\"88b 888 \"88b 88K      d88\" 888 d8P  Y8b 888  \"Y88b 888 d88\"\"88b d88P\"88b \n" +
        "888    888 888  888 888  888 \"Y8888b. 888  888 88888888 888    888 888 888  888 888  888 \n" +
        "Y88b  d88P Y88..88P 888  888      X88 Y88b 888 Y8b.     888   d88P 888 Y88..88P Y88b 888 \n" +
        " \"Y8888P\"   \"Y88P\"  888  888  88888P'  \"Y88888  \"Y8888  8888888P\"  888  \"Y88P\"   \"Y88888 \n" +
        "                                                                                        888 \n" +
        "                                                                                   Y8b d88P \n" +
        "                                                                                    \"Y88P\"  \n" +
        ConsoleTheme.RESET;

    public static final String WELCOME_MESSAGE =
        ConsoleTheme.GREEN_SCREEN +
        "+------------------------------------------+\n" +
        "|         Welcome to Console.Blog!         |\n" +
        "+------------------------------------------+\n" +
        ConsoleTheme.RESET;

    public static final String HELP_HEADER =
        ConsoleTheme.GREEN_SCREEN +
        "+------------------------------------------+\n" +
        "|           Available Commands             |\n" +
        "+------------------------------------------+\n" +
        ConsoleTheme.RESET;

    public static final String DIVIDER =
        ConsoleTheme.GREEN_SCREEN +
        "+------------------------------------------+" +
        ConsoleTheme.RESET;

    public static String formatPostTitle(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(ConsoleTheme.BRIGHT_GREEN);
        sb.append("+------------------------------------------+\n");
        sb.append("| ").append(centerText(title, 40)).append(" |\n");
        sb.append("+------------------------------------------+\n");
        sb.append(ConsoleTheme.RESET);
        return sb.toString();
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            sb.append(" ");
        }
        sb.append(text);
        for (int i = 0; i < width - text.length() - padding; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
