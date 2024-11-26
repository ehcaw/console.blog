package ryans.blog.app.cli.utils;

public class ConsoleTheme {

    public static final String GREEN_SCREEN = "\033[0;32m";
    public static final String BRIGHT_GREEN = "\033[1;32m";
    public static final String DIM_GREEN = "\033[2;32m";
    public static final String RESET = "\033[0m";

    public static final String CONSOLE_HEADER =
        BRIGHT_GREEN +
        " _____                       _       ______ _             \n" +
        "/  __ \\                     | |      | ___ \\ |            \n" +
        "| /  \\/ ___  _ __  ___  ___ | | ___  | |_/ / | ___   __ _ \n" +
        "| |    / _ \\| '_ \\/ __|/ _ \\| |/ _ \\ | ___ \\ |/ _ \\ / _` |\n" +
        "| \\__/\\ (_) | | | \\__ \\ (_) | |  __/_| |_/ / | (_) | (_| |\n" +
        "\\____/\\___/|_| |_|___/\\___/|_|\\___(_)____/|_|\\___/ \\__, |\n" +
        "                                                    __/ |\n" +
        "                                                   |___/ \n" +
        RESET;

    public static final String BOOT_SEQUENCE =
        GREEN_SCREEN +
        "[SYSTEM] Initializing Console.Blog v1.0...\n" +
        "[SYSTEM] Loading components...\n" +
        "[SYSTEM] Establishing database connection...\n" +
        "[SYSTEM] Terminal interface ready.\n" +
        "[SYSTEM] System initialized successfully.\n\n" +
        "> Type 'help' for available commands\n" +
        RESET;

    public static final String PROMPT =
        GREEN_SCREEN + "console.blog>" + RESET + " ";

    public static String formatCommand(String cmd) {
        return BRIGHT_GREEN + cmd + RESET;
    }

    public static String formatResponse(String text) {
        return GREEN_SCREEN + text + RESET;
    }

    public static String formatError(String text) {
        return DIM_GREEN + "ERROR: " + text + RESET;
    }

    public static String formatPost(String title, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append(BRIGHT_GREEN);
        sb.append("+------------------------------------------+\n");
        sb.append("| ").append(centerText(title, 40)).append(" |\n");
        sb.append("+------------------------------------------+\n");
        sb.append(formatContent(content)).append("\n");
        sb.append("+------------------------------------------+\n");
        sb.append(RESET);
        return sb.toString();
    }

    public static String formatContent(String content) {
        return GREEN_SCREEN + content.replace("\n", "\n  ") + RESET;
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

    // Additional utility methods for consistent formatting
    public static String formatListItem(int index, String text) {
        return GREEN_SCREEN + String.format(" [%d] %s\n", index, text) + RESET;
    }

    public static String formatDivider() {
        return (
            GREEN_SCREEN +
            "+------------------------------------------+\n" +
            RESET
        );
    }
}
