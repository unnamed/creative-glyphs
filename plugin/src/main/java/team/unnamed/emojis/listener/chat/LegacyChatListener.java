package team.unnamed.emojis.listener.chat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.processor.MessageProcessor;
import team.unnamed.emojis.format.Permissions;
import team.unnamed.emojis.listener.EventListener;

/**
 * Most simple and flat chat listener, it uses the
 * default {@link AsyncPlayerChatEvent} and doesn't
 * interact with rich components, so it doesn't have
 * to cancel the event. Should be compatible with
 * all chat plugins.
 */
public class LegacyChatListener
        implements EventListener<AsyncPlayerChatEvent> {

    private final Plugin plugin;
    private final EmojiRegistry emojiRegistry;

    public LegacyChatListener(Plugin plugin, EmojiRegistry emojiRegistry) {
        this.plugin = plugin;
        this.emojiRegistry = emojiRegistry;
    }

    @Override
    public Class<AsyncPlayerChatEvent> getEventType() {
        return AsyncPlayerChatEvent.class;
    }

    @Override
    public void execute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        ChatColor color = ChatColor.getByChar(plugin.getConfig()
                .getString("format.legacy.color", "f").charAt(0));

        event.setMessage(MessageProcessor.string().process(
                color + message,
                emojiRegistry,
                Permissions.permissionTest(player)
        ));
    }

}
