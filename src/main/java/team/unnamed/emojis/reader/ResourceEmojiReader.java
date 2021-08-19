package team.unnamed.emojis.reader;

import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.io.Streamable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation currently just for testing
 */
public class ResourceEmojiReader implements EmojiReader{

    private final ClassLoader classLoader;
    private final String[] resources;

    public ResourceEmojiReader(ClassLoader classLoader, String... resources) {
        this.classLoader = classLoader;
        this.resources = resources;
    }

    @Override
    public Map<String, Emoji> read(File folder) throws IOException {
        Map<String, Emoji> result = new HashMap<>();
        int charFinder = 1 << 15;
        for (String resource : resources) {
            result.put(
                    resource,
                    new Emoji(
                            resource,
                            "emoji." + resource,
                            Streamable.ofResource(classLoader, resource + ".png"),
                            14,
                            12,
                            (char) charFinder--
                    )
            );
        }
        return result;
    }

}
