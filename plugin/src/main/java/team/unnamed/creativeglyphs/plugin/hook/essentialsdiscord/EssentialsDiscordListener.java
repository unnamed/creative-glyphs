package team.unnamed.creativeglyphs.plugin.hook.essentialsdiscord;

import net.essentialsx.api.v2.events.discord.DiscordChatMessageEvent;
import net.essentialsx.api.v2.events.discord.DiscordRelayEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import team.unnamed.creativeglyphs.content.ContentFlattener;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.map.GlyphMap;

final class EssentialsDiscordListener implements Listener {
    private final GlyphMap glyphMap;

    EssentialsDiscordListener(final GlyphMap glyphMap) {
        this.glyphMap = glyphMap;
    }

    @EventHandler
    public void onMinecraftToDiscordMessage(DiscordChatMessageEvent event) {
        final String input = event.getMessage();
        final String output = ContentFlattener.stringToShorterUsage().flatten(input, glyphMap);
        event.setMessage(output);
    }

    @EventHandler
    public void onDiscordToMinecraftMessage(DiscordRelayEvent event) {
        final String raw = event.getRawMessage();
        event.setFormattedMessage(ContentProcessor.string().process(raw, glyphMap));
    }
}
