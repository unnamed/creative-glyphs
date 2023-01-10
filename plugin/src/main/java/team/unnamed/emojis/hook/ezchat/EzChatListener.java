package team.unnamed.emojis.hook.ezchat;

import me.fixeddev.ezchat.EasyTextComponent;
import me.fixeddev.ezchat.event.AsyncEzChatEvent;
import me.fixeddev.ezchat.format.ChatFormat;
import me.fixeddev.ezchat.format.ChatFormatSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.object.store.EmojiStore;
import team.unnamed.emojis.format.Permissions;
import team.unnamed.emojis.format.processor.MessageProcessor;

public class EzChatListener implements Listener {

    private final ChatFormatSerializer formatSerializer
            = new ChatFormatSerializer();

    private final EmojiStore registry;
    private final MessageProcessor<String, BaseComponent[]> messageProcessor;

    public EzChatListener(
            Plugin plugin,
            EmojiStore registry
    ) {
        this.registry = registry;
        this.messageProcessor = MessageProcessor.stringToLegacyComponent(plugin);
    }

    @EventHandler
    public void onChat(AsyncEzChatEvent event) {
        // cancel EzChat event
        event.setCancelled(true);

        Player sender = event.getPlayer();
        ChatFormat format = event.getPlayerChatFormat();
        String message = ChatFormatSerializer.color(format.getChatColor())
                + event.getMessage();

        if (sender.hasPermission("ezchat.color")) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        BaseComponent[] messageComponent = messageProcessor.process(message, registry, Permissions.permissionTest(sender));

        if (!format.isUsePlaceholderApi()) {
            EasyTextComponent component = formatSerializer.constructJsonMessage(format, sender);
            component.append(messageComponent);
            BaseComponent[] builtComponent = component.build();

            for (Player recipient : event.getRecipients()) {
                recipient.spigot().sendMessage(builtComponent);
            }
        } else {
            for (Player recipient : event.getRecipients()) {
                EasyTextComponent component = formatSerializer.constructJsonMessage(format, sender, recipient);
                component.append(messageComponent);
                recipient.spigot().sendMessage(component.build());
            }
        }
    }

}
