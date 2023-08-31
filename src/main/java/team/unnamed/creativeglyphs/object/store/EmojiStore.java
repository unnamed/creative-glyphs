package team.unnamed.creativeglyphs.object.store;

import org.ahocorasick.trie.PayloadTrie;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creativeglyphs.Emoji;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

public interface EmojiStore {

    PayloadTrie<Emoji> trie();

    @Nullable Emoji get(String name);

    default @Nullable Emoji getIgnoreCase(String name) {
        return get(name.toLowerCase(Locale.ROOT));
    }

    @Nullable Emoji getByCodePoint(int codePoint);

    void update(Emoji emoji);

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
