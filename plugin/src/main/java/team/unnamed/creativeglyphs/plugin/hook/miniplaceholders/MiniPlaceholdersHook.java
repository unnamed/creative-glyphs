package team.unnamed.creativeglyphs.plugin.hook.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.plugin.Plugin;
import team.unnamed.creativeglyphs.Glyph;
import team.unnamed.creativeglyphs.plugin.hook.PluginHook;
import team.unnamed.creativeglyphs.plugin.PluginGlyphMap;

public final class MiniPlaceholdersHook implements PluginHook {
    private final PluginGlyphMap registry;

    public MiniPlaceholdersHook(final PluginGlyphMap registry) {
        this.registry = registry;
    }

    @Override
    public String pluginName() {
        return "MiniPlaceholders";
    }

    @Override
    public void hook(final Plugin hook) {
        Expansion.builder("unemoji")
                .globalPlaceholder("emoji", (queue, ctx) -> {
                    final Tag.Argument argument = queue.popOr("You need to provide an argument");
                    Glyph glyph = registry.getByName(argument.value());
                    if (glyph == null) {
                        return null;
                    } else {
                        return Tag.selfClosingInserting(Component.text(glyph.replacement()));
                    }
                })
                .build()
                .register();
    }
}
