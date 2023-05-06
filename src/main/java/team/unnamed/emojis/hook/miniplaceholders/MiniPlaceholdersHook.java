package team.unnamed.emojis.hook.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.plugin.Plugin;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.hook.PluginHook;
import team.unnamed.emojis.object.store.EmojiStore;

public final class MiniPlaceholdersHook implements PluginHook {
    private final EmojiStore registry;

    public MiniPlaceholdersHook(final EmojiStore registry) {
        this.registry = registry;
    }

    @Override
    public String getPluginName() {
        return "MiniPlaceholders";
    }

    @Override
    public void hook(final Plugin hook) {
        Expansion.builder("unemoji")
                .globalPlaceholder("emoji", (queue, ctx) -> {
                    final Tag.Argument argument = queue.popOr("You need to provide an argument");
                    Emoji emoji = registry.get(argument.value());
                    if (emoji == null) {
                        return null;
                    } else {
                        return Tag.selfClosingInserting(Component.text(emoji.replacement()));
                    }
                })
                .build()
                .register();
    }
}
