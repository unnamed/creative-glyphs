package team.unnamed.emojis.resourcepack.export;

import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.ResourcePack;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface for exporting resources packs
 */
public interface ResourceExporter extends Closeable {

    /**
     * Exports the given ResourcePack
     */
    void export(ResourcePack resourcePack) throws IOException;

    default @Nullable UrlAndHash location() {
        return null;
    }

    @Override
    default void close() throws IOException {
        // no-op by default
    }

}