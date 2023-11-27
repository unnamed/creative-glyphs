package team.unnamed.creativeglyphs.plugin.hook.essentialsdiscord;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;

import static java.util.Objects.requireNonNull;

public final class EssentialsDiscordHook implements PluginHook {
    private final Plugin plugin;
    private final PluginGlyphMap glyphMap;

    public EssentialsDiscordHook(final @NotNull Plugin plugin, final @NotNull PluginGlyphMap glyphMap) {
        this.plugin = requireNonNull(plugin, "plugin");
        this.glyphMap = requireNonNull(glyphMap, "glyphMap");
    }

    @Override
    public @NotNull String pluginName() {
        return "EssentialsDiscord";
    }

    @Override
    public void hook(final @NotNull Plugin hook) {
        Bukkit.getPluginManager().registerEvents(new EssentialsDiscordListener(glyphMap), plugin);
        plugin.getLogger().info("Successfully hooked with EssentialsDiscord!");
    }
}
