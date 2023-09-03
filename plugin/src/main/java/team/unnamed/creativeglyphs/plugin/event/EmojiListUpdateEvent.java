package team.unnamed.creativeglyphs.plugin.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import team.unnamed.creativeglyphs.Glyph;

import java.util.Map;

public class EmojiListUpdateEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Map<String, Glyph> oldRegistry;
    private final Map<String, Glyph> newRegistry;

    public EmojiListUpdateEvent(Map<String, Glyph> oldRegistry, Map<String, Glyph> newRegistry) {
        this.oldRegistry = oldRegistry;
        this.newRegistry = newRegistry;
    }

    public Map<String, Glyph> getOldRegistry() {
        return oldRegistry;
    }

    public Map<String, Glyph> getNewRegistry() {
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
