package com.app.security;

import com.app.model.Usuario;
import com.app.model.Rol;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static SessionManager instance;
    private Usuario currentUser;
    private Rol currentRole;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivityTime;
    private int sessionTimeoutMinutes = 30;
    private Map<String, Object> sessionData;

    private SessionManager() {
        this.sessionData = new ConcurrentHashMap<>();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public void startSession(Usuario user, Rol role) {
        this.currentUser = user;
        this.currentRole = role;
        this.loginTime = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
        this.sessionData.clear();
    }

    public void endSession() {
        this.currentUser = null;
        this.currentRole = null;
        this.loginTime = null;
        this.lastActivityTime = null;
        this.sessionData.clear();
    }

    public boolean isSessionActive() {
        if (currentUser == null) {
            return false;
        }

        if (lastActivityTime == null) {
            return false;
        }

        LocalDateTime expirationTime = lastActivityTime.plusMinutes(sessionTimeoutMinutes);
        return LocalDateTime.now().isBefore(expirationTime);
    }

    public void updateLastActivity() {
        this.lastActivityTime = LocalDateTime.now();
    }

    public Usuario getCurrentUser() {
        if (!isSessionActive()) {
            endSession();
            return null;
        }
        updateLastActivity();
        return currentUser;
    }

    public Rol getCurrentRole() {
        if (!isSessionActive()) {
            endSession();
            return null;
        }
        return currentRole;
    }

    public boolean hasPermission(String module, String action) {
        if (!isSessionActive() || currentRole == null) {
            return false;
        }
        return PermissionManager.hasPermission(currentRole, module, action);
    }

    public boolean isAdmin() {
        return currentRole != null && "ADMIN".equals(currentRole.getNombre());
    }

    public boolean isVendedor() {
        return currentRole != null && "VENDEDOR".equals(currentRole.getNombre());
    }

    public boolean isUsuario() {
        return currentRole != null && "USUARIO".equals(currentRole.getNombre());
    }

    public void setSessionData(String key, Object value) {
        sessionData.put(key, value);
    }

    public Object getSessionData(String key) {
        return sessionData.get(key);
    }

    public void removeSessionData(String key) {
        sessionData.remove(key);
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setSessionTimeoutMinutes(int minutes) {
        this.sessionTimeoutMinutes = minutes;
    }

    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }
}
