package team.unnamed.creativeglyphs.serialization;

import team.unnamed.creativeglyphs.Glyph;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface GlyphReader {

    /**
     * Reads the emojis from the given {@code input}
     * <strong>Note that this method won't close the
     * given InputStream</strong>
     * @return The read emojis
     * @throws IOException If read failed
     */
    Collection<Glyph> read(InputStream input) throws IOException;

    static GlyphReader mcglyph() {
        return MCGlyphCodec.INSTANCE;
    }

}
