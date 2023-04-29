package team.unnamed.emojis.format.representation;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;

public interface EmojiRepresentationProvider<T> {

    T represent(Emoji emoji);


    static EmojiRepresentationProvider<Component> component(Plugin plugin) {
        return new ComponentEmojiRepresentationProvider(plugin);
    }

    static EmojiRepresentationProvider<TextComponent> legacyComponent(Plugin plugin) {
        return new LegacyComponentEmojiRepresentationProvider(plugin);
    }

}
