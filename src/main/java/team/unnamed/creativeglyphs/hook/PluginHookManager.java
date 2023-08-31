package team.unnamed.creativeglyphs.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for handling {@link PluginHook}
 * instances
 */
public class PluginHookManager {

    private final Map<String, PluginHook> hooks
            = new LinkedHashMap<>();

    private PluginHookManager() {
        // instantiate using the static factory method
    }

    /**
     * Registers a new hook to be checked on plugin enable
     * @return {@code this}, for a fluent api
     */
    public PluginHookManager registerHook(PluginHook hook) {
        hooks.put(hook.getPluginName(), hook);
        return this;
    }

    /**
     * Finds plugins for all the registered hooks,
     * and enables them if the hooked plugin is enabled
     * in this server.
     * @return The enabled hooks
     */
    public Set<PluginHook> hook() {
        Set<PluginHook> enabled = new HashSet<>();
        for (PluginHook hook : hooks.values()) {
            Plugin plugin = Bukkit.getPluginManager()
                    .getPlugin(hook.getPluginName());
            if (plugin != null && plugin.isEnabled()) {
                // found it, hook it
                hook.hook(plugin);
                enabled.add(hook);
            }
        }
        return enabled;
    }

    public static PluginHookManager create() {
        // because fluent api's look better using
        // static factory methods
        return new PluginHookManager();
    }

}
