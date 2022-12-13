package team.unnamed.emojis.paper;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiComponentProvider;
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
    private final EmojiComponentProvider emojiComponentProvider;

    public PaperRichChatListener(
            EmojiRegistry emojiRegistry,
            EmojiComponentProvider emojiComponentProvider
    ) {
        this.emojiRegistry = emojiRegistry;
        this.emojiComponentProvider = emojiComponentProvider;
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

                    return toAdventureComponent(emojiComponentProvider.toComponent(emoji));
                })));
    }

    private static Component toAdventureComponent(BaseComponent component) {
        return GsonComponentSerializer.gson().deserialize(ComponentSerializer.toString(component));
    }

}
