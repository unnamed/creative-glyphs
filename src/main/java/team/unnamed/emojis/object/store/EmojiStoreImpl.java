package team.unnamed.emojis.object.store;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.event.EmojiListUpdateEvent;
import team.unnamed.emojis.object.serialization.EmojiCodec;
import team.unnamed.emojis.object.serialization.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class EmojiStoreImpl implements EmojiStore {

    private Map<String, Emoji> emojisByName = new HashMap<>();
    private Map<Integer, Emoji> emojisByCodePoint = new HashMap<>();

    private final @Nullable Plugin plugin;
    private @Nullable File database;

    public EmojiStoreImpl(Plugin plugin) throws IOException {
        this.plugin = plugin;
        this.database = makeDatabase(plugin);
    }

    public EmojiStoreImpl() {
        this.plugin = null;
        this.database = null;
    }

    @Override
    public @Nullable Emoji get(String name) {
        return emojisByName.get(name);
    }
    @Override
    public @Nullable Emoji getByCodePoint(int codePoint) {
        return emojisByCodePoint.get(codePoint);
    }

    @Override
    public void update(Emoji emoji) {
        Emoji old = emojisByName.put(emoji.name(), emoji);
        emojisByCodePoint.put(emoji.character(), emoji);
        if (old != null) {
            emojisByCodePoint.remove(old.character(), old);
        }
    }

    @Override
    public void update(Collection<Emoji> emojis) {
        // create a new registry, the previous registry
        // will be replaced by this, so we don't have a
        // map with an inconsistent state for some nanoseconds
        // (I'm paranoid)
        Map<String, Emoji> newRegistry = new HashMap<>();
        Map<Integer, Emoji> newCharacters = new HashMap<>();

        for (Emoji emoji : emojis) {
            newRegistry.put(emoji.name(), emoji);
            newCharacters.put(emoji.character(), emoji);
        }

        // call emoji list update event
        Bukkit.getPluginManager().callEvent(new EmojiListUpdateEvent(emojisByName, newRegistry));

        // update the registry
        emojisByName = newRegistry;
        emojisByCodePoint = newCharacters;
    }

    @Override
    public Collection<Emoji> values() {
        return emojisByName.values();
    }

    @Override
    public void save() {
        if (database == null || plugin == null) {
            return;
        }

        try (OutputStream output = new FileOutputStream(database)) {
            EmojiCodec.mcemoji().write(output, values());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save emojis", e);
        }
        plugin.getLogger().info("Saved " + emojisByName.size() + " emojis.");
    }

    @Override
    public void load() {
        if (database == null || plugin == null) {
            return;
        }

        try (InputStream input = new FileInputStream(database)) {
            update(EmojiCodec.mcemoji().read(input));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load emojis", e);
        }
        plugin.getLogger().info("Loaded " + emojisByName.size() + " emojis.");
    }

    private static File makeDatabase(Plugin plugin) throws IOException {
        File file = new File(plugin.getDataFolder(), "emojis.mcemoji");
        if (!file.exists()) {
            if (!file.createNewFile()) {
                // this should never happen because we already
                // checked for its existence with File#exists
                throw new IOException("Cannot create file, already created?");
            }

            try (OutputStream output = new FileOutputStream(file)) {
                try (InputStream input = plugin.getResource("emojis.mcemoji")) {
                    if (input != null) {
                        // if there's a default 'emojis.mcemoji'
                        // file in our resources, copy it
                        Streams.pipe(input, output);
                    } else {
                        // if there isn't, write zero emojis
                        // to the created file, so next reads
                        // don't fail
                        EmojiCodec.mcemoji().write(output, Collections.emptySet());
                    }
                }
            }
        }
        return file;
    }

}
