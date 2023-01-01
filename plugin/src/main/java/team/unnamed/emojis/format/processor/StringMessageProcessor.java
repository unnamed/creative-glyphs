package team.unnamed.emojis.format.processor;

import org.bukkit.ChatColor;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiFormat;

import java.util.function.Predicate;

final class StringMessageProcessor implements MessageProcessor<String, String> {

    public static final MessageProcessor<String, String> INSTANCE = new StringMessageProcessor();

    private static final String WHITE_PREFIX = ChatColor.WHITE.toString();

    private StringMessageProcessor() {
    }

    @Override
    public String process(String text, EmojiRegistry registry, Predicate<Emoji> usageChecker) {

        // TODO: Make this be consistent with EMOJI_USAGE_PATTERN
        StringBuilder builder = new StringBuilder();
        StringBuilder name = new StringBuilder();

        StringBuilder lastColors = new StringBuilder();

        textLoop:
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            Emoji literal = registry.getByChar(c);
            if (literal != null && !usageChecker.test(literal)) {
                // player entered a literal emoji character,
                // and they do not have permissions to use
                // it, simply skip this character
                continue;
            }

            if (c == ChatColor.COLOR_CHAR) {
                if (i + 1 < text.length()) {
                    char code = text.charAt(++i);
                    ChatColor color = ChatColor.getByChar(code);

                    builder
                            .append(c)
                            .append(code);

                    if (color == null) {
                        continue;
                    }

                    if (color.isColor()) {
                        // reset if color
                        lastColors.setLength(0);
                    }

                    lastColors.append(color);
                } else {
                    builder.append(c);
                }
                continue;
            }

            if (c != EmojiFormat.USAGE_START) {
                builder.append(c);
                continue;
            }

            while (++i < text.length()) {
                char current = text.charAt(i);
                if (current == EmojiFormat.USAGE_END) {
                    if (name.length() < 1) {
                        builder.append(EmojiFormat.USAGE_START);
                        continue;
                    }
                    String nameStr = name.toString();
                    Emoji emoji = registry.getIgnoreCase(nameStr);

                    if (emoji == null || !usageChecker.test(emoji)) {
                        builder.append(EmojiFormat.USAGE_START).append(nameStr);
                        name.setLength(0);
                        continue;
                    } else {
                        boolean previousColors = lastColors.length() > 0;
                        if (previousColors) {
                            builder.append(WHITE_PREFIX);
                        }
                        builder.append(emoji.replacement());
                        if (previousColors) {
                            builder.append(lastColors);
                        }
                    }
                    name.setLength(0);
                    continue textLoop;
                } else {
                    name.append(current);
                }
            }

            builder.append(EmojiFormat.USAGE_START).append(name);
        }
        return builder.toString();
    }

    @Override
    public String flatten(String message, EmojiRegistry registry) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            int codepoint = message.codePointAt(i);

            if (!Character.isBmpCodePoint(codepoint)) {
                // two characters were used to represent this
                // codepoint so skip this thing
                i++;
            }

            Emoji emoji = registry.getByCodepoint(codepoint);

            if (emoji == null) {
                // codepoint did not represent an emoji, just append it
                builder.appendCodePoint(codepoint);
            } else {
                // codepoint represents an emoji, we must change it to its usage
                builder.append(EmojiFormat.usageOf(emoji));
            }
        }

        return builder.toString();
    }

}
