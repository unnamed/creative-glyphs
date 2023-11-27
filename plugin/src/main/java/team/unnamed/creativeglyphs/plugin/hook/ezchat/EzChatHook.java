package team.unnamed.creativeglyphs.plugin.hook.ezchat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;

public class EzChatHook implements PluginHook.Chat {

    private final Plugin plugin;
    private final PluginGlyphMap registry;

    public EzChatHook(
            Plugin plugin,
            PluginGlyphMap registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String pluginName() {
        return "EzChat";
    }

    @Override
    public void hook(Plugin hook) {
        Bukkit.getPluginManager().registerEvents(
                new EzChatListener(plugin, registry),
                plugin
        );
        plugin.getLogger().info("Successfully hooked with EzChat!");
    }

}
