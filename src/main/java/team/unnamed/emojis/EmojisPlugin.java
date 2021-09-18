package team.unnamed.emojis;

import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.emojis.command.EmojisCommand;
import team.unnamed.emojis.export.DefaultExportService;
import team.unnamed.emojis.export.ExportService;
import team.unnamed.emojis.export.RemoteResource;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.format.MiniMessageEmojiComponentProvider;
import team.unnamed.emojis.hook.PluginHook;
import team.unnamed.emojis.hook.PluginHookManager;
import team.unnamed.emojis.hook.ezchat.EzChatHook;
import team.unnamed.emojis.listener.EventBus;
import team.unnamed.emojis.listener.EventCancellationStrategy;
import team.unnamed.emojis.listener.ListenerFactory;
import team.unnamed.emojis.listener.ResourcePackApplyListener;
import team.unnamed.emojis.io.reader.EmojiReader;
import team.unnamed.emojis.io.reader.FileTreeEmojiReader;
import team.unnamed.emojis.io.reader.MCEmojiReader;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class EmojisPlugin extends JavaPlugin {

    private EmojiRegistry registry;
    private RemoteResource resource;
    private EmojiReader reader;
    private ExportService exportService;

    private void loadEmojis() {
        File folder = new File(getDataFolder(), "emojis");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Cannot create emojis folder");
        }

        try {
            new FileTreeEmojiReader(reader)
                    .read(folder)
                    .forEach(registry::add);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load emojis", e);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {

        saveDefaultConfig();

        this.registry = new EmojiRegistry();
        this.reader = new MCEmojiReader();

        this.loadEmojis();

        // export
        this.exportService = new DefaultExportService(this);
        this.resource = exportService.export(registry);

        EventBus eventBus = EventBus.create(this);

        if (resource != null && getConfig().getBoolean("pack.export.upload.apply")) {
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
                .hook();

        if (hooks.stream().noneMatch(hook -> hook instanceof PluginHook.Chat)) {
            // if no chat plugin hooks, let's register our own listener
            eventBus.register(ListenerFactory.create(
                    registry,
                    new MiniMessageEmojiComponentProvider(getConfig()),
                    cancellationStrategy,
                    getConfig().getBoolean("format.legacy.rich")
            ));
        }
    }

    public EmojiRegistry getRegistry() {
        return registry;
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
