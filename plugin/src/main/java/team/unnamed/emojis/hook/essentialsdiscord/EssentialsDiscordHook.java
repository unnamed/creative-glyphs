package team.unnamed.emojis.hook.essentialsdiscord;

import net.essentialsx.api.v2.events.discord.DiscordChatMessageEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.hook.PluginHook;


public class EssentialsDiscordHook
        implements PluginHook, Listener {

    private final Plugin plugin;
    private final EmojiRegistry registry;

    public EssentialsDiscordHook(Plugin plugin, EmojiRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String getPluginName() {
        return "EssentialsDiscord";
    }

    @Override
    public void hook(Plugin hook) {
        Bukkit.getPluginManager().registerEvents(
            new EssentialsDiscordListener(),
            plugin);
        plugin.getLogger().info("Successfully hooked with EssentialsDiscord!");
    }

    private class EssentialsDiscordListener implements Listener {

        @EventHandler
        public void onMessagePreProcess(DiscordChatMessageEvent event) {

            String input = event.getMessage();
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                Emoji emoji = registry.getByChar(c);

                if (emoji == null) {
                    // let it be
                    output.append(c);
                } else {
                     // emoji found, replace by its name
                    output.append(':')
                              .append(emoji.name())
                              .append(':');
                }
            }

            event.setMessage(output.toString());
    }

}

}
