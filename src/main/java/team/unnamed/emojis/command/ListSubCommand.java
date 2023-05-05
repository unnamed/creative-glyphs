package team.unnamed.emojis.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.format.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ListSubCommand implements CommandRunnable {

    private final EmojisPlugin plugin;

    public ListSubCommand(EmojisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation") // Spigot
    public void run(CommandSender sender, ArgumentStack args) {

        // load the configuration for listing emojis
        ConfigurationSection listConfig = plugin.getConfig().getConfigurationSection("messages.list");
        if (listConfig == null) {
            throw new IllegalStateException("No configuration for list subcommand");
        }

        int page = -1;
        if (args.hasNext()) {
            try {
                page = Integer.parseInt(args.next()) - 1;
            } catch (NumberFormatException ignored) {
                // lets 'page' be -1, detected later
            }
        } else {
            page = 0;
        }

        // load the separation config
        ConfigurationSection separationConfig = listConfig.getConfigurationSection("separation");
        Map<Integer, String> separators = new TreeMap<>((a, b) -> b - a);

        if (separationConfig != null) {
            for (String key : separationConfig.getKeys(false)) {
                separators.put(
                        Integer.parseInt(key),
                        ChatColor.translateAlternateColorCodes(
                                '&',
                                separationConfig.getString(key, "Not found")
                        )
                );
            }
        }

        List<Emoji> emojis = new ArrayList<>(plugin.registry().values());

        boolean showUnavailable = listConfig.getBoolean("show-unavailable", false);
        if (showUnavailable) {
            // sort the emojis alphabetically, put the unavailable ones at the end
            emojis.sort((a, b) -> {
                boolean canUseA = Permissions.canUse(sender, a);
                boolean canUseB = Permissions.canUse(sender, b);
                if (canUseA && !canUseB) {
                    return -1;
                } else if (!canUseA && canUseB) {
                    return 1;
                } else {
                    return a.name().compareToIgnoreCase(b.name());
                }
            });
        } else {
            // remove emojis that the sender can't use
            emojis.removeIf(emoji -> !Permissions.canUse(sender, emoji));
            // sort the emojis alphabetically, by name, ignoring case
            emojis.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        }

        int len = emojis.size();
        int emojisPerPage = listConfig.getInt("max-emojis-per-page", 30);
        int maxPages = (int) Math.ceil(len / (float) emojisPerPage);

        if (page < 0 || page > maxPages) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', listConfig.getString("invalid-page", "Invalid page")));
            return;
        }

        // get the emojis for the current page
        emojis = emojis.subList(page * emojisPerPage, Math.min(len, (page + 1) * emojisPerPage));

        TextComponent message = new TextComponent("");
        for (int i = 0; i < Math.min(len, emojisPerPage); i++) {
            // add separators if needed
            if (i != 0) {
                for (Map.Entry<Integer, String> entry : separators.entrySet()) {
                    if (i % entry.getKey() == 0) {
                        message.addExtra(entry.getValue());
                    }
                    break;
                }
            }

            Emoji emoji = emojis.get(i);
            boolean available = Permissions.canUse(sender, emoji);
            String basePath = "element." + (available ? "available" : "unavailable");

            TextComponent component = new TextComponent(
                    listConfig.getString(basePath + ".content")
                            .replace("<emoji>", emoji.replacement())
                            .replace("<emojiname>", emoji.name())
            );
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(
                            ChatColor.translateAlternateColorCodes(
                                            '&',
                                            listConfig.getString(basePath + ".hover", "Not found")
                                    )
                                    .replace("<emojiname>", emoji.name())
                                    .replace("<emoji>", emoji.replacement())
                    )
            ));
            if (available) {
                component.setClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        emoji.replacement()
                ));
            }
            message.addExtra(component);
        }

        // send the header message
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', listConfig.getString("header", "Not found"))
                .replace("<page>", String.valueOf(page + 1))
                .replace("<maxpages>", String.valueOf(maxPages))
        );

        // send the content
        sender.spigot().sendMessage(message);

        // send the footer message
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', listConfig.getString("footer", "Not found"))
                .replace("<page>", String.valueOf(page + 1))
                .replace("<maxpages>", String.valueOf(maxPages))
        );
    }

}
