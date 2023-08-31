package team.unnamed.creativeglyphs.hook.townychat;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.object.store.EmojiStore;
import team.unnamed.creativeglyphs.format.processor.MessageProcessor;
import team.unnamed.creativeglyphs.format.Permissions;
import team.unnamed.creativeglyphs.hook.PluginHook;

public class TownyChatHook
        implements PluginHook.Chat, Listener {

    private final Plugin plugin;
    private final EmojiStore registry;

    public TownyChatHook(Plugin plugin, EmojiStore registry) {
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

        event.setMessage(MessageProcessor.string().process(
                color + message,
                registry,
                Permissions.permissionTest(player)
        ));
    }

}
