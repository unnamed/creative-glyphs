package team.unnamed.emojis;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.emojis.command.EmojisCommand;
import team.unnamed.emojis.editor.EmojiImporter;
import team.unnamed.emojis.resourcepack.export.DefaultExportService;
import team.unnamed.emojis.resourcepack.export.ExportService;
import team.unnamed.emojis.hook.PluginHook;
import team.unnamed.emojis.hook.PluginHookManager;
import team.unnamed.emojis.hook.discordsrv.DiscordSRVHook;
import team.unnamed.emojis.hook.ezchat.EzChatHook;
import team.unnamed.emojis.hook.papi.PlaceholderApiHook;
import team.unnamed.emojis.hook.townychat.TownyChatHook;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.listener.EmojiCompletionsListener;
import team.unnamed.emojis.listener.EventBus;
import team.unnamed.emojis.listener.EventCancellationStrategy;
import team.unnamed.emojis.listener.ListenerFactory;
import team.unnamed.emojis.listener.ResourcePackApplyListener;
import team.unnamed.emojis.io.EmojiCodec;
import team.unnamed.emojis.io.MCEmojiCodec;
import team.unnamed.emojis.metrics.Metrics;
import team.unnamed.emojis.resourcepack.ResourcePack;
import team.unnamed.emojis.resourcepack.ResourcePackApplier;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class EmojisPlugin extends JavaPlugin {

    private EmojiRegistry registry;
    private ResourcePack resourcePack;

    private EmojiCodec codec;
    private EmojiImporter importer;

    private ExportService exportService;
    private File database;

    private File makeDatabase() throws IOException {
        File file = new File(getDataFolder(), "emojis.mcemoji");
        if (!file.exists()) {
            if (!file.createNewFile()) {
                // this should never happen because we already
                // checked for its existence with File#exists
                throw new IOException("Cannot create file, already created?");
            }

            try (OutputStream output = new FileOutputStream(file)) {
                try (InputStream input = getResource("emojis.mcemoji")) {
                    if (input != null) {
                        // if there's a default 'emojis.mcemoji'
                        // file in our resources, copy it
                        Streams.pipe(input, output);
                    } else {
                        // if there isn't, write zero emojis
                        // to the created file, so next reads
                        // don't fail
                        codec.write(output, Collections.emptySet());
                    }
                }
            }
        }
        return file;
    }

    public void loadEmojis() {
        try (InputStream input = new FileInputStream(database)) {
            registry.update(codec.read(input));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load emojis", e);
        }
        getLogger().info("Loaded " + registry.values().size() + " emojis.");
    }

    public void saveEmojis() {
        try (OutputStream output = new FileOutputStream(database)) {
            codec.write(output, registry.values());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save emojis", e);
        }
        getLogger().info("Saved " + registry.values().size() + " emojis.");
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.registry = new EmojiRegistry();
        this.codec = new MCEmojiCodec();
        this.importer = new EmojiImporter(this.codec);

        try {
            this.database = makeDatabase();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot create database file", e);
            this.setEnabled(false);
            return;
        }

        this.loadEmojis();

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
                .setExecutor(new EmojisCommand(this));

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
            eventBus.register(ListenerFactory.create(
                    this,
                    registry,
                    cancellationStrategy,
                    getConfig().getBoolean("compat.use-paper-listener"),
                    getConfig().getBoolean("format.legacy.rich")
            ), EventPriority.valueOf(getConfig().getString(
                    "compat.listener-priority",
                    "HIGHEST"
            ).toUpperCase()));
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

    public EmojiRegistry registry() {
        return registry;
    }

    public EmojiImporter importer() {
        return importer;
    }

    public EmojiCodec codec() {
        return codec;
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
