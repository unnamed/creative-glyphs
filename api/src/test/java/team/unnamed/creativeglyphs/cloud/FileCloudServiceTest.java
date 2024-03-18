package team.unnamed.creativeglyphs.cloud;

import org.jetbrains.annotations.NotNull;
import team.unnamed.creative.base.Writable;

import static org.junit.jupiter.api.Assertions.*;

class FileCloudServiceTest {
    // Manually run this test
    // @org.junit.jupiter.api.Test
    void test_artemis() throws Exception {
        test(FileCloudService.artemis());
    }

    // @org.junit.jupiter.api.Test
    void test_mango() throws Exception {
        test(FileCloudService.mango());
    }

    void test(final @NotNull FileCloudService cloud) throws Exception {
        final var data = Writable.stringUtf8("Hello world!");
        final var id = cloud.upload(data);
        System.out.println("Uploaded, ID is " + id);
        final var downloadedData = cloud.download(id);
        assertNotNull(downloadedData, "Downloaded data is null");
        assertArrayEquals(
                data.toByteArray(),
                downloadedData.readAsByteArray(),
                "Downloaded data is not the same as the uploaded data"
        );
    }
}
