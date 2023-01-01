package team.unnamed.emojis.listener;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import team.unnamed.emojis.EmojisPlugin;
import team.unnamed.emojis.resourcepack.ResourcePackApplier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResourcePackApplyListener implements Listener {

    private final Map<UUID, Integer> retries = new HashMap<>();
    private final EmojisPlugin plugin;
    private final FileConfiguration config;

    public ResourcePackApplyListener(EmojisPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ResourcePackApplier.setResourcePack(player, plugin.getResourcePack());
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
                handleFailedPack(player);
                break;
            }
            case FAILED_DOWNLOAD: {
                Integer count = retries.get(player.getUniqueId());
                if (count == null) {
                    count = 0;
                } else if (count > 3) {
                    handleFailedPack(player);
                    player.sendMessage(getAndFormat("messages.fail"));
                    retries.remove(player.getUniqueId());
                }

                retries.put(player.getUniqueId(), count + 1);
                break;
            }
        }
    }

    private String getAndFormat(String path) {
        String message = config.getString(path);

        if (message == null) {
            return path;
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private void handleFailedPack(Player player) {
        if (plugin.getResourcePack().required()) {
            player.kickPlayer(getAndFormat("messages.fail"));
        } else {
            player.sendMessage(getAndFormat("messages.warn"));
        }
    }

}
