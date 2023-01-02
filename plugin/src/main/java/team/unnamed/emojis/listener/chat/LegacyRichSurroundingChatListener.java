package team.unnamed.emojis.listener.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.Permissions;
import team.unnamed.emojis.format.processor.MessageProcessor;
import team.unnamed.emojis.listener.EventCancellationStrategy;
import team.unnamed.emojis.listener.EventListener;

/**
 * Class listening for {@link AsyncPlayerChatEvent} that
 * cancels the event and manually sends the component to
 * the event recipients
 */
public class LegacyRichSurroundingChatListener
        implements EventListener<AsyncPlayerChatEvent> {

    private final EmojiRegistry emojiRegistry;
    private final EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy;
    private final MessageProcessor<String, BaseComponent[]> messageProcessor;

    public LegacyRichSurroundingChatListener(
            Plugin plugin,
            EmojiRegistry emojiRegistry,
            EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy
    ) {
        this.emojiRegistry = emojiRegistry;
        this.cancellationStrategy = cancellationStrategy;
        this.messageProcessor = MessageProcessor.stringToLegacyComponent(plugin);
    }

    @Override
    public Class<AsyncPlayerChatEvent> getEventType() {
        return AsyncPlayerChatEvent.class;
    }

    @Override
    public void execute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String content = String.format(event.getFormat(), player.getName(), message);

        // log flattened content to the console (clean, without weird characters)
        // since surrounding the chat event prevents the messages from being logged
        Bukkit.getLogger().info(messageProcessor.flatten(content, emojiRegistry));

        BaseComponent[] translated = messageProcessor.process(
                content,
                emojiRegistry,
                Permissions.permissionTest(player)
        );

        for (Player recipient : event.getRecipients()) {
            recipient.spigot().sendMessage(translated);
        }

        cancellationStrategy.surround(event);
    }

}
