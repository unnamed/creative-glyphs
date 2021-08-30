package team.unnamed.emojis.hook;

import org.bukkit.plugin.Plugin;

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
    String getPluginName();

    /**
     * Hooks into the given {@code hook}
     * @param hook The target hook
     */
    default void hook(Plugin hook) {
    }

    /**
     * Marker interface for {@link PluginHook}
     * implementations supposed to be used instead
     * of our own chat listeners
     */
    interface Chat extends PluginHook {
    }

}
