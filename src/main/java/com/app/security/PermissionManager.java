package com.app.security;

import com.app.model.Rol;
import com.google.gson.JsonObject;

public class PermissionManager {

    public static boolean hasPermission(Rol role, String module, String action) {
        if (role == null || role.getPermisos() == null) {
            return false;
        }

        try {
            JsonObject permisos = role.getPermisos();

            if (!permisos.has(module)) {
                return false;
            }

            JsonObject modulePerms = permisos.getAsJsonObject(module);

            if (!modulePerms.has(action)) {
                return false;
            }

            return modulePerms.get(action).getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean canCreateUser(Rol role) {
        return hasPermission(role, "usuarios", "create");
    }

    public static boolean canReadUser(Rol role) {
        return hasPermission(role, "usuarios", "read");
    }

    public static boolean canUpdateUser(Rol role) {
        return hasPermission(role, "usuarios", "update");
    }

    public static boolean canDeleteUser(Rol role) {
        return hasPermission(role, "usuarios", "delete");
    }

    public static boolean canManageProducts(Rol role) {
        return hasPermission(role, "productos", "create") ||
               hasPermission(role, "productos", "update");
    }

    public static boolean canManageSales(Rol role) {
        return hasPermission(role, "ventas", "create");
    }

    public static boolean canManageOrders(Rol role) {
        return hasPermission(role, "pedidos", "create") ||
               hasPermission(role, "pedidos", "update");
    }

    public static boolean canViewReports(Rol role) {
        return hasPermission(role, "reportes", "read");
    }

    public static boolean canManageConfiguration(Rol role) {
        return hasPermission(role, "configuracion", "update");
    }

    public static boolean canExecuteSync(Rol role) {
        return hasPermission(role, "sincronizacion", "execute");
    }
}
