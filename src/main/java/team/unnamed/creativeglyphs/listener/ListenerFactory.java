package team.unnamed.creativeglyphs.listener;

import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.object.store.EmojiStore;
import team.unnamed.creativeglyphs.EmojisPlugin;
import team.unnamed.creativeglyphs.listener.chat.LegacyChatListener;
import team.unnamed.creativeglyphs.listener.chat.PaperRichChatListener;

import java.util.logging.Logger;

/**
 * Static utility class for instantiating
 * the proper {@link EventListener} for replacing
 * emojis in the chat
 */
public final class ListenerFactory {

    private static final Logger LOGGER = EmojisPlugin.getPlugin(EmojisPlugin.class).getLogger();

    private ListenerFactory() {
    }

    public static EventListener<?> create(
            Plugin plugin,
            EmojiStore registry,
            boolean paper
    ) {
        if (paper) {
            // try using the Paper event 'AsyncChatEvent'
            try {
                // check for modern AsyncChatEvent
                Class.forName("io.papermc.paper.event.player.AsyncChatEvent");

                LOGGER.info("Using Paper rich chat listener");
                // if it didn't throw an exception, return its event listener
                return new PaperRichChatListener(plugin, registry);
            } catch (ReflectiveOperationException ignored) {
                LOGGER.info("Failed to instantiate Paper chat listener");
            }
        }

        LOGGER.info("Using Bukkit flat chat listener");
        return new LegacyChatListener(plugin, registry);
    }

}
