package team.unnamed.emojis.util;

/**
 * Utility class containing some static
 * utility methods for handling {@link String}
 * instances and related
 */
public final class Texts {

    private Texts() {
    }

    /**
     * Adds a backslash before every double quote found
     * in the given {@code text} string
     */
    public static String escapeDoubleQuotes(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                result.append('\\');
            }
            result.append(c);
        }
        return result.toString();
    }

}
