package team.unnamed.emojis.listener;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.listener.chat.LegacyChatListener;
import team.unnamed.emojis.listener.chat.LegacyRichSurroundingChatListener;

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
            EmojiRegistry registry,
            EmojiComponentProvider componentProvider,
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
                // (instantiated via reflection because it's not available in
                // compile-time classpath)
                return (EventListener<?>) Class.forName("team.unnamed.emojis.paper.PaperRichChatListener")
                        .getDeclaredConstructor(EmojiRegistry.class, EmojiComponentProvider.class)
                        .newInstance(registry, componentProvider);
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
                    registry,
                    componentProvider,
                    cancellationStrategy
            );
        } else {
            LOGGER.info("Using Bukkit flat chat listener");
            // use the ugly legacy flat chat listener
            return new LegacyChatListener(registry);
        }
    }

}
