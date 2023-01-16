package team.unnamed.emojis.resourcepack.export;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.resourcepack.export.impl.FileExporter;
import team.unnamed.emojis.resourcepack.export.impl.FolderExporter;
import team.unnamed.emojis.resourcepack.export.impl.MCPacksHttpExporter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Factory for creating exporting methods
 * from {@link String} (from configuration)
 */
public final class ResourceExportMethodFactory {

    private ResourceExportMethodFactory() {
    }

    public static ResourceExporter createExporter(Plugin plugin)
            throws IOException {

        ConfigurationSection config = plugin.getConfig();
        String methodName = config.getString("pack.export.type", null);

        if (methodName == null) {
            // legacy way?
            return createLegacyExporter(
                    plugin,
                    config.getString("pack.export", "into:resourcepack")
            );
        }

        return switch (methodName.toLowerCase(Locale.ROOT)) {
            case "mcpacks" ->
                    new MCPacksHttpExporter(plugin.getLogger());
            case "file" ->
                    new FileExporter(
                            new File(plugin.getDataFolder(), "resource-pack.zip"),
                            plugin.getLogger()
                    );
            case "folder" ->
                    new FolderExporter(
                            new File(plugin.getDataFolder(), "resource-pack"),
                            plugin.getLogger()
                    );
            default ->
                    throw new IOException("Unknown export method: " + methodName);
        };
    }

    private static ResourceExporter createLegacyExporter(Plugin plugin, String format) throws IOException {

        File pluginFolder = plugin.getDataFolder();
        String[] args = format.split(":");
        String method = args[0].toLowerCase();

        switch (method) {
            case "mergezipfile":
            case "file": {
                if (args.length < 2) {
                    throw new IllegalArgumentException(
                            "Invalid format for file export: '" + format
                                    + "'. Use: 'file:filename'"
                    );
                }

                String filename = String.join(":", Arrays.copyOfRange(args, 1, args.length));
                return new FileExporter(new File(pluginFolder, filename), plugin.getLogger())
                        .setMergeZip(method.equals("mergezipfile"));
            }
            case "mcpacks": {
                return new MCPacksHttpExporter(plugin.getLogger());
            }
            case "into": {
                if (args.length < 2) {
                    throw new IllegalArgumentException(
                            "Invalid format for file tree export: '"
                                    + format + "'. Use: 'into:folder'"
                    );
                }

                String folderFormat = args[1];

                File targetFolder;

                if (folderFormat.startsWith("/")) {
                    targetFolder = new File(folderFormat);
                } else if (folderFormat.startsWith("@")) {
                    targetFolder = new File(
                            pluginFolder.getParentFile(), // The <server>/plugins folder
                            folderFormat.substring(1)
                    );
                } else {
                    targetFolder = new File(pluginFolder, folderFormat);
                }

                return new FolderExporter(targetFolder, plugin.getLogger());
            }
            default: {
                throw new IllegalArgumentException(
                        "Invalid format: '" + format + "', unknown export"
                                + "method: '" + method + "'"
                );
            }
        }

    }

}