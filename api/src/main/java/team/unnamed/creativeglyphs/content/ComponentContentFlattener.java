package team.unnamed.creativeglyphs.content;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.util.Patterns;

final class ComponentContentFlattener implements ContentFlattener<Component> {

    static final ContentFlattener<Component> FLATTEN_TO_SHORTER_USAGE = new ComponentContentFlattener(ContentFlattener.stringToShorterUsage());

    private final ContentFlattener<String> delegate;

    ComponentContentFlattener(ContentFlattener<String> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Component flatten(Component content, GlyphMap map) {
        return content.replaceText(TextReplacementConfig.builder()
                .match(Patterns.ANY)
                .replacement((result, builder) ->
                        // delegate to String MessageProcessor
                        builder.content(delegate.flatten(builder.content(), map)))
                .build());
    }

}
