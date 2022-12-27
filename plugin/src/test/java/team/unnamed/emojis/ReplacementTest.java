package team.unnamed.emojis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import team.unnamed.emojis.format.processor.MessageProcessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReplacementTest {

    private static final Map<String, String> EXPECTATIONS;

    static {
        // key replaces to value
        Map<String, String> expectations = new HashMap<>();

        expectations.put(
                "Executing this :test: :    oh   :testing this: hmm :test: :test::test:::::test:",
                "Executing this \u03bc :    oh   :testing this: hmm \u03bc \u03bc\u03bc:::\u03bc"
        );

        expectations.put(
                "a :test::::::::test: : :test: : : :: :: :as : ::aaa:a.a::.a::test:aa:aaa:test:",
                "a \u03bc::::::\u03bc : \u03bc : : :: :: :as : ::aaa:a.a::.a:\u03bcaa:aaa\u03bc"
        );

        expectations.put(
                "many normal text here :test: i'll be happy if this works :test:",
                "many normal text here \u03bc i'll be happy if this works \u03bc"
        );

        expectations.put(
                "§8text with §c§lcolors should :test: have colors here too",
                "§8text with §c§lcolors should §f\u03bc§c§l have colors here too"
        );

        EXPECTATIONS = Collections.unmodifiableMap(expectations);
    }

    @Test
    public void test() {

        EmojiRegistry registry = new EmojiRegistry();
        registry.add(
                Emoji.builder()
                        .name("test")
                        .permission("")
                        .data(new byte[0])
                        .height(8)
                        .ascent(7)
                        .character('\u03bc')
                        .build()
        );

        for (Map.Entry<String, String> expectation : EXPECTATIONS.entrySet()) {
            Assertions.assertEquals(
                    expectation.getValue(),
                    MessageProcessor.string().process(expectation.getKey(), registry)
            );
        }
    }

}
