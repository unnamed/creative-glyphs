package team.unnamed.emojis.provider.head;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public final class Faces {

    public static final int FACE_HEIGHT = 8;
    public static final int FACE_WIDTH = 8;

    private static final String[] OLD_DEFAULT_SKINS = { "old_steve", "old_alex" };
    private static final String[] DEFAULT_SKINS = { "alex", "ari", "efe", "kai", "makena", "noor", "steve", "sunny", "zuri" };

    private static final int[][][] OLD_DEFAULT_FACES_COLORS = new int[OLD_DEFAULT_SKINS.length][][];
    private static final int[][][] DEFAULT_FACES_COLORS = new int[DEFAULT_SKINS.length][][];

    private Faces() {
    }

    public static int[][] getDefaultFace(UUID playerId, boolean pre1_19_3) {
        if (pre1_19_3) {
            // OLD DEFAULT SKINS
            int index = getOldDefaultSkin(playerId);
            int[][] colors = OLD_DEFAULT_FACES_COLORS[index];
            if (colors == null) {
                colors = loadFace(OLD_DEFAULT_SKINS[index]);
                OLD_DEFAULT_FACES_COLORS[index] = colors;
            }
            return colors;
        } else {
            // NEW DEFAULT SKINS
            int index = getDefaultSkin(playerId);
            int[][] colors = DEFAULT_FACES_COLORS[index];
            if (colors == null) {
                colors = loadFace(DEFAULT_SKINS[index]);
                DEFAULT_FACES_COLORS[index] = colors;
            }
            return colors;
        }
    }

    private static int[][] loadFace(String skinName) {

        URL faceUrl = Faces.class.getClassLoader().getResource("faces/" + skinName + ".png");

        if (faceUrl == null) {
            throw new IllegalStateException("No face data found for skin name: '" + skinName + "'");
        }

        // load image
        BufferedImage faceData;

        try {
            faceData = ImageIO.read(faceUrl);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read face data", e);
        }

        int[][] colors = new int[FACE_HEIGHT][FACE_WIDTH];
        for (int y = 0; y < FACE_HEIGHT; y++) {
            for (int x = 0; x < FACE_WIDTH; x++) {
                colors[y][x] = faceData.getRGB(x, y);
            }
        }

        return colors;
    }

    // 1.19.3+: We have nine default skins (Steve, Alex and seven more skins)
    private static int getDefaultSkin(UUID playerId) {
        int len = DEFAULT_SKINS.length;
        int type = Math.floorMod(playerId.hashCode(), len * 2);
        // We can know if the skin model will be slim using the following code:
        // boolean slim = type < len;
        return type % len;
    }

    // Before 1.19.3: We only have two default skins (Steve & Alex)
    private static int getOldDefaultSkin(UUID playerId) {
        // even: Steve, odd: Alex
        return playerId.hashCode() % 2;
    }

}
