package team.unnamed.creativeglyphs.plugin.integration.papi;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.integration.PluginIntegration;

public class PlaceholderApiIntegration implements PluginIntegration {

    private final Plugin plugin;
    private final PluginGlyphMap registry;

    public PlaceholderApiIntegration(
            Plugin plugin,
            PluginGlyphMap registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public @NotNull String plugin() {
        return "PlaceholderAPI";
    }

    @Override
    public void enable(@NotNull Plugin hook) {
        new EmojiPlaceholderExpansion(plugin, registry).register();
        plugin.getLogger().info("Successfully registered PlaceholderAPI placeholders");
    }

}
