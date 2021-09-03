package team.unnamed.emojis.export;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.resourcepack.EmojiResourcePackWriter;
import team.unnamed.emojis.util.Texts;
import team.unnamed.emojis.util.Version;
import team.unnamed.hephaestus.io.Streamable;
import team.unnamed.hephaestus.io.Streams;
import team.unnamed.hephaestus.resourcepack.ResourcePackInfo;
import team.unnamed.hephaestus.resourcepack.ResourcePackInfoWriter;
import team.unnamed.hephaestus.resourcepack.ResourcePackWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

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
        Collection<ResourcePackWriter> writers = new HashSet<>();
        RemoteResource resource = null;

        if (config.getBoolean("pack.meta.write")) {
            String description = config.getString("pack.meta.description", "Hephaestus generated");
            File file = new File(plugin.getDataFolder(), config.getString("pack.meta.icon"));

            writers.add(new ResourcePackInfoWriter(new ResourcePackInfo(
                    getPackFormatVersion(),
                    Texts.escapeDoubleQuotes(description),
                    file.exists() ? Streamable.ofFile(file) : null
            )));
        }

        writers.add(new EmojiResourcePackWriter(registry));

        try {
            Object value = ResourceExportMethodFactory.createExporter(
                    plugin.getDataFolder(),
                    config.getString("pack.export", "into:resourcepack")
            ).export(ResourcePackWriter.compose(writers));

            if (value instanceof JsonElement) {
                JsonObject response = ((JsonElement) value).getAsJsonObject();
                resource = new RemoteResource(
                        response.get("url").getAsString(),
                        Streams.getBytesFromHex(response.get("hash").getAsString())
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot export resource pack", e);
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
