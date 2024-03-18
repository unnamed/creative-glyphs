package team.unnamed.creativeglyphs.cloud;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.base.Readable;
import team.unnamed.creative.base.Writable;

public interface FileCloudService {
    @NotNull String upload(final @NotNull Writable writable);

    @Nullable Readable download(final @NotNull String id);

    static @NotNull FileCloudService artemis() {
        return BaseFileCloudService.ARTEMIS;
    }

    static @NotNull FileCloudService mango() {
        return BaseFileCloudService.MANGO;
    }
}
