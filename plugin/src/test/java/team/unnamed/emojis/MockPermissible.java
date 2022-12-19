package team.unnamed.emojis;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MockPermissible implements Permissible {

    private final Set<String> permissions;

    public MockPermissible(Set<String> permissions) {
        this.permissions = permissions;
    }

    public MockPermissible(String... permissions) {
        this.permissions = new HashSet<>(Arrays.asList(permissions));
    }


    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return permissions.contains(name);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return permissions.contains(perm.getName());
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        if (name.isEmpty()) {
            return true;
        }
        return permissions.contains(name);
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return permissions.contains(perm.getName());
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return new PermissionAttachment(plugin, this);
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return new PermissionAttachment(plugin, this);
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {
    }

    @NotNull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {
    }

}
