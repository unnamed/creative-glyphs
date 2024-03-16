package team.unnamed.creativeglyphs.plugin.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.serialization.GlyphReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

/**
 * Service for downloading/importing glyphs from
 * the Artemis backend
 *
 * @author yusshu (Andre Roldan)
 */
public class ArtemisGlyphImporter {

    private static final String USER_AGENT = "creative-glyphs-importer";

    /**
     * Imports emojis from the given {@code url}
     * using the HTTP protocol and expecting a JSON
     * object containing a 'present' boolean value
     * and the emoji data in Base64 in the 'file'
     * property
     * @throws IOException If an unexpected error
     * happens at writing/reading time
     * @throws IllegalStateException If an expected
     * error occurs, contains a user-friendly message
     */
    public Collection<Glyph> importHttp(URL url)
            throws IOException, IllegalStateException {



        Collection<Glyph> glyphs;



        return glyphs;
    }

}
