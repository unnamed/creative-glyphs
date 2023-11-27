package team.unnamed.creativeglyphs.plugin.hook;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a plugin hook handle, contains
 * information like the plugin name and the
 * initialization and finalization logic.
 */
public interface PluginHook {
    /**
     * Returns the plugin name for this
     * plugin hook.
     */
    @NotNull String pluginName();

    /**
     * Hooks into the given {@code hook}
     * @param hook The target hook
     */
    default void hook(final @NotNull Plugin hook) {
    }

    /**
     * Marker interface for {@link PluginHook}
     * implementations supposed to be used instead
     * of our own chat listeners
     */
    interface Chat extends PluginHook {
    }
}
