package team.unnamed.emojis.download;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.io.EmojiCodec;

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
 * Service for importing emojis from an
 * external location
 * @author yusshu (Andre Roldan)
 */
public class EmojiImporter {

    private static final String USER_AGENT = "unemojis-importer";

    private final JsonParser jsonParser = new JsonParser();
    private final EmojiCodec reader;

    /**
     * Constructs a new importing service that
     * uses the given {@code reader} to read the
     * imported data
     */
    public EmojiImporter(EmojiCodec reader) {
        this.reader = reader;
    }

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
    public Collection<Emoji> importHttp(URL url)
            throws IOException, IllegalStateException {

        HttpURLConnection connection
                = (HttpURLConnection) url.openConnection();

        // setup request
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        Collection<Emoji> emojis;

        // execute and read response
        try (Reader responseReader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        )) {
            // TODO: we should use the 404 status instead of this
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
                emojis = this.reader.read(input);
            }
        } catch (IOException e) {
            int status;
            try {
                // TODO: we should probably handle more statuses
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

        return emojis;
    }

}
