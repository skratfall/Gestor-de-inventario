package com.app.service;

import com.app.dao.EventoSeguridadDAO;
import com.app.model.EventoSeguridad;
import com.app.model.Usuario;
import com.app.security.SessionManager;
import javafx.scene.control.Alert;
import java.time.LocalDateTime;
import java.util.List;
import java.util.prefs.Preferences;

public class SeguridadService {
    private static SeguridadService instance;
    private final EventoSeguridadDAO eventoDAO;
    private final Preferences preferences;
    private final SessionManager sessionManager;

    private static final String PREF_2FA_ENABLED = "2fa.enabled";
    private static final String PREF_SESSION_TIMEOUT = "session.timeout";
    private static final String PREF_MAX_LOGIN_ATTEMPTS = "login.maxAttempts";
    private static final String PREF_AUDIT_ACCESS = "audit.access";
    private static final String PREF_AUDIT_ROLES = "audit.roles";
    private static final String PREF_AUDIT_CONFIG = "audit.config";

    private SeguridadService() {
        this.eventoDAO = new EventoSeguridadDAO();
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
            Usuario usuarioActual = sessionManager.getCurrentUser();
            EventoSeguridad evento = new EventoSeguridad(
                LocalDateTime.now(),
                tipo,
                usuarioActual != null ? usuarioActual.getId() : null,
                descripcion
            );
            eventoDAO.save(evento);
        } catch (Exception e) {
            mostrarError("Error al registrar evento", e.getMessage());
        }
    }

    public List<EventoSeguridad> obtenerEventos() {
        return eventoDAO.findAll();
    }

    public List<EventoSeguridad> obtenerEventosPorTipo(String tipo) {
        return eventoDAO.findByTipo(tipo);
    }

    public List<EventoSeguridad> obtenerEventosPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        return eventoDAO.findByFechaRange(desde, hasta);
    }

    public void desbloquearUsuarios() {
        // Aquí iría la lógica para desbloquear usuarios bloqueados
        registrarEvento(EventoSeguridad.TIPO_SEGURIDAD, "Desbloqueo manual de usuarios");
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

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}