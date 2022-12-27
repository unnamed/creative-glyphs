package team.unnamed.emojis.listener.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.processor.MessageProcessor;
import team.unnamed.emojis.format.Permissions;
import team.unnamed.emojis.listener.EventListener;

/**
 * Implementation for listening to Paper's AsyncChatEvent,
 * won't cancel anything and should not be incompatible with
 * other plugins that use this event.
 *
 * Thank you PaperMC <3
 */
public class PaperRichChatListener
        implements EventListener<AsyncChatEvent> {

    private final EmojiRegistry emojiRegistry;
    private final MessageProcessor<Component, Component> messageProcessor;

    public PaperRichChatListener(Plugin plugin, EmojiRegistry emojiRegistry) {
        this.emojiRegistry = emojiRegistry;
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
                emojiRegistry,
                Permissions.permissionTest(player)
        ));
    }

}
