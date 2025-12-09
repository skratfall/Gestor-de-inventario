package com.app.controller;

import com.app.security.InactivityMonitorManager;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Base controller class with common functionality for all controllers
 * Maneja el registro automático de monitoreo de inactividad para todas las vistas
 */
public abstract class BaseController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
    
    protected Stage currentStage;
    protected Scene currentScene;

    /**
     * Método final de inicialización que registra monitoreo automáticamente
     * Los controllers pueden sobrescribir initializeController() para su lógica custom
     */
    @Override
    public final void initialize(URL location, ResourceBundle resources) {
        // Obtener la escena después de que el controller esté completamente cargado
        Platform.runLater(() -> {
            try {
                // Llamar a lógica custom del controller
                initializeController();
                
                // Registrar esta vista para monitoreo de inactividad
                registerForInactivityMonitoring();
                
            } catch (Exception e) {
                logger.error("Error en inicialización del controller", e);
            }
        });
    }
    
    /**
     * Registra la vista actual para monitoreo de inactividad
     * Se llama automáticamente desde initialize()
     */
    protected void registerForInactivityMonitoring() {
        try {
            // Obtener la escena y stage desde el primer nodo disponible en la jerarquía
            // Esto se hace en una subclase específica que tiene acceso a @FXML nodes
            logger.debug("Listo para registrar monitoreo de inactividad");
        } catch (Exception e) {
            logger.error("Error registrando monitoreo de inactividad", e);
        }
    }
    
    /**
     * Método que deben implementar los controladores para su lógica de inicialización
     */
    public abstract void initializeController();
    
    /**
     * Método helper para registrar una escena con el monitor de inactividad
     * Llamar desde cada controller específico que tenga acceso a @FXML nodes
     */
    protected void registerSceneForMonitoring(Scene scene, Stage stage) {
        try {
            InactivityMonitorManager manager = InactivityMonitorManager.getInstance();
            if (manager.isMonitoringEnabled()) {
                manager.registerScene(scene, stage);
                logger.info("✓ {} registrado en gestor de monitoreo", this.getClass().getSimpleName());
            }
        } catch (Exception e) {
            logger.error("Error registrando escena para monitoreo", e);
        }
    }
    
    /**
     * Método helper para desregistrar una escena
     * Llamar cuando se cierra la vista
     */
    protected void unregisterSceneFromMonitoring() {
        try {
            InactivityMonitorManager.getInstance().unregisterScene();
            logger.debug("✓ {} desregistrado del gestor de monitoreo", this.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Error desregistrando escena del monitoreo", e);
        }
    }

    /**
     * Show an information alert dialog
     */
    protected void showInfoAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Show an error alert dialog
     */
    protected void showErrorAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Show a warning alert dialog
     */
    protected void showWarningAlert(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    /**
     * Generic method to show alert dialogs
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}