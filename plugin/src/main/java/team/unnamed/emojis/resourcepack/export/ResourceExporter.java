package team.unnamed.emojis.resourcepack.export;

import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.file.FileTreeWriter;
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
    UrlAndHash export(FileTreeWriter writer) throws IOException;

}