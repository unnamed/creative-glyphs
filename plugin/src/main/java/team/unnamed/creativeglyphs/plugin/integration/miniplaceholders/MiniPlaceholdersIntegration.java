package team.unnamed.creativeglyphs.plugin.integration.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.plugin.CreativeGlyphsPlugin;
import team.unnamed.creativeglyphs.plugin.integration.PluginIntegration;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;

import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

public final class MiniPlaceholdersIntegration implements PluginIntegration {
    private final CreativeGlyphsPlugin plugin;
    private boolean warnAboutDeprecation = true;

    public MiniPlaceholdersIntegration(final @NotNull CreativeGlyphsPlugin plugin) {
        this.plugin = requireNonNull(plugin, "plugin");
    }

    @Override
    public @NotNull String plugin() {
        return "MiniPlaceholders";
    }

    @Override
    public void enable(final @NotNull Plugin miniPlaceholders) {
        final var placeholder = (BiFunction<ArgumentQueue, Context, Tag>) (queue, ctx) -> {
            final var argument = queue.popOr("You need to provide an argument");
            final var glyph = plugin.registry().getByName(argument.value());
            if (glyph == null) {
                return null;
            } else {
                return Tag.selfClosingInserting(Component.text(glyph.replacement()));
            }
        };

        Expansion.builder("creativeglyphs")
                .globalPlaceholder("glyph", placeholder)
                .build()
                .register();

        Expansion.builder("unemoji")
                .globalPlaceholder("emoji", (queue, ctx) -> {
                    if (warnAboutDeprecation) {
                        plugin.getLogger().warning("(MiniPlaceholders Integration) Detected usage of deprecated" +
                                " placeholder format: '<unemoji_emoji:NAME>', please use '<creativeglyphs_glyph:NAME>' instead.");
                        warnAboutDeprecation = false;
                    }
                    return placeholder.apply(queue, ctx);
                })
                .build()
                .register();
    }
}
