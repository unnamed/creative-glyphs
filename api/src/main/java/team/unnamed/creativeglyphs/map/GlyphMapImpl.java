package team.unnamed.creativeglyphs.map;

import org.ahocorasick.trie.PayloadTrie;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creativeglyphs.Glyph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

final class GlyphMapImpl implements GlyphMap {

    private final GlyphMapOptions options;

    private PayloadTrie<Glyph> trie = PayloadTrie.<Glyph>builder().build();
    private Map<String, Glyph> byName = new HashMap<>();
    private Map<Integer, Glyph> byCodePoint = new HashMap<>();

    GlyphMapImpl(GlyphMapOptions options) {
        this.options = options;
    }

    @Override
    public PayloadTrie<Glyph> trie() {
        return trie;
    }

    @Override
    public @Nullable Glyph getByName(String name) {
        return byName.get(name);
    }

    @Override
    public @Nullable Glyph getByCodePoint(int codePoint) {
        return byCodePoint.get(codePoint);
    }

    @Override
    public void update(Glyph glyph) {
        Glyph old = byName.put(glyph.name(), glyph);
        byCodePoint.put(glyph.character(), glyph);
        if (old != null) {
            byCodePoint.remove(old.character(), old);
        }
        trie = buildTrie(byName);
    }

    @Override
    public void setGlyphs(Collection<Glyph> glyphs) {
        // create a new registry, the previous registry
        // will be replaced by this, so we don't have a
        // map with an inconsistent state for some nanoseconds
        // (I'm paranoid)
        Map<String, Glyph> newRegistry = new HashMap<>();
        Map<Integer, Glyph> newCharacters = new HashMap<>();

        for (Glyph glyph : glyphs) {
            newRegistry.put(glyph.name(), glyph);
            newCharacters.put(glyph.character(), glyph);
        }

        // build trie and update the registries
        trie = buildTrie(newRegistry);
        byName = newRegistry;
        byCodePoint = newCharacters;
    }

    private PayloadTrie<Glyph> buildTrie(Map<String, Glyph> emojisByName) {
        PayloadTrie.PayloadTrieBuilder<Glyph> builder = PayloadTrie.builder();
        if (options.ignoreCase()) {
            builder.ignoreCase();
        }
        if (options.ignoreOverlaps()) {
            builder.ignoreOverlaps();
        }
        for (Glyph glyph : emojisByName.values()) {
            for (String usage : glyph.usages()) {
                builder.addKeyword(usage, glyph);
            }
        }
        return builder.build();
    }

    @Override
    public Map<String, Glyph> asMapByNames() {
        return new HashMap<>(byName);
    }

    @Override
    public Collection<Glyph> values() {
        return byName.values();
    }

}
