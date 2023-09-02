package team.unnamed.creativeglyphs;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import team.unnamed.creative.base.Writable;

import java.util.Set;

/**
 * Represents a glyph, a mapping of an image to a codepoint.
 *
 * @author yusshu (Andre Roldan)
 */
public interface Glyph {

    @Subst("smiley")
    String name();

    Set<String> usages();

    String permission();

    int dataLength();

    Writable data();

    int height();

    int ascent();

    int character();

    String replacement();

    static Glyph.Builder builder() {
        return new GlyphImpl.BuilderImpl();
    }

    interface Builder {

        Builder name(@Pattern(GlyphImpl.NAME_PATTERN) String name);

        Builder permission(String permission);

        Builder addUsage(String usage);

        Builder usages(Set<String> usages);

        Builder dataLength(int dataLength);

        Builder data(Writable data);

        Builder data(byte[] imageBytes);

        Builder height(int height);

        Builder ascent(int ascent);

        Builder character(int character);

        Glyph build();

    }

}
