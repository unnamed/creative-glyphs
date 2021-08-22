package team.unnamed.emojis.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.export.RemoteResource;

public class ResourcePackApplyListener implements Listener {

    private final EmojisPlugin plugin;

    public ResourcePackApplyListener(EmojisPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        RemoteResource resource = plugin.getRemoteResource();
        Player player = event.getPlayer();
        player.setResourcePack(resource.getUrl(), resource.getHash());
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        switch (status) {
            case DECLINED: {
                // TODO: De-hardcode the message
                player.kickPlayer("§cPlease accept the resource pack");
                break;
            }
            case FAILED_DOWNLOAD: {
                player.kickPlayer("§cAn error occurred while downloading resource pack, please re-join");
                break;
            }
        }
    }

}
