package team.unnamed.emojis.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import team.unnamed.emojis.Emoji;

import java.util.Map;

public class EmojiListUpdateEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Map<String, Emoji> oldRegistry;
    private final Map<String, Emoji> newRegistry;

    public EmojiListUpdateEvent(Map<String, Emoji> oldRegistry, Map<String, Emoji> newRegistry) {
        this.oldRegistry = oldRegistry;
        this.newRegistry = newRegistry;
    }

    public Map<String, Emoji> getOldRegistry() {
        return oldRegistry;
    }

    public Map<String, Emoji> getNewRegistry() {
        return newRegistry;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
