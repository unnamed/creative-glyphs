package team.unnamed.emojis.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.format.Permissions;

import java.util.Iterator;
import java.util.Stack;

public class ListSubCommand implements CommandRunnable {

    private static final int EMOJIS_PER_LINE = 10;

    private final EmojisPlugin plugin;

    public ListSubCommand(EmojisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation") // Spigot
    public void run(CommandSender sender, Stack<String> args) {

        EmojiRegistry emojiRegistry = plugin.registry();
        Iterator<Emoji> iterator = emojiRegistry.values().iterator();

        while (iterator.hasNext()) {
            TextComponent line = new TextComponent("");
            for (int i = 0; i < EMOJIS_PER_LINE && iterator.hasNext(); i++) {
                Emoji emoji = iterator.next();

                if (!Permissions.canUse(sender, emoji)) {
                    i--;
                    continue;
                }

                String hover = ChatColor.translateAlternateColorCodes(
                                '&',
                                plugin.getConfig().getString("messages.list.hover", "Not found")
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
            sender.spigot().sendMessage(line);
        }
    }

}
