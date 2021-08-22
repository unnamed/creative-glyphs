package team.unnamed.emojis.reader;

import team.unnamed.emojis.Emoji;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileTreeEmojiReader {

    /**
     * Pattern for file names, also for extracting the
     * emoji name (removing the file extension)
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^([A-Za-z_]{1,14})\\.mcemoji$");

    /**
     * Logger for this class, to log all important information.
     * Exceptions won't be logged by this logger, only warnings
     * and information.
     */
    private static final Logger LOGGER = Logger.getLogger(FileTreeEmojiReader.class.getSimpleName());

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
            Matcher matcher = NAME_PATTERN.matcher(file.getName());
            if (matcher.matches()) {
                try (InputStream input = new FileInputStream(file)) {
                    emojis.addAll(reader.read(input));
                }
            } else {
                LOGGER.warning("File '" + file.getName() + "' doesn't " +
                        "match the filename pattern: " + NAME_PATTERN.pattern());
            }
        }

        return emojis;
    }

}
