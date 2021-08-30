package team.unnamed.emojis.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Utility class for replacing emojis and obtaining
 * the result in a rich component like {@link Component[]}
 */
public class ComponentEmojiReplacer {

    private ComponentEmojiReplacer() {
    }

    public static TextComponent replace(
            Permissible permissible,
            TextComponent origin,
            EmojiRegistry registry,
            EmojiComponentProvider emojiComponentProvider
    ) {
        List<Component> children = new ArrayList<>();
        String content = origin.content();

        Matcher matcher = Patterns.EMOJI_PATTERN.matcher(content);
        int lastEnd = 0;

        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);

            if (start - lastEnd > 0) {
                // so there's text within this emoji and the previous emoji (or text start)
                String previous = content.substring(lastEnd, start - 1);
                children.add(Component.text(previous));
            }

            String emojiName = content.substring(start, end);
            Emoji emoji = registry.get(emojiName);

            if (emoji == null || !permissible.hasPermission(emoji.getPermission())) {
                // if invalid emoji, lastEnd is the current start - 1, so it
                // consumes the emoji and its starting colon for the next
                // "previous" text
                lastEnd = start - 1;
            } else {
                children.add(emojiComponentProvider.toAdventureComponent(emoji));
                // if valid emoji, lastEnd is the emoji end + 1, so it doesn't
                // consume the emoji nor its closing colon
                lastEnd = end + 1;
            }
        }

        // append remaining text
        if (content.length() - lastEnd > 0) {
            children.add(Component.text(content.substring(lastEnd)));
        }

        children.addAll(origin.children());

        return Component.text().append(children).build();
    }

}
