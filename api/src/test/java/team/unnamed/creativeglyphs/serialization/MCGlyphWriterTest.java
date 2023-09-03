package team.unnamed.creativeglyphs.serialization;

import org.junit.jupiter.api.Test;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.util.Glyphs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class MCGlyphWriterTest {

    @Test
    public void test_write() throws IOException {
        writeAndCompareWithRead(Set.of(
                Glyphs.SMILEY,
                Glyphs.FLUSHED,
                Glyphs.HEART
        ));
    }

    public void writeAndCompareWithRead(Collection<Glyph> glyphs) throws IOException {
        // serialize to bytes
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GlyphWriter.mcglyph().write(output, glyphs);

        // deserialize from bytes
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Collection<Glyph> deserialized = GlyphReader.mcglyph().read(input);

        assertIterableEquals(glyphs, deserialized, "Re-deserialized glyphs didn't equal to the original glyphs");
    }

}
