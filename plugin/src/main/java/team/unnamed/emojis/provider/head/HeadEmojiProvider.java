package team.unnamed.emojis.provider.head;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.font.FontProvider;
import team.unnamed.creative.texture.Texture;
import team.unnamed.emojis.util.Version;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeadEmojiProvider {

    private static final String[] PIXELS_BY_HEIGHT = {
            string(0x10B00B),
            string(0x10B00C),
            string(0x10B00D),
            string(0x10B00E),
            string(0x10B00F),
            string(0x10B010),
            string(0x10B011),
            string(0x10B012)
    };
    private static final String OFFSET_MINUS_1 = string(0x10B013);
    private static final String OFFSET_MINUS_8 = string(0x10B014);

    private static final Map<UUID, int[][]> heads = new HashMap<>();
    
    private HeadEmojiProvider() {
    }

    public static void installResources(Collection<FontProvider> defaultFontProviders, ResourcePack resourcePack) {

        Key pixelTexture = Key.key("emojis", "emojiutil/pixel");

        // write pixel font providers
        for (int height = 0; height < Faces.FACE_HEIGHT; height++) {
            defaultFontProviders.add(
                    FontProvider.bitMap()
                            .file(pixelTexture)
                            .height(8)
                            .ascent(Faces.FACE_HEIGHT - height - 1)
                            .characters(PIXELS_BY_HEIGHT[height])
                            .build()
            );
        }

        // write offset font provider
        if (Version.CURRENT.minor() >= 19) {
            // 1.19 added space font providers
            defaultFontProviders.add(
                    FontProvider.space()
                            .advance(OFFSET_MINUS_1, -1)
                            .advance(OFFSET_MINUS_8, -8)
                            .build()
            );
        } else {
            // TODO:
            throw new UnsupportedOperationException("1.19+ required");
        }

        resourcePack.texture(
                Texture.builder()
                        .key(pixelTexture)
                        .data(Writable.resource(HeadEmojiProvider.class.getClassLoader(), "pixel.png"))
                        .build()
        );
    }

    public static Component of(Player player) {
        int[][] headColors = heads.computeIfAbsent(player.getUniqueId(), k -> findHead(player));
        TextComponent.Builder component = Component.text();
        for (int y = 0; y < Faces.FACE_HEIGHT; y++) {
            if (y != 0) {
                component.append(Component.text(OFFSET_MINUS_8));
            }
            for (int x = 0; x < Faces.FACE_WIDTH; x++) {
                int color = headColors[y][x];
                component.append(
                        Component.text()
                                .content(PIXELS_BY_HEIGHT[y])
                                .color(TextColor.color(color))
                );

                component.append(Component.text(OFFSET_MINUS_1));
            }
        }
        return component.build();
    }

    public static int[][] findHead(Player player) {
        for (ProfileProperty property : player.getPlayerProfile().getProperties()) {
            String name = property.getName();
            String value = property.getValue();

            if ("textures".equals(name)) {
                JsonObject skinJson = new JsonParser()
                        .parse(new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8))
                        .getAsJsonObject();

                String skinUrl = skinJson.getAsJsonObject("textures")
                        .getAsJsonObject("SKIN")
                        .get("url")
                        .getAsString();

                BufferedImage skinData;

                // read image
                try {
                    skinData = ImageIO.read(new URL(skinUrl));
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read skin data", e);
                }

                int[][] headColors = new int[Faces.FACE_HEIGHT][Faces.FACE_WIDTH];

                // head layer
                for (int x = 8; x < 16; x++) {
                    for (int y = 8; y < 16; y++) {
                        headColors[y - 8][x - 8] = skinData.getRGB(x, y);
                    }
                }

                // helmet layer
                for (int x = 40; x < 48; x++) {
                    for (int y = 8; y < 16; y++) {
                        int rgba = skinData.getRGB(x, y);
                        int alpha = (rgba >> 24) & 0xff;
                        if ((alpha) == 0xFF) {
                            headColors[y - 8][x - 40] = rgba;
                        }
                    }
                }

                return headColors;
            }
        }

        return Faces.getDefaultFace(player.getUniqueId(), true);
    }

    private static String string(int codePoint) {
        return new String(Character.toChars(codePoint));
    }

}
