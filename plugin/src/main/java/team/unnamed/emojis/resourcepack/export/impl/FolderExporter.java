package team.unnamed.emojis.resourcepack.export.impl;

import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.file.FileTree;
import team.unnamed.creative.file.FileTreeWriter;
import team.unnamed.emojis.resourcepack.UrlAndHash;
import team.unnamed.emojis.resourcepack.export.ResourceExporter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class FolderExporter implements ResourceExporter {

    private final File target;
    private final Logger logger;

    public FolderExporter(File target, Logger logger) {
        this.target = target;
        this.logger = logger;
    }

    @Override
    public @Nullable UrlAndHash export(FileTreeWriter writer) throws IOException {
        try (FileTree output = FileTree.directory(target)) {
            writer.write(output);
        }
        logger.info("Exported resource pack to folder: " + target);
        return null;
    }

}
