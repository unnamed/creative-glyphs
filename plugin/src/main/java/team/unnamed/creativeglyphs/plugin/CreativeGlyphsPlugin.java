package team.unnamed.creativeglyphs.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.creative.central.CreativeCentralProvider;
import team.unnamed.creative.central.event.pack.ResourcePackGenerateEvent;
import team.unnamed.creativeglyphs.plugin.command.RootCommand;
import team.unnamed.creativeglyphs.plugin.integration.essentialsdiscord.EssentialsDiscordIntegration;
import team.unnamed.creativeglyphs.plugin.listener.misc.AnvilEditListener;
import team.unnamed.creativeglyphs.plugin.listener.misc.CommandPreprocessListener;
import team.unnamed.creativeglyphs.plugin.util.ArtemisGlyphImporter;
import team.unnamed.creativeglyphs.plugin.listener.bus.EventListener;
import team.unnamed.creativeglyphs.plugin.integration.PluginIntegration;
import team.unnamed.creativeglyphs.plugin.integration.IntegrationManager;
import team.unnamed.creativeglyphs.plugin.integration.discordsrv.DiscordSRVIntegration;
import team.unnamed.creativeglyphs.plugin.integration.ezchat.EzChatIntegration;
import team.unnamed.creativeglyphs.plugin.integration.miniplaceholders.MiniPlaceholdersIntegration;
import team.unnamed.creativeglyphs.plugin.integration.papi.PlaceholderApiIntegration;
import team.unnamed.creativeglyphs.plugin.integration.townychat.TownyChatIntegration;
import team.unnamed.creativeglyphs.plugin.listener.chat.ChatCompletionsListener;
import team.unnamed.creativeglyphs.plugin.listener.bus.EventBus;
import team.unnamed.creativeglyphs.plugin.listener.ListenerFactory;
import team.unnamed.creativeglyphs.resourcepack.ResourcePackGlyphWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class CreativeGlyphsPlugin extends JavaPlugin {

    private PluginGlyphMap registry;
    private ArtemisGlyphImporter importer;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        try {
            this.registry = PluginGlyphMap.create(this);
        } catch (IOException e) {
            getLogger().severe("Cannot create emoji store...");
            setEnabled(false);
            return;
        }
        this.importer = new ArtemisGlyphImporter();

        this.registry.load();

        // export
        CreativeCentralProvider.get().eventBus().listen(this, ResourcePackGenerateEvent.class, new ResourcePackGlyphWriter(registry));

        EventBus eventBus = EventBus.create(this);

        Objects.requireNonNull(getCommand("emojis"), "'emojis' command not registered")
                .setExecutor(new RootCommand(this).asExecutor());

        Set<PluginIntegration> hooks = IntegrationManager.integrationManager(this)
                .register(new EzChatIntegration(this, registry))
                .register(new TownyChatIntegration(this, registry))
                .register(new PlaceholderApiIntegration(this, registry))
                .register(new DiscordSRVIntegration(registry))
                .register(new MiniPlaceholdersIntegration(registry))
                .register(new EssentialsDiscordIntegration(this, registry))
                .check();

        if (hooks.stream().noneMatch(hook -> hook instanceof PluginIntegration.Chat)) {
            // if no chat plugin hooks, let's register our own listener
            EventPriority priority = EventPriority.valueOf(getConfig().getString(
                    "compat.listener-priority",
                    "HIGHEST"
            ).toUpperCase(Locale.ROOT));

            EventListener<?> chatListener = ListenerFactory.create(
                    this,
                    registry,
                    getConfig().getBoolean("compat.use-paper-listener")
            );

            eventBus.register(chatListener, priority);
        }

        try {
            // check if methods required to make completions work exist
            // (they may not exist in Spigot)
            Player.class.getDeclaredMethod("addAdditionalChatCompletions", Collection.class);

            // register emoji completions listener
            listen(new ChatCompletionsListener(registry));
        } catch (NoSuchMethodException ignored) {
        }

        listen(new AnvilEditListener(registry));
        listen(new CommandPreprocessListener(registry));

        // Metrics
        new Metrics(this, 17168);
    }

    private void listen(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public PluginGlyphMap registry() {
        return registry;
    }

    public ArtemisGlyphImporter importer() {
        return importer;
    }

}
