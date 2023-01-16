package team.unnamed.emojis.resourcepack.writer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import team.unnamed.creative.file.FileTreeWriter;
import team.unnamed.emojis.object.store.EmojiStore;

public class TreeWriters {

    private TreeWriters() {
    }

    public static FileTreeWriter writer(Plugin plugin, EmojiStore emojiStore) {

        ConfigurationSection config = plugin.getConfig();
        FileTreeWriter writer = tree -> {};

        if (config.getBoolean("pack.meta.write")) {
            writer = writer.andThen(new PackMetaWriter(plugin));
        }

        return writer.andThen(new EmojisWriter(emojiStore));
    }

}
