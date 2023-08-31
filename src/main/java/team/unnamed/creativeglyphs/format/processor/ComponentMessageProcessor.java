package team.unnamed.creativeglyphs.format.processor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.ahocorasick.trie.PayloadEmit;
import team.unnamed.creativeglyphs.Emoji;
import team.unnamed.creativeglyphs.object.store.EmojiStore;
import team.unnamed.creativeglyphs.format.representation.EmojiRepresentationProvider;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

final class ComponentMessageProcessor implements MessageProcessor<Component, Component> {

    private static final Pattern ANY = Pattern.compile(".*", Pattern.MULTILINE);

    private final EmojiRepresentationProvider<Component> representationProvider;

    ComponentMessageProcessor(EmojiRepresentationProvider<Component> representationProvider) {
        this.representationProvider = representationProvider;
    }

    @Override
    @SuppressWarnings("DuplicatedCode") // detects comments as duplicated code too
    public Component process(Component message, EmojiStore registry, Predicate<Emoji> usageChecker) {
        return message.replaceText(config -> config
                .match(ANY)
                .replacement((result, componentBuilder) -> {
                    String text = componentBuilder.content();

                    Collection<PayloadEmit<Emoji>> emits = registry.trie().parseText(text);

                    // set empty content
                    componentBuilder.content("");

                    Iterator<PayloadEmit<Emoji>> emitIterator = emits.iterator();
                    PayloadEmit<Emoji> emit = emitIterator.hasNext() ? emitIterator.next() : null;

                    StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);

                        Emoji literal = registry.getByCodePoint(c);
                        if (literal != null && !usageChecker.test(literal)) {
                            // player entered a literal emoji character,
                            // and they do not have permissions to use
                            // it, simply skip this character
                            continue;
                        }

                        // check if emit is null, if the emit is null, that
                        // means that we are not looking for an emoji usage,
                        // so we can skip next processes.
                        // It is null if, and only if:
                        // - There aren't any emoji usages in the message
                        // - We have finished processing all the emoji usages
                        if (emit == null) {
                            builder.append(c);
                            continue;
                        }

                        int start = emit.getStart();

                        if (i < start) {
                            builder.append(c);
                            continue;
                        }

                        Emoji emoji = emit.getPayload();
                        if (usageChecker.test(emoji)) {

                            if (builder.length() > 0) {
                                componentBuilder.append(Component.text(builder.toString()));
                                builder.setLength(0);
                            }

                            Component insertion = representationProvider.represent(emoji);
                            componentBuilder.append(insertion);

                            // skip to the end of the emoji usage, we
                            // have already replaced the emoji
                            i += emit.getEnd() - start;
                        }

                        emit = emitIterator.hasNext() ? emitIterator.next() : null;
                    }

                    if (builder.length() > 0) {
                        if (componentBuilder.children().isEmpty()) {
                            componentBuilder.content(builder.toString());
                            return componentBuilder;
                        } else {
                            componentBuilder.append(Component.text(builder.toString()));
                        }
                    }

                    return componentBuilder;
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
