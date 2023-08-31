package team.unnamed.creativeglyphs.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;
import team.unnamed.creativeglyphs.Emoji;
import team.unnamed.creativeglyphs.object.store.EmojiStore;
import team.unnamed.creativeglyphs.event.EmojiListUpdateEvent;
import team.unnamed.creativeglyphs.format.EmojiFormat;
import team.unnamed.creativeglyphs.format.Permissions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class EmojiCompletionsListener implements Listener {

    private final EmojiStore registry;

    public EmojiCompletionsListener(EmojiStore registry) {
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
                removedCompletions.add(EmojiFormat.usageOf(entry.getValue()));
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
                completions.add(EmojiFormat.usageOf(emoji));
            }
        }
        return completions;
    }

}
