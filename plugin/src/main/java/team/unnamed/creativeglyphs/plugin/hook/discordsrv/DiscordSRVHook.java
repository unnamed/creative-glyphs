package team.unnamed.creativeglyphs.plugin.hook.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Emote;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.format.NamedTextColor;
import org.ahocorasick.trie.PayloadEmit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;
import team.unnamed.creativeglyphs.util.Patterns;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class DiscordSRVHook
        implements PluginHook, Listener {

    private final PluginGlyphMap registry;

    public DiscordSRVHook(PluginGlyphMap registry) {
        this.registry = registry;
    }

    @Override
    public String getPluginName() {
        return "DiscordSRV";
    }

    @Override
    public void hook(Plugin hook) {
        DiscordSRV.api.subscribe(new DiscordSRVListener());
    }

    private class DiscordSRVListener {

        // handles messages from Discord to Minecraft
        @Subscribe
        public void onMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
            // find account if exists
            UUID playerId = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(event.getAuthor().getId());

            event.setMinecraftMessage(event.getMinecraftMessage().replaceText(replacementConfig -> replacementConfig
                    .match(Patterns.ANY)
                    .replacement((result, componentBuilder) -> {
//                        String emojiName = result.group(1);
//                        Glyph glyph = registry.getIgnoreCase(emojiName);
//
//                        if (glyph == null) {
//                            // TODO: Check linked account
//                            // can't use this emoji, return the same component
//                            return builder;
//                        }
//
//                        // TODO: Add hover
//                        return Component.text()
//                                .color(NamedTextColor.WHITE)
//                                .content(glyph.replacement());
                        String text = componentBuilder.content();

                        Collection<PayloadEmit<Glyph>> emits = registry.trie().parseText(text);

                        // set empty content
                        componentBuilder.content("");

                        Iterator<PayloadEmit<Glyph>> emitIterator = emits.iterator();
                        PayloadEmit<Glyph> emit = emitIterator.hasNext() ? emitIterator.next() : null;

                        StringBuilder builder = new StringBuilder();

                        for (int i = 0; i < text.length(); i++) {
                            char c = text.charAt(i);

                            Glyph literal = registry.getByCodePoint(c);
                            if (literal != null /*&& !usageChecker.test(literal)*/) {
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

                            Glyph glyph = emit.getPayload();
                            //if (usageChecker.test(glyph)) {

                                if (builder.length() > 0) {
                                    componentBuilder.append(Component.text(builder.toString()));
                                    builder.setLength(0);
                                }

                                Component insertion = /*representationProvider.represent(glyph)*/ Component.text()
                                        .content(glyph.replacement())
                                        .color(NamedTextColor.WHITE)
                                        .build();
                                componentBuilder.append(insertion);

                                // skip to the end of the emoji usage, we
                                // have already replaced the emoji
                                i += emit.getEnd() - start;
                            //}

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
                    })));
        }


        // handles messages from Minecraft to Discord
        @Subscribe
        public void onMessagePreProcess(GameChatMessagePreProcessEvent event) {

            DiscordSRV discordSRV = DiscordSRV.getPlugin();
            String channelId = discordSRV.getChannels().get(event.getChannel());
            TextChannel channel = discordSRV.getJda().getTextChannelById(channelId);
            Guild guild = channel.getGuild();
            boolean webhook = isGoingToBeSentToAWebHook(event);

            Component input = event.getMessageComponent();
            Component output = input.replaceText(TextReplacementConfig.builder()
                    .match(Patterns.ANY)
                    .replacement((result, builder) -> {
                        StringBuilder replaced = new StringBuilder();
                        String content = builder.content();

                        for (int i = 0; i < content.length(); i++) {
                            int codePoint = content.codePointAt(i);

                            if (!Character.isBmpCodePoint(codePoint)) {
                                // two characters were used to represent this
                                // code point so skip this thing
                                i++;
                            }

                            Glyph glyph = registry.getByCodePoint(codePoint);

                            if (glyph == null) {
                                // code point did not represent an emoji, just append it
                                replaced.appendCodePoint(codePoint);
                            } else {
                                List<Emote> emotes = guild.getEmotesByName(glyph.name(), true);
                                Emote emote = emotes.isEmpty() ? null : emotes.get(0);

                                if (emote != null && webhook) {
                                    replaced.append("<:")
                                            .append(emote.getName())
                                            .append(':')
                                            .append(emote.getId())
                                            .append(">");
                                } else {
                                    // emoji found, replace by its name
                                    replaced.append(':')
                                            .append(glyph.name())
                                            .append(':');
                                }
                            }
                        }

                        return builder.content(replaced.toString());
                    })
                    .build());
            event.setMessageComponent(output);
        }

        private boolean isGoingToBeSentToAWebHook(GameChatMessagePreProcessEvent event) {
            return DiscordSRV.config().getBoolean("Experiment_WebhookChatMessageDelivery");
        }

    }

}
