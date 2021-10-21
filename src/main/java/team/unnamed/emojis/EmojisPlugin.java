package team.unnamed.emojis;

import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.emojis.command.EmojisCommand;
import team.unnamed.emojis.download.EmojiImporter;
import team.unnamed.emojis.export.DefaultExportService;
import team.unnamed.emojis.export.ExportService;
import team.unnamed.emojis.export.RemoteResource;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.format.MiniMessageEmojiComponentProvider;
import team.unnamed.emojis.hook.PluginHook;
import team.unnamed.emojis.hook.PluginHookManager;
import team.unnamed.emojis.hook.ezchat.EzChatHook;
import team.unnamed.emojis.hook.papi.PlaceholderApiHook;
import team.unnamed.emojis.io.EmojiWriter;
import team.unnamed.emojis.io.MCEmojiWriter;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.listener.EventBus;
import team.unnamed.emojis.listener.EventCancellationStrategy;
import team.unnamed.emojis.listener.ListenerFactory;
import team.unnamed.emojis.listener.ResourcePackApplyListener;
import team.unnamed.emojis.io.EmojiReader;
import team.unnamed.emojis.io.MCEmojiReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class EmojisPlugin extends JavaPlugin {

    private EmojiRegistry registry;
    private RemoteResource resource;

    private EmojiReader reader;
    private EmojiWriter writer;
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
                        writer.write(output, Collections.emptySet());
                    }
                }
            }
        }
        return file;
    }

    public void loadEmojis() {
        try (InputStream input = new FileInputStream(database)) {
            registry.update(reader.read(input));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load emojis", e);
        }
        getLogger().info("Loaded " + registry.values().size() + " emojis.");
    }

    public void saveEmojis() {
        try (OutputStream output = new FileOutputStream(database)) {
            writer.write(output, registry.values());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save emojis", e);
        }
        getLogger().info("Saved " + registry.values().size() + " emojis.");
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.registry = new EmojiRegistry();
        this.reader = new MCEmojiReader();
        this.writer = new MCEmojiWriter();
        this.importer = new EmojiImporter(this.reader);

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
        this.resource = exportService.export(registry);

        EventBus eventBus = EventBus.create(this);

        if (resource != null) {
            Bukkit.getPluginManager().registerEvents(
                    new ResourcePackApplyListener(this),
                    this
            );
        }

        Objects.requireNonNull(getCommand("emojis"), "'emojis' command not registered")
                .setExecutor(new EmojisCommand(this));

        EmojiComponentProvider emojiComponentProvider = new MiniMessageEmojiComponentProvider(getConfig());
        EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy =
                "clearRecipients".equals(getConfig().getString(""))
                        ? EventCancellationStrategy.removingRecipients()
                        : EventCancellationStrategy.cancellingDefault();

        Set<PluginHook> hooks = PluginHookManager.create()
                .registerHook(new EzChatHook(this, registry, emojiComponentProvider))
                .registerHook(new PlaceholderApiHook(this, registry))
                .hook();

        if (hooks.stream().noneMatch(hook -> hook instanceof PluginHook.Chat)) {
            // if no chat plugin hooks, let's register our own listener
            eventBus.register(ListenerFactory.create(
                    registry,
                    new MiniMessageEmojiComponentProvider(getConfig()),
                    cancellationStrategy,
                    getConfig().getBoolean("compat.use-paper-listener"),
                    getConfig().getBoolean("format.legacy.rich")
            ), EventPriority.valueOf(getConfig().getString(
                    "compat.listener-priority",
                    "HIGHEST"
            ).toUpperCase()));
        }
    }

    public EmojiRegistry getRegistry() {
        return registry;
    }

    public EmojiImporter getImporter() {
        return importer;
    }

    public EmojiReader getReader() {
        return reader;
    }

    public ExportService getExportService() {
        return exportService;
    }

    public RemoteResource getRemoteResource() {
        return resource;
    }

    public void setRemoteResource(RemoteResource resource) {
        this.resource = resource;
    }
}
