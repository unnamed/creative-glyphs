package team.unnamed.creativeglyphs.plugin.hook.ezchat;

import me.fixeddev.ezchat.event.AsyncEzChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.util.Permissions;
import team.unnamed.creativeglyphs.content.ContentProcessor;

public class EzChatListener implements Listener {

    private final PluginGlyphMap registry;
    private final ContentProcessor<Component> contentProcessor;

    public EzChatListener(Plugin plugin, PluginGlyphMap registry) {
        this.registry = registry;
        this.contentProcessor = ContentProcessor.component(plugin);
    }

    @EventHandler
    public void onChat(AsyncEzChatEvent event) {
        event.setMessage(
                contentProcessor.process(
                        event.getMessage(),
                        registry,
                        Permissions.permissionTest(event.getPlayer())
                )
        );
    }

}
