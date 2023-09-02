package team.unnamed.creativeglyphs.plugin.hook.papi;

import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;

public class PlaceholderApiHook implements PluginHook {

    private final Plugin plugin;
    private final PluginGlyphMap registry;

    public PlaceholderApiHook(
            Plugin plugin,
            PluginGlyphMap registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String getPluginName() {
        return "PlaceholderAPI";
    }

    @Override
    public void hook(Plugin hook) {
        new EmojiPlaceholderExpansion(plugin, registry).register();
        plugin.getLogger().info("Successfully registered PlaceholderAPI placeholders");
    }

}
