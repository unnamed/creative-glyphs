package team.unnamed.creativeglyphs.listener.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.object.store.EmojiStore;
import team.unnamed.creativeglyphs.format.processor.MessageProcessor;
import team.unnamed.creativeglyphs.format.Permissions;
import team.unnamed.creativeglyphs.listener.EventListener;

/**
 * Implementation for listening to Paper's AsyncChatEvent,
 * won't cancel anything and should not be incompatible with
 * other plugins that use this event.
 */
public class PaperRichChatListener
        implements EventListener<AsyncChatEvent> {

    private final EmojiStore emojiStore;
    private final MessageProcessor<Component, Component> messageProcessor;

    public PaperRichChatListener(Plugin plugin, EmojiStore emojiStore) {
        this.emojiStore = emojiStore;
        this.messageProcessor = MessageProcessor.component(plugin);
    }

    @Override
    public Class<AsyncChatEvent> getEventType() {
        return AsyncChatEvent.class;
    }

    @Override
    public void execute(AsyncChatEvent event) {
        Player player = event.getPlayer();
        event.message(messageProcessor.process(
                event.message(),
                emojiStore,
                Permissions.permissionTest(player)
        ));
    }

}
