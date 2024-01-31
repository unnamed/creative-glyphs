package team.unnamed.creativeglyphs.plugin.integration.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.plugin.integration.PluginIntegration;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;

public final class MiniPlaceholdersIntegration implements PluginIntegration {
    private final PluginGlyphMap registry;

    public MiniPlaceholdersIntegration(final PluginGlyphMap registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull String plugin() {
        return "MiniPlaceholders";
    }

    @Override
    public void enable(final @NotNull Plugin hook) {
        Expansion.builder("unemoji")
                .globalPlaceholder("emoji", (queue, ctx) -> {
                    final Tag.Argument argument = queue.popOr("You need to provide an argument");
                    Glyph glyph = registry.getByName(argument.value());
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
