package team.unnamed.emojis.format;

import java.util.regex.Pattern;

/**
 * Static utility class holding regular
 * expressions ("patterns") for this plugin
 */
public final class Patterns {

    /**
     * Pattern for matching emojis from a string
     */
    public static final Pattern EMOJI_PATTERN
            = Pattern.compile(":([A-Za-z_-]{1,14}):");

    private Patterns() {
    }

}
