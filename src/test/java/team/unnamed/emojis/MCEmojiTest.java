package team.unnamed.emojis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import team.unnamed.emojis.io.EmojiCodec;
import team.unnamed.emojis.io.MCEmojiCodec;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MCEmojiTest {

    private InputStream getResource(String name) {
        InputStream resource = MCEmojiTest.class.getClassLoader().getResourceAsStream(name);
        if (resource == null) {
            throw new NullPointerException("Resource not found: " + name);
        } else {
            return resource;
        }
    }

    @Test
    public void test() throws IOException {
        EmojiCodec codec = new MCEmojiCodec();

        // default emoji pack
        {
            Collection<Emoji> emojis = codec.read(getResource("defaults.mcemoji"));
            Assertions.assertEquals(21, emojis.size());
        }


    }

}
