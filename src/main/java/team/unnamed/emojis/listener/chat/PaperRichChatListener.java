package team.unnamed.emojis.listener.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.ComponentEmojiReplacer;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.listener.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
        Component message = event.message();
        event.message(replaceEmojisRecursively(player, message));
    }

    private Component replaceEmojisRecursively(Permissible permissible, Component component) {

        Component newComponent = component;

        if (component instanceof TextComponent) {
            newComponent = ComponentEmojiReplacer.replace(
                    permissible,
                    (TextComponent) component,
                    emojiRegistry,
                    emojiComponentProvider
            );
        }

        List<Component> children = new ArrayList<>(newComponent.children());
        ListIterator<Component> childIterator = children.listIterator();

        while (childIterator.hasNext()) {
            Component child = childIterator.next();
            Component newChild = replaceEmojisRecursively(permissible, child);

            for (Component grandChild : newChild.children()) {
                childIterator.add(grandChild);
            }
        }

        return newComponent.children(children);
    }

}
