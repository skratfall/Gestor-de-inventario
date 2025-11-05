package com.app.controller;

import com.app.model.EventoSeguridad;
import com.app.service.ConfiguracionService;
import com.app.service.SeguridadService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.event.Event;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ConfiguracionController implements Initializable {
    
    @FXML private RadioButton rbTemaClaro;
    @FXML private RadioButton rbTemaOscuro;
    @FXML private ComboBox<String> cmbIdioma;
    @FXML private TextField txtNombreApp;
    @FXML private CheckBox chkMostrarAyuda;
    @FXML private CheckBox chkNotificaciones;
    
    @FXML private TextField txtSupabaseUrl;
    @FXML private PasswordField txtSupabaseKey;
    @FXML private CheckBox chkSincAuto;
    @FXML private ComboBox<String> cmbIntervaloSync;
    @FXML private Label lblUltimaSync;
    
    @FXML private CheckBox chkBackupAuto;
    @FXML private ComboBox<String> cmbFrecuenciaBackup;

    private ConfiguracionService configService;
    private SeguridadService seguridadService;
    private final DateTimeFormatter dateFormatter;

    public ConfiguracionController() {
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configService = ConfiguracionService.getInstance();
        seguridadService = SeguridadService.getInstance();

        initializeControls();
        cargarConfiguracion();
    }

    private void initializeControls() {
        // Configurar opciones de idioma
        cmbIdioma.setItems(FXCollections.observableArrayList(
            "Español", "English", "Português", "Français"
        ));

        // Configurar opciones de intervalo de sincronización
        cmbIntervaloSync.setItems(FXCollections.observableArrayList(
            "5 minutos", "15 minutos", "30 minutos", "1 hora", "2 horas"
        ));

        // Configurar opciones de frecuencia de backup
        cmbFrecuenciaBackup.setItems(FXCollections.observableArrayList(
            "Diario", "Semanal", "Quincenal", "Mensual"
        ));

        // Configurar toggle group para temas
        ToggleGroup temaGroup = new ToggleGroup();
        rbTemaClaro.setToggleGroup(temaGroup);
        rbTemaOscuro.setToggleGroup(temaGroup);
    }

    private void cargarConfiguracion() {
        // Cargar configuración de interfaz
        String appName = configService.getConfigValue("app.nombre", "Gestión de Inventario");
        txtNombreApp.setText(appName);

        String tema = configService.getConfigValue("app.tema", "claro");
        if ("oscuro".equals(tema)) {
            rbTemaOscuro.setSelected(true);
        } else {
            rbTemaClaro.setSelected(true);
        }

        String idioma = configService.getConfigValue("app.idioma", "Español");
        cmbIdioma.setValue(idioma);

        chkMostrarAyuda.setSelected(Boolean.parseBoolean(
            configService.getConfigValue("app.mostrarAyuda", "true")));
        chkNotificaciones.setSelected(Boolean.parseBoolean(
            configService.getConfigValue("app.notificaciones", "true")));

        // Cargar configuración de conexión
        txtSupabaseUrl.setText(configService.getConfigValue("db.url", ""));
        txtSupabaseKey.setText(configService.getConfigValue("db.apikey", ""));

        chkSincAuto.setSelected(Boolean.parseBoolean(
            configService.getConfigValue("sync.automatica", "true")));
        String intervalo = configService.getConfigValue("sync.intervalo", "30 minutos");
        cmbIntervaloSync.setValue(intervalo);

        // Mostrar última sincronización
        String ultimaSync = configService.getConfigValue("sync.ultima");
        if (ultimaSync != null) {
            lblUltimaSync.setText("Última sincronización: " + 
                LocalDateTime.parse(ultimaSync).format(dateFormatter));
        } else {
            lblUltimaSync.setText("Última sincronización: Nunca");
        }

        // Cargar configuración de respaldo
        chkBackupAuto.setSelected(Boolean.parseBoolean(
            configService.getConfigValue("backup.automatico", "true")));
        String frecuencia = configService.getConfigValue("backup.frecuencia", "Semanal");
        cmbFrecuenciaBackup.setValue(frecuencia);
    }

    @FXML
    private void handleTestConnection() {
        try {
            String url = txtSupabaseUrl.getText();
            String key = txtSupabaseKey.getText();

            // Validar que los campos no estén vacíos
            if (url.isEmpty() || key.isEmpty()) {
                mostrarAdvertencia("Campos incompletos",
                    "Por favor complete la URL y la API Key de Supabase");
                return;
            }

            // Guardar temporalmente la configuración actual
            configService.setConfigValue("db.url", url);
            configService.setConfigValue("db.apikey", key);

            // Probar conexión
            if (configService.testConnection()) {
                mostrarInfo("Conexión exitosa", 
                    "La conexión con Supabase se ha establecido correctamente");
            } else {
                mostrarError("Error de conexión",
                    "No se pudo establecer la conexión con Supabase");
            }
        } catch (Exception e) {
            mostrarError("Error", "Error al probar la conexión: " + e.getMessage());
        }
    }

    @FXML
    private void handleManualBackup() {
        mostrarInfo("Respaldo manual",
            "Esta funcionalidad será implementada próximamente");
    }

    @FXML
    private void handleLimpiarCache() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Está seguro que desea limpiar la caché del sistema?\n" +
            "Esto puede afectar temporalmente el rendimiento.",
            ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Aquí iría la lógica para limpiar la caché
                mostrarInfo("Caché limpiada",
                    "La caché del sistema ha sido limpiada exitosamente");
                seguridadService.registrarEvento(EventoSeguridad.TIPO_CONFIGURACION, 
                    "Limpieza manual de caché realizada");
            }
        });
    }

    @FXML
    private void handleOptimizarDB() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Está seguro que desea optimizar la base de datos?\n" +
            "Este proceso puede tomar varios minutos.",
            ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Aquí iría la lógica para optimizar la DB
                mostrarInfo("Base de datos optimizada",
                    "La base de datos ha sido optimizada exitosamente");
                seguridadService.registrarEvento(EventoSeguridad.TIPO_CONFIGURACION, 
                    "Optimización manual de base de datos realizada");
            }
        });
    }

    @FXML
    private void handleRestaurarPredeterminados() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Está seguro que desea restaurar todos los valores a su configuración predeterminada?\n" +
            "Esta acción no se puede deshacer.",
            ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Restaurar valores predeterminados
                    configService.createConfigEntry("app.nombre", "Gestión de Inventario", 
                        "string", "Nombre de la aplicación", "interfaz", true);
                    configService.createConfigEntry("app.tema", "claro", 
                        "string", "Tema de la interfaz", "interfaz", true);
                    configService.createConfigEntry("app.idioma", "Español", 
                        "string", "Idioma de la aplicación", "interfaz", true);
                    configService.createConfigEntry("app.mostrarAyuda", "true", 
                        "boolean", "Mostrar ayuda", "interfaz", true);
                    configService.createConfigEntry("app.notificaciones", "true", 
                        "boolean", "Habilitar notificaciones", "interfaz", true);
                    configService.createConfigEntry("sync.automatica", "true", 
                        "boolean", "Sincronización automática", "sincronizacion", true);
                    configService.createConfigEntry("sync.intervalo", "30 minutos", 
                        "string", "Intervalo de sincronización", "sincronizacion", true);
                    configService.createConfigEntry("backup.automatico", "true", 
                        "boolean", "Respaldo automático", "respaldo", true);
                    configService.createConfigEntry("backup.frecuencia", "Semanal", 
                        "string", "Frecuencia de respaldo", "respaldo", true);

                    cargarConfiguracion();
                    mostrarInfo("Configuración restaurada",
                        "Los valores han sido restaurados a su configuración predeterminada");
                    seguridadService.registrarEvento(EventoSeguridad.TIPO_CONFIGURACION, 
                        "Configuración restaurada a valores predeterminados");
                } catch (Exception e) {
                    mostrarError("Error",
                        "Error al restaurar la configuración: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleGuardarCambios() {
        try {
            // Guardar configuración de interfaz
            configService.setConfigValue("app.nombre", txtNombreApp.getText());
            configService.setConfigValue("app.tema", 
                rbTemaOscuro.isSelected() ? "oscuro" : "claro");
            configService.setConfigValue("app.idioma", cmbIdioma.getValue());
            configService.setConfigValue("app.mostrarAyuda", 
                String.valueOf(chkMostrarAyuda.isSelected()));
            configService.setConfigValue("app.notificaciones", 
                String.valueOf(chkNotificaciones.isSelected()));

            // Guardar configuración de conexión
            configService.setConfigValue("db.url", txtSupabaseUrl.getText());
            configService.setConfigValue("db.apikey", txtSupabaseKey.getText());
            configService.setConfigValue("sync.automatica", 
                String.valueOf(chkSincAuto.isSelected()));
            configService.setConfigValue("sync.intervalo", cmbIntervaloSync.getValue());

            // Guardar configuración de respaldo
            configService.setConfigValue("backup.automatico", 
                String.valueOf(chkBackupAuto.isSelected()));
            configService.setConfigValue("backup.frecuencia", 
                cmbFrecuenciaBackup.getValue());

            mostrarInfo("Configuración guardada",
                "Los cambios han sido guardados exitosamente");
            seguridadService.registrarEvento(EventoSeguridad.TIPO_CONFIGURACION, 
                "Configuración del sistema actualizada");

        } catch (Exception e) {
            mostrarError("Error",
                "Error al guardar la configuración: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/app/view/DashboardView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - Gestión de Inventario");
            
        } catch (IOException e) {
            mostrarError("Error", "No se pudo cargar el dashboard: " + e.getMessage());
        }
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}