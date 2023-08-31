package team.unnamed.creativeglyphs.listener;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Class for handling {@link EventListener} and
 * wrapping {@link PluginManager}, making easy
 * its usage and using a pretty fluent-api
 */
public class EventBus {

    // No-operation listener, Spigot requires it
    private static final Listener NOP_LISTENER = new Listener() {};

    private final Plugin plugin;

    private EventBus(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers the given {@code listener} for
     * the specified {@code eventType} and the
     * specified event {@code priority}
     */
    public <E extends Event> EventBus register(
            EventListener<E> listener,
            EventPriority priority
    ) {
        plugin
                .getServer()
                .getPluginManager()
                .registerEvent(
                        listener.getEventType(),
                        NOP_LISTENER,
                        priority,
                        (ignored, rawEvent) -> {
                            @SuppressWarnings("unchecked")
                            E event = (E) rawEvent;
                            listener.execute(event);
                        },
                        plugin
                );
        return this;
    }

    /**
     * Registers the given {@code listener} for
     * the specified {@code eventType}, using
     * {@link EventPriority#NORMAL} as priority
     */
    public <E extends Event> EventBus register(EventListener<E> listener) {
        return register(listener, EventPriority.NORMAL);
    }

    /**
     * Creates a new {@link EventBus} instance, registering
     * the listeners for the specified {@code plugin}
     */
    public static EventBus create(Plugin plugin) {
        // I think that a fluent api looks better
        // using a static factory method instead of
        // a constructor
        return new EventBus(plugin);
    }

}
