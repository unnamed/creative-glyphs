package team.unnamed.emojis.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackApplyListener implements Listener {

    private final String downloadUrl;
    //private final byte[] hash;

    public ResourcePackApplyListener(String downloadUrl, byte[] hash) {
        this.downloadUrl = downloadUrl;
        //this.hash = hash;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setResourcePack(downloadUrl);
        // TODO: Use NMS and set the hash too
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
