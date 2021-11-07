package team.unnamed.emojis.resourcepack;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.uracle.event.ResourcePackGenerateEvent;

import java.util.Iterator;

/**
 * Responsible for writing resources for the emojis
 * plugin.
 * @author yusshu (Andre Roldan)
 */
public class EmojiAssetWriter implements Listener {

    private final EmojiRegistry registry;

    public EmojiAssetWriter(EmojiRegistry registry) {
        this.registry = registry;
    }

    /**
     * Transfers the resource pack information to the
     * given {@code output}
     *
     * <strong>Note that this method won't close the
     * given {@code output}</strong>
     */
    @EventHandler
    public void write(ResourcePackGenerateEvent event) {

        // write font file
        event.write("assets/minecraft/font/default.json", createFontJson());

        // write emojis images
        for (Emoji emoji : registry.values()) {
            event.write("assets/minecraft/textures/emojis/" + emoji.getName() + ".png", emoji.getData());
        }
    }

    private String createFontJson() {
        StringBuilder builder = new StringBuilder("{ \"providers\": [ ");

        Iterator<Emoji> iterator = registry.values().iterator();
        while (iterator.hasNext()) {
            Emoji emoji = iterator.next();
            builder
                    .append("{\"ascent\":").append(emoji.getAscent())
                    .append(",\"chars\":[\"").append(emoji.getCharacter())
                    .append("\"],\"file\":\"emojis/").append(emoji.getName()).append(".png")
                    .append("\",\"height\":").append(emoji.getHeight())
                    .append(",\"type\":\"bitmap\"}");
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }

        return builder.append("]}").toString();
    }

}
