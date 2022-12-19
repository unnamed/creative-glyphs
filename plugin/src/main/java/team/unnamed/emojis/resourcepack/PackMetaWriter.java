package team.unnamed.emojis.resourcepack;

import team.unnamed.emojis.io.AssetWriter;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.io.TreeOutputStream;
import team.unnamed.emojis.io.Writeable;

import java.io.IOException;

/**
 * Implementation of {@link AssetWriter} that
 * writes the resource pack information.
 * @see PackMeta
 */
public class PackMetaWriter
        implements AssetWriter {

    private final PackMeta info;

    public PackMetaWriter(PackMeta info) {
        this.info = info;
    }

    @Override
    public void write(TreeOutputStream output) throws IOException {
        // write the pack data
        output.useEntry("pack.mcmeta");
        Streams.writeUTF(
                output,
                "{ " +
                        "\"pack\":{" +
                        "\"pack_format\":" + info.getFormat() + "," +
                        "\"description\":\"" + info.getDescription() + "\"" +
                        "}" +
                        "}"
        );
        output.closeEntry();

        // write the pack icon if not null
        Writeable icon = info.getIcon();
        if (icon != null) {
            output.useEntry("pack.png");
            icon.write(output);
            output.closeEntry();
        }
    }

}