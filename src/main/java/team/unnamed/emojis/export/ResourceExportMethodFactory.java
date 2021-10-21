package team.unnamed.emojis.export;

import team.unnamed.emojis.export.http.ArtemisHttpExporter;
import team.unnamed.emojis.export.http.MCPacksHttpExporter;
import team.unnamed.emojis.io.TreeOutputStream;

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

    public static ResourceExporter createExporter(File folder, String format)
            throws IOException {
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
                return new FileExporter(new File(folder, filename))
                        .setMergeZip(method.equals("mergezipfile"));
            }
            case "upload": {
                if (args.length < 3) {
                    throw new IllegalArgumentException(
                            "Invalid format for upload export: '" + format
                                    + "'. Use: 'upload:authorization:url'"
                    );
                }
                String authorization = args[1];
                String url = String.join(":", Arrays.copyOfRange(args, 2, args.length));

                if (authorization.equalsIgnoreCase("none")) {
                    authorization = null;
                }

                return new ArtemisHttpExporter(url)
                        .setAuthorization(authorization);
            }
            case "mcpacks": {
                return new MCPacksHttpExporter();
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
                    targetFolder = new File(folderFormat.substring(1));
                } else {
                    File pluginsFolder = folder.getParentFile();
                    targetFolder = new File(pluginsFolder, folderFormat);
                }

                return writer -> {
                    try (TreeOutputStream output = TreeOutputStream.forFolder(targetFolder)) {
                        writer.write(output);
                    }
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