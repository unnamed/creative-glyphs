package team.unnamed.emojis.export;

import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.io.AssetWriter;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.IOException;

/**
 * Interface for exporting resources packs
 */
public interface ResourceExporter {

    /**
     * Exports the data written by the
     * given {@code writer}
     */
    @Nullable
    UrlAndHash export(AssetWriter writer) throws IOException;

}