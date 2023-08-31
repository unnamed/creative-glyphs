package team.unnamed.creativeglyphs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team.unnamed.creativeglyphs.format.processor.MessageProcessor;
import team.unnamed.creativeglyphs.object.store.EmojiStore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessingTest {

    private static final Map<String, String> EXPECTATIONS;
    private static final EmojiStore REGISTRY = EmojiStore.createCachedOnly();

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

        // fill registry
        REGISTRY.update(
                Emoji.builder()
                        .name("test")
                        .permission("")
                        .data(new byte[0])
                        .height(8)
                        .ascent(7)
                        .character('\u03bc')
                        .addNameUsage()
                        .build()
        );
    }

    @Test
    @DisplayName("Test direct processing")
    public void test_direct_processing() {
        for (Map.Entry<String, String> expectation : EXPECTATIONS.entrySet()) {
            assertEquals(
                    expectation.getValue(),
                    MessageProcessor.string().process(expectation.getKey(), REGISTRY)
            );
        }
    }

    @Test
    @DisplayName("Test flattening")
    public void test_flattening() {
        for (Map.Entry<String, String> expectation : EXPECTATIONS.entrySet()) {
            String input = expectation.getKey();
//            String output = expectation.getValue();
//
//            assertEquals(
//                    input,
//                    MessageProcessor.string().flatten(output, REGISTRY)
//            );
            assertEquals(
                    input,
                    MessageProcessor.string().flatten(input, REGISTRY)
            );
        }
    }

}
