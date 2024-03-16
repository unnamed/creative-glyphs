package team.unnamed.creativeglyphs.plugin.command;

import me.fixeddev.commandflow.annotated.AnnotatedCommandTreeBuilder;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.annotated.part.defaults.DefaultsModule;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.factory.BukkitModule;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creativeglyphs.plugin.CreativeGlyphsPlugin;

import static java.util.Objects.requireNonNull;

public final class CommandService {
    private final CreativeGlyphsPlugin plugin;

    public CommandService(final @NotNull CreativeGlyphsPlugin plugin) {
        this.plugin = requireNonNull(plugin, "plugin");
    }

    public void start() {
        final var manager = new BukkitCommandManager(plugin.getName());
        final var partInjector = PartInjector.create();

        partInjector.install(new DefaultsModule());
        partInjector.install(new BukkitModule());

        final var builder = AnnotatedCommandTreeBuilder.create(partInjector);

        manager.registerCommands(builder.fromClass(new GlyphsCommand(plugin)));
    }
}
