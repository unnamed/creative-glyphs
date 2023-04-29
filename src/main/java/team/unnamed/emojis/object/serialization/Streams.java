package team.unnamed.emojis.object.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for working with
 * {@link InputStream}s and {@link OutputStream}s
 * @author yusshu (Andre Roldan)
 */
public final class Streams {

    /**
     * Determines the length of the buffer
     * used in the {@link Streams#pipe}
     * operation
     */
    private static final int BUFFER_LENGTH = 1024;

    private Streams() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Reads and writes the data from the
     * given {@code input} to the given {@code output}
     * by using a fixed-size byte buffer
     * (fastest way)
     *
     * <p>Note that this method doesn't close
     * the inputs or outputs</p>
     *
     * @throws IOException If an error occurs while
     * reading or writing the data
     */
    public static void pipe(
            InputStream input,
            OutputStream output
    ) throws IOException {
        byte[] buffer = new byte[BUFFER_LENGTH];
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
    }

    /**
     * Writes the given {@code string} into
     * the specified {@code output} using the
     * UTF-8 charset
     * @throws IOException If an error occurs
     * while writing the string
     */
    public static void writeUTF(
            OutputStream output,
            String string
    ) throws IOException {
        byte[] data = string.getBytes(StandardCharsets.UTF_8);
        output.write(data, 0, data.length);
    }

    /**
     * Reads a string from the given {@code input}.
     * The resulting string must have the given
     * {@code length}.
     */
    public static String readString(
            InputStream input,
            int length
    ) throws IOException {
        char[] data = new char[length];
        for (int i = 0; i < length; i++) {
            int byte1 = input.read();
            int byte2 = input.read();
            if ((byte1 | byte2) < 0) {
                // if byte1 or byte2 is -1
                // we reached the eof
                throw new EOFException();
            } else {
                data[i] = (char) ((byte1 << 8) + byte2);
            }
        }
        return new String(data);
    }

}