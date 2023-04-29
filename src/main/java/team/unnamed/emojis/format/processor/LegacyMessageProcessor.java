package team.unnamed.emojis.format.processor;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.object.store.EmojiStore;
import team.unnamed.emojis.format.EmojiFormat;
import team.unnamed.emojis.format.representation.EmojiRepresentationProvider;
import team.unnamed.emojis.util.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class LegacyMessageProcessor implements MessageProcessor<String, BaseComponent[]> {

    private static final Pattern URL_PATTERN = Pattern.compile("^(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private static final BaseComponent[] EMPTY_COMPONENT_ARRAY = new BaseComponent[0];

    private final EmojiRepresentationProvider<TextComponent> representationProvider;

    LegacyMessageProcessor(EmojiRepresentationProvider<TextComponent> representationProvider) {
        this.representationProvider = representationProvider;
    }

    @Override
    public BaseComponent[] process(String message, EmojiStore registry, Predicate<Emoji> usageChecker) {
        List<TextComponent> components = new ArrayList<>();
        StringBuilder pre = new StringBuilder();
        StringBuilder name = new StringBuilder();
        TextComponent last = new TextComponent();

        textLoop:
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            Emoji literal = registry.getByCodePoint(c);

            if (literal != null && !usageChecker.test(literal)) {
                // player entered a literal emoji character,
                // and they do not have permissions to use
                // it, simply skip this character
                continue;
            }

            if (c == EmojiFormat.USAGE_START) {
                // found the start of an emoji, try to
                // replace it
                while (++i < message.length()) {
                    char current = message.charAt(i);

                    if (current != EmojiFormat.USAGE_END) {
                        // not the end of the emoji,
                        // append this character to
                        // the emoji name
                        name.append(current);
                    } else {
                        // end of the emoji found
                        if (name.length() < 1) {
                            pre.append(EmojiFormat.USAGE_START);
                            continue;
                        }

                        String nameStr = name.toString();
                        Emoji emoji = registry.getIgnoreCase(nameStr);

                        if (emoji == null || !usageChecker.test(emoji)) {
                            pre.append(EmojiFormat.USAGE_START).append(nameStr);

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

                        components.add(representationProvider.represent(emoji));
                        name.setLength(0);
                        continue textLoop;
                    }
                }

                pre.append(EmojiFormat.USAGE_END).append(name);
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

    @Override
    public String flatten(String message, EmojiStore registry) {
        // delegate to the String MessageProcessor
        return MessageProcessor.string().flatten(message, registry);
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
                if (current == 'x' && i + 12 < message.length() && Version.CURRENT.minor() >= 6) {
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

}
