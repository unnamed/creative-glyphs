package team.unnamed.emojis.listener.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiReplacer;
import team.unnamed.emojis.listener.EventListener;

/**
 * Most simple and flat chat listener, it uses the
 * default {@link AsyncPlayerChatEvent} and doesn't
 * interact with rich components, so it doesn't have
 * to cancel the event. Should be compatible with
 * all chat plugins.
 */
@SuppressWarnings("deprecation") // AsyncPlayerChatEvent thing
public class LegacyChatListener
        implements EventListener<AsyncPlayerChatEvent> {

    private final EmojiRegistry emojiRegistry;

    public LegacyChatListener(EmojiRegistry emojiRegistry) {
        this.emojiRegistry = emojiRegistry;
    }

    @Override
    public Class<AsyncPlayerChatEvent> getEventType() {
        return AsyncPlayerChatEvent.class;
    }

    @Override
    public void execute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String newMessage = EmojiReplacer.replace(player, emojiRegistry, message);
        event.setMessage(newMessage);
    }

}
