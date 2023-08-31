package team.unnamed.creativeglyphs.command;

import java.util.Arrays;
import java.util.List;

public class ArgumentStack {

    private final List<String> args;
    private int cursor;

    private ArgumentStack(List<String> args) {
        this.args = args;
    }

    public String next() {
        return args.get(cursor++);
    }

    public void back() {
        cursor--;
    }

    public String peek() {
        return args.get(cursor);
    }

    public int available() {
        return args.size() - cursor;
    }

    public boolean hasNext() {
        return cursor < args.size();
    }

    public static ArgumentStack of(String... args) {
        return new ArgumentStack(Arrays.asList(args));
    }

}
