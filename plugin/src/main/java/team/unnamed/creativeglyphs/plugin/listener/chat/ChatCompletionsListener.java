package team.unnamed.creativeglyphs.plugin.listener.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.event.EmojiListUpdateEvent;
import team.unnamed.creativeglyphs.plugin.util.Permissions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ChatCompletionsListener implements Listener {

    private final PluginGlyphMap registry;

    public ChatCompletionsListener(PluginGlyphMap registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        player.addAdditionalChatCompletions(glyphsToCompletions(
                player,
                registry.values()
        ));
    }

    @EventHandler
    public void onEmojiListUpdate(EmojiListUpdateEvent event) {

        var oldRegistry = event.getOldRegistry();
        var newRegistry = event.getNewRegistry();

        var addedCompletions = new HashSet<Glyph>();
        var removedCompletions = new HashSet<String>();

        for (Map.Entry<String, Glyph> entry : newRegistry.entrySet()) {
            String name = entry.getKey();
            if (!oldRegistry.containsKey(name)) {
                addedCompletions.add(entry.getValue());
            }
        }

        for (Map.Entry<String, Glyph> entry : oldRegistry.entrySet()) {
            String name = entry.getKey();
            if (!newRegistry.containsKey(name)) {
                removedCompletions.addAll(entry.getValue().usages());
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removeAdditionalChatCompletions(removedCompletions);
            player.addAdditionalChatCompletions(glyphsToCompletions(player, addedCompletions));
        }
    }

    private static Collection<String> glyphsToCompletions(Permissible object, Collection<Glyph> glyphs) {
        Collection<String> completions = new HashSet<>();
        for (Glyph glyph : glyphs) {
            if (Permissions.canUse(object, glyph)) {
                completions.addAll(glyph.usages());
            }
        }
        return completions;
    }

}
