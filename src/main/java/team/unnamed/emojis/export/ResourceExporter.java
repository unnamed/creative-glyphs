package team.unnamed.emojis.export;

import org.jetbrains.annotations.Nullable;
import team.unnamed.hephaestus.resourcepack.ResourcePackWriter;

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
    RemoteResource export(ResourcePackWriter writer) throws IOException;

}