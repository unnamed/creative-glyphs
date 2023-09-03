package team.unnamed.creativeglyphs;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.intellij.lang.annotations.Subst;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.font.BitMapFontProvider;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

final class GlyphImpl implements Glyph {

    static final @RegExp String NAME_PATTERN = "[a-z0-9_]+";
    static final java.util.regex.Pattern NAME_COMPILED_PATTERN = java.util.regex.Pattern.compile(NAME_PATTERN);

    private final String name;
    private final String permission;
    private final Set<String> usages;

    private final int dataLength;
    private final Writable data;

    private final int height;
    private final int ascent;
    private final int character;
    private final String characterString; // cached string value for 'character'

    GlyphImpl(
            @Pattern(NAME_PATTERN) String name,
            String permission,
            Set<String> usages,
            int dataLength,
            Writable data,
            int height,
            int ascent,
            int character
    ) {
        this.name = name;
        this.usages = usages;
        this.permission = permission;
        this.dataLength = dataLength;
        this.data = data;
        this.height = height;
        this.ascent = ascent;
        this.character = character;
        this.characterString = new StringBuilder()
                .appendCodePoint(character)
                .toString();
    }

    public @Subst("smile") String name() {
        return name;
    }

    public Set<String> usages() {
        return usages;
    }

    public String permission() {
        return permission;
    }

    public int dataLength() {
        return dataLength;
    }

    public Writable data() {
        return data;
    }

    public int height() {
        return height;
    }

    public int ascent() {
        return ascent;
    }

    public int character() {
        return character;
    }

    public String replacement() {
        return  characterString;
    }

    @Override
    public String toString() {
        return "Glyph {" +
                "name='" + name + '\'' +
                ", permission='" + permission + '\'' +
                ", usages=" + usages +
                ", dataLength=" + dataLength +
                ", data=" + data +
                ", height=" + height +
                ", ascent=" + ascent +
                ", character=" + character +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlyphImpl glyph = (GlyphImpl) o;
        if (dataLength != glyph.dataLength) return false;
        if (height != glyph.height) return false;
        if (ascent != glyph.ascent) return false;
        if (character != glyph.character) return false;
        if (!name.equals(glyph.name)) return false;
        if (!permission.equals(glyph.permission)) return false;
        if (!usages.equals(glyph.usages)) return false;
        //return data.equals(glyph.data);
        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + permission.hashCode();
        result = 31 * result + usages.hashCode();
        result = 31 * result + dataLength;
        //result = 31 * result + data.hashCode();
        result = 31 * result + height;
        result = 31 * result + ascent;
        result = 31 * result + character;
        return result;
    }

    static class BuilderImpl implements Glyph.Builder {

        private @Subst("smiley") String name;
        private String permission;
        private Set<String> usages = new HashSet<>();
        private int dataLength;
        private Writable data;
        private int height = BitMapFontProvider.DEFAULT_HEIGHT;
        private int ascent;
        private int character;

        BuilderImpl() {
        }

        @Override
        public Builder name(@Pattern(NAME_PATTERN) String name) {
            requireNonNull(name, "name");
            if (!NAME_COMPILED_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException("Invalid emoji name '" + name
                        + "', it must match the emoji name pattern: " + NAME_PATTERN);
            }
            this.name = name;
            return this;
        }

        @Override
        public Builder permission(String permission) {
            requireNonNull(permission, "permission");
            this.permission = permission;
            return this;
        }

        @Override
        public Builder addUsage(String usage) {
            requireNonNull(usage, "usage");
            this.usages.add(usage);
            return this;
        }

        @Override
        public Builder usages(Set<String> usages) {
            requireNonNull(usages, "usages");
            this.usages = usages;
            return this;
        }

        @Override
        public Builder dataLength(int dataLength) {
            this.dataLength = dataLength;
            return this;
        }

        @Override
        public Builder data(Writable data) {
            requireNonNull(data, "data");
            this.data = data;
            return this;
        }

        @Override
        public Builder data(byte[] imageBytes) {
            requireNonNull(imageBytes, "imageBytes");
            this.dataLength = imageBytes.length;
            this.data = Writable.bytes(imageBytes);
            return this;
        }

        @Override
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        @Override
        public Builder ascent(int ascent) {
            this.ascent = ascent;
            return this;
        }

        @Override
        public Builder character(int character) {
            this.character = character;
            return this;
        }

        @Override
        public Glyph build() {
            requireNonNull(name, "name");
            requireNonNull(permission, "permission");
            requireNonNull(data, "data");
            return new GlyphImpl(name, permission, usages, dataLength, data, height, ascent, character);
        }

    }

}
