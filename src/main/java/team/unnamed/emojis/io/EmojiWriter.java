package team.unnamed.emojis.io;

import team.unnamed.emojis.Emoji;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Responsible for writing emojis to sequences
 * of bytes.
 * @author yusshu (Andre Roldan)
 */
public interface EmojiWriter {

    /**
     * Writes the {@code emojis} to the given {@code output}
     * <strong>Note that this method won't close the
     * given OutputStream</strong>
     * @throws IOException If read failed
     */
    void write(
            OutputStream output,
            Collection<Emoji> emojis
    ) throws IOException;

}
