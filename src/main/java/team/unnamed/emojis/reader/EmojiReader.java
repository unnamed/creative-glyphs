package team.unnamed.emojis.reader;

import team.unnamed.emojis.Emoji;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Responsible for reading emojis from files.
 * @author yusshu (Andre Roldan)
 */
public interface EmojiReader {

    /**
     * Reads the emojis from the given {@code folder}
     * @return The read emojis (it may be immutable)
     * @throws IOException If read failed or found an
     * invalid emoji
     */
    Map<String, Emoji> read(File folder) throws IOException;

}
