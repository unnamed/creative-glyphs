package team.unnamed.emojis;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.font.BitMapFontProvider;
import team.unnamed.creative.util.Validate;
import team.unnamed.emojis.format.EmojiFormat;

import java.util.HashSet;
import java.util.Set;

import static team.unnamed.emojis.format.EmojiFormat.NAME_PATTERN;
import static team.unnamed.emojis.format.EmojiFormat.NAME_PATTERN_STR;
import static team.unnamed.emojis.format.EmojiFormat.PERMISSION_PATTERN;
import static team.unnamed.emojis.format.EmojiFormat.PERMISSION_PATTERN_STR;

/**
 * Represents an emoji, has a name, size and
 * its image itself.
 */
public class Emoji {

    private final String name;
    private final String permission;
    private final Set<String> usages;

    private final int dataLength;
    private final Writable data;

    private final int height;
    private final int ascent;
    private final int character;
    private final String characterString; // cached string value for 'character'

    private Emoji(
            @Pattern(NAME_PATTERN_STR) String name,
            @Pattern(EmojiFormat.PERMISSION_PATTERN_STR) String permission,
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
        return "Emoji{" +
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

    public static Emoji.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private String permission;
        private Set<String> usages = new HashSet<>();
        private int dataLength;
        private Writable data;
        private int height = BitMapFontProvider.DEFAULT_HEIGHT;
        private int ascent;
        private int character;

        private Builder() {
        }

        public Builder name(@Pattern(NAME_PATTERN_STR) String name) {
            Validate.isNotNull(name, "name");
            Validate.isTrue(NAME_PATTERN.matcher(name).matches(), "Invalid emoji name '" + name
                    + "', it must match the emoji name pattern: " + NAME_PATTERN_STR);
            this.name = name;
            return this;
        }

        public Builder permission(@Pattern(PERMISSION_PATTERN_STR) String permission) {
            Validate.isNotNull(permission, "permission");
            Validate.isTrue(PERMISSION_PATTERN.matcher(permission).matches(), "Invalid emoji permission '" + permission);
            this.permission = permission;
            return this;
        }

        public Builder addNameUsage() {
            Validate.isNotNull(name, "name");
            this.usages.add(EmojiFormat.usageOf(name));
            return this;
        }

        public Builder addUsage(String usage) {
            Validate.isNotNull(usage, "usage");
            this.usages.add(usage);
            return this;
        }

        public Builder usages(Set<String> usages) {
            Validate.isNotNull(usages, "usages");
            this.usages = usages;
            return this;
        }

        public Builder dataLength(int dataLength) {
            this.dataLength = dataLength;
            return this;
        }

        public Builder data(Writable data) {
            this.data = Validate.isNotNull(data, "data");
            return this;
        }

        public Builder data(byte[] imageBytes) {
            Validate.isNotNull(imageBytes, "imageBytes");
            this.dataLength = imageBytes.length;
            this.data = Writable.bytes(imageBytes);
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder ascent(int ascent) {
            this.ascent = ascent;
            return this;
        }

        public Builder character(int character) {
            this.character = character;
            return this;
        }

        public Emoji build() {
            Validate.isNotNull(name, "name");
            Validate.isNotNull(permission, "permission");
            Validate.isNotNull(data, "data");
            return new Emoji(name, permission, usages, dataLength, data, height, ascent, character);
        }

    }

}
