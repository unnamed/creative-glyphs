package team.unnamed.creativeglyphs.hook.ezchat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.object.store.EmojiStore;
import team.unnamed.creativeglyphs.hook.PluginHook;

public class EzChatHook implements PluginHook.Chat {

    private final Plugin plugin;
    private final EmojiStore registry;

    public EzChatHook(
            Plugin plugin,
            EmojiStore registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String getPluginName() {
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
