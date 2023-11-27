package team.unnamed.creativeglyphs.plugin.hook.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;

import static java.util.Objects.requireNonNull;

public final class DiscordSRVHook implements PluginHook, Listener {
    private final Plugin plugin;
    private final GlyphMap glyphMap;

    public DiscordSRVHook(final @NotNull Plugin plugin, final @NotNull GlyphMap glyphMap) {
        this.plugin = requireNonNull(plugin, "plugin");
        this.glyphMap = requireNonNull(glyphMap, "glyphMap");
    }

    @Override
    public @NotNull String pluginName() {
        return "DiscordSRV";
    }

    @Override
    public void hook(final @NotNull Plugin hook) {
        DiscordSRV.api.subscribe(new DiscordSRVListener(plugin, glyphMap));
    }
}
