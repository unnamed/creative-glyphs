package team.unnamed.emojis.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.export.RemoteResource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResourcePackApplyListener implements Listener {

    private final Map<UUID, Integer> retries = new HashMap<>();
    private final EmojisPlugin plugin;

    public ResourcePackApplyListener(EmojisPlugin plugin) {
        this.plugin = plugin;
    }

    private String formatMessage(char formatChar,String colorString){
        return colorString.replace(formatChar, '§');
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
            case SUCCESSFULLY_LOADED: {
                retries.remove(player.getUniqueId());
                break;
            }
            case DECLINED: {
                if(plugin.getConfig().getBoolean("feature.require-pack"))
                    //player.kickPlayer(plugin.getConfig().getString("messages.kick"));
                    player.sendMessage(formatMessage('&',plugin.getConfig().getString("messages.kick")));
                else
                    player.sendMessage(formatMessage('&',plugin.getConfig().getString("messages.warn")));
                break;
            }
            case FAILED_DOWNLOAD: {
                Integer count = retries.get(player.getUniqueId());
                if (count == null) {
                    count = 0;
                } else if (count > 3) {
                    //player.kickPlayer("§cAn error occurred while downloading resource pack, please re-join");
                    if(plugin.getConfig().getBoolean("feature.require-pack")){
                        player.kickPlayer(formatMessage('&',plugin.getConfig().getString("messages.fail")));
                    }
                    else{
                        player.sendMessage(formatMessage('&',plugin.getConfig().getString("messages.fail")));
                        player.sendMessage(formatMessage('&',plugin.getConfig().getString("messages.warn")));
                    }
                    retries.remove(player.getUniqueId());
                }

                retries.put(player.getUniqueId(), count + 1);
                break;
            }
        }
    }

}
