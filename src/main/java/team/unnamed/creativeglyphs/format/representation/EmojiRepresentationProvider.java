package team.unnamed.creativeglyphs.format.representation;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.Emoji;

public interface EmojiRepresentationProvider<T> {

    T represent(Emoji emoji);

    static EmojiRepresentationProvider<Component> component(Plugin plugin) {
        return new ComponentEmojiRepresentationProvider(plugin);
    }

}
