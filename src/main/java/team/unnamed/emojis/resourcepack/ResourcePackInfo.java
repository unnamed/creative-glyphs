package team.unnamed.emojis.resourcepack;

import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.io.Streamable;

/**
 * Resource pack information, contains extra information
 * that can be omitted by a {@link Streamable} writer
 * @author yusshu (Andre Roldan)
 */
public class ResourcePackInfo {

    private final int format;
    private final String description;
    @Nullable
    private final Streamable icon;

    /**
     * Constructs a new resource pack info object
     * @param format The pack format, depends on minecraft version
     * @param description The resource-pack description
     * @param icon The resource-pack icon, it won't be written if
     *             it's null
     */
    public ResourcePackInfo(
            int format,
            String description,
            @Nullable Streamable icon
    ) {
        this.format = format;
        this.description = description;
        this.icon = icon;
    }

    public int getFormat() {
        return format;
    }

    public String getDescription() {
        return description;
    }

    @Nullable
    public Streamable getIcon() {
        return icon;
    }

}