package com.app.util;

import com.app.service.SeguridadService;
import com.app.model.EventoSeguridad;
import com.app.security.SessionManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Utilidad central para control de acceso: muestra alertas uniformes y registra eventos de seguridad
 */
public class AccessControlUtil {

    /**
     * Verifica permiso y, si falta, muestra advertencia y registra evento.
     * @param module módulo a verificar (ej. "configuracion")
     * @param action acción a verificar (ej. "read")
     * @param userMessage mensaje legible para mostrar al usuario
     * @return true si tiene permiso, false en caso contrario
     */
    public static boolean checkAndWarn(String module, String action, String userMessage) {
        SessionManager session = SessionManager.getInstance();
        boolean allowed = session.hasPermission(module, action);
        if (allowed) {
            return true;
        }

        // Registrar evento de acceso denegado
        try {
            SeguridadService.getInstance().registrarEvento(EventoSeguridad.TIPO_ACCESO,
                    "Intento de acceso denegado al módulo='" + module + "' acción='" + action + "'");
        } catch (Exception e) {
            // no bloquear por error en registro
            System.err.println("Error registrando evento de acceso denegado: " + e.getMessage());
        }

        // Mostrar alerta en JavaFX Application Thread (texto localizado)
        Platform.runLater(() -> {
            String title = com.app.service.LanguageService.getInstance().get("access.denied.title");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title != null ? title : "Acceso denegado");
            alert.setHeaderText(null);
            alert.setContentText(userMessage);
            alert.showAndWait();
        });

        return false;
    }

    /**
     * Verifica que el usuario sea administrador; si no, registra evento y muestra advertencia.
     * @param userMessage mensaje legible para mostrar al usuario
     * @return true si es admin, false en caso contrario
     */
    public static boolean checkAdminOrWarn(String userMessage) {
        SessionManager session = SessionManager.getInstance();
        boolean isAdmin = session.isAdmin();
        if (isAdmin) {
            return true;
        }

        try {
            SeguridadService.getInstance().registrarEvento(EventoSeguridad.TIPO_ACCESO,
                    "Intento de acceso denegado a área administrativa por usuario id='" +
                            (session.getCurrentUser() != null ? session.getCurrentUser().getId() : "-") + "'");
        } catch (Exception e) {
            System.err.println("Error registrando evento de acceso denegado (admin): " + e.getMessage());
        }

        Platform.runLater(() -> {
            String title = com.app.service.LanguageService.getInstance().get("access.denied.title");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title != null ? title : "Acceso denegado");
            alert.setHeaderText(null);
            alert.setContentText(userMessage);
            alert.showAndWait();
        });

        return false;
    }
}
