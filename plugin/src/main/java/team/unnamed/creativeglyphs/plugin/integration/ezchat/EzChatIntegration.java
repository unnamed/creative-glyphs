package team.unnamed.creativeglyphs.plugin.integration.ezchat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.integration.PluginIntegration;

public class EzChatIntegration implements PluginIntegration.Chat {

    private final Plugin plugin;
    private final PluginGlyphMap registry;

    public EzChatIntegration(
            Plugin plugin,
            PluginGlyphMap registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public @NotNull String plugin() {
        return "EzChat";
    }

    @Override
    public void enable(@NotNull Plugin hook) {
        Bukkit.getPluginManager().registerEvents(
                new EzChatListener(plugin, registry),
                plugin
        );
        plugin.getLogger().info("Successfully hooked with EzChat!");
    }

}
