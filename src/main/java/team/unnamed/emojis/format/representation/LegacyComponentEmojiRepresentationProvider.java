package team.unnamed.emojis.format.representation;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;

final class LegacyComponentEmojiRepresentationProvider
        implements EmojiRepresentationProvider<TextComponent> {

    private static final String DEFAULT_FORMAT = ChatColor.WHITE + "<emoji> " + ChatColor.GRAY + ":<emojiname>: "
            + ChatColor.LIGHT_PURPLE + "/emojis";

    private final Plugin plugin;

    LegacyComponentEmojiRepresentationProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public TextComponent represent(Emoji emoji) {
        String format = ChatColor.translateAlternateColorCodes(
                '&',
                        plugin.getConfig().getString("format.paper.emoji", DEFAULT_FORMAT)
        )
                .replace("<emoji>", emoji.replacement())
                .replace("<emojiname>", emoji.name());

        BaseComponent[] components = TextComponent.fromLegacyText(format);
        TextComponent component = new TextComponent(emoji.replacement());
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                components
        ));
        return component;
    }

}
