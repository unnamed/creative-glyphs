package team.unnamed.emojis.format.processor;

import net.kyori.adventure.text.Component;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiFormat;
import team.unnamed.emojis.format.representation.EmojiRepresentationProvider;

import java.util.function.Predicate;

final class ComponentMessageProcessor implements MessageProcessor<Component, Component> {

    private final EmojiRepresentationProvider<Component> representationProvider;

    ComponentMessageProcessor(EmojiRepresentationProvider<Component> representationProvider) {
        this.representationProvider = representationProvider;
    }

    @Override
    public Component process(Component message, EmojiRegistry registry, Predicate<Emoji> usageChecker) {
        return message.replaceText(replacementConfig -> replacementConfig
                .match(EmojiFormat.USAGE_PATTERN)
                .replacement((result, builder) -> {
                    String emojiName = result.group(1);
                    Emoji emoji = registry.getIgnoreCase(emojiName);

                    if (emoji == null || !usageChecker.test(emoji)) {
                        // can't use this emoji, return the same component
                        return builder;
                    }

                    return representationProvider.represent(emoji);
                }));
    }

}
