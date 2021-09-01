package team.unnamed.emojis.resourcepack;

import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.util.Texts;
import team.unnamed.hephaestus.io.Streamable;
import team.unnamed.hephaestus.io.Streams;
import team.unnamed.hephaestus.io.TreeOutputStream;
import team.unnamed.hephaestus.resourcepack.ResourcePackInfo;
import team.unnamed.hephaestus.resourcepack.ResourcePackWriter;

import java.io.IOException;
import java.util.Iterator;

/**
 * Responsible for writing resources for the emojis
 * plugin.
 * @author yusshu (Andre Roldan)
 */
public class EmojiResourcePackWriter
        implements ResourcePackWriter {

    private final EmojiRegistry registry;
    @Nullable private final ResourcePackInfo packInfo;

    public EmojiResourcePackWriter(
            EmojiRegistry registry,
            @Nullable ResourcePackInfo packInfo
    ) {
        this.registry = registry;
        this.packInfo = packInfo;
    }

    /**
     * Transfers the resource pack information to the
     * given {@code output}
     *
     * <strong>Note that, as specified in {@link Streamable#transfer},
     * this method won't close the given {@code output}</strong>
     */
    @Override
    public void write(TreeOutputStream output) throws IOException {
        if (packInfo != null) {
            // pack.mcmeta write
            output.useEntry("pack.mcmeta");
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
                output.useEntry("pack.png");
                icon.transfer(output);
                output.closeEntry();
            }
        }

        // write the font json file
        output.useEntry("assets/minecraft/font/default.json");
        Streams.writeUTF(output, createFontJson());
        output.closeEntry();

        // write the emojis images
        for (Emoji emoji : registry.values()) {
            output.useEntry("assets/minecraft/textures/emojis/" + emoji.getName() + ".png");
            emoji.getData().transfer(output);
            output.closeEntry();
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
