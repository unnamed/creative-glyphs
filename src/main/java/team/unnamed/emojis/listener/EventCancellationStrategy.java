package team.unnamed.emojis.listener;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Responsible for cancelling events, there can be
 * multiple cancellation strategies like, for chat,
 * removing all recipients (so it's compatible for
 * plugins like DiscordSRV)
 * @param <E> The target event type
 * @author yusshu (Andre Roldan)
 */
@FunctionalInterface
public interface EventCancellationStrategy<E extends Event> {

    /**
     * Cancels the given {@code event}, not
     * always using {@link Cancellable#setCancelled},
     * it depends on event type and implementation
     */
    void surround(E event);

    /**
     * Instantiates a {@link EventCancellationStrategy} using
     * the default behavior, that is just cancelling the event.
     * Requires the event being a {@link Cancellable} event
     */
    static <T extends Event & Cancellable> EventCancellationStrategy<T> cancellingDefault() {
        return event -> event.setCancelled(true);
    }

    /**
     * Instantiates a {@link EventCancellationStrategy} for
     * {@link AsyncPlayerChatEvent} events, it removes all the
     * recipients (message receivers) from the event.
     */
    @SuppressWarnings("deprecation")
    static EventCancellationStrategy<AsyncPlayerChatEvent> removingRecipients() {
        return event -> event.getRecipients().clear();
    }

}
