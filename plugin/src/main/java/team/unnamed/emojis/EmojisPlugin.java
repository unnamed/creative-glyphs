package team.unnamed.emojis;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.emojis.command.RootCommand;
import team.unnamed.emojis.object.cloud.EmojiImporter;
import team.unnamed.emojis.listener.EventListener;
import team.unnamed.emojis.resourcepack.export.DefaultExportService;
import team.unnamed.emojis.resourcepack.export.ExportService;
import team.unnamed.emojis.hook.PluginHook;
import team.unnamed.emojis.hook.PluginHookManager;
import team.unnamed.emojis.hook.discordsrv.DiscordSRVHook;
import team.unnamed.emojis.hook.ezchat.EzChatHook;
import team.unnamed.emojis.hook.papi.PlaceholderApiHook;
import team.unnamed.emojis.hook.townychat.TownyChatHook;
import team.unnamed.emojis.listener.EmojiCompletionsListener;
import team.unnamed.emojis.listener.EventBus;
import team.unnamed.emojis.listener.EventCancellationStrategy;
import team.unnamed.emojis.listener.ListenerFactory;
import team.unnamed.emojis.listener.ResourcePackApplyListener;
import team.unnamed.emojis.metrics.Metrics;
import team.unnamed.emojis.resourcepack.ResourcePack;
import team.unnamed.emojis.resourcepack.ResourcePackApplier;
import team.unnamed.emojis.resourcepack.UrlAndHash;
import team.unnamed.emojis.object.store.EmojiStore;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class EmojisPlugin extends JavaPlugin {

    private EmojiStore registry;
    private ResourcePack resourcePack;

    private EmojiImporter importer;

    private ExportService exportService;

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
        this.exportService = new DefaultExportService(this);
        getLogger().info("Exporting resource-pack...");
        UrlAndHash location = exportService.export(registry);

        EventBus eventBus = EventBus.create(this);

        if (location != null) {
            // TODO: Remove support for 'application.prompt'
            String prompt = getConfig().getString("application.prompt", null);
            if (prompt == null) {
                prompt = getConfig().getString("pack.prompt", null);
            }
            if (prompt != null) {
                prompt = ChatColor.translateAlternateColorCodes('&', prompt);
                // TODO: refactor this ugly code
                prompt = ComponentSerializer.toString(TextComponent.fromLegacyText(prompt));
            }

            this.resourcePack = new ResourcePack(
                    location.url(),
                    location.hash(),
                    // TODO: Remove support for "feature.require-pack"
                    getConfig().getBoolean("feature.require-pack", false) || getConfig().getBoolean("pack.required", false),
                    prompt
            );
            Bukkit.getPluginManager().registerEvents(
                    new ResourcePackApplyListener(this),
                    this
            );
        }

        Objects.requireNonNull(getCommand("emojis"), "'emojis' command not registered")
                .setExecutor(new RootCommand(this).asExecutor());

        EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy =
                "clearRecipients".equals(getConfig().getString(""))
                        ? EventCancellationStrategy.removingRecipients()
                        : EventCancellationStrategy.cancellingDefault();

        Set<PluginHook> hooks = PluginHookManager.create()
                .registerHook(new EzChatHook(this, registry))
                .registerHook(new TownyChatHook(this, registry))
                .registerHook(new PlaceholderApiHook(this, registry))
                .registerHook(new DiscordSRVHook(registry))
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
                    cancellationStrategy,
                    getConfig().getBoolean("compat.use-paper-listener"),
                    getConfig().getBoolean("format.legacy.rich")
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

    public ExportService exportService() {
        return exportService;
    }

    public ResourcePack pack() {
        return resourcePack;
    }

    public void pack(ResourcePack resourcePack) {
        this.resourcePack = resourcePack;
    }

    public void updateResourcePackLocation(UrlAndHash location) {
        this.resourcePack = resourcePack.withLocation(
                location.url(),
                location.hash()
        );

        // for current players
        for (Player player : Bukkit.getOnlinePlayers()) {
            ResourcePackApplier.setResourcePack(player, resourcePack);
        }
    }

}
