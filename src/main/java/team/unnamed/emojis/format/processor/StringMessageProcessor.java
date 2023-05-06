package team.unnamed.emojis.format.processor;

import org.ahocorasick.trie.PayloadEmit;
import org.bukkit.ChatColor;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.object.store.EmojiStore;
import team.unnamed.emojis.format.EmojiFormat;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

final class StringMessageProcessor implements MessageProcessor<String, String> {

    public static final MessageProcessor<String, String> INSTANCE = new StringMessageProcessor();

    private static final String WHITE_PREFIX = ChatColor.WHITE.toString();

    private StringMessageProcessor() {
    }

    @Override
    public String process(String text, EmojiStore registry, Predicate<Emoji> usageChecker) {

        Collection<PayloadEmit<Emoji>> emits = registry.trie().parseText(text);
        if (emits.isEmpty()) {
            // no Emoji emits for this text, return it as-is
            return text;
        }

        Iterator<PayloadEmit<Emoji>> emitIterator = emits.iterator();
        PayloadEmit<Emoji> emit = emitIterator.next();

        StringBuilder builder = new StringBuilder();

        StringBuilder lastColors = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            Emoji literal = registry.getByCodePoint(c);
            if (literal != null && !usageChecker.test(literal)) {
                // player entered a literal emoji character,
                // and they do not have permissions to use
                // it, simply skip this character
                continue;
            }

            // check for colors and formatting, so we keep
            // them after placing the emojis (they need to
            // have white color and no decoration)
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
                    // a color char used at the end of the message
                    builder.append(c);
                }
                continue;
            }

            int start = emit.getStart();
            int end = emit.getEnd();

            if (i < start) {
                builder.append(c);
                continue;
            }

            Emoji emoji = emit.getPayload();
            if (usageChecker.test(emoji)) {
                boolean previousColors = lastColors.length() > 0;
                if (previousColors) {
                    builder.append(WHITE_PREFIX);
                }
                builder.append(emoji.replacement());
                if (previousColors) {
                    builder.append(lastColors);
                }

                i += end - start;
            } else {
                // no permission, do not replace
            }

            if (emitIterator.hasNext()) {
                emit = emitIterator.next();
            } else {
                // no more emojis to replace, just append
                // the rest of the text and break the loop
                builder.append(text, i + 1, text.length());
                break;
            }
        }
        return builder.toString();
    }

    @Override
    public String flatten(String message, EmojiStore registry) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            int codePoint = message.codePointAt(i);

            if (!Character.isBmpCodePoint(codePoint)) {
                // two characters were used to represent this
                // code point so skip this thing
                i++;
            }

            Emoji emoji = registry.getByCodePoint(codePoint);

            if (emoji == null) {
                // code point did not represent an emoji, just append it
                builder.appendCodePoint(codePoint);
            } else {
                // code point represents an emoji, we must change it to its usage
                builder.append(EmojiFormat.usageOf(emoji));
            }
        }

        return builder.toString();
    }

}
