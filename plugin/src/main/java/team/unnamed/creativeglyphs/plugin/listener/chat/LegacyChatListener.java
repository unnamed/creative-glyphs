package team.unnamed.creativeglyphs.plugin.listener.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.plugin.ComponentGlyphRenderer;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.plugin.util.Permissions;
import team.unnamed.creativeglyphs.plugin.listener.bus.EventListener;

/**
 * Most simple and flat chat listener, it uses the
 * default {@link AsyncPlayerChatEvent} and doesn't
 * interact with rich components, so it doesn't have
 * to cancel the event. Should be compatible with
 * all chat plugins.
 */
@SuppressWarnings("deprecation") // AsyncPlayerChatEvent is deprecated in Paper
public class LegacyChatListener
        implements EventListener<AsyncPlayerChatEvent> {
    private final PluginGlyphMap glyphMap;
    private final ContentProcessor<Component> contentProcessor;

    public LegacyChatListener(Plugin plugin, PluginGlyphMap glyphMap) {
        this.glyphMap = glyphMap;
        this.contentProcessor = ContentProcessor.component(new ComponentGlyphRenderer(plugin));
    }

    @Override
    public Class<AsyncPlayerChatEvent> getEventType() {
        return AsyncPlayerChatEvent.class;
    }

    @Override
    public void execute(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final Component message = LegacyComponentSerializer.legacySection().deserialize(event.getMessage());
        final Component processedMessage = contentProcessor.process(
                message,
                glyphMap,
                Permissions.permissionTest(player)
        );
        event.setMessage(LegacyComponentSerializer.legacySection().serialize(processedMessage));
    }
}
