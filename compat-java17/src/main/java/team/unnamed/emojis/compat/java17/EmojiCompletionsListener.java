package team.unnamed.emojis.compat.java17;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.event.EmojiListUpdateEvent;
import team.unnamed.emojis.format.Permissions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("unused")
public class EmojiCompletionsListener implements Listener {

    private final EmojiRegistry registry;

    public EmojiCompletionsListener(EmojiRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        player.addAdditionalChatCompletions(emojisToCompletions(
                player,
                registry.values()
        ));
    }

    @EventHandler
    public void onEmojiListUpdate(EmojiListUpdateEvent event) {

        var oldRegistry = event.getOldRegistry();
        var newRegistry = event.getNewRegistry();

        var addedCompletions = new HashSet<Emoji>();
        var removedCompletions = new HashSet<String>();

        for (Map.Entry<String, Emoji> entry : newRegistry.entrySet()) {
            String name = entry.getKey();
            if (!oldRegistry.containsKey(name)) {
                addedCompletions.add(entry.getValue());
            }
        }

        for (Map.Entry<String, Emoji> entry : oldRegistry.entrySet()) {
            String name = entry.getKey();
            if (!newRegistry.containsKey(name)) {
                removedCompletions.add(':' + name + ':');
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removeAdditionalChatCompletions(removedCompletions);
            player.addAdditionalChatCompletions(emojisToCompletions(player, addedCompletions));
        }
    }

    private static Collection<String> emojisToCompletions(Permissible object, Collection<Emoji> emojis) {
        Collection<String> completions = new HashSet<>();
        for (Emoji emoji : emojis) {
            if (Permissions.canUse(object, emoji)) {
                completions.add(':' + emoji.name() + ':');
            }
        }
        return completions;
    }

}
