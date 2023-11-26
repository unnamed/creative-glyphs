package team.unnamed.creativeglyphs.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
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
    private static final String API_URL = "https://artemis.unnamed.team/tempfiles/get/%id%";

    private static final String USER_AGENT = "creative-glyphs-importer";

    private final JsonParser jsonParser = new JsonParser();

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
    public Collection<Glyph> importHttp(final @NotNull String id)
            throws IOException, IllegalStateException {
        final URL url = new URL(API_URL.replace("%id%", id));

        HttpURLConnection connection
                = (HttpURLConnection) url.openConnection();

        // setup request
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        Collection<Glyph> glyphs;

        // execute and read response
        try (Reader responseReader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        )) {
            // response should be like:
            // {
            //   present: true/false,
            //   file: 'base64 string'
            // }
            JsonObject response = jsonParser.parse(responseReader)
                    .getAsJsonObject();

            // check for presence: false
            if (!response.get("present").getAsBoolean()) {
                throw new IllegalStateException("Emojis not found in" +
                        " the given location");
            }

            // read 'file' base64 data
            byte[] base64File = response.get("file")
                    .getAsString()
                    .getBytes(StandardCharsets.UTF_8);

            try (InputStream input = Base64.getDecoder()
                    .wrap(new ByteArrayInputStream(base64File))) {
                glyphs = GlyphReader.mcglyph().read(input);
            }
        } catch (IOException e) {
            int status;
            try {
                status = connection.getResponseCode();
            } catch (IOException ignored) {
                // if getResponseCode() failed, just throw
                // the original exception
                throw e;
            }

            if (status == 404) {
                // 404: Not found
                // this status is currently not used in our backend,
                // but we should prepare this for the future
                throw new IllegalStateException(
                        "Emojis not found in the given location", e);
            } else if (status == 429) {
                // 429: Too many requests
                throw new IllegalStateException(
                        "Too many requests, you're being rate-" +
                                "limited, try again later", e);
            } else {
                // we're not going to handle this status
                // code so just throw the exception
                throw e;
            }
        }

        return glyphs;
    }

}
