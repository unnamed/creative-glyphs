package team.unnamed.emojis.format;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import team.unnamed.emojis.Emoji;

public class DefaultEmojiComponentProvider
        implements EmojiComponentProvider {

    private static final String DEFAULT_FORMAT = ChatColor.WHITE + "<emoji> " + ChatColor.GRAY + ":<emojiname>: "
            + ChatColor.LIGHT_PURPLE + "/emojis";

    private final ConfigurationSection config;

    public DefaultEmojiComponentProvider(ConfigurationSection config) {
        this.config = config;
    }

    @Override
    public TextComponent toComponent(Emoji emoji) {
        String format = ChatColor.translateAlternateColorCodes(
                '&',
                        config.getString("format.paper.emoji", DEFAULT_FORMAT)
        )
                .replace("<emoji>", emoji.character() + "")
                .replace("<emojiname>", emoji.name());

        BaseComponent[] components = TextComponent.fromLegacyText(format);
        TextComponent component = new TextComponent(emoji.character() + "");
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                components
        ));
        return component;
    }

}
