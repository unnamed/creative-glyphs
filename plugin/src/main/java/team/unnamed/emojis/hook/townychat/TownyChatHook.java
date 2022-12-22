package team.unnamed.emojis.hook.townychat;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiFormat;
import team.unnamed.emojis.hook.PluginHook;

public class TownyChatHook
        implements PluginHook.Chat, Listener {

    private final Plugin plugin;
    private final EmojiRegistry registry;

    public TownyChatHook(Plugin plugin, EmojiRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String getPluginName() {
        return "TownyChat";
    }

    @Override
    public void hook(Plugin hook) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Successfully hooked into TownyChat!");
    }

    @EventHandler
    public void onTownyChat(AsyncChatHookEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String color = ChatColor.translateAlternateColorCodes(
                '&',
                event.getChannel().getMessageColour()
        );

        event.setMessage(EmojiFormat.replaceRawToRaw(
                player, registry, color + message
        ));
    }

}
