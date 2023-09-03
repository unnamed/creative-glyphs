package team.unnamed.creativeglyphs.content;

import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.map.GlyphMap;

final class StringContentFlattener implements ContentFlattener<String> {

    static final ContentFlattener<String> FLATTEN_TO_SHORTER_USAGE = new StringContentFlattener(GlyphRenderer.shorterUsage());

    private final GlyphRenderer<String> renderer;

    StringContentFlattener(GlyphRenderer<String> renderer) {
        this.renderer = renderer;
    }

    @Override
    public String flatten(String content, GlyphMap map) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < content.length(); i++) {
            int codePoint = content.codePointAt(i);

            if (!Character.isBmpCodePoint(codePoint)) {
                // two characters were used to represent this
                // code point so skip this thing
                i++;
            }

            Glyph glyph = map.getByCodePoint(codePoint);

            if (glyph == null) {
                // code point did not represent an emoji, just append it
                builder.appendCodePoint(codePoint);
            } else {
                // code point represents an emoji, we must change it to its usage
                String usage = renderer.render(glyph);
                if (usage != null) {
                    builder.append(usage);
                }
            }
        }

        return builder.toString();
    }

}
