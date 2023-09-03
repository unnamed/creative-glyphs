package team.unnamed.creativeglyphs.plugin;

import org.ahocorasick.trie.PayloadTrie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.plugin.event.EmojiListUpdateEvent;
import team.unnamed.creativeglyphs.serialization.GlyphReader;
import team.unnamed.creativeglyphs.serialization.GlyphWriter;
import team.unnamed.creativeglyphs.serialization.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class PluginGlyphMap implements GlyphMap {

    private final GlyphMap delegate;
    private final Plugin plugin;
    private final File database;

    private PluginGlyphMap(Plugin plugin) throws IOException {
        this.delegate = GlyphMap.map();
        this.plugin = plugin;
        this.database = makeDatabase(plugin);
    }

    public void save() {
        try (OutputStream output = new FileOutputStream(database)) {
            GlyphWriter.mcglyph().write(output, values());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save emojis", e);
        }
        plugin.getLogger().info("Saved " + values().size() + " emojis.");
    }

    public void load() {
        try (InputStream input = new FileInputStream(database)) {
            setGlyphs(GlyphReader.mcglyph().read(input));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load emojis", e);
        }
        plugin.getLogger().info("Loaded " + values().size() + " emojis.");
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
                        GlyphWriter.mcglyph().write(output, Collections.emptySet());
                    }
                }
            }
        }
        return file;
    }

    @Override
    public PayloadTrie<Glyph> trie() {
        return delegate.trie();
    }

    @Override
    public @Nullable Glyph getByName(String name) {
        return delegate.getByName(name);
    }

    @Override
    public @Nullable Glyph getByCodePoint(int codePoint) {
        return delegate.getByCodePoint(codePoint);
    }

    @Override
    public void update(Glyph glyph) {
        delegate.update(glyph);
    }

    @Override
    public void setGlyphs(Collection<Glyph> glyphs) {
        Map<String, Glyph> oldGlyphs = delegate.asMapByNames();
        delegate.setGlyphs(glyphs);
        Map<String, Glyph> newGlyphs = delegate.asMapByNames();

        // Call the emoji list update event. If the completions
        // feature is enabled, the completions listener will
        // receive this event and update the completions for
        // everyone
        Bukkit.getPluginManager().callEvent(new EmojiListUpdateEvent(oldGlyphs, newGlyphs));
    }

    @Override
    public Map<String, Glyph> asMapByNames() {
        return delegate.asMapByNames();
    }

    @Override
    public Collection<Glyph> values() {
        return delegate.values();
    }

    static PluginGlyphMap create(Plugin plugin) throws IOException {
        return new PluginGlyphMap(plugin);
    }

}
