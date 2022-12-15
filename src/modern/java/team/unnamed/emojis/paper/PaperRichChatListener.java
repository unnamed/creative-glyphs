package team.unnamed.emojis.paper;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiReplacer;
import team.unnamed.emojis.format.Permissions;
import team.unnamed.emojis.listener.EventListener;

/**
 * Implementation for listening to Paper's AsyncChatEvent,
 * won't cancel anything and should not be incompatible with
 * other plugins that use this event.
 *
 * Thank you PaperMC <3
 */
@SuppressWarnings("unused") // instantiated via reflection
public class PaperRichChatListener
        implements EventListener<AsyncChatEvent> {

    private final EmojiRegistry emojiRegistry;
    private final PaperEmojiComponentProvider emojiComponentProvider;

    public PaperRichChatListener(Plugin plugin, EmojiRegistry emojiRegistry) {
        this.emojiRegistry = emojiRegistry;
        this.emojiComponentProvider = new PaperEmojiComponentProvider(plugin);
    }

    @Override
    public Class<AsyncChatEvent> getEventType() {
        return AsyncChatEvent.class;
    }

    @Override
    public void execute(AsyncChatEvent event) {
        Player player = event.getPlayer();
        event.message(event.message().replaceText(replacementConfig -> replacementConfig
                .match(EmojiReplacer.EMOJI_PATTERN)
                .replacement((result, builder) -> {
                    String emojiName = result.group(1);
                    Emoji emoji = emojiRegistry.get(emojiName);

                    if (!Permissions.canUse(player, emoji)) {
                        // can't use this emoji, return the same component
                        return builder;
                    }

                    return emojiComponentProvider.componentOf(emoji);
                })));
    }

}
