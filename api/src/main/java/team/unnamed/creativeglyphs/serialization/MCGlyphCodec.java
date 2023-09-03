package team.unnamed.creativeglyphs.serialization;

import org.intellij.lang.annotations.Subst;
import team.unnamed.creativeglyphs.Glyph;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>An implementation of {@link GlyphReader} and
 * {@link GlyphReader} that reads and writes glyphs
 * from/to the MCGlyph (formerly MCEmoji) format</p>
 *
 * <p>This implementation currently supports MCGlyph
 * (formerly MCEmoji) format versions [ 1, 2, 3, 4, 5 ]</p>
 *
 * @author yusshu (Andre Roldan)
 */
final class MCGlyphCodec implements GlyphReader, GlyphWriter {

    static final MCGlyphCodec INSTANCE = new MCGlyphCodec();

    private MCGlyphCodec() {
    }

    // newest version: 5

    @Override
    public Collection<Glyph> read(InputStream input) throws IOException {

        // not in a try-with-resources because this shouldn't close the
        // original input stream
        DataInputStream dataInput = new DataInputStream(input);

        byte formatVersion = dataInput.readByte();
        if (formatVersion < 1 || formatVersion > 5) {
            // Currently, there are no other versions
            throw new IOException("Invalid format version: '"
                    + formatVersion + "'. Are you from the future?"
                    + " Update this plugin");
        }

        // emoji count read
        // FEATURE (from version 4): uses an int instead of an unsigned byte to represent
        //    the emoji count, supporting more than 256 emojis
        int emojiCount = formatVersion >= 4 ? dataInput.readInt() : dataInput.readUnsignedByte();
        Glyph[] glyphs = new Glyph[emojiCount];

        for (short i = 0; i < emojiCount; i++) {
            // name read
            byte nameLength = dataInput.readByte();
            @Subst("emoji") String name = Streams.readString(dataInput, nameLength);

            int height = dataInput.readShort();
            int ascent = dataInput.readShort();

            // character read
            // FEATURE (from version 3): uses an int instead of an unsigned short to represent
            //   characters, so it now supports UTF-16 surrogate pairs
            int character = formatVersion >= 3
                    ? dataInput.readInt()
                    : dataInput.readShort() & 0xFFFF;

            // permission read
            byte permissionLength = dataInput.readByte();
            @Subst("emojis.emoji") String permission = Streams.readString(dataInput, permissionLength);

            Set<String> usages = new HashSet<>();
            if (formatVersion >= 5) {
                // FEATURE (from version 5): supports multiple usages per emoji, like ":)", "<3"
                int usagesCount = dataInput.readInt();
                for (int j = 0; j < usagesCount; j++) {
                    byte usageLength = dataInput.readByte();
                    @Subst(":smile:")
                    String usage = Streams.readString(dataInput, usageLength);
                    usages.add(usage);
                }
            } else {
                // fall-back to use the old default usages,
                // that only support usages like ":smile:",
                // or ":tree:", not something like ":)", "<3"
                usages.add(defaultGlyphUsage(name));
            }

            // image read
            // FIX (from version 2): uses an int instead of an unsigned short to represent image lengths
            int imageLength = formatVersion >= 2
                    ? dataInput.readInt()
                    : (dataInput.readShort() & 0xFFFF);

            byte[] imageBytes = new byte[imageLength];
            if (imageLength != 0) {
                int read = input.read(imageBytes); // save the image bytes
                if (read != imageLength) {
                    throw new IOException("Image length mismatch, specified: '"
                            + imageLength + "', found: '" + read + "'");
                }
            }

            glyphs[i] = Glyph.builder()
                    .name(name)
                    .permission(permission)
                    .usages(usages)
                    .data(imageBytes)
                    .height(height)
                    .ascent(ascent)
                    .character(character)
                    .build();
        }

        return Arrays.asList(glyphs);
    }

    @Override
    public void write(
            OutputStream output,
            Collection<Glyph> glyphs
    ) throws IOException {

        // not in a try-with-resources because this shouldn't close the
        // original output stream
        DataOutputStream dataOutput = new DataOutputStream(output);
        byte formatVersion = 1;

        for (Glyph glyph : glyphs) {
            // if an emoji data length is >= than an unsigned short
            // max value, we must use the format version 2, that fixes it
            if (glyph.dataLength() >= 0xFFFF) {
                formatVersion = formatVersion < 2 ? 2 : formatVersion;
            }

            if (!Character.isBmpCodePoint(glyph.character())) {
                formatVersion = formatVersion < 3 ? 3 : formatVersion;
            }

            if (glyph.usages().size() != 1 || !glyph.usages().contains(defaultGlyphUsage(glyph.name()))) {
                formatVersion = formatVersion < 5 ? 5 : formatVersion;
            }
        }

        if (glyphs.size() >= 250) {
            formatVersion = formatVersion < 4 ? 4 : formatVersion;
        }

        // write current MCEmoji format
        dataOutput.write(formatVersion);

        // write emoji length
        if (formatVersion >= 4) {
            dataOutput.writeInt(glyphs.size());
        } else {
            dataOutput.writeByte(glyphs.size());
        }

        // write all emojis
        for (Glyph glyph : glyphs) {

            String name = glyph.name();
            String permission = glyph.permission();

            // write name
            dataOutput.writeByte(name.length());
            dataOutput.writeChars(name);

            // height, ascent and character
            dataOutput.writeShort(glyph.height());
            dataOutput.writeShort(glyph.ascent());
            if (formatVersion >= 3) {
                dataOutput.writeInt(glyph.character());
            } else {
                dataOutput.writeChar((char) glyph.character());
            }

            // write permission
            dataOutput.writeByte(permission.length());
            dataOutput.writeChars(permission);

            // write usages
            if (formatVersion >= 5) {
                dataOutput.writeInt(glyph.usages().size());
                for (String usage : glyph.usages()) {
                    dataOutput.writeByte(usage.length());
                    dataOutput.writeChars(usage);
                }
            }

            // image write
            if (formatVersion >= 2) {
                dataOutput.writeInt(glyph.dataLength()); // fix from format version 2
            } else {
                dataOutput.writeShort(glyph.dataLength());
            }
            glyph.data().write(dataOutput);
        }
    }

    // returns the default glyph usage, before MCGlyph (MCEmoji) v5,
    // the "usages" option did not exist, so a default one was used
    // with a format like: ":smile:", ":heart:", ":heart_eyes:"
    private static String defaultGlyphUsage(String glyphName) {
        return ':' + glyphName + ':';
    }

}
