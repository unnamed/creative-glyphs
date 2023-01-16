package team.unnamed.emojis.resourcepack.export;

import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.object.store.EmojiStore;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.Closeable;
import java.io.IOException;

/**
 * High-level resource-pack exporting
 * service, responsible for exporting the
 * existing loaded emojis
 */
public interface ExportService extends Closeable {

    /**
     * Exports the emojis in the given
     * {@code registry}
     * @return The url and hash for
     * the resource pack, null if not
     * exported to a remote location
     */
    @Nullable
    UrlAndHash export(EmojiStore registry);

    @Override
    default void close() throws IOException {
        // no-op by default
    }

}
