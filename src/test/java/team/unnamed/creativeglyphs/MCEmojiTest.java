package team.unnamed.creativeglyphs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MCEmojiTest {

    private InputStream getResource(String name) {
        InputStream resource = MCEmojiTest.class.getClassLoader().getResourceAsStream(name);
        if (resource == null) {
            throw new NullPointerException("Resource not found: " + name);
        } else {
            return resource;
        }
    }

    @Test
    public void test() throws IOException {
        GlyphCodec codec = GlyphCodec.mcemoji();

        // default emoji pack
        {
            Collection<Glyph> glyphs = codec.read(getResource("defaults.mcemoji"));
            Assertions.assertEquals(21, glyphs.size());
        }


    }

}
