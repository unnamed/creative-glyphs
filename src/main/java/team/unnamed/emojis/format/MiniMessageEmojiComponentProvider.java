package team.unnamed.emojis.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import team.unnamed.emojis.Emoji;

import java.util.HashMap;
import java.util.Map;

public class MiniMessageEmojiComponentProvider
        implements EmojiComponentProvider {

    private static final String DEFAULT_FORMAT = "<white><emoji> <gray>:<emojiname>: <#ff8df8>/emojis";
    private static final GsonComponentSerializer JSON_COMPONENT_SERIALIZER = GsonComponentSerializer.gson();

    private final MiniMessage formatter = MiniMessage.get();
    private final ConfigurationSection config;

    public MiniMessageEmojiComponentProvider(ConfigurationSection config) {
        this.config = config;
    }

    @Override
    public Component toAdventureComponent(Emoji emoji) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("emoji", emoji.getName());
        placeholders.put("emojiname", emoji.getName());

        return formatter.parse(
                config.getString("format.paper.emoji", DEFAULT_FORMAT),
                placeholders
        );
    }

    @Override
    public TextComponent toBungeeComponent(Emoji emoji) {
        // TODO: We should not convert it to JSON and from JSON to BaseComponent[]...
        String json = JSON_COMPONENT_SERIALIZER.serialize(toAdventureComponent(emoji));
        BaseComponent[] components = ComponentSerializer.parse(json);
        TextComponent component;

        if (components.length == 1
                && components[0] instanceof TextComponent) {
            component = (TextComponent) components[0];
        } else {
            component = new TextComponent("");
            for (BaseComponent extra : components) {
                component.addExtra(extra);
            }
        }

        return component;
    }

}
