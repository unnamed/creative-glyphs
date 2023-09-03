package team.unnamed.creativeglyphs.plugin.listener.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import team.unnamed.creativeglyphs.content.ContentProcessor;
import team.unnamed.creativeglyphs.map.GlyphMap;
import team.unnamed.creativeglyphs.plugin.util.Permissions;

import java.util.List;

public class AnvilEditListener implements Listener {


    private final ContentProcessor<Component> processor = ContentProcessor.component(glyph -> Component.text()
            .content(glyph.replacement())
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE) // <--- Remove italics!
            .build());
    private final GlyphMap glyphMap;

    public AnvilEditListener(GlyphMap glyphMap) {
        this.glyphMap = glyphMap;
    }

    @EventHandler
    public void onAnvilUse(PrepareAnvilEvent event) {
        ItemStack item = event.getResult();

        if (item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        Component displayName = meta.displayName();
        if (displayName == null) {
            return;
        }

        List<HumanEntity> viewers = event.getViewers();
        Component newDisplayName = processor.process(displayName, glyphMap, glyph -> {
            // if any of the viewers can use the glyph,
            // let them use it. it seems like it is not
            // possible to check who made the change
            for (HumanEntity viewer : viewers) {
                if (Permissions.canUse(viewer, glyph)) {
                    return true;
                }
            }
            return false;
        });
        meta.displayName(newDisplayName);
        item.setItemMeta(meta);
        event.setResult(item);
    }

}
