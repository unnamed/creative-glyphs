package team.unnamed.emojis.format;

import net.md_5.bungee.api.chat.TextComponent;
import team.unnamed.emojis.Emoji;

/**
 * Responsible for creating components from
 * {@link Emoji}, uses multiple component types
 * for compatibility purposes.
 */
public interface EmojiComponentProvider {

    /**
     * Creates a {@link TextComponent} from the given
     * {@code emoji}
     */
    TextComponent toComponent(Emoji emoji);

}
