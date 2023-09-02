package team.unnamed.creativeglyphs.plugin.listener.chat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
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

    private final Plugin plugin;
    private final PluginGlyphMap glyphMap;

    public LegacyChatListener(Plugin plugin, PluginGlyphMap glyphMap) {
        this.plugin = plugin;
        this.glyphMap = glyphMap;
    }

    @Override
    public Class<AsyncPlayerChatEvent> getEventType() {
        return AsyncPlayerChatEvent.class;
    }

    @Override
    public void execute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String messagePrefix = getMessagePrefix();

        event.setMessage(ContentProcessor.string().process(
                messagePrefix + message,
                glyphMap,
                Permissions.permissionTest(player)
        ));
    }

    private String getMessagePrefix() {
        ConfigurationSection config = plugin.getConfig();
        String prefix = config.getString("format.legacy.message-prefix", null);

        if (prefix != null) {
            return ChatColor.translateAlternateColorCodes('&', prefix);
        } else {
            String legacyColor = config.getString("format.legacy.color", null);

            if (legacyColor == null) {
                // no prefix
                return "";
            } else if (legacyColor.length() == 1) {
                // backwards compatibility
                // TODO: Remove, backwards compatibility
                return String.valueOf(ChatColor.getByChar(legacyColor));
            } else {
                // same behavior as new, but new path is recommended
                return ChatColor.translateAlternateColorCodes('&', legacyColor);
            }
        }
    }

}
