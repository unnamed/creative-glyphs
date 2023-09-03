package team.unnamed.creativeglyphs.map;

import org.ahocorasick.trie.PayloadTrie;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.content.ContentProcessor;

import java.util.Collection;
import java.util.Map;

/**
 * A data-structure that holds {@link Glyph} instances by
 * name, codepoint and usages, for efficient string matching
 * and replacing algorithms.
 *
 * <p>The Glyph map holds a {@link PayloadTrie} which is synced
 * when the map is modified</p>
 *
 * <p>The main purpose of this class is to provide constant-time
 * access to {@link Glyph} instances by their name and codepoint,
 * and provide efficient string matching by {@link Glyph} usages
 * ({@link Glyph#usages()}) using the {@link PayloadTrie} data
 * structure</p>
 *
 * @author yusshu (Andre Roldan)
 */
public interface GlyphMap {

    /**
     * Returns a {@link PayloadTrie} with all
     * the glyphs registered in this {@link GlyphMap}
     * instance
     *
     * <p>The {@link PayloadTrie} is a data-structure
     * implementing the <a href="http://cr.yp.to/bib/1975/aho.pdf">Aho-Corasick</a>
     * data-structure and algorithm for efficient string
     * matching</p>
     *
     * <p>The returned {@link PayloadTrie} maps the
     * glyph usages ({@link Glyph#usages()}) to the
     * glyph instance ({@link Glyph}), so it is very
     * efficient to use this to replace usages. See
     * the {@link ContentProcessor} interface and
     * implementations</p>
     *
     * <p>Note that it is not safe to store the reference
     * to the returned trie for a considerable amount of
     * time, since it may change. The trie changes when
     * the holding {@link GlyphMap} changes.</p>
     *
     * @return The glyph map trie
     */
    PayloadTrie<Glyph> trie();

    /**
     * Find a {@link Glyph} by its name (case-sensitive),
     * or null if glyph with provided name doesn't exist
     *
     * @param name The glyph name
     * @return The glyph, or null if not found
     */
    @Nullable Glyph getByName(String name);

    /**
     * Find a {@link Glyph} by its codepoint, or null
     * if glyph with provided codepoint doesn't exist
     *
     * @param codePoint The glyph codepoint
     * @return The glyph, or null if not found
     */
    @Nullable Glyph getByCodePoint(int codePoint);

    /**
     * Registers or updates the given {@link Glyph}
     * instance. Note that mutation operations will
     * make the internal trie change, so it is not
     * ensured for {@link GlyphMap#trie()} to return
     * the same trie instance
     *
     * @param glyph The registered/updated glyph instance
     */
    void update(Glyph glyph);

    /**
     * Removes all the existing glyphs and sets the given ones
     *
     * @param glyphs The registered/updated glyphs
     */
    void setGlyphs(Collection<Glyph> glyphs);

    /**
     * Returns a collection of all the glyphs registered
     * in this map
     *
     * @return The registered glyphs
     */
    Collection<Glyph> values();

    /**
     * Returns a {@link Map} of the registered {@link Glyph}
     * by their name.
     *
     * <p>Note that this method creates a new Map every time
     * it is called</p>
     *
     * @return The map of all the registered glyphs by name
     */
    @Contract("-> new")
    Map<String, Glyph> asMapByNames();

    /**
     * Creates a new {@link GlyphMap} instance from the
     * default implementation with the provided options
     *
     * @param options The glyph map options
     * @return The glyph map
     */
    static GlyphMap map(GlyphMapOptions options) {
        return new GlyphMapImpl(options);
    }

    /**
     * Creates a new {@link GlyphMap} instance from the
     * default implementation with the default options
     * specified by the {@link GlyphMapOptions#DEFAULT}
     * constant
     *
     * @return The glyph map
     */
    static GlyphMap map() {
        return map(GlyphMapOptions.DEFAULT);
    }

}
