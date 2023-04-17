package team.unnamed.emojis.resourcepack.export;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.ResourcePack;
import team.unnamed.emojis.object.store.EmojiStore;
import team.unnamed.emojis.resourcepack.UrlAndHash;
import team.unnamed.emojis.resourcepack.writer.EmojisWriter;
import team.unnamed.emojis.resourcepack.writer.PackMetaWriter;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ExportService},
 * its behaviour is defined by the user in configuration
 * @author yusshu (Andre Roldan)
 */
public class DefaultExportService
        implements ExportService {

    private final Plugin plugin;
    private final ResourceExporter exporter;

    public DefaultExportService(Plugin plugin) {
        this.plugin = plugin;
        try {
            this.exporter = ResourceExportMethodFactory.createExporter(plugin);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create resource exporter", e);
        }
    }

    @Override
    public @Nullable UrlAndHash export(EmojiStore emojiStore) {

        ConfigurationSection config = plugin.getConfig();
        Consumer<ResourcePack> writer = rp -> {};

        if (config.getBoolean("pack.meta.write")) {
            writer = writer.andThen(new PackMetaWriter(plugin));
        }

        writer = writer.andThen(new EmojisWriter(emojiStore));

        try {
            ResourcePack resourcePack = ResourcePack.create();
            writer.accept(resourcePack);
            exporter.export(resourcePack);
            return exporter.location();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot export resource pack", e);
        }
    }

    @Override
    public void close() throws IOException {
        exporter.close();
    }

}
