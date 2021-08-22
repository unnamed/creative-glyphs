package team.unnamed.emojis;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EmojiRegistry {

    private Map<String, Emoji> registry = new HashMap<>();

    @Nullable
    public Emoji get(String name) {
        return registry.get(name);
    }

    public void add(Emoji emoji) {
        registry.put(emoji.getName(), emoji);
    }

    public void update(Collection<Emoji> emojis) {
        // create a new registry, the previous registry
        // will be replaced by this, so we don't have a
        // map with an inconsistent state for some nanoseconds
        // (I'm paranoid)
        Map<String, Emoji> newRegistry = new HashMap<>();
        for (Emoji emoji : emojis) {
            newRegistry.put(emoji.getName(), emoji);
        }
        // update the registry
        registry = newRegistry;
    }

    public Collection<Emoji> values() {
        return registry.values();
    }

}
