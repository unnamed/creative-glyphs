package team.unnamed.emojis.io;

import java.io.IOException;
import java.util.Arrays;

/**
 * Responsible for writing assets for a Minecraft resource pack,
 * it writes the information into a {@link TreeOutputStream}
 * @author yusshu (Andre Roldan)
 */
public interface AssetWriter {

    /**
     * Writes the resource pack assets into
     * the given tree {@code output}
     *
     * <strong>Implementations of this method
     * should not close the provided {@code output}
     * </strong>
     */
    void write(TreeOutputStream output) throws IOException;

    /**
     * Creates a {@link AssetWriter} instance compound
     * by other {@code writers}. The {@link AssetWriter#write}
     * method is invoked for all the given writers
     */
    static AssetWriter compose(Iterable<? extends AssetWriter> writers) {
        return output -> {
            for (AssetWriter writer : writers) {
                writer.write(output);
            }
        };
    }

    /**
     * Creates a {@link AssetWriter} instance compound
     * by other {@code writers}. The {@link AssetWriter#write}
     * method is invoked for all the given writers
     */
    static AssetWriter compose(AssetWriter... writers) {
        return compose(Arrays.asList(writers));
    }

}