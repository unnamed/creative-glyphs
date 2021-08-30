package team.unnamed.emojis.format;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;
import team.unnamed.emojis.Emoji;

/**
 * Responsible for creating components from
 * {@link Emoji}, uses multiple component types
 * for compatibility purposes.
 */
public interface EmojiComponentProvider {

    /**
     * Creates an Adventure {@link Component} from
     * the given {@code emoji}, used in Paper 1.17+
     */
    Component toAdventureComponent(Emoji emoji);

    /**
     * Creates a BungeeCord Chat {@link TextComponent}
     * from the given {@code emoji}, used in older
     * versions of the server software
     */
    TextComponent toBungeeComponent(Emoji emoji);

}
