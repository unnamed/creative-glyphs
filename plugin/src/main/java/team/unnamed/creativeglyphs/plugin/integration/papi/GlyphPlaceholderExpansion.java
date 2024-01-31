package team.unnamed.creativeglyphs.plugin.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.map.GlyphMap;

import static java.util.Objects.requireNonNull;

/**
 * Placeholder expansion for PlaceholderAPI, provides the
 * glyphs allowing to be used in other places using the
 * {@code %glyph_<emojiname>%} format.
 */
public class GlyphPlaceholderExpansion extends PlaceholderExpansion {
    public static final String IDENTIFIER = "glyph";

    private final Plugin plugin;
    private final GlyphMap glyphMap;

    public GlyphPlaceholderExpansion(final @NotNull Plugin plugin, final @NotNull GlyphMap glyphMap) {
        this.plugin = requireNonNull(plugin, "plugin");
        this.glyphMap = requireNonNull(glyphMap, "glyphMap");
    }

    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(final OfflinePlayer player, final @NotNull String name) {
        final Glyph glyph = glyphMap.getByName(name);
        if (glyph == null) {
            return null;
        } else {
            return glyph.replacement();
        }
    }
}
