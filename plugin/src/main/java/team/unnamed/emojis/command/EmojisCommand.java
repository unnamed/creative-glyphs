package team.unnamed.emojis.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.editor.EmojiImporter;
import team.unnamed.emojis.resourcepack.export.ExportService;
import team.unnamed.emojis.format.Permissions;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

public class EmojisCommand implements CommandExecutor {

    private static final int EMOJIS_PER_LINE = 10;

    private static final String API_URL = "https://artemis.unnamed.team/tempfiles/get/%id%";

    private final EmojiImporter importer;
    private final EmojiRegistry emojiRegistry;
    private final ExportService exportService;
    private final ConfigurationSection config;
    private final EmojisPlugin plugin;

    public EmojisCommand(EmojisPlugin plugin) {
        this.importer = plugin.importer();
        this.emojiRegistry = plugin.registry();
        this.exportService = plugin.exportService();
        this.config = plugin.getConfig();
        this.plugin = plugin;
    }

    private void execute(CommandSender sender, String id) {
        try {
            URL url = new URL(API_URL.replace("%id%", id));
            Collection<Emoji> emojis = importer.importHttp(url);

            // synchronous update and save
            Bukkit.getScheduler().runTask(plugin, () -> {
                emojiRegistry.update(emojis);
                plugin.saveEmojis();

                // asynchronous export
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    UrlAndHash resource = exportService.export(emojiRegistry);

                    // update
                    if (resource != null) {
                        plugin.updateResourcePackLocation(resource);
                    }
                });
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

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to do this");
            return true;
        }

        Player player = (Player) sender;

        // if no permission for subcommands or no arguments given,
        // just send the emoji list
        if ((!sender.isOp() && !sender.hasPermission("emojis.admin")) || args.length == 0) {
            Iterator<Emoji> iterator = emojiRegistry.values().iterator();
            while (iterator.hasNext()) {
                TextComponent line = new TextComponent("");
                for (int i = 0; i < EMOJIS_PER_LINE && iterator.hasNext(); i++) {
                    Emoji emoji = iterator.next();

                    if (!Permissions.canUse(player, emoji)) {
                        i--;
                        continue;
                    }

                    String hover = ChatColor.translateAlternateColorCodes(
                            '&',
                            config.getString("messages.list.hover", "Not found")
                    )
                            .replace("<emojiname>", emoji.name())
                            .replace("<emoji>", emoji.replacement());
                    TextComponent component = new TextComponent(emoji.replacement());
                    component.setHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            TextComponent.fromLegacyText(hover)
                    ));
                    component.setClickEvent(new ClickEvent(
                            ClickEvent.Action.SUGGEST_COMMAND,
                            emoji.replacement()
                    ));
                    line.addExtra(component);
                }
                player.spigot().sendMessage(line);
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
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> execute(sender, downloadId));
                break;
            }

            case "reload": {
                plugin.loadEmojis();
                break;
            }

            case "help": {
                // todo: replace this
                sender.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        config.getString("messages.help", "Message not found")
                ));
                break;
            }

            default: {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand");
                break;
            }
        }

        return true;
    }

}
