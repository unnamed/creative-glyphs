package team.unnamed.creativeglyphs.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.content.GlyphRenderer;

import static java.util.Objects.requireNonNull;

/**
 * A {@link GlyphRenderer} implementation that renders glyphs to {@link Component}s
 * using the {@link MiniMessage} format and taking everything from the plugin's
 * configuration.
 */
public final class ComponentGlyphRenderer implements GlyphRenderer<Component> {
    private final Plugin plugin;

    public ComponentGlyphRenderer(final @NotNull Plugin plugin) {
        this.plugin = requireNonNull(plugin, "plugin");
    }

    @Override
    public Component render(Glyph glyph) {
        final String format = plugin.getConfig().getString("emoji-format", null);

        if (format == null) {
            plugin.getLogger().warning("'emoji-format' is not set in the config.yml," +
                    " emoji rendering may not work as expected. This may be caused by an " +
                    " update that changed the configuration structure, please check the plugin's" +
                    " documentation and change-log for more information.");
            return Component.text(glyph.replacement());
        }

        return MiniMessage.miniMessage().deserialize(format,
                TagResolver.resolver("emoji", Tag.preProcessParsed(glyph.replacement())),
                TagResolver.resolver("emojiname", Tag.preProcessParsed(glyph.name())));
    }
}
