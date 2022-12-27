package team.unnamed.emojis.resourcepack.export;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.file.FileTreeWriter;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.resourcepack.EmojisWriter;
import team.unnamed.emojis.resourcepack.PackMetaWriter;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.IOException;

/**
 * Default implementation of {@link ExportService},
 * its behaviour is defined by the user in configuration
 * @author yusshu (Andre Roldan)
 */
public class DefaultExportService
        implements ExportService {

    private final Plugin plugin;

    public DefaultExportService(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable UrlAndHash export(EmojiRegistry registry) {

        ConfigurationSection config = plugin.getConfig();
        FileTreeWriter writer = tree -> {};

        if (config.getBoolean("pack.meta.write")) {
            writer = writer.andThen(new PackMetaWriter(plugin));
        }

        writer = writer.andThen(new EmojisWriter(registry));

        try {
            return ResourceExportMethodFactory.createExporter(
                    plugin,
                    config.getString("pack.export", "into:resourcepack")
            ).export(writer);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot export resource pack", e);
        }
    }

}
