package team.unnamed.creativeglyphs.content;

import org.ahocorasick.trie.PayloadEmit;
import org.bukkit.ChatColor;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.map.GlyphMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

final class StringContentProcessor implements ContentProcessor<String> {

    public static final ContentProcessor<String> INSTANCE = new StringContentProcessor();

    private static final String WHITE_PREFIX = ChatColor.WHITE.toString();

    private StringContentProcessor() {
    }

    @Override
    public String process(String content, GlyphMap registry, Predicate<Glyph> filter) {

        Collection<PayloadEmit<Glyph>> emits = registry.trie().parseText(content);

        Iterator<PayloadEmit<Glyph>> emitIterator = emits.iterator();
        PayloadEmit<Glyph> emit = emitIterator.hasNext() ? emitIterator.next() : null;

        StringBuilder builder = new StringBuilder();

        StringBuilder lastColors = new StringBuilder();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            Glyph literal = registry.getByCodePoint(c);
            if (literal != null && !filter.test(literal)) {
                // player entered a literal emoji character,
                // and they do not have permissions to use
                // it, simply skip this character
                continue;
            }

            // check if emit is null, if the emit is null, that
            // means that we are not looking for an emoji usage,
            // so we can skip next processes.
            // It is null if, and only if:
            // - There aren't any emoji usages in the message
            // - We have finished processing all the emoji usages
            if (emit == null) {
                builder.append(c);
                continue;
            }

            // check for colors and formatting, so we keep
            // them after placing the emojis (they need to
            // have white color and no decoration)
            if (c == ChatColor.COLOR_CHAR) {
                if (i + 1 < content.length()) {
                    char code = content.charAt(++i);
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

            if (i < start) {
                builder.append(c);
                continue;
            }

            Glyph glyph = emit.getPayload();
            if (filter.test(glyph)) {
                boolean previousColors = lastColors.length() > 0;
                if (previousColors) {
                    builder.append(WHITE_PREFIX);
                }
                builder.append(glyph.replacement());
                if (previousColors) {
                    builder.append(lastColors);
                }

                // skip to the end of the emoji usage, we
                // have already replaced the emoji
                i += emit.getEnd() - start;
            }

            emit = emitIterator.hasNext() ? emitIterator.next() : null;
        }
        return builder.toString();
    }

}
