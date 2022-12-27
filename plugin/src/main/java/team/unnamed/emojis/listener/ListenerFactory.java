package team.unnamed.emojis.listener;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.listener.chat.LegacyChatListener;
import team.unnamed.emojis.listener.chat.LegacyRichSurroundingChatListener;
import team.unnamed.emojis.listener.chat.PaperRichChatListener;

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
            EmojiRegistry registry,
            EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy,
            boolean paper,
            boolean rich
    ) {
        if (paper) {
            // try using the Paper event 'AsyncChatEvent'
            try {
                // check for modern AsyncChatEvent
                Class.forName("io.papermc.paper.event.player.AsyncChatEvent");

                LOGGER.info("Paper detected, trying to use Paper chat listener");

                // if it didn't throw an exception, return its event listener
                return new PaperRichChatListener(plugin, registry);
            } catch (ReflectiveOperationException ignored) {
                LOGGER.info("Failed to instantiate Paper chat listener");
            }
        }

        if (rich) {
            LOGGER.info("Using Bukkit rich chat listener");
            // not on paper and user wants it to be rich text, use
            // this another ugly legacy that may generate compatibility
            // problems with other plugins...
            return new LegacyRichSurroundingChatListener(
                    plugin,
                    registry,
                    cancellationStrategy
            );
        } else {
            LOGGER.info("Using Bukkit flat chat listener");
            // use the ugly legacy flat chat listener
            return new LegacyChatListener(plugin, registry);
        }
    }

}
