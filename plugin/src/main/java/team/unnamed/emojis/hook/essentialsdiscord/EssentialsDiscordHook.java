package team.unnamed.emojis.hook.essentialsdiscord;

import net.essentialsx.api.v2.events.discord.DiscordChatMessageEvent;
import net.essentialsx.api.v2.events.discord.DiscordRelayEvent;

import java.util.regex.Matcher;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiFormat;
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

    private class StringReplacement {

        public final Integer start;
        public final Integer end;
        public final String replacement;

        public StringReplacement(Integer start, Integer end, String replacement) {
            this.start = start;
            this.end = end;
            this.replacement = replacement;
        }

    }

    private class EssentialsDiscordListener implements Listener {

        @EventHandler
        public void onMessagePostProcess(DiscordRelayEvent event) {

            // Build up a list of string replacements which need to be made
            String input = event.getFormattedMessage();
            Stack<StringReplacement> replacements = new Stack<>();
            Matcher matcher = EmojiFormat.EMOJI_USAGE_PATTERN.matcher(input);
            while (matcher.find()) {
                String emojiName = matcher.group(1);
                Emoji emoji = registry.getIgnoreCase(emojiName);
                if (emoji != null) {
                    replacements.add(new StringReplacement(matcher.start(), matcher.end(), emoji.replacement()));
                }
            }

            // Apply the replacement in reverse order (right-to-left)
            // (This prevents replacements from overwriting each other.)
            StringBuilder output = new StringBuilder(input);
            while (!replacements.empty()) {
                StringReplacement replacement = replacements.pop();
                output.replace(replacement.start, replacement.end, replacement.replacement);
            }
            event.setFormattedMessage(output.toString());
        }

        @EventHandler
        public void onMessagePreProcess(DiscordChatMessageEvent event) {

            String input = event.getMessage();
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                Emoji emoji = registry.getByChar(c);

                if (emoji == null) {
                    output.append(c);
                } else {
                    output.append(':').append(emoji.name()).append(':');
                }
            }

            event.setMessage(output.toString());
    }

}

}
