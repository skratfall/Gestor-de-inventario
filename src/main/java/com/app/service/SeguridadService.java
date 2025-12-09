package com.app.service;

import com.app.dao.EventoSeguridadDAO;
import com.app.model.EventoSeguridad;
import com.app.model.Usuario;
import com.app.security.SessionManager;
import javafx.scene.control.Alert;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

public class SeguridadService {
    private static SeguridadService instance;
    private EventoSeguridadDAO eventoDAO; // Cambiar de final a no-final
    private final Preferences preferences;
    private final SessionManager sessionManager;

    private static final String PREF_2FA_ENABLED = "2fa.enabled";
    private static final String PREF_SESSION_TIMEOUT = "session.timeout";
    private static final String PREF_MAX_LOGIN_ATTEMPTS = "login.maxAttempts";
    private static final String PREF_AUDIT_ACCESS = "audit.access";
    private static final String PREF_AUDIT_ROLES = "audit.roles";
    private static final String PREF_AUDIT_CONFIG = "audit.config";
    private static final String PREF_INACTIVITY_MONITORING_ENABLED = "inactivity.monitoring.enabled";

    private SeguridadService() {
        try {
            this.eventoDAO = new EventoSeguridadDAO();
        } catch (Exception e) {
            System.err.println("⚠️ Advertencia: No se pudo inicializar EventoSeguridadDAO. Algunos eventos no se registrarán: " + e.getMessage());
            this.eventoDAO = null; // Permitir inicialización sin BD
        }
        this.preferences = Preferences.userNodeForPackage(SeguridadService.class);
        this.sessionManager = SessionManager.getInstance();
    }

    public static SeguridadService getInstance() {
        if (instance == null) {
            instance = new SeguridadService();
        }
        return instance;
    }

