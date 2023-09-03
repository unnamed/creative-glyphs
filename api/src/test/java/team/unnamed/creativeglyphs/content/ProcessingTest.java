package team.unnamed.creativeglyphs.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.content.ContentFlattener;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.util.Glyphs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessingTest {

    private static final Map<String, String> EXPECTATIONS;
    private static final GlyphMap REGISTRY = GlyphMap.map();

    static {
        // key replaces to value
        Map<String, String> expectations = new HashMap<>();



        EXPECTATIONS = Collections.unmodifiableMap(expectations);

        // fill registry
        REGISTRY.update(
                Glyphs.HEART
        );
    }

    @Test
    @DisplayName("Test direct processing")
    public void test_direct_processing() {
        for (Map.Entry<String, String> expectation : EXPECTATIONS.entrySet()) {
            assertEquals(
                    expectation.getValue(),
                    ContentProcessor.string().process(expectation.getKey(), REGISTRY)
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
                    ContentFlattener.stringToShorterUsage().flatten(input, REGISTRY)
            );
        }
    }

}
