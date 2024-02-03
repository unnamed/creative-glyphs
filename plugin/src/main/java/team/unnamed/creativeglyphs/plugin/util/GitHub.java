package team.unnamed.creativeglyphs.plugin.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class GitHub {
    private static final JsonParser JSON_PARSER = new JsonParser();

    private GitHub() {
    }

    /**
     * Fetches and returns the latest release tag name from the specified repository
     * using the GitHub API.
     *
     * <p>Note that, conventionally, the returned tag version may differ from the plugin
     * version because tags may start with 'v' while the plugin version may not, but still
     * be the same version.</p>
     *
     * @param repositoryOwner The owner of the repository
     * @param repositoryName The name of the repository
     * @return The latest release tag name
     * @throws IOException If an I/O error occurs
     */
    public static @NotNull String fetchLatestReleaseTagName(final @NotNull String repositoryOwner, final @NotNull String repositoryName) throws IOException {
        Plugin plugin;
        try {
            plugin = JavaPlugin.getProvidingPlugin(GitHub.class);
        } catch (final IllegalArgumentException e) {
            // not provided by a plugin
            plugin = null;
        }

        final var url = new URL("https://api.github.com/repos/" + repositoryOwner + "/" + repositoryName + "/releases/latest");
        final var connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github+json");

        if (plugin != null) {
            //noinspection deprecation
            connection.setRequestProperty("User-Agent", plugin.getName() + "/" + plugin.getDescription().getVersion());
        }

        // Execute and read response
        final JsonObject json;
        try (final var responseReader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            json = JSON_PARSER.parse(responseReader).getAsJsonObject();
        }

        return json.get("tag_name").getAsString();
    }
}
