package team.unnamed.emojis.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.export.ExportService;
import team.unnamed.emojis.export.RemoteResource;
import team.unnamed.emojis.io.reader.EmojiReader;

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

public class EmojisCommand implements CommandExecutor {

    private static final String API_URL = "https://artemis.unnamed.team/tempfiles/get/%id%";
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final EmojiReader emojiReader;
    private final EmojiRegistry emojiRegistry;
    private final ExportService exportService;
    private final EmojisPlugin plugin;

    public EmojisCommand(EmojisPlugin plugin) {
        this.emojiReader = plugin.getReader();
        this.emojiRegistry = plugin.getRegistry();
        this.exportService = plugin.getExportService();
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!sender.isOp() || !sender.hasPermission("emojis.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission to do this.");
            return true;
        }

        // TODO: Add more commands
        if (args.length != 2 || !args[0].equalsIgnoreCase("update")) {
            sender.sendMessage(ChatColor.RED + "Bad usage, use: /emojis update <id>");
            return true;
        }

        String id = args[1];
        try {
            URL url = new URL(API_URL.replace("%id%", id));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "UnnamedEmojis");

            // execute and read the response
            try (Reader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            )) {
                JsonObject response = JSON_PARSER.parse(reader).getAsJsonObject();
                byte[] base64 = response.get("file").getAsString().getBytes(StandardCharsets.UTF_8);

                Collection<Emoji> emojis;
                try (InputStream input = Base64.getDecoder().wrap(new ByteArrayInputStream(base64))) {
                    emojis = emojiReader.read(input);
                }

                emojiRegistry.update(emojis);
                plugin.saveEmojis();
                RemoteResource resource = exportService.export(emojiRegistry);

                // if there is a remote resource location, update players
                if (resource != null) {
                    // for current players
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setResourcePack(resource.getUrl(), resource.getHash());
                    }

                    // for future player joins
                    plugin.setRemoteResource(resource);
                }
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Something went wrong, please contact an administrator to read the console.");
            e.printStackTrace();
        }
        return true;
    }

}
