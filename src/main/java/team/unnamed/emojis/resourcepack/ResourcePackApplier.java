package team.unnamed.emojis.resourcepack;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import team.unnamed.emojis.util.Version;

import java.lang.reflect.Method;

/**
 * Utility for applying resource packs to specific
 * {@link Player} across all supported Bukkit,
 * Spigot and Paper versions
 *
 * @author yusshu (Andre Roldan)
 */
public final class ResourcePackApplier {

    private static Method SET_RESOURCE_PACK_METHOD;
    @Nullable private static Method GET_HANDLE_METHOD;

    static {
        try {
            try {
                SET_RESOURCE_PACK_METHOD = Player.class
                        .getDeclaredMethod("setResourcePack", String.class, String.class);

                // if there is a setResourcePack method in Player class
                // that accepts url and hash, we don't have to use internal
                // net.minecraft.server classes
                GET_HANDLE_METHOD = null;
            } catch (NoSuchMethodException ignored) {
                // no method found in bukkit, use nms,
                // don't worry about 1.17 naming, bukkit 1.17 should
                // already have setResourcePack(String, String) so
                // it won't reach this part
                Class<?> entityPlayerClass = Class.forName(
                        "net.minecraft.server." + Version.CURRENT + ".EntityPlayer"
                );
                Class<?> craftPlayerClass = Class.forName(
                        "org.bukkit.craftbukkit." + Version.CURRENT + ".entity.CraftPlayer"
                );

                GET_HANDLE_METHOD = craftPlayerClass.getDeclaredMethod("getHandle");
                SET_RESOURCE_PACK_METHOD = entityPlayerClass
                        .getDeclaredMethod("setResourcePack", String.class, String.class);
            }
        } catch (ReflectiveOperationException e) {
            // probably found an unsupported version of spigot
            throw new IllegalStateException(
                    "Cannot find setResourcePack method",
                    e
            );
        }
    }

    /**
     * Applies the given {@code resourcePack} to the specified {@code player},
     * if some property of {@code resourcePack} isn't available in current
     * server version, it's silently ignored (e.g. prompts in <1.17)
     *
     * @param player Player to apply resource pack
     * @param resourcePack The applied resource pack
     */
    public static void setResourcePack(Player player, ResourcePack resourcePack) {
        try {
            SET_RESOURCE_PACK_METHOD.invoke(
                    GET_HANDLE_METHOD == null
                            ? player
                            : GET_HANDLE_METHOD.invoke(player),
                    resourcePack.getUrl(),
                    resourcePack.getHash()
            );
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot apply resource pack",
                    e
            );
        }
    }

}
