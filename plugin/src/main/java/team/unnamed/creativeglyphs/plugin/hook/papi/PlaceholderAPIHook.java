package team.unnamed.creativeglyphs.plugin.hook.papi;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;

import static java.util.Objects.requireNonNull;

public final class PlaceholderAPIHook implements PluginHook {
    private final Plugin plugin;
    private final PluginGlyphMap registry;

    public PlaceholderAPIHook(
            final @NotNull Plugin plugin,
            final @NotNull PluginGlyphMap registry
    ) {
        this.plugin = requireNonNull(plugin, "plugin");
        this.registry = requireNonNull(registry, "registry");
    }

    @Override
    public @NotNull String pluginName() {
        return "PlaceholderAPI";
    }

    @Override
    public void hook(final @NotNull Plugin hook) {
        new GlyphPlaceholderExpansion(plugin, registry).register();
        plugin.getLogger().info("Successfully registered PlaceholderAPI placeholder expansion");
    }
}
