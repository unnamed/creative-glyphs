package team.unnamed.emojis.resourcepack;

import net.kyori.adventure.key.Key;
import team.unnamed.creative.file.FileTree;
import team.unnamed.creative.file.FileTreeWriter;
import team.unnamed.creative.font.Font;
import team.unnamed.creative.font.FontProvider;
import team.unnamed.creative.texture.Texture;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Responsible for writing resources for the emojis
 * plugin.
 * @author yusshu (Andre Roldan)
 */
public class EmojisWriter implements FileTreeWriter {

    private final EmojiRegistry registry;

    public EmojisWriter(EmojiRegistry registry) {
        this.registry = registry;
    }

    /**
     * Transfers the resource pack information to the
     * given {@code output}
     *
     * <strong>Note that this method won't close the
     * given {@code output}</strong>
     */
    @Override
    public void write(FileTree tree) {

        Collection<Emoji> emojis = registry.values();
        List<FontProvider> providers = new ArrayList<>(emojis.size());

        for (Emoji emoji : emojis) {

            Key textureKey = Key.key(Key.MINECRAFT_NAMESPACE, "emojis/" + emoji.name());

            // write emoji image
            tree.write(
                    Texture.builder()
                            .key(textureKey)
                            .data(emoji.data())
                            .build()
            );

            providers.add(
                    FontProvider.bitMap()
                            .height(emoji.height())
                            .ascent(emoji.ascent())
                            .characters(emoji.replacement())
                            .file(textureKey)
                            .build()
            );
        }

        // write the default.json font
        tree.write(Font.of(Font.MINECRAFT_DEFAULT, providers));
    }

}
