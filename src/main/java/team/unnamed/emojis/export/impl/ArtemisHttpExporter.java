package team.unnamed.emojis.export.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.export.ResourceExporter;
import team.unnamed.emojis.io.AssetWriter;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.io.TreeOutputStream;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

/**
 * Fluent-style class for exporting resource
 * packs and upload it using HTTP servers like
 * our Artemis File-server, that responds with
 * a JSON object containing the file download
 * "url" and its SHA-1 "hash"
 */
public class ArtemisHttpExporter
        implements ResourceExporter {

    private static final JsonParser PARSER = new JsonParser();

    private static final String BOUNDARY = "HephaestusBoundary";
    private static final String LINE_FEED = "\r\n";

    private final URL url;
    private final Map<String, String> headers;
    private String fileName;

    public ArtemisHttpExporter(String url)
            throws MalformedURLException {
        this.url = new URL(url);
        this.headers = new HashMap<>();;
    }

    /**
     * Sets the authorization token for this
     * exporter class
     */
    public ArtemisHttpExporter setAuthorization(@Nullable String authorization) {
        return setProperty("Authorization", authorization);
    }

    /**
     * Sets a request property for the export
     * @param name The property name
     * @param value The property value
     */
    public ArtemisHttpExporter setProperty(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Sets the filename passed to the HTTP server
     * when uploading the data
     */
    public ArtemisHttpExporter setFileName(String fileName) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        return this;
    }

    @Override
    @NotNull
    public UrlAndHash export(AssetWriter writer) throws IOException {

        if (fileName == null) {
            // use 'resourcepack' as default name
            fileName = "resourcepack";
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(10000);

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        connection.setRequestProperty("User-Agent", "Hephaestus-Engine");
        connection.setRequestProperty("Charset", "utf-8");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        headers.forEach(connection::setRequestProperty);

        // write http request body
        try (OutputStream output = connection.getOutputStream()) {
            Streams.writeUTF(
                    output,
                    "--" + BOUNDARY + LINE_FEED
                            + "Content-Disposition: form-data; name=\"" + fileName + "\"; filename=\""
                            + fileName + "\"" + LINE_FEED + "Content-Type: application/octet-stream;" +
                            " charset=utf-8" + LINE_FEED + LINE_FEED
            );

            TreeOutputStream treeOutput = TreeOutputStream.forZip(new ZipOutputStream(output));
            try {
                writer.write(treeOutput);
            } finally {
                treeOutput.finish();
            }

            Streams.writeUTF(
                    output,
                    LINE_FEED + "--" + BOUNDARY + "--" + LINE_FEED
            );
        }

        // execute and read the response
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        )) {
            JsonObject response = PARSER.parse(reader).getAsJsonObject();
            return new UrlAndHash(
                    response.get("url").getAsString(),
                    response.get("hash").getAsString()
            );
        }
    }

}
