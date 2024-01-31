package team.unnamed.creativeglyphs.plugin.integration.papi;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.integration.PluginIntegration;

import static java.util.Objects.requireNonNull;

public final class PlaceholderAPIIntegration implements PluginIntegration {
    private final Plugin plugin;
    private final PluginGlyphMap registry;

    public PlaceholderAPIIntegration(final @NotNull Plugin plugin, final @NotNull PluginGlyphMap registry) {
        this.plugin = requireNonNull(plugin, "plugin");
        this.registry = requireNonNull(registry, "registry");
    }

    @Override
    public @NotNull String plugin() {
        return "PlaceholderAPI";
    }

    @Override
    @SuppressWarnings("deprecation")
    public void enable(final @NotNull Plugin placeholderAPI) {
        new GlyphPlaceholderExpansion(plugin, registry).register();
        new EmojiPlaceholderExpansion(plugin, registry).register();
    }
}
