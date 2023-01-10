package team.unnamed.emojis.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import team.unnamed.emojis.EmojisPlugin;

import java.util.Locale;
import java.util.Stack;

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
    public void run(CommandSender sender, Stack<String> args) {

        // if no permission for subcommands or no arguments given,
        // just send the emoji list
        if ((!sender.isOp() && !sender.hasPermission("emojis.admin")) || args.isEmpty()) {
            listSubCommand.run(sender, args);
            return;
        }

        switch (args.pop().toLowerCase(Locale.ROOT)) {
            case "update" -> updateSubCommand.run(sender, args);
            case "reload" -> plugin.registry().load();
            case "help" ->
                // todo: replace this
                sender.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        plugin.getConfig().getString("messages.help", "Message not found")
                ));
            default ->
                sender.sendMessage(ChatColor.RED + "Unknown subcommand");
        }
    }

}
