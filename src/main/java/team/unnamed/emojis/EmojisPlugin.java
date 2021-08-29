package team.unnamed.emojis;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.emojis.command.EmojisCommand;
import team.unnamed.emojis.export.DefaultExportService;
import team.unnamed.emojis.export.ExportService;
import team.unnamed.emojis.export.RemoteResource;
import team.unnamed.emojis.hook.ezchat.EzChatHook;
import team.unnamed.emojis.listener.ChatListener;
import team.unnamed.emojis.listener.ResourcePackApplyListener;
import team.unnamed.emojis.reader.EmojiReader;
import team.unnamed.emojis.reader.FileTreeEmojiReader;
import team.unnamed.emojis.reader.MCEmojiReader;

import java.io.File;
import java.io.IOException;

public class EmojisPlugin extends JavaPlugin {

    private EmojiRegistry registry;
    private RemoteResource resource;
    private EmojiReader reader;
    private ExportService exportService;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.registry = new EmojiRegistry();
        this.reader = new MCEmojiReader(); //new MCEmojiReader();

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

        // export
        this.exportService = new DefaultExportService(this);
        this.resource = exportService.export(registry);

        if (resource != null && getConfig().getBoolean("pack.export.upload.apply")) {
            Bukkit.getPluginManager().registerEvents(
                    new ResourcePackApplyListener(this),
                    this
            );
        }

        getCommand("emojis").setExecutor(new EmojisCommand(this));

        new EzChatHook(this, registry).init();
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
