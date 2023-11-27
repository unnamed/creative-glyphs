package team.unnamed.creativeglyphs.plugin.hook.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Emote;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.content.ContentFlattener;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.plugin.ComponentGlyphRenderer;
import team.unnamed.creativeglyphs.plugin.util.Permissions;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

final class DiscordSRVListener {
    private final GlyphMap glyphMap;
    private final ContentProcessor<net.kyori.adventure.text.Component> processor;

    DiscordSRVListener(Plugin plugin, GlyphMap glyphMap) {
        this.glyphMap = glyphMap;
        this.processor = ContentProcessor.component(new ComponentGlyphRenderer(plugin));
    }

    @Subscribe
    public void onDiscordToMinecraft(final @NotNull DiscordGuildMessagePostProcessEvent event) {
        // find account if exists
        final UUID playerId = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(event.getAuthor().getId());
        final Player player = playerId == null ? null : Bukkit.getPlayer(playerId);
        final Predicate<Glyph> usageChecker = player == null
                ? glyph -> glyph.permission().isEmpty() // if player is null, only allow using glyphs that do not require permissions
                : Permissions.permissionTest(player);

        // I hate DiscordSRV, if they hadn't relocated adventure...
        final Component message = event.getMinecraftMessage();

        // convert relocated Component to non-relocated Component
        final net.kyori.adventure.text.Component messageComponent = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(GsonComponentSerializer.gson().serialize(message));

        // process non-relocated Component
        final net.kyori.adventure.text.Component processedMessage = processor.process(messageComponent, glyphMap, usageChecker);

        // convert non-relocated Component to relocated Component
        final Component relocatedProcessedMessage = GsonComponentSerializer.gson().deserialize(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(processedMessage));
        event.setMinecraftMessage(relocatedProcessedMessage);
    }


    @Subscribe
    public void onMinecraftToDiscord(final @NotNull GameChatMessagePostProcessEvent event) {
        final DiscordSRV discordSRV = DiscordSRV.getPlugin();
        final String channelId = discordSRV.getChannels().get(event.getChannel());
        final TextChannel channel = discordSRV.getJda().getTextChannelById(channelId);
        final Guild guild = channel.getGuild();
        final boolean webhook = isGoingToBeSentToAWebHook(event);

        final String input = event.getProcessedMessage();
        final String output = ContentFlattener.string(glyph -> {
            final List<Emote> emotes = guild.getEmotesByName(glyph.name(), true);
            final Emote emote = emotes.isEmpty() ? null : emotes.get(0);

            if (emote != null && webhook) {
                return "<:" + emote.getName() + ':' + emote.getId() + '>';
            } else {
                return ":" + glyph.name() + ':';
            }
        }).flatten(input, glyphMap);

        event.setProcessedMessage(output);
    }

    @SuppressWarnings("unused")
    private static boolean isGoingToBeSentToAWebHook(final @NotNull GameChatMessagePostProcessEvent event) {
        // "event" is unused, but I believe they are going to use it in the future
        return DiscordSRV.config().getBoolean("Experiment_WebhookChatMessageDelivery");
    }
}
