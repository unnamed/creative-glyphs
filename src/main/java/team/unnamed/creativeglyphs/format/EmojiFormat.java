package team.unnamed.creativeglyphs.format;

import org.intellij.lang.annotations.RegExp;
import team.unnamed.creativeglyphs.Emoji;

import java.util.regex.Pattern;

/**
 * Utility class for replacing emojis in strings
 * @author yusshu (Andre Roldan)
 */
public final class EmojiFormat {

    public static final char USAGE_START = ':';
    public static final char USAGE_END = ':';

    public static final @RegExp String NAME_PATTERN_STR = "[a-z0-9_]{1,32}";
    public static final @RegExp String PERMISSION_PATTERN_STR = "[A-Za-z0-9_.]*";

    // The exact same that EMOJI_NAME_PATTERN_STRING but accepting uppercase characters
    public static final @RegExp String USAGE_PATTERN_STR = USAGE_START + "([A-Za-z0-9_]{1,32})" + USAGE_END;

    public static final Pattern PERMISSION_PATTERN = Pattern.compile(PERMISSION_PATTERN_STR);
    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_STR);
    public static final Pattern USAGE_PATTERN = Pattern.compile(USAGE_PATTERN_STR);

    private EmojiFormat() {
    }

    public static String usageOf(Emoji emoji) {
        return usageOf(emoji.name());
    }

    public static String usageOf(String emojiName) {
        return USAGE_START + emojiName + USAGE_END;
    }

}