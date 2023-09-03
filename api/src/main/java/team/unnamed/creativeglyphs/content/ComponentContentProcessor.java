package team.unnamed.creativeglyphs.content;

import net.kyori.adventure.text.Component;
import org.ahocorasick.trie.PayloadEmit;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.util.Patterns;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

final class ComponentContentProcessor implements ContentProcessor<Component> {

    private final GlyphRenderer<Component> representationProvider;

    ComponentContentProcessor(GlyphRenderer<Component> representationProvider) {
        this.representationProvider = representationProvider;
    }

    @Override
    @SuppressWarnings("DuplicatedCode") // detects comments as duplicated code too
    public Component process(Component content, GlyphMap registry, Predicate<Glyph> filter) {
        return content.replaceText(config -> config
                .match(Patterns.ANY)
                .replacement((result, componentBuilder) -> {
                    String text = componentBuilder.content();

                    Collection<PayloadEmit<Glyph>> emits = registry.trie().parseText(text);

                    // set empty content
                    componentBuilder.content("");

                    Iterator<PayloadEmit<Glyph>> emitIterator = emits.iterator();
                    PayloadEmit<Glyph> emit = emitIterator.hasNext() ? emitIterator.next() : null;

                    StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);

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

                        int start = emit.getStart();

                        if (i < start) {
                            builder.append(c);
                            continue;
                        }

                        Glyph glyph = emit.getPayload();
                        if (filter.test(glyph)) {

                            if (builder.length() > 0) {
                                componentBuilder.append(Component.text(builder.toString()));
                                builder.setLength(0);
                            }

                            Component insertion = representationProvider.render(glyph);
                            componentBuilder.append(insertion);

                            // skip to the end of the emoji usage, we
                            // have already replaced the emoji
                            i += emit.getEnd() - start;
                        }

                        emit = emitIterator.hasNext() ? emitIterator.next() : null;
                    }

                    if (builder.length() > 0) {
                        if (componentBuilder.children().isEmpty()) {
                            componentBuilder.content(builder.toString());
                            return componentBuilder;
                        } else {
                            componentBuilder.append(Component.text(builder.toString()));
                        }
                    }

                    return componentBuilder;
                }));
    }

}
