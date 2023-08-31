package team.unnamed.creativeglyphs.listener;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Class for replacing {@link Listener} and
 * its annotation system using {@link EventHandler},
 * this interface is meant to be used with
 * {@link EventBus}
 */
public interface EventListener<E extends Event> {

    /**
     * Returns the listened event type
     * @return The listened event type
     */
    Class<E> getEventType();

    /**
     * Executes the listener logic for
     * the given {@code event}
     */
    void execute(E event);

}
