package team.unnamed.creativeglyphs.plugin.hook.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;

final class GlyphPlaceholderExpansion extends PlaceholderExpansion {
    private final Plugin plugin;
    private final PluginGlyphMap registry;

    GlyphPlaceholderExpansion(Plugin plugin, PluginGlyphMap registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String name) {
        final Glyph glyph = registry.getByName(name);
        if (glyph == null) {
            return null;
        } else {
            return glyph.replacement();
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "glyph";
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
