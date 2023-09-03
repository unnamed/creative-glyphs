package team.unnamed.creativeglyphs.util;

import java.io.IOException;
import java.io.InputStream;

public final class Resources {

    private Resources() {
    }

    public static InputStream get(String name) {
        InputStream resource = Resources.class.getClassLoader().getResourceAsStream(name);
        if (resource == null) {
            throw new NullPointerException("Resource not found: " + name);
        } else {
            return resource;
        }
    }

}
