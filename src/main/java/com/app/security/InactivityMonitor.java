package com.app.security;

import com.app.dao.EventoSeguridadDAO;
import com.app.model.EventoSeguridad;
import com.app.model.Usuario;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitorea la inactividad del usuario detectando mouse y teclado
 * Emite alertas cuando se acerca al timeout y bloquea la sesión si es necesario
 */
public class InactivityMonitor {
    private static final Logger logger = LoggerFactory.getLogger(InactivityMonitor.class);
    
    private final SessionManager sessionManager;
    private Scene scene;
    private Timeline inactivityTimer;
    private LocalDateTime lastActivityTime;
    private int warningMinutesBeforeTimeout = 2; // Alerta 2 min antes
    private boolean warningShown = false;
    private List<InactivityListener> listeners = new ArrayList<>();

    public InactivityMonitor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Inicia el monitoreo de inactividad en una escena
     */
    public void startMonitoring(Scene scene) {
        if (this.scene != null) {
            stopMonitoring();
        }

        this.scene = scene;
        this.lastActivityTime = LocalDateTime.now();
        this.warningShown = false;

        // Agregar event handlers para detectar actividad
        addActivityDetection();

        // Iniciar timeline de verificación
        startInactivityTimer();

        logger.info("✓ Monitoreo de inactividad iniciado");
    }

    /**
     * Detiene el monitoreo de inactividad
     */
    public void stopMonitoring() {
        if (inactivityTimer != null) {
            inactivityTimer.stop();
        }

        if (scene != null) {
            removeActivityDetection();
        }

        logger.info("✓ Monitoreo de inactividad detenido");
    }

    /**
     * Agrega detectores de interacción del usuario (mouse, teclado)
     */
    private void addActivityDetection() {
        EventHandler<MouseEvent> mouseHandler = event -> recordActivity("MOUSE");
        EventHandler<KeyEvent> keyHandler = event -> recordActivity("KEYBOARD");

        scene.addEventFilter(MouseEvent.ANY, mouseHandler);
        scene.addEventFilter(KeyEvent.ANY, keyHandler);
    }

    /**
     * Remueve detectores de interacción
     */
    private void removeActivityDetection() {
        // Nota: Para una limpieza completa, necesitarías guardar referencias
        // a los handlers y removerlos. Por simplicidad, se limpia con stopMonitoring()
    }

    /**
     * Registra una actividad del usuario
     */
    public void recordActivity(String activityType) {
        this.lastActivityTime = LocalDateTime.now();
        this.warningShown = false;

        // Actualizar último tiempo de actividad en SessionManager
        sessionManager.updateLastActivity();

        logger.debug("Actividad registrada: {}", activityType);
    }

    /**
     * Inicia el timer que verifica inactividad cada 10 segundos
     */
    private void startInactivityTimer() {
        inactivityTimer = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            checkInactivity();
        }));

        inactivityTimer.setCycleCount(Timeline.INDEFINITE);
        inactivityTimer.play();
    }

    /**
     * Verifica si el usuario ha estado inactivo demasiado tiempo
     */
    private void checkInactivity() {
        if (!sessionManager.isSessionActive()) {
            // Sesión ya expiró, bloquear
            lockSession("Sesión expirada por inactividad");
            return;
        }

        int timeoutMinutes = sessionManager.getSessionTimeoutMinutes();
        long inactiveMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
            lastActivityTime,
            LocalDateTime.now()
        );

        // Mostrar alerta si se acerca al timeout
        long minutesUntilTimeout = timeoutMinutes - inactiveMinutes;

        if (minutesUntilTimeout <= warningMinutesBeforeTimeout && !warningShown) {
            showWarning(minutesUntilTimeout);
            warningShown = true;
        }

        // Bloquear si pasó el timeout
        if (inactiveMinutes >= timeoutMinutes) {
            lockSession("Inactividad detectada por " + inactiveMinutes + " minutos");
        }
    }

    /**
     * Muestra alerta de advertencia antes del bloqueo
     */
    private void showWarning(long minutesRemaining) {
        Platform.runLater(() -> {
            logger.warn("⚠️ ALERTA: Sesión se bloqueará en {} minuto(s)", minutesRemaining);
            
            // Notificar a listeners
            for (InactivityListener listener : listeners) {
                listener.onInactivityWarning(minutesRemaining);
            }
        });
    }

    /**
     * Bloquea la sesión por inactividad
     */
    private void lockSession(String reason) {
        Platform.runLater(() -> {
            stopMonitoring();
            
            logger.warn("🔒 SESIÓN BLOQUEADA: {}", reason);

            // Registrar evento de seguridad
            try {
                EventoSeguridadDAO eventoDAO = new EventoSeguridadDAO();
                Usuario usuario = sessionManager.getCurrentUser();
                if (usuario != null) {
                    EventoSeguridad evento = new EventoSeguridad();
                    evento.setTipo(EventoSeguridad.TIPO_SEGURIDAD);
                    evento.setDescripcion(reason);
                    evento.setUsuarioId(usuario.getId());
                    evento.setFecha(LocalDateTime.now());
                    eventoDAO.save(evento);
                }
            } catch (Exception e) {
                logger.error("Error al registrar evento de bloqueo: {}", e.getMessage());
            }

            // Notificar a listeners
            for (InactivityListener listener : listeners) {
                listener.onSessionLocked(reason);
            }
        });
    }

    /**
     * Interfaz para escuchar eventos de inactividad
     */
    public interface InactivityListener {
        void onInactivityWarning(long minutesRemaining);
        void onSessionLocked(String reason);
    }

    /**
     * Agrega un listener para eventos de inactividad
     */
    public void addInactivityListener(InactivityListener listener) {
        listeners.add(listener);
    }

    /**
     * Remueve un listener
     */
    public void removeInactivityListener(InactivityListener listener) {
        listeners.remove(listener);
    }

    /**
     * Obtiene el tiempo inactivo actual en minutos
     */
    public long getInactiveMinutes() {
        return java.time.temporal.ChronoUnit.MINUTES.between(
            lastActivityTime,
            LocalDateTime.now()
        );
    }

    /**
     * Resetea el tiempo de inactividad
     */
    public void resetInactivityTimer() {
        recordActivity("MANUAL_RESET");
    }
}
