package team.unnamed.emojis.reader;

import team.unnamed.emojis.Emoji;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.zip.ZipInputStream;

/**
 * Implementation of {@link EmojiReader} for
 * reading emojis from a ZIP file, delegates
 * the read to another {@link EmojiReader}
 * instance.
 * @author yusshu (Andre Roldan)
 */
public class ZipEmojiReader implements EmojiReader {

    /**
     * The delegated emoji reader, this is the one
     * who actually reads the emojis
     */
    private final EmojiReader delegate;

    public ZipEmojiReader(EmojiReader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Collection<Emoji> read(InputStream input) throws IOException {

        Collection<Emoji> emojis = new HashSet<>();

        // should not close the input
        ZipInputStream zipInput = new ZipInputStream(input, StandardCharsets.UTF_8);

        // delegate all entry reads
        while (zipInput.getNextEntry() != null) {
            emojis.addAll(delegate.read(zipInput));
        }

        return emojis;
    }

}
