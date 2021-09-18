package team.unnamed.emojis.io.reader;

import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.io.MCEmojiFormat;
import team.unnamed.hephaestus.io.Streamable;
import team.unnamed.hephaestus.io.Streams;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * Implementation of {@link EmojiReader} for reading
 * emojis from the MCEmoji format
 * @author yusshu (Andre Roldan)
 */
public class MCEmojiReader implements EmojiReader {

    @Override
    public Collection<Emoji> read(InputStream input) throws IOException {

        // not in a try-with-resources because this shouldn't close the
        // original input stream
        DataInputStream dataInput = new DataInputStream(input);

        byte formatVersion = dataInput.readByte();
        if (formatVersion != MCEmojiFormat.VERSION) {
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
            int imageLength = dataInput.readShort();

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
                    Streamable.ofBytes(imageBytes),
                    height,
                    ascent,
                    character
            );
        }

        return Arrays.asList(emojis);
    }

}
