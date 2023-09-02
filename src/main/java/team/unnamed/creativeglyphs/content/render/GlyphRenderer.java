package team.unnamed.creativeglyphs.content.render;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.Glyph;

@FunctionalInterface
public interface GlyphRenderer<T> {

    T render(Glyph glyph);

    static GlyphRenderer<String> shorterUsage() {
        return glyph -> {
            String shorter = null;
            for (String usage : glyph.usages()) {
                if (shorter == null || usage.length() < shorter.length()) {
                    shorter = usage;
                }
            }
            return shorter;
        };
    }

    static GlyphRenderer<Component> component() {
        return glyph -> Component.text()
                .color(NamedTextColor.WHITE) // text color affects character texture
                .content(glyph.replacement()) // use the glyph character
                .build();
    }

    static GlyphRenderer<Component> component(Plugin plugin) {
        return new ComponentGlyphRenderer(plugin);
    }

}
