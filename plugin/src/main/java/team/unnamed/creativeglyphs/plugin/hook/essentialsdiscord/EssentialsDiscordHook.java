package team.unnamed.creativeglyphs.plugin.hook.essentialsdiscord;

import net.essentialsx.api.v2.events.discord.DiscordChatMessageEvent;
import net.essentialsx.api.v2.events.discord.DiscordRelayEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.content.ContentFlattener;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;

public class EssentialsDiscordHook implements PluginHook {
    private final Plugin plugin;
    private final PluginGlyphMap glyphMap;

    public EssentialsDiscordHook(Plugin plugin, PluginGlyphMap glyphMap) {
        this.plugin = plugin;
        this.glyphMap = glyphMap;
    }

    @Override
    public String getPluginName() {
        return "EssentialsDiscord";
    }

    @Override
    public void hook(Plugin hook) {
        Bukkit.getPluginManager().registerEvents(
                new EssentialsDiscordChatListener(),
                plugin
        );
        plugin.getLogger().info("Successfully hooked with EssentialsDiscord!");
    }

    private class EssentialsDiscordChatListener implements Listener {
        @EventHandler
        public void onMinecraftToDiscordMessage(DiscordChatMessageEvent event) {
            final String input = event.getMessage();
            // todo: we probably want to use a different glyph renderer for this
            final String output = ContentFlattener.stringToShorterUsage().flatten(input, glyphMap);
            event.setMessage(output);
        }

        @EventHandler
        public void onDiscordToMinecraftMessage(DiscordRelayEvent event) {
            final String raw = event.getRawMessage();
            event.setFormattedMessage(ContentProcessor.string().process(raw, glyphMap));
        }
    }
}
