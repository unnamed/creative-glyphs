package team.unnamed.creativeglyphs.format;

import org.bukkit.permissions.Permissible;
import team.unnamed.creativeglyphs.Emoji;

import javax.annotation.Nullable;
import java.util.function.Predicate;

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
            String permission = emoji.permission();
            return permission.isEmpty() || permissible.hasPermission(permission);
        }
    }

    public static Predicate<Emoji> permissionTest(Permissible permissible) {
        return emoji -> {
            String permission = emoji.permission();
            return permission.isEmpty() || permissible.hasPermission(permission);
        };
    }

}
