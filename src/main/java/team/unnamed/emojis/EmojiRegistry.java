package team.unnamed.emojis;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EmojiRegistry {

    private final Map<String, Emoji> registry = new HashMap<>();

    @Nullable
    public Emoji get(String name) {
        return registry.get(name);
    }

    public void add(Emoji emoji) {
        registry.put(emoji.getName(), emoji);
    }

    public Collection<Emoji> values() {
        return registry.values();
    }

}
