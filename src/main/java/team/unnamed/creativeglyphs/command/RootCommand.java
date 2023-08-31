package team.unnamed.creativeglyphs.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import team.unnamed.creativeglyphs.EmojisPlugin;

import java.util.Locale;

public class RootCommand implements CommandRunnable {

    private final EmojisPlugin plugin;

    private final CommandRunnable listSubCommand;
    private final CommandRunnable updateSubCommand;

    public RootCommand(EmojisPlugin plugin) {
        this.plugin = plugin;

        this.listSubCommand = new ListSubCommand(plugin);
        this.updateSubCommand = new UpdateSubCommand(plugin);
    }

    @Override
    public void run(CommandSender sender, ArgumentStack args) {

        // if no permission for subcommands or no arguments given,
        // just send the emoji list
        if ((!sender.isOp() && !sender.hasPermission("emojis.admin")) || !args.hasNext()) {
            listSubCommand.run(sender, args);
            return;
        }

        switch (args.next().toLowerCase(Locale.ROOT)) {
            case "update" -> updateSubCommand.run(sender, args);
            case "reload" -> {
                plugin.reloadConfig();
                plugin.registry().load();
            }
            case "list" -> listSubCommand.run(sender, args);
            case "help" ->
                // todo: replace this
                sender.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        plugin.getConfig().getString("messages.help", "Message not found")
                ));
            default -> {
                // try to parse the page number, if it's a number, run
                // the list subcommand
                try {
                    args.back();
                    Integer.parseInt(args.peek());
                    listSubCommand.run(sender, args);
                    return;
                } catch (NumberFormatException ignored) {
                }

                sender.sendMessage(ChatColor.RED + "Unknown subcommand");
            }
        }
    }

}
