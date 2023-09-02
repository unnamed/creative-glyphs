package team.unnamed.creativeglyphs.util;

public final class Classes {

    private Classes() {
    }

    public static boolean isInClasspath(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
