package team.unnamed.emojis.hook.ezchat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.hook.PluginHook;

public class EzChatHook implements PluginHook.Chat {

    private final Plugin plugin;
    private final EmojiRegistry registry;
    private final EmojiComponentProvider emojiComponentProvider;

    public EzChatHook(
            Plugin plugin,
            EmojiRegistry registry,
            EmojiComponentProvider emojiComponentProvider
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.emojiComponentProvider = emojiComponentProvider;
    }

    @Override
    public String getPluginName() {
        return "EzChat";
    }

    @Override
    public void hook(Plugin hook) {
        Bukkit.getPluginManager().registerEvents(
                new EzChatListener(registry, emojiComponentProvider),
                plugin
        );
        plugin.getLogger().info("Successfully hooked with EzChat!");
    }

}
