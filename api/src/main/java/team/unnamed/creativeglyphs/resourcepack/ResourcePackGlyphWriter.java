package team.unnamed.creativeglyphs.resourcepack;

import net.kyori.adventure.key.Key;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.central.event.EventListener;
import team.unnamed.creative.central.event.pack.ResourcePackGenerateEvent;
import team.unnamed.creative.font.Font;
import team.unnamed.creative.font.FontProvider;
import team.unnamed.creative.texture.Texture;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.map.GlyphMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResourcePackGlyphWriter implements EventListener<ResourcePackGenerateEvent> {

    private final GlyphMap glyphMap;

    public ResourcePackGlyphWriter(GlyphMap glyphMap) {
        this.glyphMap = glyphMap;
    }

    @Override
    public void on(ResourcePackGenerateEvent event) {
        ResourcePack resourcePack = event.resourcePack();

        Collection<Glyph> glyphs = glyphMap.values();
        List<FontProvider> providers = new ArrayList<>(glyphs.size());

        for (Glyph glyph : glyphs) {

            Key textureKey = Key.key("emojis/" + glyph.name());

            // write emoji image
            resourcePack.texture(
                    Texture.texture()
                            .key(textureKey)
                            .data(glyph.data())
                            .build()
            );

            // add a provider for this glyph
            providers.add(
                    FontProvider.bitMap()
                            .height(glyph.height())
                            .ascent(glyph.ascent())
                            .characters(glyph.replacement())
                            .file(textureKey)
                            .build()
            );
        }

        // write the default.json font
        // keep old font providers
        Font defaultFont = resourcePack.font(Font.MINECRAFT_DEFAULT);
        if (defaultFont != null) {
            providers.addAll(defaultFont.providers());
        }
        resourcePack.font(Font.font(Font.MINECRAFT_DEFAULT, providers));
    }

}
