package team.unnamed.emojis.hook.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.object.store.EmojiStore;
import team.unnamed.emojis.provider.head.HeadEmojiProvider;

/**
 * Placeholder expansion for PlaceholderAPI, provides the
 * emojis allowing to be used in other places
 */
public class EmojiPlaceholderExpansion
        extends PlaceholderExpansion {

    private final Plugin plugin;
    private final EmojiStore registry;

    public EmojiPlaceholderExpansion(
            Plugin plugin,
            EmojiStore registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String name) {
        Emoji emoji = registry.get(name);
        if (emoji == null) {
            Player p = Bukkit.getPlayer(name);
            if (p == null) return null;
            return LegacyComponentSerializer.legacySection().serialize(HeadEmojiProvider.of(p));
        } else {
            return emoji.replacement();
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "emoji";
    }

    @Override
    public @NotNull String getAuthor() {
        // return the first author in the list
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

}
