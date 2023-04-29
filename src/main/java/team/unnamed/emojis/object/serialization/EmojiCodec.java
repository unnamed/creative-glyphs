package team.unnamed.emojis.object.serialization;

import team.unnamed.emojis.Emoji;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Responsible for reading and writing emojis
 * from/to sequences of bytes with {@link InputStream}
 * and {@link OutputStream}
 * @author yusshu (Andre Roldan)
 */
public interface EmojiCodec {

    /**
     * Reads the emojis from the given {@code input}
     * <strong>Note that this method won't close the
     * given InputStream</strong>
     * @return The read emojis
     * @throws IOException If read failed
     */
    Collection<Emoji> read(
            InputStream input
    ) throws IOException;

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

    static EmojiCodec mcemoji() {
        return MCEmojiCodec.INSTANCE;
    }

    static EmojiCodec yaml(File texturesFolder) {
        return new YamlEmojiCodec(texturesFolder);
    }

}
