package team.unnamed.emojis.hook.ezchat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.EmojiRegistry;

public class EzChatHook {

    private final Plugin plugin;
    private final EmojiRegistry registry;

    public EzChatHook(
            Plugin plugin,
            EmojiRegistry registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("EzChat") == null) {
            // TODO: I don't really like doing this
            return;
        }

        Bukkit.getPluginManager().registerEvents(
                new EzChatListener(registry),
                plugin
        );
    }

}
