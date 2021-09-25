package team.unnamed.emojis.command;

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
import team.unnamed.emojis.download.EmojiImporter;
import team.unnamed.emojis.export.ExportService;
import team.unnamed.emojis.export.RemoteResource;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmojisCommand implements CommandExecutor {

    private static final int EMOJIS_PER_LINE = 10;

    private static final String API_URL = "https://artemis.unnamed.team/tempfiles/get/%id%";

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final EmojiImporter importer;
    private final EmojiRegistry emojiRegistry;
    private final ExportService exportService;
    private final EmojisPlugin plugin;

    public EmojisCommand(EmojisPlugin plugin) {
        this.importer = plugin.getImporter();
        this.emojiRegistry = plugin.getRegistry();
        this.exportService = plugin.getExportService();
        this.plugin = plugin;
    }

    private void execute(CommandSender sender, String id) {
        try {
            URL url = new URL(API_URL.replace("%id%", id));
            Collection<Emoji> emojis = importer.importHttp(url);

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
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Something went wrong, please" +
                    " contact an administrator to read the console.");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // stack trace in this case isn't so relevant
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        // if no permission for subcommands or no arguments given,
        // just send the emoji list
        if (!sender.isOp() || !sender.hasPermission("emojis.admin") || args.length == 0) {
            Iterator<Emoji> iterator = emojiRegistry.values().iterator();
            while (iterator.hasNext()) {
                StringBuilder lineBuilder = new StringBuilder();
                for (int i = 0; i < EMOJIS_PER_LINE && iterator.hasNext(); i++) {
                    Emoji emoji = iterator.next();
                    lineBuilder
                            .append(emoji.getCharacter())
                            .append(' ');
                }

                sender.sendMessage(lineBuilder.toString());
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "update": {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Bad usage, use: /emojis update <id>");
                    break;
                }

                String downloadId = args[1];
                executor.submit(() -> execute(sender, downloadId));
                break;
            }

            case "reload": {
                plugin.loadEmojis();
                break;
            }

            case "help": {
                sender.sendMessage(
                        ChatColor.LIGHT_PURPLE + "/emojis update <id> " + ChatColor.DARK_GRAY
                                + "-" + ChatColor.GRAY + " Import emojis from https://unnamed.team/emojis\n" +
                        ChatColor.LIGHT_PURPLE + "/emojis reload " + ChatColor.DARK_GRAY
                                + "-" + ChatColor.GRAY + " Reload emojis from the emojis.mcemoji file"
                );
                break;
            }
        }

        return true;
    }

}
