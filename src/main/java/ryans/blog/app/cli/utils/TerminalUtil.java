package ryans.blog.app.cli.utils;

import java.io.IOException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class TerminalUtil {

    private static Terminal terminal;
    private static LineReader lineReader;

    static {
        System.setProperty("org.jline.terminal.dumb", "true");
    }

    public static LineReader getLineReader() throws IOException {
        if (lineReader == null) {
            terminal = TerminalBuilder.terminal();
            lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        }
        return lineReader;
    }
}
