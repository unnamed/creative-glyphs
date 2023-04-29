package team.unnamed.emojis.object.store;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.Emoji;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

public interface EmojiStore {

    @Nullable Emoji get(String name);

    default @Nullable Emoji getIgnoreCase(String name) {
        return get(name.toLowerCase(Locale.ROOT));
    }

    @Nullable Emoji getByCodePoint(int codePoint);

    void update(Emoji emoji);

    @Deprecated
    default void add(Emoji emoji) {
        update(emoji);
    }

    void update(Collection<Emoji> emojis);

    Collection<Emoji> values();

    //#region Persistent
    void save();

    void load();
    //#endregion

    static EmojiStore create(Plugin plugin) throws IOException {
        return new EmojiStoreImpl(plugin);
    }

    static EmojiStore createCachedOnly() {
        return new EmojiStoreImpl();
    }

}
