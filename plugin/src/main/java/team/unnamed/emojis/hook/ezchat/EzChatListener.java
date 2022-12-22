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
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.format.EmojiFormat;

public class EzChatListener implements Listener {

    private final ChatFormatSerializer formatSerializer
            = new ChatFormatSerializer();

    private final EmojiRegistry registry;
    private final EmojiComponentProvider emojiComponentProvider;

    public EzChatListener(
            EmojiRegistry registry,
            EmojiComponentProvider emojiComponentProvider
    ) {
        this.registry = registry;
        this.emojiComponentProvider = emojiComponentProvider;
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

        BaseComponent[] messageComponent = EmojiFormat.replaceRawToRich(
                sender,
                registry,
                message,
                emojiComponentProvider
        );

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
