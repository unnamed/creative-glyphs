package team.unnamed.emojis.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.util.EmojiReplacer;

public class ChatListener implements Listener {

    private final EmojiRegistry registry;

    public ChatListener(EmojiRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        event.setMessage(EmojiReplacer.replace(player, registry, message));
    }

}
