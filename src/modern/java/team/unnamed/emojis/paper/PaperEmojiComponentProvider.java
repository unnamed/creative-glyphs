package team.unnamed.emojis.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;

import static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver;

public class PaperEmojiComponentProvider {

    private final Plugin plugin;
    private final boolean isMiniMessageAvailable;
    private boolean infoSupportMiniMessage = true;

    public PaperEmojiComponentProvider(Plugin plugin) {
        this.plugin = plugin;
        this.isMiniMessageAvailable = isMiniMessageAvailable();

        if (isMiniMessageAvailable) {
            plugin.getLogger().info("MiniMessage format is supported by the server, " +
                    "trying to use it for emoji replacement...");
        }
    }

    public Component componentOf(Emoji emoji) {

        ConfigurationSection config = plugin.getConfig();

        Component hoverComponent = null;
        if (isMiniMessageAvailable) {
            String source = config.getString("format.hover.mini-message", null);
            if (source != null) {
                hoverComponent = MiniMessage.miniMessage().deserialize(
                        source,
                        resolver("emoji", Tag.selfClosingInserting(emojiComponent(emoji))),
                        resolver("emojiname", Tag.inserting(Component.text(emoji.getName())))
                );
            } else if (infoSupportMiniMessage
                    && (config.contains("format.hover.legacy", true)
                    || config.contains("format.paper.emoji", true))) {
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
                                .replace("<emoji>", emoji.getCharacter() + "")
                                .replace("<emojiname>", emoji.getName())
                );
            }
        }

        TextComponent.Builder builder = emojiComponent(emoji);

        // Using a hover text for emojis is optional
        if (hoverComponent != null) {
            builder.hoverEvent(HoverEvent.showText(hoverComponent));
        }

        return builder.build();
    }

    private TextComponent.Builder emojiComponent(Emoji emoji) {
        return Component.text()
                .color(NamedTextColor.WHITE) // text color affects character texture
                .content(Character.toString(emoji.getCharacter())); // use the emoji character
                // TODO: We could use an specialized font for emojis
                // .font(emojisFont)
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
