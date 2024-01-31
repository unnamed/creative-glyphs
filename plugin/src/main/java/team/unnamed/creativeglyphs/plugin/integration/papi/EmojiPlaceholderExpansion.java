package team.unnamed.creativeglyphs.plugin.integration.papi;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;

/**
 * Placeholder expansion for PlaceholderAPI, provides the
 * glyphs allowing to be used in other places using the
 * {@code %emoji_<emojiname>%} format.
 *
 * @deprecated Use {@link GlyphPlaceholderExpansion} instead.
 */
@Deprecated
public final class EmojiPlaceholderExpansion extends GlyphPlaceholderExpansion {
    private final Plugin plugin;
    private boolean warnAboutDeprecation = true;

    public EmojiPlaceholderExpansion(final @NotNull Plugin plugin, final @NotNull PluginGlyphMap registry) {
        super(plugin, registry);
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "emoji";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String name) {
        if (warnAboutDeprecation) {
            plugin.getLogger().warning("(PlaceholderAPI Integration) Detected usage of deprecated" +
                    " placeholder format: '%emoji_" + name + "%', please use '%glyph_" + name + "%' instead.");
            warnAboutDeprecation = false;
        }
        return super.onRequest(player, name);
    }
}
