package team.unnamed.creativeglyphs.content;

import net.kyori.adventure.text.Component;
import team.unnamed.creativeglyphs.content.render.GlyphRenderer;
import team.unnamed.creativeglyphs.map.GlyphMap;

public interface ContentFlattener<T> {

    T flatten(T content, GlyphMap map);

    static ContentFlattener<String> stringToShorterUsage() {
        return StringContentFlattener.FLATTEN_TO_SHORTER_USAGE;
    }

    static ContentFlattener<String> string(GlyphRenderer<String> renderer) {
        return new StringContentFlattener(renderer);
    }

    static ContentFlattener<Component> componentToShorterUsage() {
        return ComponentContentFlattener.FLATTEN_TO_SHORTER_USAGE;
    }

    static ContentFlattener<Component> componentToString(GlyphRenderer<String> renderer) {
        return new ComponentContentFlattener(string(renderer));
    }

}
