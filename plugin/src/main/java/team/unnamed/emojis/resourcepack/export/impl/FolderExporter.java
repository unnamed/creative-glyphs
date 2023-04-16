package team.unnamed.emojis.resourcepack.export.impl;

import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter;
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
    public void export(ResourcePack resourcePack) throws IOException {
        MinecraftResourcePackWriter.minecraft().writeToDirectory(target, resourcePack);
        logger.info("Exported resource pack to folder: " + target);
    }

}
