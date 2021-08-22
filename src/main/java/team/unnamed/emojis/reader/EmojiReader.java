package team.unnamed.emojis.reader;

import team.unnamed.emojis.Emoji;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Responsible for reading emojis from sequences
 * of bytes.
 * @author yusshu (Andre Roldan)
 */
public interface EmojiReader {

    /**
     * Reads the emojis from the given {@code input}
     * <strong>Note that this method won't close the
     * given InputStream</strong>
     * @return The read emojis
     * @throws IOException If read failed
     */
    Collection<Emoji> read(InputStream input) throws IOException;

}
