package team.unnamed.creativeglyphs.content;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.util.Glyphs;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringContentProcessorTest {

    private static final GlyphMap GLYPH_MAP = GlyphMap.map();

    @BeforeAll
    public static void setGlyphs() {
        GLYPH_MAP.setGlyphs(Set.of(
                Glyphs.FLUSHED,
                Glyphs.SMILEY,
                Glyphs.HEART
        ));
    }

    @Test
    @DisplayName("Test simple glyph replacing, no colors")
    public void test_simple_replacing() {
        exec(
                "I love you <3",
                "I love you ❤"
        );

        exec(
                "I love you so much <3<3<3<3<3<3",
                "I love you so much ❤❤❤❤❤❤"
        );

        exec(
                "I'm happy right now :)",
                "I'm happy right now 😀"
        );

        exec(
                "I'm happy :smiley: and I love you <3<3:smiley:",
                "I'm happy 😀 and I love you ❤❤😀"
        );

        exec(
                "W-What did you just say, F-Fixed-chan? :flushed:",
                "W-What did you just say, F-Fixed-chan? 😳"
        );
    }

    @Test
    @DisplayName("Test glyph replacing with colored strings")
    public void test_colored_usage() {
        exec(
                "§8text with §c§lcolors should :heart: have colors here too",
                "§8text with §c§lcolors should §f❤§c§l have colors here too"
        );

        exec(
                "§cLove :heart:§7, §6Smiley :smiley:§7, §fText only§7, §dFlushed :flushed:",
                "§cLove §f❤§c§7, §6Smiley §f😀§6§7, §fText only§7, §dFlushed §f😳§d"
        );

        exec(
                "§c§lFormat should be kept :smiley:, and §rGone :smiley:",
                "§c§lFormat should be kept §f😀§c§l, and §rGone §f😀§r"
        );
    }

    private void exec(String raw, String expected) {
        String got = ContentProcessor.string().process(raw, GLYPH_MAP);
        assertEquals(expected, got);
    }

}
