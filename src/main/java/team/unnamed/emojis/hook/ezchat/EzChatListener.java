package team.unnamed.emojis.hook.ezchat;

import me.fixeddev.ezchat.EasyTextComponent;
import me.fixeddev.ezchat.event.AsyncEzChatEvent;
import me.fixeddev.ezchat.format.ChatFormat;
import me.fixeddev.ezchat.format.ChatFormatSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.util.ComponentEmojiReplacer;

public class EzChatListener implements Listener {

    private final ChatFormatSerializer formatSerializer
            = new ChatFormatSerializer();

    private final EmojiRegistry registry;

    public EzChatListener(EmojiRegistry registry) {
        this.registry = registry;
    }

    private TextComponent buildEmojiComponent(Emoji emoji) {
        // TODO: this is temporal, should be configurable
        TextComponent component = new TextComponent(emoji.getCharacter() + "");
        component.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text(new ComponentBuilder()
                        .append(emoji.getCharacter() + "")
                        .color(net.md_5.bungee.api.ChatColor.WHITE)
                        .append(" :" + emoji.getName() + ": ")
                        .color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append("/emojis")
                        .color(net.md_5.bungee.api.ChatColor.RED)
                        .create())
        ));
        return component;
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

        BaseComponent[] messageComponent = ComponentEmojiReplacer.replace(
                sender,
                registry,
                message,
                (components, emoji) -> components.add(buildEmojiComponent(emoji))
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
