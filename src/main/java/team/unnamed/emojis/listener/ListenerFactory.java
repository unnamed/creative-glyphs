package team.unnamed.emojis.listener;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.listener.chat.LegacyChatListener;
import team.unnamed.emojis.listener.chat.LegacyRichSurroundingChatListener;
import team.unnamed.emojis.listener.chat.PaperRichChatListener;

/**
 * Static utility class for instantiating
 * the proper {@link EventListener} for replacing
 * emojis in the chat
 */
public final class ListenerFactory {

    private ListenerFactory() {
    }

    @SuppressWarnings("deprecation")
    public static EventListener<?> create(
            EmojiRegistry registry,
            EmojiComponentProvider componentProvider,
            EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy,
            boolean rich
    ) {
        // try using the Paper event 'AsyncChatEvent'
        try {
            // check for modern AsyncChatEvent
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");

            // if it didn't throw an exception, return its event listener
            return new PaperRichChatListener(registry, componentProvider);
        } catch (ClassNotFoundException ignored) {
        }

        if (rich) {
            // not on paper and user wants it to be rich text, use
            // this another ugly legacy that may generate compatibility
            // problems with other plugins...
            return new LegacyRichSurroundingChatListener(
                    registry,
                    componentProvider,
                    cancellationStrategy
            );
        } else {
            // use the ugly legacy flat chat listener
            return new LegacyChatListener(registry);
        }
    }

}
