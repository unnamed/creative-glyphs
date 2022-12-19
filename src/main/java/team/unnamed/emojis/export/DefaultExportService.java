package team.unnamed.emojis.export;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.resourcepack.PackMeta;
import team.unnamed.emojis.resourcepack.PackMetaWriter;
import team.unnamed.emojis.io.AssetWriter;
import team.unnamed.emojis.io.Writeable;
import team.unnamed.emojis.resourcepack.EmojiAssetWriter;
import team.unnamed.emojis.resourcepack.UrlAndHash;
import team.unnamed.emojis.util.Texts;
import team.unnamed.emojis.util.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

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
        Collection<AssetWriter> writers = new HashSet<>();

        if (config.getBoolean("pack.meta.write")) {
            String description = config.getString("pack.meta.description", "Hephaestus generated");
            File file = new File(plugin.getDataFolder(), "icon.png");

            if (!file.exists()) {
                plugin.getLogger().warning("Resource-pack icon not found " +
                        "(must be at unemojis/icon.png), using a default one");
                try (OutputStream output = new FileOutputStream(file)) {
                    Streams.pipe(
                            plugin.getResource("icon.png"),
                            output
                    );
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to create a default resource-pack icon", e);
                }
            }

            writers.add(new PackMetaWriter(new PackMeta(
                    getPackFormatVersion(),
                    Texts.escapeDoubleQuotes(description),
                    file.exists() ? Writeable.ofFile(file) : null
            )));
        }

        writers.add(new EmojiAssetWriter(registry));

        try {
            return ResourceExportMethodFactory.createExporter(
                    plugin,
                    config.getString("pack.export", "into:resourcepack")
            ).export(AssetWriter.compose(writers));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot export resource pack", e);
        }
    }

    private static void failUnsupportedVersion() {
        throw new UnsupportedOperationException("Unsupported version: " + Version.CURRENT);
    }

    private static int getPackFormatVersion() {
        Version version = Version.CURRENT;
        byte major = version.getMajor();

        if (major != 1) {
            failUnsupportedVersion();
        }

        byte minor = version.getMinor();

        if (minor < 6) failUnsupportedVersion();
        if (minor < 9) return 1;
        if (minor < 11) return 2;
        if (minor < 13) return 3;
        if (minor < 15) return 4;

        // Minecraft 1.15, 1.16, 1.17 and 1.18 use
        // their minor number - 10 as resource-pack
        // format version, this may change, so we
        // may have to change this later
        return minor - 10;
    }

}
