package team.unnamed.creativeglyphs;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.creative.central.CreativeCentralProvider;
import team.unnamed.creative.central.event.pack.ResourcePackGenerateEvent;
import team.unnamed.creativeglyphs.command.RootCommand;
import team.unnamed.creativeglyphs.object.cloud.EmojiImporter;
import team.unnamed.creativeglyphs.listener.EventListener;
import team.unnamed.creativeglyphs.hook.PluginHook;
import team.unnamed.creativeglyphs.hook.PluginHookManager;
import team.unnamed.creativeglyphs.hook.discordsrv.DiscordSRVHook;
import team.unnamed.creativeglyphs.hook.ezchat.EzChatHook;
import team.unnamed.creativeglyphs.hook.miniplaceholders.MiniPlaceholdersHook;
import team.unnamed.creativeglyphs.hook.papi.PlaceholderApiHook;
import team.unnamed.creativeglyphs.hook.townychat.TownyChatHook;
import team.unnamed.creativeglyphs.listener.EmojiCompletionsListener;
import team.unnamed.creativeglyphs.listener.EventBus;
import team.unnamed.creativeglyphs.listener.ListenerFactory;
import team.unnamed.creativeglyphs.util.Metrics;
import team.unnamed.creativeglyphs.object.store.EmojiStore;
import team.unnamed.creativeglyphs.resourcepack.EmojisWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class EmojisPlugin extends JavaPlugin {

    private EmojiStore registry;
    private EmojiImporter importer;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        try {
            this.registry = EmojiStore.create(this);
        } catch (IOException e) {
            getLogger().severe("Cannot create emoji store...");
            setEnabled(false);
            return;
        }
        this.importer = new EmojiImporter();

        this.registry.load();

        // export
        CreativeCentralProvider.get().eventBus().listen(this, ResourcePackGenerateEvent.class, new EmojisWriter(registry));

        EventBus eventBus = EventBus.create(this);

        Objects.requireNonNull(getCommand("emojis"), "'emojis' command not registered")
                .setExecutor(new RootCommand(this).asExecutor());

        Set<PluginHook> hooks = PluginHookManager.create()
                .registerHook(new EzChatHook(this, registry))
                .registerHook(new TownyChatHook(this, registry))
                .registerHook(new PlaceholderApiHook(this, registry))
                .registerHook(new DiscordSRVHook(registry))
                .registerHook(new MiniPlaceholdersHook(registry))
                .hook();

        if (hooks.stream().noneMatch(hook -> hook instanceof PluginHook.Chat)) {
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
            getServer().getPluginManager().registerEvents(new EmojiCompletionsListener(registry), this);
        } catch (NoSuchMethodException ignored) {
        }

        // Metrics
        new Metrics(this, 17168);
    }

    public EmojiStore registry() {
        return registry;
    }

    public EmojiImporter importer() {
        return importer;
    }

}
