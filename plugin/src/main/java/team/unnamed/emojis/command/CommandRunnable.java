package team.unnamed.emojis.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Stack;

public interface CommandRunnable {

    void run(CommandSender sender, Stack<String> args);

    default CommandExecutor asExecutor() {
        return (sender, command, label, args) -> {
            Stack<String> argumentStack = new Stack<>();
            Collections.addAll(argumentStack, args);
            run(sender, argumentStack);
            return true;
        };
    }

}
