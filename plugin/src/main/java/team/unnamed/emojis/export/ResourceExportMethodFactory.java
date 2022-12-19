package team.unnamed.emojis.export;

import org.bukkit.plugin.Plugin;
import team.unnamed.creative.file.FileTree;
import team.unnamed.emojis.export.impl.FileExporter;
import team.unnamed.emojis.export.impl.MCPacksHttpExporter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Factory for creating exporting methods
 * from {@link String} (from configuration)
 */
public final class ResourceExportMethodFactory {

    private ResourceExportMethodFactory() {
    }

    public static ResourceExporter createExporter(Plugin plugin, String format)
            throws IOException {
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

                return writer -> {
                    try (FileTree output = FileTree.directory(targetFolder)) {
                        writer.write(output);
                    }
                    plugin.getLogger().info("Exported resource pack to folder: " + targetFolder);
                    return null;
                };
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