package team.unnamed.creativeglyphs.plugin.hook.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;

/**
 * Placeholder expansion for PlaceholderAPI, provides the
 * emojis allowing to be used in other places
 */
public class EmojiPlaceholderExpansion
        extends PlaceholderExpansion {

    private final Plugin plugin;
    private final PluginGlyphMap registry;

    public EmojiPlaceholderExpansion(
            Plugin plugin,
            PluginGlyphMap registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String name) {
        Glyph glyph = registry.getByName(name);
        if (glyph == null) {
            return null;
        } else {
            return glyph.replacement();
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "emoji";
    }

    @Override
    public @NotNull String getAuthor() {
        // return the first author in the list
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

}
