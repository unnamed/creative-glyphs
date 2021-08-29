package team.unnamed.emojis.export;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.resourcepack.ZipResourcePackWriter;
import team.unnamed.emojis.util.Version;
import team.unnamed.hephaestus.io.Streamable;
import team.unnamed.hephaestus.io.Streams;
import team.unnamed.hephaestus.resourcepack.ResourceExports;
import team.unnamed.hephaestus.resourcepack.ResourcePackInfo;

import java.io.File;
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
    public @Nullable RemoteResource export(EmojiRegistry registry) {

        ConfigurationSection config = plugin.getConfig();
        ResourcePackInfo packInfo = null;
        RemoteResource resource = null;

        if (config.getBoolean("pack.meta.write")) {
            String description = config.getString("pack.meta.description");
            File file = new File(plugin.getDataFolder(), config.getString("pack.meta.icon"));

            packInfo = new ResourcePackInfo(
                    getPackFormatVersion(),
                    description,
                    file.exists() ? Streamable.ofFile(file) : null
            );
        }

        if (config.getBoolean("pack.export.file.do")) {
            File target = new File(plugin.getDataFolder(), config.getString("pack.export.file.target"));
            boolean mergeZip = config.getBoolean("pack.export.file.merge");

            try {
                if (!target.exists() && !target.createNewFile()) {
                    throw new IOException("Already exists, huh?");
                }


                ResourceExports.newFileExporter(target)
                        .setMergeZip(mergeZip)
                        .export(new ZipResourcePackWriter(registry, packInfo));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create output file", e);
            }
        }

        if (config.getBoolean("pack.export.upload.do")) {
            String url = config.getString("pack.export.upload.target");
            String authorization = config.getString("pack.export.upload.authorization");

            try {
                ResourceExports.HttpExporter exporter = ResourceExports.newHttpExporter(url);
                if (authorization != null) {
                    exporter.setAuthorization(authorization);
                }
                JsonObject response = new JsonParser()
                        .parse(exporter.export(new ZipResourcePackWriter(registry, packInfo)))
                        .getAsJsonObject();

                String downloadUrl = response.get("url").getAsString();
                byte[] hash = Streams.getBytesFromHex(response.get("hash").getAsString());

                resource = new RemoteResource(downloadUrl, hash);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot upload output file", e);
            }
        }

        return resource;
    }

    private static void failUnsupportedVersion() {
        throw new UnsupportedOperationException("Unsupported version: " + Version.CURRENT);
    }

    private static int getPackFormatVersion() {
        // 1 for 1.6.1–1.8.9, 2 for 1.9–1.10.2, 3 for 1.11–1.12.2, 4 for 1.13–1.14.4, 5 for 1.15–1.16.1, 6 for 1.16.2–1.16.5, and 7 for 1.17.
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
        if (minor < 16) return 5;
        if (minor < 17) return 6;
        if (minor < 18) return 7;
        failUnsupportedVersion();

        return 0;
    }

}
