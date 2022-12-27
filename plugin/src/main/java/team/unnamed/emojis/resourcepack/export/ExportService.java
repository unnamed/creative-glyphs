package team.unnamed.emojis.resourcepack.export;

import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.resourcepack.UrlAndHash;

/**
 * High-level resource-pack exporting
 * service, responsible for exporting the
 * existing loaded emojis
 */
public interface ExportService {

    /**
     * Exports the emojis in the given
     * {@code registry}
     * @return The url and hash for
     * the resource pack, null if not
     * exported to a remote location
     */
    @Nullable
    UrlAndHash export(EmojiRegistry registry);

}
