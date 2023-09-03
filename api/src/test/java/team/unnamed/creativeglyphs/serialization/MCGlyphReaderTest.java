package team.unnamed.creativeglyphs.serialization;

import org.junit.jupiter.api.Test;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.util.Resources;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MCGlyphReaderTest {

    @Test
    public void test_deserialize_defaults() throws IOException {
        Collection<Glyph> glyphs = GlyphReader.mcglyph().read(Resources.get("defaults.mcglyph"));
        assertEquals(21, glyphs.size(), "Expected 21 glyphs in the default glyph pack");

        Set<String> glyphNames = new HashSet<>(Set.of(
                "blush",
                "grin",
                "grinning",
                "heart_eyes",
                "innocent",
                "joy",
                "kissing",
                "kissing_heart",
                "laughing",
                "pensive",
                "relaxed",
                "relieved",
                "rofl",
                "slight_smile",
                "smile",
                "smiley",
                "sweat_smile",
                "upside_down",
                "wink",
                "smiling_hearts",
                "smiling_tear"
        ));
        for (Glyph glyph : glyphs) {
            assertTrue(
                    glyphNames.remove(glyph.name()),
                    "Unexpected or repeated glyph: '" + glyph.name() + "'"
            );
        }

        assertTrue(glyphNames.isEmpty(), "Missing glyphs in glyph pack! " + glyphNames);
    }

    @Test
    public void test_deserialize_all_discord_emojis_except_for_2() throws IOException {
        Collection<Glyph> glyphs = GlyphReader.mcglyph().read(Resources.get("all_discord_emojis_except_for_2.mcglyph"));

        assertEquals(1563, glyphs.size(), "Expected 1563 glyphs!");
    }

}
