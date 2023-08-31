package team.unnamed.creativeglyphs.format.processor;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.Emoji;
import team.unnamed.creativeglyphs.object.store.EmojiStore;
import team.unnamed.creativeglyphs.format.representation.EmojiRepresentationProvider;

import java.util.function.Predicate;

public interface MessageProcessor<TInput, TOutput> {

    TOutput process(TInput message, EmojiStore registry, Predicate<Emoji> usageChecker);

    TInput flatten(TInput message, EmojiStore registry);

    default TOutput process(TInput message, EmojiStore registry) {
        // has permission to use all the emojis
        return process(message, registry, emoji -> true);
    }

    static MessageProcessor<String, String> string() {
        return StringMessageProcessor.INSTANCE;
    }

    static MessageProcessor<Component, Component> component(EmojiRepresentationProvider<Component> representationProvider) {
        return new ComponentMessageProcessor(representationProvider);
    }

    static MessageProcessor<Component, Component> component(Plugin plugin) {
        return component(EmojiRepresentationProvider.component(plugin));
    }

}
