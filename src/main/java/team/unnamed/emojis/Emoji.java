package team.unnamed.emojis;

import team.unnamed.uracle.io.Writeable;

/**
 * Represents an emoji, has a name, size and
 * its image itself.
 */
public class Emoji {

    private final String name;
    private final String permission;

    private final int dataLength;
    private final Writeable data;

    private final int height;
    private final int ascent;
    private final char character;

    public Emoji(
            String name,
            String permission,
            int dataLength,
            Writeable data,
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

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public int getDataLength() {
        return dataLength;
    }

    public Writeable getData() {
        return data;
    }

    public int getHeight() {
        return height;
    }

    public int getAscent() {
        return ascent;
    }

    public char getCharacter() {
        return character;
    }

}
