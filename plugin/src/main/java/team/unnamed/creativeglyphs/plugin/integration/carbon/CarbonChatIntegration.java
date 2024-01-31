package team.unnamed.creativeglyphs.plugin.integration.carbon;

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.event.events.CarbonPrivateChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.plugin.ComponentGlyphRenderer;
import team.unnamed.creativeglyphs.plugin.CreativeGlyphsPlugin;
import team.unnamed.creativeglyphs.plugin.integration.PluginIntegration;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public final class CarbonChatIntegration implements PluginIntegration.Chat {
    private final ContentProcessor<Component> contentProcessor;
    private final GlyphMap glyphMap;

    public CarbonChatIntegration(final @NotNull CreativeGlyphsPlugin plugin) {
        requireNonNull(plugin, "plugin");
        this.contentProcessor = ContentProcessor.component(new ComponentGlyphRenderer(plugin));
        this.glyphMap = plugin.registry();
    }

    @Override
    public @NotNull String plugin() {
        return "CarbonChat";
    }

    @Override
    public void enable(final @NotNull Plugin carbonChat) {
        final CarbonEventHandler carbonEventHandler = CarbonChatProvider.carbonChat().eventHandler();

        // process for CarbonChatEvent
        carbonEventHandler.subscribe(CarbonChatEvent.class, event ->
            event.message(contentProcessor.process(
                    event.message(),
                    glyphMap,
                    carbonPlayerPermissionTest(event.sender())
            )));

        // process for CarbonPrivateChatEvent
        carbonEventHandler.subscribe(CarbonPrivateChatEvent.class, event ->
                event.message(contentProcessor.process(
                        event.message(),
                        glyphMap,
                        carbonPlayerPermissionTest(event.sender())
                )));
    }

    private static @NotNull Predicate<Glyph> carbonPlayerPermissionTest(final @NotNull CarbonPlayer player) {
        return glyph -> {
            final String permission = glyph.permission();
            return permission.isEmpty() || player.hasPermission(permission);
        };
    }
}
