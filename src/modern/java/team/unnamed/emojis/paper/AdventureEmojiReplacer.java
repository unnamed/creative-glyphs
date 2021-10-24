package team.unnamed.emojis;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.TextComponentSerializer;
import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.format.EmojiReplacer;
import team.unnamed.emojis.format.Permissions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class AdventureEmojiReplacer {

    private static final JsonSerializationContext SERIALIZATION_CONTEXT = new JsonSerializationContext() {

        private final Gson gson = new Gson();

        @Override
        public JsonElement serialize(Object object) {
            return gson.toJsonTree(object);
        }

        @Override
        public JsonElement serialize(Object object, Type type) {
            return gson.toJsonTree(object, type);
        }
    };
    private static final TextComponentSerializer COMPONENT_SERIALIZER = new TextComponentSerializer();

    public static net.kyori.adventure.text.TextComponent replaceRichToRich(
            Permissible permissible,
            net.kyori.adventure.text.TextComponent origin,
            EmojiRegistry registry,
            EmojiComponentProvider emojiComponentProvider
    ) {
        List<Component> parts = new ArrayList<>();
        String content = origin.content();

        Matcher matcher = EmojiReplacer.EMOJI_PATTERN.matcher(content);
        int lastEnd = 0;

        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);

            if (start - lastEnd > 0) {
                // so there's text within this emoji and the previous emoji (or text start)
                String previous = content.substring(lastEnd, start - 1);
                parts.add(Component.text(previous));
            }

            String emojiName = content.substring(start, end);
            Emoji emoji = registry.get(emojiName);

            if (!Permissions.canUse(permissible, emoji)) {
                // if invalid emoji, lastEnd is the current start - 1, so it
                // consumes the emoji and its starting colon for the next
                // "previous" text
                lastEnd = start - 1;
            } else {
                parts.add(GsonComponentSerializer.gson().deserializeFromTree(COMPONENT_SERIALIZER.serialize(
                        emojiComponentProvider.toComponent(emoji),
                        TextComponent.class,
                        SERIALIZATION_CONTEXT
                )));
                // if valid emoji, lastEnd is the emoji end + 1, so it doesn't
                // consume the emoji nor its closing colon
                lastEnd = end + 1;
            }
        }

        // append remaining text
        if (content.length() - lastEnd > 0) {
            parts.add(origin.content(content.substring(lastEnd)));
        }

        if (parts.isEmpty()) {
            return Component.text("");
        } else {
            Component first = parts.get(0);
            if (first instanceof net.kyori.adventure.text.TextComponent) {
                parts.remove(0);
                return ((net.kyori.adventure.text.TextComponent) first)
                        .children(parts);
            } else {
                return Component.text()
                        .append(parts)
                        .build();
            }
        }
    }

}
