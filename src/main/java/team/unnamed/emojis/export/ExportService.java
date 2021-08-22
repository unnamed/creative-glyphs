package team.unnamed.emojis.export;

import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.EmojiRegistry;

/**
 * High-level resource-pack exporting
 * service, responsible for exporting the
 * existing loaded emojis
 */
public interface ExportService {

    /**
     * Exports the emojis in the given
     * {@code registry}
     * @return The remote location for
     * the resource pack, null if not
     * exported to a remote location
     */
    @Nullable
    RemoteResource export(EmojiRegistry registry);

}
