package team.unnamed.emojis.reader;

import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.io.Streamable;
import team.unnamed.emojis.io.Streams;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link EmojiReader} for reading
 * emojis from files in the MCEmoji format
 * @author yusshu (Andre Roldan)
 */
public class MCEmojiReader implements EmojiReader {

    /**
     * Pattern for file names, also for extracting the
     * emoji name (removing the file extension)
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("([A-Za-z_]{1,14})\\.mcemoji");

    /**
     * Logger for this class, to log all important information.
     * Exceptions won't be logged by this logger, only warnings
     * and information.
     */
    private static final Logger LOGGER = Logger.getLogger(MCEmojiReader.class.getSimpleName());

    /**
     * The current MCEmoji format version, to allow retro-
     * compatibility
     */
    private static final byte FORMAT_VERSION = 1;

    @Override
    public Map<String, Emoji> read(File folder) throws IOException {

        if (!folder.exists()) {
            // if folder doesn't exist just return
            return Collections.emptyMap();
        }

        File[] files = folder.listFiles(File::isFile);

        if (files == null) {
            // this should never happen but check anyways
            return Collections.emptyMap();
        }

        Map<String, Emoji> emojis = new HashMap<>();

        for (File file : files) {
            Matcher matcher = NAME_PATTERN.matcher(file.getName());
            if (matcher.matches()) {
                String name = matcher.group(1);
                int height, ascent, read;
                char character;
                String permission;

                try (DataInputStream input = createInputStream(file)) {
                    byte formatVersion = input.readByte();
                    if (formatVersion != FORMAT_VERSION) {
                        // Currently, there are no other versions
                        throw new IOException("Invalid format version: '"
                                + formatVersion + "'. Are you from the future?"
                                + " Update this plugin");
                    }

                    height = input.readByte();
                    ascent = input.readByte();
                    character = input.readChar();
                    byte permissionLength = input.readByte();
                    permission = Streams.readString(input, permissionLength);
                    read = permissionLength + 6;
                }

                Streamable data = new Streamable() {
                    @Override
                    public InputStream openIn() throws IOException {
                        InputStream input = new FileInputStream(file);
                        if (input.read(new byte[read]) == -1) { // skip the metadata, just read the image
                            throw new IOException("No image found for emoji " + name);
                        }
                        return input;
                    }
                };

                LOGGER.info("Loaded emoji '" + name + "' with permission '" + permission + "', height: " + height + ", ascent: " + ascent + " char: " + character);
                Emoji emoji = new Emoji(name, permission, data, height, ascent, character);
                emojis.put(name, emoji);
            } else {
                LOGGER.warning("File '" + file.getName()
                        + "' doesn't match the filename pattern '" + NAME_PATTERN.pattern() + "'");
            }
        }

        return emojis;
    }

    /**
     * Simple shortener to create the input stream
     * for the given {@code file}
     */
    private DataInputStream createInputStream(File file) throws IOException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
    }

}
