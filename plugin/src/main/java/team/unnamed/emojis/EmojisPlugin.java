package team.unnamed.emojis;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.emojis.command.EmojisCommand;
import team.unnamed.emojis.download.EmojiImporter;
import team.unnamed.emojis.export.DefaultExportService;
import team.unnamed.emojis.export.ExportService;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.format.DefaultEmojiComponentProvider;
import team.unnamed.emojis.hook.PluginHook;
import team.unnamed.emojis.hook.PluginHookManager;
import team.unnamed.emojis.hook.discordsrv.DiscordSRVHook;
import team.unnamed.emojis.hook.essentialsdiscord.EssentialsDiscordHook;
import team.unnamed.emojis.hook.ezchat.EzChatHook;
import team.unnamed.emojis.hook.papi.PlaceholderApiHook;
import team.unnamed.emojis.hook.townychat.TownyChatHook;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.listener.EventBus;
import team.unnamed.emojis.listener.EventCancellationStrategy;
import team.unnamed.emojis.listener.ListenerFactory;
import team.unnamed.emojis.listener.ResourcePackApplyListener;
import team.unnamed.emojis.io.EmojiCodec;
import team.unnamed.emojis.io.MCEmojiCodec;
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
            String prompt = getConfig().getString("application.prompt");
            if (prompt != null) {
                prompt = ChatColor.translateAlternateColorCodes('&', prompt);
                // TODO: refactor this ugly code
                prompt = ComponentSerializer.toString(TextComponent.fromLegacyText(prompt));
            }

            this.resourcePack = new ResourcePack(
                    location.url(),
                    location.hash(),
                    getConfig().getBoolean("feature.require-pack"),
                    prompt
            );
            Bukkit.getPluginManager().registerEvents(
                    new ResourcePackApplyListener(this),
                    this
            );
        }

        Objects.requireNonNull(getCommand("emojis"), "'emojis' command not registered")
                .setExecutor(new EmojisCommand(this));

        EmojiComponentProvider emojiComponentProvider = new DefaultEmojiComponentProvider(getConfig());
        EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy =
                "clearRecipients".equals(getConfig().getString(""))
                        ? EventCancellationStrategy.removingRecipients()
                        : EventCancellationStrategy.cancellingDefault();

        Set<PluginHook> hooks = PluginHookManager.create()
                .registerHook(new EzChatHook(this, registry, emojiComponentProvider))
                .registerHook(new TownyChatHook(this, registry))
                .registerHook(new PlaceholderApiHook(this, registry))
                .registerHook(new DiscordSRVHook(registry))
                .registerHook(new EssentialsDiscordHook(this, registry))
                .hook();

        if (hooks.stream().noneMatch(hook -> hook instanceof PluginHook.Chat)) {
            // if no chat plugin hooks, let's register our own listener
            eventBus.register(ListenerFactory.create(
                    this,
                    registry,
                    new DefaultEmojiComponentProvider(getConfig()),
                    cancellationStrategy,
                    getConfig().getBoolean("compat.use-paper-listener"),
                    getConfig().getBoolean("format.legacy.rich")
            ), EventPriority.valueOf(getConfig().getString(
                    "compat.listener-priority",
                    "HIGHEST"
            ).toUpperCase()));
        }

        Class<?> playerJoinListenerClass;
        try {
            playerJoinListenerClass = Class.forName("team.unnamed.emojis.compat.java17.EmojiCompletionsListener");

            // check if methods required to make completions work exist
            // (they may not exist in Spigot)
            Player.class.getDeclaredMethod("addAdditionalChatCompletions", Collection.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // do not make it work
            playerJoinListenerClass = null;
        }

        if (playerJoinListenerClass != null) {
            try {
                Listener listener = (Listener) playerJoinListenerClass
                        .getDeclaredConstructor(EmojiRegistry.class)
                        .newInstance(registry);
                getServer().getPluginManager().registerEvents(listener, this);
            } catch (ReflectiveOperationException e) {
                getLogger().log(Level.SEVERE, "Failed to instantiate" +
                        " EmojiCompletionsListener, no completions available", e);
            }
        }
    }

    public EmojiRegistry getRegistry() {
        return registry;
    }

    public EmojiImporter getImporter() {
        return importer;
    }

    public EmojiCodec getCodec() {
        return codec;
    }

    public ExportService getExportService() {
        return exportService;
    }

    public ResourcePack getResourcePack() {
        return resourcePack;
    }

    public void setResourcePack(ResourcePack resourcePack) {
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
