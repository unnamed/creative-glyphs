package team.unnamed.creativeglyphs.plugin.listener.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.plugin.ComponentGlyphRenderer;
import team.unnamed.creativeglyphs.plugin.listener.bus.EventListener;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.plugin.util.Permissions;

/**
 * Implementation for listening to Paper's AsyncChatEvent,
 * won't cancel anything and should not be incompatible with
 * other plugins that use this event.
 */
public class PaperRichChatListener
        implements EventListener<AsyncChatEvent> {

    private final PluginGlyphMap glyphMap;
    private final ContentProcessor<Component> contentProcessor;

    public PaperRichChatListener(Plugin plugin, PluginGlyphMap glyphMap) {
        this.glyphMap = glyphMap;
        this.contentProcessor = ContentProcessor.component(new ComponentGlyphRenderer(plugin));
    }

    @Override
    public Class<AsyncChatEvent> getEventType() {
        return AsyncChatEvent.class;
    }

    @Override
    public void execute(AsyncChatEvent event) {
        Player player = event.getPlayer();
        event.message(contentProcessor.process(
                event.message(),
                glyphMap,
                Permissions.permissionTest(player)
        ));
    }

}
