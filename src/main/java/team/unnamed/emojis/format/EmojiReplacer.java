package team.unnamed.emojis.format;

import org.bukkit.ChatColor;
import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;

/**
 * Utility class for replacing emojis in strings
 * @author yusshu (Andre Roldan)
 */
public class EmojiReplacer {

    private static final String WHITE_PREFIX = ChatColor.WHITE.toString();

    /**
     * Replaces the emojis in the given {@code text}
     * if the given {@code permissible} has permission
     * to use them
     */
    public static String replace(
            Permissible permissible,
            EmojiRegistry registry,
            CharSequence text
    ) {

        StringBuilder builder = new StringBuilder();
        StringBuilder name = new StringBuilder();

        StringBuilder lastColors = new StringBuilder();

        textLoop:
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
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
            } else if (c == ':') {
                while (++i < text.length()) {
                    char current = text.charAt(i);
                    if (current == ':') {
                        if (name.length() < 1) {
                            builder.append(':');
                            continue;
                        }
                        String nameStr = name.toString();
                        Emoji emoji = registry.get(nameStr);
                        if (!Permissions.canUse(permissible, emoji)) {
                            builder
                                    .append(':')
                                    .append(nameStr);
                            name.setLength(0);
                            continue;
                        } else {
                            boolean previousColors = lastColors.length() > 0;
                            if (previousColors) {
                                builder.append(WHITE_PREFIX);
                            }
                            builder.append(emoji.getCharacter());

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
                builder.append(':').append(name);
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}