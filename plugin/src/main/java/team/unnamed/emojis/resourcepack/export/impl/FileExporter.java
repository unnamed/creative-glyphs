package team.unnamed.emojis.resourcepack.export.impl;

import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.file.FileTree;
import team.unnamed.creative.file.FileTreeWriter;
import team.unnamed.emojis.resourcepack.export.ResourceExporter;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Fluent-style class for exporting resource
 * packs to {@link File}s
 */
public class FileExporter
        implements ResourceExporter {

    private final File target;
    private final Logger logger;
    private boolean mergeZip;

    public FileExporter(File target, Logger logger) {
        this.target = target;
        this.logger = logger;
    }

    /**
     * Set to true if the export must open a
     * {@link ZipOutputStream} if the {@code target}
     * file exists. If it exists, it will read its
     * entries and put them in the output
     */
    public FileExporter setMergeZip(boolean mergeZip) {
        this.mergeZip = mergeZip;
        return this;
    }

    @Override
    @Nullable
    public UrlAndHash export(FileTreeWriter writer) throws IOException {
        if (!target.exists() && !target.createNewFile()) {
            throw new IOException("Failed to create target resource pack file");
        }
        if (mergeZip && target.exists()) {

            File tmpTarget = new File(
                    target.getParentFile(),
                    Long.toHexString(System.nanoTime()) + ".tmp"
            );

            if (!tmpTarget.createNewFile()) {
                throw new IOException(
                        "Cannot generate temporary file to write the merged output"
                );
            }

            try (FileTree tree = FileTree.zip(new ZipOutputStream(new FileOutputStream(tmpTarget)))) {
                try (ZipInputStream input = new ZipInputStream(new FileInputStream(target))) {
                    ZipEntry entry;
                    while ((entry = input.getNextEntry()) != null) {
                        tree.write(entry.getName(), w -> Streams.pipe(input, w));
                    }
                }

                writer.write(tree);
            }

            // delete old file
            if (!target.delete()) {
                throw new IOException("Cannot delete original ZIP file");
            }

            if (!tmpTarget.renameTo(target)) {
                throw new IOException("Cannot move temporary file to original ZIP file");
            }
        } else {
            try (FileTree output = FileTree.zip(
                    new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target))))
            ) {
                writer.write(output);
            }
        }

        logger.info("Exported resource-pack to file: " + target);
        return null;
    }
}