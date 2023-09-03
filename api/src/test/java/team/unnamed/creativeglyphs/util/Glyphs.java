package team.unnamed.creativeglyphs.util;

import team.unnamed.creativeglyphs.Glyph;

public class Glyphs {

    public static final Glyph SMILEY = Glyph.builder()
            .name("smiley")
            .permission("")
            .height(9)
            .ascent(8)
            .data(new byte[0])
            .addUsage(":smiley:")
            .addUsage(":)")
            .character("😀".codePointAt(0))
            .build();

    public static final Glyph FLUSHED = Glyph.builder()
            .name("flushed")
            .permission("glyph.fluched")
            .height(9)
            .ascent(8)
            .data(new byte[] { 0x1, 0x2, 0x3 })
            .addUsage(":flushed:")
            .character("😳".codePointAt(0))
            .build();

    public static final Glyph HEART = Glyph.builder()
            .name("test")
            .permission("")
            .data(new byte[] { 0x5, 0x3, 0xB })
            .height(8)
            .ascent(7)
            .addUsage(":heart:")
            .addUsage("<3")
            .character('❤')
            .build();

    private Glyphs() {
    }

}
