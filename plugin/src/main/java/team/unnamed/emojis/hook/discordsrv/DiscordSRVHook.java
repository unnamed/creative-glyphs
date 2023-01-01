package team.unnamed.emojis.hook.discordsrv;

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
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiFormat;
import team.unnamed.emojis.hook.PluginHook;

import java.util.List;
import java.util.regex.Pattern;

public class DiscordSRVHook
        implements PluginHook, Listener {

    private static final Pattern ANY = Pattern.compile(".*", Pattern.MULTILINE);
    private final EmojiRegistry registry;

    public DiscordSRVHook(EmojiRegistry registry) {
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

        @Subscribe
        public void onMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
            event.setMinecraftMessage(event.getMinecraftMessage().replaceText(replacementConfig -> replacementConfig
                    .match(EmojiFormat.USAGE_PATTERN)
                    .replacement((result, builder) -> {
                        String emojiName = result.group(1);
                        Emoji emoji = registry.getIgnoreCase(emojiName);

                        if (emoji == null) {
                            // TODO: Check linked account
                            // can't use this emoji, return the same component
                            return builder;
                        }

                        // TODO: Add hover
                        return Component.text()
                                .color(NamedTextColor.WHITE)
                                .content(emoji.replacement());
                    })));
        }

        @Subscribe
        public void onMessagePreProcess(GameChatMessagePreProcessEvent event) {

            DiscordSRV discordSRV = DiscordSRV.getPlugin();
            String channelId = discordSRV.getChannels().get(event.getChannel());
            TextChannel channel = discordSRV.getJda().getTextChannelById(channelId);
            Guild guild = channel.getGuild();
            boolean webhook = isGoingToBeSentToAWebHook(event);

            Component input = event.getMessageComponent();
            Component output = input.replaceText(TextReplacementConfig.builder()
                    .match(ANY)
                    .replacement((result, builder) -> {
                        StringBuilder replaced = new StringBuilder();
                        String content = builder.content();

                        for (int i = 0; i < content.length(); i++) {
                            int codePoint = content.codePointAt(i);

                            if (!Character.isBmpCodePoint(codePoint)) {
                                // two characters were used to represent this
                                // codepoint so skip this thing
                                i++;
                            }

                            Emoji emoji = registry.getByCodepoint(codePoint);

                            if (emoji == null) {
                                // codepoint did not represent an emoji, just append it
                                replaced.appendCodePoint(codePoint);
                            } else {
                                List<Emote> emotes = guild.getEmotesByName(emoji.name(), true);
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
                                            .append(emoji.name())
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
