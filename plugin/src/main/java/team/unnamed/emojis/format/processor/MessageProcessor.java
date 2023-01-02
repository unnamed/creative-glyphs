package team.unnamed.emojis.format.processor;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.representation.EmojiRepresentationProvider;

import java.util.function.Predicate;

public interface MessageProcessor<TInput, TOutput> {

    TOutput process(TInput message, EmojiRegistry registry, Predicate<Emoji> usageChecker);

    TInput flatten(TInput message, EmojiRegistry registry);

    default TOutput process(TInput message, EmojiRegistry registry) {
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

    static MessageProcessor<String, BaseComponent[]> stringToLegacyComponent(EmojiRepresentationProvider<TextComponent> representationProvider) {
        return new LegacyMessageProcessor(representationProvider);
    }

    static MessageProcessor<String, BaseComponent[]> stringToLegacyComponent(Plugin plugin) {
        return stringToLegacyComponent(EmojiRepresentationProvider.legacyComponent(plugin));
    }

}
