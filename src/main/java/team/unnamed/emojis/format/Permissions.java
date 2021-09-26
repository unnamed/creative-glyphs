package team.unnamed.emojis.format;

import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.Emoji;

import javax.annotation.Nullable;

public final class Permissions {

    private Permissions() {
    }

    public static boolean canUse(
            Permissible permissible,
            @Nullable Emoji emoji
    ) {
        if (emoji == null) {
            return false;
        } else {
            String permission = emoji.getPermission();
            return permission.isEmpty() || permissible.hasPermission(permission);
        }
    }

}