    // Configuración de 2FA
    public void set2FAEnabled(boolean enabled) {
        preferences.putBoolean(PREF_2FA_ENABLED, enabled);
        registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, 
            "Configuración 2FA " + (enabled ? "activada" : "desactivada"));
    }

    public boolean is2FAEnabled() {
        return preferences.getBoolean(PREF_2FA_ENABLED, false);
    }

    // Configuración de bloqueo de sesión
    public void setSessionTimeout(int minutes) {
        preferences.putInt(PREF_SESSION_TIMEOUT, minutes);
        // Actualizar SessionManager en tiempo real
        sessionManager.setSessionTimeoutMinutes(minutes);
        registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, 
            "Tiempo de bloqueo de sesión establecido a " + minutes + " minutos");
    }

    public int getSessionTimeout() {
        return preferences.getInt(PREF_SESSION_TIMEOUT, 30); // 30 minutos por defecto
    }

    // Configuración de intentos de login
    public void setMaxLoginAttempts(int attempts) {
        preferences.putInt(PREF_MAX_LOGIN_ATTEMPTS, attempts);
        registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, 
            "Intentos máximos de login establecidos a " + attempts);
    }

    public int getMaxLoginAttempts() {
        return preferences.getInt(PREF_MAX_LOGIN_ATTEMPTS, 3); // 3 intentos por defecto
    }
    
    // Control de intentos fallidos por usuario
    public void registrarIntentoFallido(String username) {
        try {
            String key = "login.attempts." + username;
            int currentAttempts = preferences.getInt(key, 0);
            preferences.putInt(key, currentAttempts + 1);
            
            // Registrar en auditoría
            registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, 
                "Intento de login fallido para usuario: " + username + 
                " (Intento " + (currentAttempts + 1) + ")");
                
            // Si se alcanzó el límite, bloquear usuario
            if (currentAttempts + 1 >= getMaxLoginAttempts()) {
                bloquearUsuario(username);
                registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, 
                    "Usuario bloqueado por exceso de intentos fallidos: " + username);
            }
        } catch (Exception e) {
            System.err.println("Error registrando intento fallido: " + e.getMessage());
        }
    }
    
    public void restablecerIntentosUsuario(String username) {
        try {
            String key = "login.attempts." + username;
            preferences.remove(key);
            desbloquearUsuarioTemp(username);
        } catch (Exception e) {
            System.err.println("Error restableciendo intentos: " + e.getMessage());
        }
    }
    
    public int obtenerIntentosRestantes(String username) {
        try {
            String key = "login.attempts." + username;
            int currentAttempts = preferences.getInt(key, 0);
            int maxAttempts = getMaxLoginAttempts();
            return Math.max(0, maxAttempts - currentAttempts);
        } catch (Exception e) {
            return getMaxLoginAttempts();
        }
    }
    
    public boolean estaUsuarioBloqueado(String username) {
        try {
            String key = "blocked." + username;
            return preferences.getBoolean(key, false);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void bloquearUsuario(String username) {
        try {
            String key = "blocked." + username;
            preferences.putBoolean(key, true);
            
            // Guardar tiempo de bloqueo para futuras referencias
            String timeKey = "blocked.time." + username;
            preferences.putLong(timeKey, System.currentTimeMillis());
        } catch (Exception e) {
            System.err.println("Error bloqueando usuario: " + e.getMessage());
        }
    }
    
    private void desbloquearUsuarioTemp(String username) {
        try {
            String key = "blocked." + username;
            preferences.remove(key);
            
            String timeKey = "blocked.time." + username;
            preferences.remove(timeKey);
        } catch (Exception e) {
            System.err.println("Error desbloqueando usuario temporal: " + e.getMessage());
        }
    }

    // Configuración de auditorías
    public void setAuditAccess(boolean enabled) {
        preferences.putBoolean(PREF_AUDIT_ACCESS, enabled);
    }

    public boolean isAuditAccessEnabled() {
        return preferences.getBoolean(PREF_AUDIT_ACCESS, true);
    }

    public void setAuditRoles(boolean enabled) {
        preferences.putBoolean(PREF_AUDIT_ROLES, enabled);
    }

    public boolean isAuditRolesEnabled() {
        return preferences.getBoolean(PREF_AUDIT_ROLES, true);
    }

    public void setAuditConfig(boolean enabled) {
        preferences.putBoolean(PREF_AUDIT_CONFIG, enabled);
    }

    public boolean isAuditConfigEnabled() {
        return preferences.getBoolean(PREF_AUDIT_CONFIG, true);
    }

    // Gestión de eventos
    public void registrarEvento(String tipo, String descripcion) {
        try {
            if (eventoDAO == null) {
                // Base de datos no disponible, pero no mostrar alerta
                return;
            }
            Usuario usuarioActual = sessionManager.getCurrentUser();
            EventoSeguridad evento = new EventoSeguridad(
                LocalDateTime.now(),
                tipo,
                usuarioActual != null ? usuarioActual.getId() : null,
                descripcion
            );
            eventoDAO.save(evento);
        } catch (Exception e) {
            // No mostrar alertas de eventos no registrados durante login
            System.err.println("⚠️ No se pudo registrar evento: " + e.getMessage());
        }
    }

    public List<EventoSeguridad> obtenerEventos() {
        if (eventoDAO == null) {
            return new ArrayList<>();
        }
        return eventoDAO.findAll();
    }

    public List<EventoSeguridad> obtenerEventosPorTipo(String tipo) {
        if (eventoDAO == null) {
            return new ArrayList<>();
        }
        return eventoDAO.findByTipo(tipo);
    }

    public List<EventoSeguridad> obtenerEventosPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        if (eventoDAO == null) {
            return new ArrayList<>();
        }
        return eventoDAO.findByFechaRange(desde, hasta);
    }

    public void desbloquearUsuarios() {
        try {
            // Limpiar todos los usuarios bloqueados
            Arrays.stream(preferences.keys())
                .filter(key -> key.startsWith("blocked.") && !key.startsWith("blocked.time."))
                .forEach(preferences::remove);
            
            // Limpiar intentos fallidos
            Arrays.stream(preferences.keys())
                .filter(key -> key.startsWith("login.attempts."))
                .forEach(preferences::remove);
            
            // Limpiar tiempos de bloqueo
            Arrays.stream(preferences.keys())
                .filter(key -> key.startsWith("blocked.time."))
                .forEach(preferences::remove);
                
            registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, "Desbloqueo manual de todos los usuarios");
        } catch (Exception e) {
            System.err.println("Error desbloqueando usuarios: " + e.getMessage());
        }
    }

    public void reiniciarConfiguracion() {
        preferences.put(PREF_2FA_ENABLED, "false");
        preferences.putInt(PREF_SESSION_TIMEOUT, 30);
        preferences.putInt(PREF_MAX_LOGIN_ATTEMPTS, 3);
        preferences.putBoolean(PREF_AUDIT_ACCESS, true);
        preferences.putBoolean(PREF_AUDIT_ROLES, true);
        preferences.putBoolean(PREF_AUDIT_CONFIG, true);
        
        registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, 
            "Configuración de seguridad restablecida a valores predeterminados");
    }

    // Configuración de monitoreo de inactividad
    public void setInactivityMonitoringEnabled(boolean enabled) {
        preferences.putBoolean(PREF_INACTIVITY_MONITORING_ENABLED, enabled);
        registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, 
            "Monitoreo de inactividad " + (enabled ? "activado" : "desactivado"));
    }

    public boolean isInactivityMonitoringEnabled() {
        return preferences.getBoolean(PREF_INACTIVITY_MONITORING_ENABLED, true); // Habilitado por defecto
    }

    private void mostrarError(String titulo, String mensaje) {
        // No mostrar alertas de error durante la operación
        // Loguear en consola en su lugar
        System.err.println("❌ " + titulo + ": " + mensaje);
    }
}