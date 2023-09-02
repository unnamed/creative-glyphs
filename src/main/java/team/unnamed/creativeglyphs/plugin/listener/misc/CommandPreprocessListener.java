package team.unnamed.creativeglyphs.plugin.listener.misc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.plugin.util.Permissions;

public class CommandPreprocessListener implements Listener {

    private final ContentProcessor<String> processor = ContentProcessor.string();
    private final GlyphMap glyphMap;

    public CommandPreprocessListener(GlyphMap glyphMap) {
        this.glyphMap = glyphMap;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        event.setMessage(processor.process(
                event.getMessage(),
                glyphMap,
                Permissions.permissionTest(player)
        ));
    }

}
