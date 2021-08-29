package team.unnamed.emojis;

import org.bukkit.permissions.Permissible;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import team.unnamed.emojis.util.EmojiReplacer;

public class ReplacementTest {

    @Test
    public void test() {

        Permissible permissible = new MockPermissible();

        EmojiRegistry registry = new EmojiRegistry();
        registry.add(new Emoji("test", "", null, 0, 0, '\u03bc'));

        Assertions.assertEquals(
                "Executing this \u03bc :    oh   :testing this: hmm \u03bc \u03bc\u03bc:::\u03bc",
                EmojiReplacer.replace(permissible, registry, "Executing this :test: :    oh   :testing this: hmm :test: :test::test:::::test:")
        );

        Assertions.assertEquals(
                "a \u03bc::::::\u03bc : \u03bc : : :: :: :as : ::aaa:a.a::.a:\u03bcaa:aaa\u03bc",
                EmojiReplacer.replace(permissible, registry, "a :test::::::::test: : :test: : : :: :: :as : ::aaa:a.a::.a::test:aa:aaa:test:")
        );

        Assertions.assertEquals(
                "many normal text here \u03bc i'll be happy if this works \u03bc",
                EmojiReplacer.replace(permissible, registry, "many normal text here :test: i'll be happy if this works :test:")
        );
    }

}
