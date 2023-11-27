package team.unnamed.creativeglyphs.plugin.hook.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;

import java.util.Objects;

public final class MiniPlaceholdersHook implements PluginHook {
    private final GlyphMap registry;

    public MiniPlaceholdersHook(final @NotNull GlyphMap registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public @NotNull String pluginName() {
        return "MiniPlaceholders";
    }

    @Override
    public void hook(final @NotNull Plugin hook) {
        // usage: <glyphs_glyph:name>
        Expansion.builder("glyphs")
                .globalPlaceholder("glyph", (queue, ctx) -> {
                    final Tag.Argument argument = queue.popOr("You need to provide an argument");
                    final Glyph glyph = registry.getByName(argument.value());
                    if (glyph == null) {
                        return null;
                    } else {
                        return Tag.selfClosingInserting(Component.text(glyph.replacement()));
                    }
                })
                .build()
                .register();
    }
}
