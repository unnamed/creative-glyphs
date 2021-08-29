package team.unnamed.emojis.resourcepack;

import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.util.Texts;
import team.unnamed.hephaestus.io.Streamable;
import team.unnamed.hephaestus.io.Streams;
import team.unnamed.hephaestus.resourcepack.ResourcePackInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Responsible for exporting the emojis using
 * ZIP compression
 * @author yusshu (Andre Roldan)
 */
public class ZipResourcePackWriter
        implements Streamable {

    private final EmojiRegistry registry;
    @Nullable private final ResourcePackInfo packInfo;

    public ZipResourcePackWriter(
            EmojiRegistry registry,
            @Nullable ResourcePackInfo packInfo
    ) {
        this.registry = registry;
        this.packInfo = packInfo;
    }

    /**
     * Invokes {@link ZipOutputStream#putNextEntry} using
     * some default {@link ZipEntry} properties to avoid
     * creating different ZIPs when the resource pack is
     * the same (So hash doesn't change)
     */
    private void putNext(ZipOutputStream output, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        entry.setTime(0L);
        output.putNextEntry(entry);
    }

    /**
     * Transfers the resource pack information to the
     * given {@code output}
     *
     * <strong>Note that, as specified in {@link Streamable#transfer},
     * this method won't close the given {@code output}</strong>
     */
    @Override
    public void transfer(OutputStream stream) throws IOException {

        ZipOutputStream output = stream instanceof ZipOutputStream
                ? (ZipOutputStream) stream
                : new ZipOutputStream(stream);

        try {
            if (packInfo != null) {
                // pack.mcmeta write
                putNext(output, "pack.mcmeta");
                Streams.writeUTF(
                        output,
                        "{\"pack\": {" +
                                "\"pack_format\": " + packInfo.getFormat() + "," +
                                "\"description\": \"" + Texts.escapeDoubleQuotes(packInfo.getDescription()) + "\"" +
                                "}}"
                );
                output.closeEntry();

                // icon write
                Streamable icon = packInfo.getIcon();
                if (icon != null) {
                    putNext(output, "pack.png");
                    icon.transfer(output);
                    output.closeEntry();
                }
            }

            // write the font json file
            putNext(output, "assets/minecraft/font/default.json");
            Streams.writeUTF(output, createFontJson());
            output.closeEntry();

            // write the emojis images
            for (Emoji emoji : registry.values()) {
                putNext(output, "assets/minecraft/textures/emojis/" + emoji.getName() + ".png");
                emoji.getData().transfer(output);
                output.closeEntry();
            }
        } finally {
            if (stream != output) {
                // finish but don't close
                output.finish();
            }
        }
    }

    private String createFontJson() {
        StringBuilder builder = new StringBuilder("{ \"providers\": [ ");

        Iterator<Emoji> iterator = registry.values().iterator();
        while (iterator.hasNext()) {
            Emoji emoji = iterator.next();
            builder
                    .append("{\"ascent\":").append(emoji.getAscent())
                    .append(",\"chars\":[\"").append(emoji.getCharacter())
                    .append("\"],\"file\":\"emojis/").append(emoji.getName()).append(".png")
                    .append("\",\"height\":").append(emoji.getHeight())
                    .append(",\"type\":\"bitmap\"}");
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }

        return builder.append("]}").toString();
    }

}
