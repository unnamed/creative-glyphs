package team.unnamed.creativeglyphs.content;

import net.kyori.adventure.text.Component;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.map.GlyphMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface ContentProcessor<T> {

    T process(T content, GlyphMap map, Predicate<Glyph> filter);

    default T process(T content, GlyphMap map) {
        // has permission to use all the glyphs
        return process(content, map, glyph -> true);
    }

    default List<T> process(List<T> content, GlyphMap map, Predicate<Glyph> filter) {
        List<T> processed = new ArrayList<>(content.size());
        for (T e : content) {
            processed.add(process(e, map, filter));
        }
        return processed;
    }

    static ContentProcessor<String> string() {
        return StringContentProcessor.INSTANCE;
    }

    static ContentProcessor<Component> component(GlyphRenderer<Component> representationProvider) {
        return new ComponentContentProcessor(representationProvider);
    }

}
