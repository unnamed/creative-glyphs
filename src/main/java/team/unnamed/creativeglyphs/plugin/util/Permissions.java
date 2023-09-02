package team.unnamed.creativeglyphs.plugin.util;

import org.bukkit.permissions.Permissible;
import team.unnamed.creativeglyphs.Glyph;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class Permissions {

    private Permissions() {
    }

    public static boolean canUse(
            Permissible permissible,
            @Nullable Glyph glyph
    ) {
        if (glyph == null) {
            return false;
        } else {
            String permission = glyph.permission();
            return permission.isEmpty() || permissible.hasPermission(permission);
        }
    }

    public static Predicate<Glyph> permissionTest(Permissible permissible) {
        return glyph -> {
            String permission = glyph.permission();
            return permission.isEmpty() || permissible.hasPermission(permission);
        };
    }

}
