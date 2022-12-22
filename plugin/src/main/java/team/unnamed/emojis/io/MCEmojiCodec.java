package team.unnamed.emojis.io;

import team.unnamed.creative.base.Writable;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.format.EmojiFormat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * Implementation of {@link EmojiCodec} for reading
 * and writing emojis from/to the MCEmoji format
 * @author yusshu (Andre Roldan)
 */
public class MCEmojiCodec implements EmojiCodec {

    // current version: 2

    @Override
    public Collection<Emoji> read(InputStream input) throws IOException {

        // not in a try-with-resources because this shouldn't close the
        // original input stream
        DataInputStream dataInput = new DataInputStream(input);

        byte formatVersion = dataInput.readByte();
        if (formatVersion != 1 && formatVersion != 2) {
            // Currently, there are no other versions
            throw new IOException("Invalid format version: '"
                    + formatVersion + "'. Are you from the future?"
                    + " Update this plugin");
        }

        int emojiCount = dataInput.readUnsignedByte();
        Emoji[] emojis = new Emoji[emojiCount];

        for (short i = 0; i < emojiCount; i++) {
            // name read
            byte nameLength = dataInput.readByte();
            String name = Streams.readString(dataInput, nameLength);

            if (!EmojiFormat.EMOJI_NAME_PATTERN.matcher(name).matches()) {
                throw new IOException("Invalid emoji name: '" + name + "', must match pattern: "
                        + EmojiFormat.EMOJI_NAME_PATTERN_STRING);
            }

            int height = dataInput.readShort();
            int ascent = dataInput.readShort();
            char character = dataInput.readChar();

            // permission read
            byte permissionLength = dataInput.readByte();
            String permission = Streams.readString(dataInput, permissionLength);

            if (!EmojiFormat.EMOJI_PERMISSION_PATTERN.matcher(permission).matches()) {
                throw new IOException("Invalid emoji permission: '" + permission + "', for emoji '"
                        + name + "'. Must match pattern: " + EmojiFormat.EMOJI_PERMISSION_PATTERN_STRING);
            }

            // image read
            // FIX (from version 2): uses an int instead of an unsigned short to represent image lengths
            int imageLength = formatVersion == 1
                    ? (dataInput.readShort() & 0xFFFF)
                    : dataInput.readInt();

            byte[] imageBytes = new byte[imageLength];
            int read = input.read(imageBytes); // save the image bytes

            if (read != imageLength) {
                throw new IOException("Image length mismatch, specified: '"
                        + imageLength + "', found: '" + read + "'");
            }

            emojis[i] = new Emoji(
                    name,
                    permission,
                    imageLength,
                    Writable.bytes(imageBytes),
                    height,
                    ascent,
                    character
            );
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

        // TODO: Remove, only for backwards-compatibility
        for (Emoji emoji : emojis) {
            // if an emoji data length is >= than an unsigned short
            // max value, we must use the format version 2, that fixes it
            if (emoji.dataLength() >= 0xFFFF) {
                formatVersion = 2;
                break;
            }
        }

        // write current MCEmoji format
        dataOutput.write(formatVersion);

        // write emoji length
        dataOutput.writeByte(emojis.size());

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
            dataOutput.writeChar(emoji.replacement().charAt(0));

            // write permission
            dataOutput.writeByte(permission.length());
            dataOutput.writeChars(permission);

            // image write
            if (formatVersion == 1) {
                dataOutput.writeShort(emoji.dataLength());
            } else {
                dataOutput.writeInt(emoji.dataLength()); // fix from format version 2
            }
            emoji.data().write(dataOutput);
        }
    }

}
