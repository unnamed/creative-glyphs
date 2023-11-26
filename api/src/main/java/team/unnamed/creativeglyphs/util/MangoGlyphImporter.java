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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.function.UnaryOperator;

/**
 * Utility class for downloading/importing glyphs from
 * the <a href="https://mango.unnamed.team/">Mango service</a>.
 */
public final class MangoGlyphImporter {
    private static final UnaryOperator<String> URL_FORMAT = id -> "https://mango.unnamed.team/get/" + URLEncoder.encode(id, StandardCharsets.UTF_8);

    @SuppressWarnings("deprecation")
    private static final JsonParser JSON_PARSER = new JsonParser();

    public @NotNull Collection<Glyph> importGlyphs(final @NotNull String id) throws IOException {
        final URL url = new URL(URL_FORMAT.apply(id));
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // setup request
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "creative-glyphs-importer");

        final Collection<Glyph> glyphs;

        // execute and read response
        try (final Reader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            // response should be like:
            // {
            //   status: ok/error,
            //   error: 'optional error message',
            //   present: true/false,
            //   file: 'base64 string'
            // }
            @SuppressWarnings("deprecation")
            final JsonObject response = JSON_PARSER.parse(responseReader).getAsJsonObject();

            final String status = response.get("status").getAsString();

            // got an error from backend
            if (status.equalsIgnoreCase("error")) {
                final String error = response.get("error").getAsString();
                throw new IllegalStateException(error);
            }

            // check for presence: false
            if (!response.get("present").getAsBoolean()) {
                throw new IllegalStateException("Emojis not found in the given location");
            }

            // read 'file' base64 data
            byte[] base64File = response.get("file")
                    .getAsString()
                    .getBytes(StandardCharsets.UTF_8);

            try (InputStream input = Base64.getDecoder()
                    .wrap(new ByteArrayInputStream(base64File))) {
                glyphs = GlyphReader.mcglyph().read(input);
            }
        } catch (final IOException e) {
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
                throw new IllegalStateException("Emojis not found in the given location", e);
            } else if (status == 429) {
                // 429: Too many requests
                throw new IllegalStateException("Too many requests, you're being rate-limited, try again later", e);
            } else {
                // we're not going to handle this status
                // code so just throw the exception
                throw e;
            }
        }

        return glyphs;
    }
}
