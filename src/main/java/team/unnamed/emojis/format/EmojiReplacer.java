package team.unnamed.emojis.format;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.util.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for replacing emojis in strings
 * @author yusshu (Andre Roldan)
 */
public class EmojiReplacer {

    private static final char EMOJI_START = ':';
    private static final char EMOJI_END = ':';

    /**
     * Pattern for matching URLs
     */
    private static final Pattern URL_PATTERN
            = Pattern.compile("^(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?$");

    /**
     * Pattern for matching emojis from a string
     */
    public static final Pattern EMOJI_PATTERN
            = Pattern.compile(":([A-Za-z_-]{1,14}):");

    // convenience constant holding an empty component array
    private static final BaseComponent[] EMPTY_COMPONENT_ARRAY
            = new BaseComponent[0];

    private static final String WHITE_PREFIX = ChatColor.WHITE.toString();

    /**
     * Replaces the emojis in the given {@code text}
     * if the given {@code permissible} has permission
     * to use them
     */
    public static String replaceRawToRaw(
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

            Emoji literal = registry.getByChar(c);
            if (Permissions.canUse(permissible, literal)) {
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

            if (c != EMOJI_START) {
                builder.append(c);
                continue;
            }

            while (++i < text.length()) {
                char current = text.charAt(i);
                if (current == EMOJI_END) {
                    if (name.length() < 1) {
                        builder.append(EMOJI_START);
                        continue;
                    }
                    String nameStr = name.toString();
                    Emoji emoji = registry.get(nameStr);

                    if (!Permissions.canUse(permissible, emoji)) {
                        builder.append(EMOJI_START).append(nameStr);
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

            builder.append(EMOJI_START).append(name);
        }
        return builder.toString();
    }

    private static void fromLegacyText(
            String message,
            List<TextComponent> components,
            TextComponent component
    ) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = URL_PATTERN.matcher(message);

        for (int i = 0; i < message.length(); i++) {
            char current = message.charAt(i);
            if (current == net.md_5.bungee.api.ChatColor.COLOR_CHAR) {
                if (++i >= message.length()) {
                    break;
                }

                current = message.charAt(i);

                // basically, a toLowerCase()
                if (current >= 'A' && current <= 'Z') {
                    current += 32;
                }

                net.md_5.bungee.api.ChatColor format;
                if (current == 'x' && i + 12 < message.length() && Version.CURRENT.getMinor() >= 6) {
                    StringBuilder hex = new StringBuilder("#");
                    for (int j = 0; j < 6; j++) {
                        hex.append(message.charAt(i + 2 + (j * 2)));
                    }
                    //try {
                    //    format = net.md_5.bungee.api.ChatColor.of(hex.toString());
                    //} catch (IllegalArgumentException ex) {
                        format = null;
                    //}

                    i += 12;
                } else {
                    format = net.md_5.bungee.api.ChatColor.getByChar(current);
                }

                if (format == null) {
                    continue;
                }

                if (builder.length() > 0) {
                    TextComponent old = component;
                    component = new TextComponent(old);
                    old.setText(builder.toString());
                    builder = new StringBuilder();
                    components.add(old);
                }
                if (format == net.md_5.bungee.api.ChatColor.BOLD) {
                    component.setBold(true);
                } else if (format == net.md_5.bungee.api.ChatColor.ITALIC) {
                    component.setItalic(true);
                } else if (format == net.md_5.bungee.api.ChatColor.UNDERLINE) {
                    component.setUnderlined(true);
                } else if (format == net.md_5.bungee.api.ChatColor.STRIKETHROUGH) {
                    component.setStrikethrough(true);
                } else if (format == net.md_5.bungee.api.ChatColor.MAGIC) {
                    component.setObfuscated(true);
                } else if (format == net.md_5.bungee.api.ChatColor.RESET) {
                    format = net.md_5.bungee.api.ChatColor.GRAY;
                    component = new TextComponent();
                    component.setColor(format);
                } else {
                    component = new TextComponent();
                    component.setColor(format);
                }
                continue;
            }

            int pos = message.indexOf(' ', i);
            if (pos == -1) {
                pos = message.length();
            }
            if (matcher.region(i, pos).find()) { //Web link handling
                if (builder.length() > 0) {
                    TextComponent old = component;
                    component = new TextComponent(old);
                    old.setText(builder.toString());
                    builder = new StringBuilder();
                    components.add(old);
                }

                TextComponent old = component;
                component = new TextComponent(old);
                String urlString = message.substring(i, pos);
                component.setText(urlString);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        urlString.startsWith( "http" ) ? urlString : "https://" + urlString ) );
                components.add(component);
                i += pos - i - 1;
                component = old;
                continue;
            }
            builder.append(current);
        }

        component.setText(builder.toString());
        components.add(component);
    }

    public static BaseComponent[] replaceRawToRich(
            Permissible permissible,
            EmojiRegistry registry,
            String message,
            EmojiComponentProvider emojiComponentProvider
    ) {

        List<TextComponent> components = new ArrayList<>();
        StringBuilder pre = new StringBuilder();
        StringBuilder name = new StringBuilder();
        TextComponent last = new TextComponent();

        textLoop:
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            Emoji literal = registry.getByChar(c);

            if (Permissions.canUse(permissible, literal)) {
                // player entered a literal emoji character,
                // and they do not have permissions to use
                // it, simply skip this character
                continue;
            }

            if (c == EMOJI_START) {
                // found the start of an emoji, try to
                // replace it
                while (++i < message.length()) {
                    char current = message.charAt(i);

                    if (current != EMOJI_END) {
                        // not the end of the emoji,
                        // append this character to
                        // the emoji name
                        name.append(current);
                    } else {
                        // end of the emoji found
                        if (name.length() < 1) {
                            pre.append(EMOJI_START);
                            continue;
                        }

                        String nameStr = name.toString();
                        Emoji emoji = registry.get(nameStr);

                        if (!Permissions.canUse(permissible, emoji)) {
                            pre.append(EMOJI_START).append(nameStr);

                            // continue searching for emojis, starting
                            // from the end of this invalid emoji
                            name.setLength(0);
                            continue;
                        }

                        // so there's text within this emoji and the previous emoji (or text start)
                        String previous = pre.toString();
                        fromLegacyText(previous, components, new TextComponent(last));
                        pre.setLength(0);
                        last = components.get(components.size() - 1);

                        components.add(emojiComponentProvider.toComponent(emoji));
                        name.setLength(0);
                        continue textLoop;
                    }
                }
                pre.append(EMOJI_END).append(name);
                continue;
            }

            // append normal character to the builder
            pre.append(c);
        }

        // append remaining text
        if (pre.length() > 0) {
            fromLegacyText(
                    pre.toString(),
                    components,
                    new TextComponent(last)
            );
        }

        return components.toArray(EMPTY_COMPONENT_ARRAY);
    }

}