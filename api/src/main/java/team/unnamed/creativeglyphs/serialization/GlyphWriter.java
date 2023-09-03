package team.unnamed.creativeglyphs.serialization;

import team.unnamed.creativeglyphs.Glyph;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public interface GlyphWriter {

    /**
     * Writes the {@code emojis} to the given {@code output}
     * <strong>Note that this method won't close the
     * given OutputStream</strong>
     * @throws IOException If read failed
     */
    void write(OutputStream output, Collection<Glyph> glyphs) throws IOException;

    static GlyphWriter mcglyph() {
        return MCGlyphCodec.INSTANCE;
    }

}
