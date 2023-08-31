package team.unnamed.creativeglyphs.object.serialization;

import org.intellij.lang.annotations.Subst;
import team.unnamed.creativeglyphs.Emoji;
import team.unnamed.creativeglyphs.format.EmojiFormat;

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
 * <p>Implementation of {@link EmojiCodec} for reading
 * and writing emojis from/to the MCEmoji format</p>
 *
 * <p>This implementation currently supports MCEmoji
 * format versions [ 1, 2, 3, 4, 5 ]</p>
 *
 * @author yusshu (Andre Roldan)
 */
final class MCEmojiCodec implements EmojiCodec {

    public static EmojiCodec INSTANCE = new MCEmojiCodec();

    private MCEmojiCodec() {
    }

    // newest version: 5

    @Override
    public Collection<Emoji> read(InputStream input) throws IOException {

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
        //    the emoji count, supporting more than 256+ emojis
        int emojiCount = formatVersion >= 4 ? dataInput.readInt() : dataInput.readUnsignedByte();
        Emoji[] emojis = new Emoji[emojiCount];

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

            if (!EmojiFormat.PERMISSION_PATTERN.matcher(permission).matches()) {
                throw new IOException("Invalid emoji permission: '" + permission + "', for emoji '"
                        + name + "'. Must match pattern: " + EmojiFormat.PERMISSION_PATTERN_STR);
            }

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
                usages.add(EmojiFormat.usageOf(name));
            }

            // image read
            // FIX (from version 2): uses an int instead of an unsigned short to represent image lengths
            int imageLength = formatVersion >= 2
                    ? dataInput.readInt()
                    : (dataInput.readShort() & 0xFFFF);

            byte[] imageBytes = new byte[imageLength];
            int read = input.read(imageBytes); // save the image bytes

            if (read != imageLength) {
                throw new IOException("Image length mismatch, specified: '"
                        + imageLength + "', found: '" + read + "'");
            }

            emojis[i] = Emoji.builder()
                    .name(name)
                    .permission(permission)
                    .usages(usages)
                    .data(imageBytes)
                    .height(height)
                    .ascent(ascent)
                    .character(character)
                    .build();
        }

        return Arrays.asList(emojis);
    }

    @Override
    public void write(
            OutputStream output,
            Collection<Emoji> emojis
    ) throws IOException {

        // not in a try-with-resources because this shouldn't close the
        // original output stream
        DataOutputStream dataOutput = new DataOutputStream(output);
        byte formatVersion = 1;

        for (Emoji emoji : emojis) {
            // if an emoji data length is >= than an unsigned short
            // max value, we must use the format version 2, that fixes it
            if (emoji.dataLength() >= 0xFFFF) {
                formatVersion = formatVersion < 2 ? 2 : formatVersion;
            }

            if (!Character.isBmpCodePoint(emoji.character())) {
                formatVersion = formatVersion < 3 ? 3 : formatVersion;
            }

            if (emoji.usages().size() != 1 || !emoji.usages().contains(EmojiFormat.usageOf(emoji.name()))) {
                formatVersion = formatVersion < 5 ? 5 : formatVersion;
            }
        }

        if (emojis.size() >= 250) {
            formatVersion = formatVersion < 4 ? 4 : formatVersion;
        }

        // write current MCEmoji format
        dataOutput.write(formatVersion);

        // write emoji length
        if (formatVersion >= 4) {
            dataOutput.writeInt(emojis.size());
        } else {
            dataOutput.writeByte(emojis.size());
        }

        // write all emojis
        for (Emoji emoji : emojis) {

            String name = emoji.name();
            String permission = emoji.permission();

            // write name
            dataOutput.writeByte(name.length());
            dataOutput.writeChars(name);

            // height, ascent and character
            dataOutput.writeShort(emoji.height());
            dataOutput.writeShort(emoji.ascent());
            if (formatVersion >= 3) {
                dataOutput.writeInt(emoji.character());
            } else {
                dataOutput.writeChar((char) emoji.character());
            }

            // write permission
            dataOutput.writeByte(permission.length());
            dataOutput.writeChars(permission);

            // write usages
            if (formatVersion >= 5) {
                dataOutput.writeInt(emoji.usages().size());
                for (String usage : emoji.usages()) {
                    dataOutput.writeByte(usage.length());
                    dataOutput.writeChars(usage);
                }
            }

            // image write
            if (formatVersion >= 2) {
                dataOutput.writeInt(emoji.dataLength()); // fix from format version 2
            } else {
                dataOutput.writeShort(emoji.dataLength());
            }
            emoji.data().write(dataOutput);
        }
    }

}
