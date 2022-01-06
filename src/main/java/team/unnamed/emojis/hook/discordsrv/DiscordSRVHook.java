package team.unnamed.emojis.hook.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.hook.PluginHook;

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
        DiscordSRV.api.subscribe(this);
    }

    @Subscribe
    public void onMessagePreProcess(GameChatMessagePreProcessEvent event) {
        Component component = event.getMessageComponent()
                .replaceText(TextReplacementConfig.builder()
                        .match(ANY)
                        .replacement((result, builder) -> {
                            StringBuilder replaced = new StringBuilder();
                            String content = builder.content();

                            for (int i = 0; i < content.length(); i++) {
                                char c = content.charAt(i);
                                Emoji emoji = registry.getByChar(c);

                                if (emoji == null) {
                                    // let it be
                                    replaced.append(c);
                                } else {
                                    // emoji found, replace by its name
                                    replaced.append(':')
                                            .append(emoji.getName())
                                            .append(':');
                                }
                            }

                            return builder.content(replaced.toString());
                        })
                        .build());
        event.setMessageComponent(component);
    }

}
