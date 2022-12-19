package team.unnamed.emojis.util;

import org.bukkit.Bukkit;

import java.util.regex.Pattern;

/**
 * Class that represents a version following the
 * semantic versioning. See https://semver.org
 */
public class Version {

    public static final String VERSION_STRING
            = Bukkit.getServer().getClass().getName().split(Pattern.quote("."))[3];

    public static final Version CURRENT = parseString(VERSION_STRING);

    private final byte major;
    private final byte minor;
    private final byte patch;

    public Version(byte major, byte minor, byte patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public byte major() {
        return major;
    }

    public byte minor() {
        return minor;
    }

    public byte patch() {
        return patch;
    }

    @Override
    public String toString() {
        return "v" + major + '_' + minor + "_R" + patch;
    }

    /**
     * Resolves a {@link Version} from a given {@code versionString}
     * with the format v(major)_(minor)_R(patch)
     *
     * @throws NumberFormatException If major, minor or patch versions
     *                               are not bytes
     */
    public static Version parseString(String versionString) {
        String[] args = versionString.split(Pattern.quote("_"));
        byte major = Byte.parseByte(args[0].substring(1));
        byte minor = Byte.parseByte(args[1]);
        byte patch = Byte.parseByte(args[2].substring(1));
        return new Version(major, minor, patch);
    }

}