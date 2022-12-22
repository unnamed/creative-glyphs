package team.unnamed.emojis;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.event.EmojiListUpdateEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmojiRegistry {

    private Map<String, Emoji> registry = new HashMap<>();
    private Map<String, Emoji> characters = new HashMap<>();

    public @Nullable Emoji get(String name) {
        return registry.get(name);
    }

    public @Nullable Emoji getIgnoreCase(String name) {
        return get(name.toLowerCase(Locale.ROOT));
    }

    public @Nullable Emoji getByChar(char c) {
        return characters.get(Character.toString(c));
    }

    public void add(Emoji emoji) {
        registry.put(emoji.name(), emoji);
        characters.put(emoji.replacement(), emoji);
    }

    public void update(Collection<Emoji> emojis) {
        // create a new registry, the previous registry
        // will be replaced by this, so we don't have a
        // map with an inconsistent state for some nanoseconds
        // (I'm paranoid)
        Map<String, Emoji> newRegistry = new HashMap<>();
        Map<String, Emoji> newCharacters = new HashMap<>();

        for (Emoji emoji : emojis) {
            newRegistry.put(emoji.name(), emoji);
            newCharacters.put(emoji.replacement(), emoji);
        }

        // call emoji list update event
        Bukkit.getPluginManager().callEvent(new EmojiListUpdateEvent(registry, newRegistry));

        // update the registry
        registry = newRegistry;
        characters = newCharacters;
    }

    public Collection<Emoji> values() {
        return registry.values();
    }

}
