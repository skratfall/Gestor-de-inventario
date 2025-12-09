package com.app.security;

import com.app.dao.EventoSeguridadDAO;
import com.app.model.EventoSeguridad;
import com.app.service.SeguridadService;
import com.app.util.SessionLockScreen;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor global de monitoreo de inactividad
 * Controla una instancia única que funciona en TODAS las vistas
 * Activable/Desactivable desde panel de Seguridad
 */
public class InactivityMonitorManager {
    private static final Logger logger = LoggerFactory.getLogger(InactivityMonitorManager.class);
    private static InactivityMonitorManager instance;
    
    private InactivityMonitor currentMonitor;
    private SeguridadService seguridadService;
    private SessionManager sessionManager;
    private boolean isMonitoringEnabled;
    private List<MonitoringStateListener> stateListeners = new ArrayList<>();
    
    private InactivityMonitorManager() {
        this.seguridadService = SeguridadService.getInstance();
        this.sessionManager = SessionManager.getInstance();
        this.isMonitoringEnabled = seguridadService.isInactivityMonitoringEnabled();
        logger.info("✓ InactivityMonitorManager inicializado - Monitoreo: {}", 
            isMonitoringEnabled ? "ACTIVO" : "INACTIVO");
    }
    
    public static InactivityMonitorManager getInstance() {
        if (instance == null) {
            synchronized (InactivityMonitorManager.class) {
                if (instance == null) {
                    instance = new InactivityMonitorManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Registra una escena para monitoreo de inactividad
     * Se llama desde cada controller cuando se abre una vista
     */
    public void registerScene(Scene scene, Stage stage) {
        if (!isMonitoringEnabled) {
            logger.debug("Monitoreo deshabilitado - escena no registrada");
            return;
        }
        
        // Detener monitor anterior si existe
        if (currentMonitor != null) {
            currentMonitor.stopMonitoring();
        }
        
        // Crear nuevo monitor para esta escena
        currentMonitor = new InactivityMonitor(sessionManager);
        currentMonitor.startMonitoring(scene);
        
        // Configurar listeners
        setupMonitorListeners(stage);
        
        logger.info("✓ Escena registrada para monitoreo de inactividad");
    }
    
    /**
     * Desregistra la escena actual del monitoreo
     * Se llama cuando se cierra una vista
     */
    public void unregisterScene() {
        if (currentMonitor != null) {
            currentMonitor.stopMonitoring();
            currentMonitor = null;
            logger.debug("Escena desregistrada del monitoreo");
        }
    }
    
    /**
     * Activa/Desactiva el monitoreo de inactividad globalmente
     * Se llama desde SeguridadController cuando cambia el checkbox
     */
    public void setMonitoringEnabled(boolean enabled) {
        this.isMonitoringEnabled = enabled;
        
        if (enabled) {
            logger.info("✓ Monitoreo de inactividad ACTIVADO globalmente");
            // Si hay una escena registrada, iniciar monitor
            notifyStateChange(true);
        } else {
            logger.info("✓ Monitoreo de inactividad DESACTIVADO globalmente");
            // Detener monitor actual
            if (currentMonitor != null) {
                currentMonitor.stopMonitoring();
            }
            notifyStateChange(false);
        }
        
        // Guardar en preferencias
        seguridadService.setInactivityMonitoringEnabled(enabled);
    }
    
    /**
     * Verifica si el monitoreo está habilitado
     */
    public boolean isMonitoringEnabled() {
        return isMonitoringEnabled;
    }
    
    /**
     * Obtiene el monitor actual (para testing o debug)
     */
    public InactivityMonitor getCurrentMonitor() {
        return currentMonitor;
    }
    
    /**
     * Configura listeners de inactividad en el monitor actual
     */
    private void setupMonitorListeners(Stage stage) {
        if (currentMonitor == null) return;
        
        currentMonitor.addInactivityListener(new InactivityMonitor.InactivityListener() {
            @Override
            public void onInactivityWarning(long minutesRemaining) {
                logger.warn("⚠️ Alerta de inactividad: {} minutos restantes", minutesRemaining);
                
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("⚠️ Sesión Expirando");
                alert.setHeaderText("Por seguridad, su sesión está a punto de expirar");
                alert.setContentText(
                    "Tiempo restante: " + minutesRemaining + " minuto(s)\n\n" +
                    "Por favor, continúe usando la aplicación para mantener su sesión activa."
                );
                alert.setOnCloseRequest(e -> {
                    // Registrar que el usuario vio la advertencia
                    currentMonitor.recordActivity("ALERT_VIEWED");
                });
                alert.showAndWait();
            }
            
            @Override
            public void onSessionLocked(String reason) {
                logger.warn("🔒 Sesión bloqueada por: {}", reason);
                
                SessionLockScreen lockScreen = new SessionLockScreen(stage);
                lockScreen.show(reason);
            }
        });
    }
    
    /**
     * Agrega listener para cambios de estado de monitoreo
     */
    public void addStateListener(MonitoringStateListener listener) {
        stateListeners.add(listener);
    }
    
    /**
     * Notifica a todos los listeners sobre cambio de estado
     */
    private void notifyStateChange(boolean enabled) {
        stateListeners.forEach(listener -> listener.onMonitoringStateChanged(enabled));
    }
    
    /**
     * Interface para escuchar cambios de estado del monitoreo
     */
    public interface MonitoringStateListener {
        void onMonitoringStateChanged(boolean enabled);
    }
    
    /**
     * Limpia recursos cuando la aplicación se cierra
     */
    public void shutdown() {
        if (currentMonitor != null) {
            currentMonitor.stopMonitoring();
        }
        logger.info("✓ InactivityMonitorManager shutdown");
    }
}
