package team.unnamed.emojis.io.reader;

import team.unnamed.emojis.Emoji;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class FileTreeEmojiReader {

    /**
     * The delegated emoji reader, this is the actual responsible
     * for reading the files
     */
    private final EmojiReader reader;

    public FileTreeEmojiReader(EmojiReader reader) {
        this.reader = reader;
    }

    public Collection<Emoji> read(File folder) throws IOException {

        if (!folder.exists()) {
            // if folder doesn't exist just return
            return Collections.emptySet();
        }

        File[] files = folder.listFiles(File::isFile);

        if (files == null) {
            // this should never happen but check anyways
            return Collections.emptySet();
        }

        Collection<Emoji> emojis = new HashSet<>();

        for (File file : files) {
            try (InputStream input = new FileInputStream(file)) {
                emojis.addAll(reader.read(input));
            }
        }

        return emojis;
    }

}
