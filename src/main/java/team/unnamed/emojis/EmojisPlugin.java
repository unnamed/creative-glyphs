package team.unnamed.emojis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.emojis.io.Streamable;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.listener.ChatListener;
import team.unnamed.emojis.listener.ResourcePackApplyListener;
import team.unnamed.emojis.reader.EmojiReader;
import team.unnamed.emojis.reader.ResourceEmojiReader;
import team.unnamed.emojis.resourcepack.ResourceExports;
import team.unnamed.emojis.resourcepack.ResourcePackInfo;
import team.unnamed.emojis.resourcepack.ZipResourcePackWriter;
import team.unnamed.emojis.util.Version;

import java.io.File;
import java.io.IOException;

public class EmojisPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        saveDefaultConfig();

        EmojiRegistry registry = new EmojiRegistry();
        EmojiReader reader = new ResourceEmojiReader(
                getClassLoader(),
                "blush", "grin", "grinning", "heart_eyes", "innocent"
        ); //new MCEmojiReader();

        File folder = new File(getDataFolder(), "emojis");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Cannot create emojis folder");
        }

        try {
            reader.read(folder).forEach((name, emoji) -> registry.add(emoji));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load emojis", e);
        }

        Bukkit.getPluginManager().registerEvents(
                new ChatListener(registry),
                this
        );

        // export
        ConfigurationSection config = getConfig();
        ResourcePackInfo packInfo = null;

        if (config.getBoolean("pack.meta.write")) {
            String description = config.getString("pack.meta.description");
            File file = new File(getDataFolder(), config.getString("pack.meta.icon"));

            packInfo = new ResourcePackInfo(
                    getPackFormatVersion(),
                    description,
                    file.exists() ? Streamable.ofFile(file) : null
            );
        }

        if (config.getBoolean("pack.export.file.do")) {
            File target = new File(getDataFolder(), config.getString("pack.export.file.target"));
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

                if (config.getBoolean("pack.export.upload.apply")) {
                    Bukkit.getPluginManager().registerEvents(
                            new ResourcePackApplyListener(downloadUrl, hash),
                            this
                    );
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot upload output file", e);
            }
        }
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
