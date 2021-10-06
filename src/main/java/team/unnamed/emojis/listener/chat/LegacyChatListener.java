package team.unnamed.emojis.listener.chat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.format.StringEmojiReplacer;
import team.unnamed.emojis.listener.EventListener;

/**
 * Most simple and flat chat listener, it uses the
 * default {@link AsyncPlayerChatEvent} and doesn't
 * interact with rich components, so it doesn't have
 * to cancel the event. Should be compatible with
 * all chat plugins.
 */
@SuppressWarnings("deprecation") // AsyncPlayerChatEvent thing
public class LegacyChatListener
        implements EventListener<AsyncPlayerChatEvent> {

    private final Plugin plugin = JavaPlugin.getPlugin(EmojisPlugin.class); // todo: ugly
    private final EmojiRegistry emojiRegistry;

    public LegacyChatListener(EmojiRegistry emojiRegistry) {
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

        String newMessage = StringEmojiReplacer.replace(player, emojiRegistry, color + message);
        event.setMessage(newMessage);
    }

}
