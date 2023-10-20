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

@SuppressWarnings("deprecation") // they deprecated addAdditionalChatCompletions and removeAdditionalChatCompletions
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            var addedCompletions = new HashSet<String>();
            var removedCompletions = new HashSet<String>();

            var oldCompletions = new HashSet<String>();
            for (Glyph oldGlyph : oldRegistry.values()) {
                glyphToCompletions(oldGlyph, player, oldCompletions);
            }

            var newCompletions = new HashSet<String>();
            for (Glyph newGlyph : newRegistry.values()) {
                glyphToCompletions(newGlyph, player, newCompletions);
            }

            difference(oldCompletions, newCompletions, removedCompletions, addedCompletions);

            player.removeAdditionalChatCompletions(removedCompletions);
            player.addAdditionalChatCompletions(addedCompletions);
        }
    }

    // calculate difference of sets
    private static <E> void difference(Collection<E> a, Collection<E> b, Collection<E> aResult, Collection<E> bResult) {
        for (E e : a) {
            if (!b.contains(e)) {
                aResult.add(e);
            }
        }
        for (E e : b) {
            if (!a.contains(e)) {
                bResult.add(e);
            }
        }
    }

    private static void glyphToCompletions(Glyph glyph, Permissible permissible, Collection<String> into) {
        if (Permissions.canUse(permissible, glyph)) {
            into.addAll(glyph.usages());
        }
    }

    private static Collection<String> glyphsToCompletions(Permissible object, Collection<Glyph> glyphs) {
        Collection<String> completions = new HashSet<>();
        for (Glyph glyph : glyphs) {
            glyphToCompletions(glyph, object, completions);
        }
        return completions;
    }

}
