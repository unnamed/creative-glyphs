package team.unnamed.emojis.export.impl;

import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.export.ResourceExporter;
import team.unnamed.emojis.io.ResourcePackWriter;
import team.unnamed.emojis.io.Streams;
import team.unnamed.emojis.io.TreeOutputStream;
import team.unnamed.emojis.resourcepack.UrlAndHash;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private boolean mergeZip;

    public FileExporter(File target) {
        this.target = target;
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
    public UrlAndHash export(ResourcePackWriter writer) throws IOException {
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

            try (TreeOutputStream output = TreeOutputStream.forZip(
                    new ZipOutputStream(new FileOutputStream(tmpTarget))
            )) {
                try (ZipInputStream input = new ZipInputStream(new FileInputStream(target))) {
                    ZipEntry entry;
                    while ((entry = input.getNextEntry()) != null) {
                        output.useEntry(entry.getName());
                        Streams.pipe(input, output);
                        output.closeEntry();
                    }
                }

                writer.write(output);
            }

            // delete old file
            if (!target.delete()) {
                throw new IOException("Cannot delete original ZIP file");
            }

            if (!tmpTarget.renameTo(target)) {
                throw new IOException("Cannot move temporary file to original ZIP file");
            }
        } else {
            try (TreeOutputStream output = TreeOutputStream.forZip(
                    new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target))))
            ) {
                writer.write(output);
            }
        }

        return null;
    }
}