package team.unnamed.creativeglyphs.cloud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.base.Readable;
import team.unnamed.creative.base.Writable;
import team.unnamed.creativeglyphs.util.HttpUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.util.Objects.requireNonNull;
import static team.unnamed.creativeglyphs.util.HttpUtil.LINE_FEED;

/**
 * Shared logic for File cloud service for Artemis
 * and Mango.
 */
final class BaseFileCloudService implements FileCloudService {
    static final FileCloudService ARTEMIS = new BaseFileCloudService("https://artemis.unnamed.team/tempfiles");
    static final FileCloudService MANGO = new BaseFileCloudService("https://mango.unnamed.team");


    @SuppressWarnings("deprecation") // We have to use the constructor for compatibility
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final String baseUrl;

    BaseFileCloudService(final @NotNull String baseUrl) {
        this.baseUrl = requireNonNull(baseUrl, "baseUrl");
    }

    @Override
    public @NotNull String upload(final @NotNull Writable writable) {
        try {
            return upload0(writable);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    private @NotNull String upload0(final @NotNull Writable writable) throws IOException {
        requireNonNull(writable, "writable");

        final var url = new URL(baseUrl + "/upload");
        final var connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);

        final var multipartBoundary = HttpUtil.generateBoundary();

        connection.setRequestProperty("User-Agent", "creative-glyphs-importer");
        connection.setRequestProperty("Charset", "utf-8");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + multipartBoundary);

        try (final var output = connection.getOutputStream()) {
            // write "file" field
            output.write((
                    "--" + multipartBoundary + LINE_FEED
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"file\"" + LINE_FEED
                    + "Content-Type: application/octet-stream" + LINE_FEED + LINE_FEED
            ).getBytes(StandardCharsets.UTF_8));

            // write file data
            writable.write(output);

            // end
            output.write((LINE_FEED + "--" + multipartBoundary + "--" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
        }

        final JsonObject responseJson;
        try (final var reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            //noinspection deprecation
            responseJson = JSON_PARSER.parse(reader).getAsJsonObject();
        } catch (final IOException e) {
            int status;
            try {
                status = connection.getResponseCode();
            } catch (IOException ignored) {
                // if getResponseCode() failed, just throw
                // the original exception
                throw new IllegalStateException("Failed to upload file, failed to get response code", e);
            }

            final String errorInfo;
            try {
                errorInfo = ((Readable) (connection::getErrorStream)).readAsUTF8String();
            } catch (final IOException ignored) {
                throw new IllegalStateException("Failed to upload file, failed to read error stream", e);
            }

            throw new IllegalStateException("Failed to upload file, status: " + status + ", error: " + errorInfo, e);
        }

        final var success = responseJson.get("ok").getAsBoolean();

        if (!success) {
            final var code = responseJson.get("code").getAsInt();
            final var error = responseJson.get("error").getAsString();
            throw new IllegalStateException("Failed to upload file, code: " + code + ", error: " + error);
        }

        return responseJson.get("id").getAsString();
    }

    @Override
    public @Nullable Readable download(final @NotNull String id) {
        try {
            return download0(id);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to download file with id: '" + id + "'", e);
        }
    }

    private @Nullable Readable download0(final @NotNull String id) throws IOException {
        requireNonNull(id, "id");

        final var url = new URL(baseUrl + "/get/" + id);
        final var connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("User-Agent", "creative-glyphs-importer");

        // execute and read response
        try (final var responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            // response should be like:
            // {
            //   present: true/false,
            //   file: 'base64 string'
            // }
            @SuppressWarnings("deprecation") // We have to use the instance method for compatibility
            final var response = JSON_PARSER.parse(responseReader).getAsJsonObject();

            // check for presence: false
            if (!response.get("present").getAsBoolean()) {
                return null;
            }

            // read 'file' base64 data
            final var base64File = response.get("file").getAsString().getBytes(StandardCharsets.UTF_8);

            // return a readable instance that wraps the base64 data
            return () -> Base64.getDecoder().wrap(new ByteArrayInputStream(base64File));
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
                return null;
            } else if (status == 429) {
                // 429: Too many requests
                throw new IllegalStateException("Too many requests, you're being rate-limited, try again later", e);
            } else {
                // we're not going to handle this status
                // code so just throw the exception
                throw e;
            }
        }
    }
}
