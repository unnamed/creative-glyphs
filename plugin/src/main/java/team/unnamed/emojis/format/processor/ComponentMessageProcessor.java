package team.unnamed.emojis.format.processor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.object.store.EmojiStore;
import team.unnamed.emojis.format.EmojiFormat;
import team.unnamed.emojis.format.representation.EmojiRepresentationProvider;

import java.util.function.Predicate;
import java.util.regex.Pattern;

final class ComponentMessageProcessor implements MessageProcessor<Component, Component> {

    private static final Pattern ANY = Pattern.compile(".*", Pattern.MULTILINE);

    private final EmojiRepresentationProvider<Component> representationProvider;

    ComponentMessageProcessor(EmojiRepresentationProvider<Component> representationProvider) {
        this.representationProvider = representationProvider;
    }

    @Override
    public Component process(Component message, EmojiStore registry, Predicate<Emoji> usageChecker) {
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

    @Override
    public Component flatten(Component message, EmojiStore registry) {
        return message.replaceText(TextReplacementConfig.builder()
                .match(ANY)
                .replacement((result, builder) ->
                        // delegate to String MessageProcessor
                        builder.content(MessageProcessor.string().flatten(builder.content(), registry)))
                .build());
    }

}
