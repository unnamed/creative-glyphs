package team.unnamed.emojis.hook.papi;

import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.object.store.EmojiStore;
import team.unnamed.emojis.hook.PluginHook;

public class PlaceholderApiHook implements PluginHook {

    private final Plugin plugin;
    private final EmojiStore registry;

    public PlaceholderApiHook(
            Plugin plugin,
            EmojiStore registry
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
