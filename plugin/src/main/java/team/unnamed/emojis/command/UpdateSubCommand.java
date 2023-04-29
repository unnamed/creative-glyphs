package team.unnamed.emojis.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import team.unnamed.creative.central.CreativeCentralProvider;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojisPlugin;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

public class UpdateSubCommand implements CommandRunnable {

    private static final String API_URL = "https://artemis.unnamed.team/tempfiles/get/%id%";

    private final EmojisPlugin plugin;

    public UpdateSubCommand(EmojisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(CommandSender sender, ArgumentStack args) {
        if (args.available() != 1) {
            sender.sendMessage(ChatColor.RED + "Bad usage, use: /emojis update <id>");
            return;
        }

        String downloadId = args.next();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> execute(sender, downloadId));
    }

    private void execute(CommandSender sender, String id) {
        try {
            URL url = new URL(API_URL.replace("%id%", id));
            Collection<Emoji> emojis = plugin.importer().importHttp(url);

            // synchronous update and save
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.registry().update(emojis);
                plugin.registry().save();

                // asynchronous export
                CreativeCentralProvider.get().generate();
            });
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Something went wrong, please" +
                    " contact an administrator to read the console.");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // stack trace in this case isn't so relevant
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

}
