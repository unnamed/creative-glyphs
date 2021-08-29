package team.unnamed.emojis.reader;

import team.unnamed.emojis.Emoji;
import team.unnamed.hephaestus.io.Streamable;
import team.unnamed.hephaestus.io.Streams;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of {@link EmojiReader} for reading
 * emojis from the MCEmoji format
 * @author yusshu (Andre Roldan)
 */
public class MCEmojiReader implements EmojiReader {

    /**
     * The current MCEmoji format version
     */
    private static final byte FORMAT_VERSION = 1;

    @Override
    public Collection<Emoji> read(InputStream input) throws IOException {

        // not in a try-with-resources because this shouldn't close the
        // original input stream
        DataInputStream dataInput = new DataInputStream(input);

        byte formatVersion = dataInput.readByte();
        if (formatVersion != FORMAT_VERSION) {
            // Currently, there are no other versions
            throw new IOException("Invalid format version: '"
                    + formatVersion + "'. Are you from the future?"
                    + " Update this plugin");
        }

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

        return Collections.singleton(new Emoji(
                name,
                permission,
                Streamable.ofBytes(imageBytes),
                height,
                ascent,
                character
        ));
    }

}
