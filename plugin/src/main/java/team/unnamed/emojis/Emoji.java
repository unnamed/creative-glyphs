package team.unnamed.emojis;

import team.unnamed.creative.base.Writable;

/**
 * Represents an emoji, has a name, size and
 * its image itself.
 */
public class Emoji {

    private final String name;
    private final String permission;

    private final int dataLength;
    private final Writable data;

    private final int height;
    private final int ascent;
    private final char character;

    public Emoji(
            String name,
            String permission,
            int dataLength,
            Writable data,
            int height,
            int ascent,
            char character
    ) {
        this.name = name;
        this.permission = permission;
        this.dataLength = dataLength;
        this.data = data;
        this.height = height;
        this.ascent = ascent;
        this.character = character;
    }

    public String name() {
        return name;
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

    public char character() {
        return character;
    }

    public String replacement() {
        return Character.toString(character);
    }

    @Override
    public String toString() {
        return "Emoji{" +
                "name='" + name + '\'' +
                ", permission='" + permission + '\'' +
                ", dataLength=" + dataLength +
                ", data=" + data +
                ", height=" + height +
                ", ascent=" + ascent +
                ", character=" + character +
                '}';
    }

}
