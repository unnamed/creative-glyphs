package team.unnamed.emojis.io;

import team.unnamed.creative.base.Writable;
import team.unnamed.emojis.Emoji;

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

    private static final byte VERSION = 1;

    @Override
    public Collection<Emoji> read(InputStream input) throws IOException {

        // not in a try-with-resources because this shouldn't close the
        // original input stream
        DataInputStream dataInput = new DataInputStream(input);

        byte formatVersion = dataInput.readByte();
        if (formatVersion != VERSION) {
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

            int height = dataInput.readShort();
            int ascent = dataInput.readShort();
            char character = dataInput.readChar();

            // permission read
            byte permissionLength = dataInput.readByte();
            String permission = Streams.readString(dataInput, permissionLength);

            // image read
            int imageLength = dataInput.readShort() & 0xFFFF;

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

        // write current MCEmoji format
        dataOutput.write(VERSION);

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
            dataOutput.writeChar(emoji.character());

            // write permission
            dataOutput.writeByte(permission.length());
            dataOutput.writeChars(permission);

            // image write
            dataOutput.writeShort(emoji.dataLength());
            emoji.data().write(dataOutput);
        }
    }

}
