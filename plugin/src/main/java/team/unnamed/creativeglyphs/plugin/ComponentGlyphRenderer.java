package team.unnamed.creativeglyphs.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.content.GlyphRenderer;

import static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver;

public final class ComponentGlyphRenderer
        implements GlyphRenderer<Component> {

    private final Plugin plugin;
    private final boolean isMiniMessageAvailable;
    private boolean infoSupportMiniMessage = true;

    public ComponentGlyphRenderer(Plugin plugin) {
        this.plugin = plugin;
        this.isMiniMessageAvailable = isMiniMessageAvailable();

        if (isMiniMessageAvailable) {
            plugin.getLogger().info("MiniMessage format is supported by the server, " +
                    "trying to use it for emoji replacement...");
        }
    }

    @Override
    public Component render(Glyph glyph) {

        ConfigurationSection config = plugin.getConfig();

        Component hoverComponent = null;
        if (isMiniMessageAvailable) {
            String source = config.getString("format.hover.mini-message", null);
            if (source != null) {
                hoverComponent = MiniMessage.miniMessage().deserialize(
                        source,
                        resolver("emoji", Tag.selfClosingInserting(emojiComponent(glyph))),
                        resolver("emojiname", Tag.inserting(Component.text(glyph.name())))
                );
            } else if (infoSupportMiniMessage
                    && (config.getString("format.hover.legacy", null) != null
                    || config.getString("format.paper.emoji", null) != null)) {
                // MiniMessage is supported but mini-message format is not set, warn
                plugin.getLogger().warning("MiniMessage format is supported by the server, " +
                        "but you are not using it, we recommend using it " +
                        "(De-comment 'format.hover.mini-message' in config.yml)");
                infoSupportMiniMessage = false;
            }
        }

        if (hoverComponent == null) {
            String legacyFormat = config.getString("format.hover.legacy", null);

            if (legacyFormat == null) {
                // Try with the old path (format.paper.emoji)
                // for backwards compatibility
                legacyFormat = config.getString("format.paper.emoji", null);
            }

            if (legacyFormat != null) {
                hoverComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(
                        legacyFormat
                                .replace("<emoji>", glyph.replacement())
                                .replace("<emojiname>", glyph.name())
                );
            }
        }

        TextComponent.Builder builder = emojiComponent(glyph);

        // Using a hover text for emojis is optional
        if (hoverComponent != null) {
            builder.hoverEvent(HoverEvent.showText(hoverComponent));
        }

        return builder.build();
    }

    private TextComponent.Builder emojiComponent(Glyph glyph) {
        return Component.text()
                .color(NamedTextColor.WHITE) // text color affects character texture
                .content(glyph.replacement()); // use the emoji character
    }

    private static boolean isMiniMessageAvailable() {
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
