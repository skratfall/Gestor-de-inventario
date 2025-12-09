package com.app.controller;

import com.app.dao.RolDAO;
import com.app.dao.SupabaseDatabaseConnection;
import com.app.dao.UsuarioDAOImpl;
import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.security.SessionManager;
import com.app.service.AuthenticationService;
import com.app.service.ConfiguracionService;
import com.app.service.SyncService;
import com.app.service.ThemeService;
import com.app.service.LanguageService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tooltip;
import com.app.util.AccessControlUtil;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController extends BaseController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label lblUserRole;
    @FXML private Label lblCurrentUser;
    @FXML private Label lblLastLogin;
    @FXML private Label lblCurrentDate;
    @FXML private Label lblCurrentTime;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalRoles;
    @FXML private Label lblConnectionStatus;
    @FXML private Label lblConnectionText;
    @FXML private Label lblConnectionDetails;
    @FXML private Label lblLastSync;
    @FXML private Label lblSessionDuration;
    @FXML private Label lblConfigStatus;

    @FXML private Label lblSystemVersion;
    @FXML private Label lblDatabaseInfo;
    @FXML private Label lblServerInfo;
    @FXML private Label lblEnvironment;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsuarios;
    @FXML private Button btnRoles;
    @FXML private Button btnSync;
    @FXML private Button btnConfiguracion;
    @FXML private Button btnSeguridad;
    @FXML private Button btnLogout;

    private SessionManager sessionManager;
    private AuthenticationService authService;
    private UsuarioDAOImpl usuarioDAO;
    private RolDAO rolDAO;
    private SyncService syncService;
    private ConfiguracionService configService;

    private Timeline clockTimeline;
    private Timeline sessionTimeline;
    private LocalDateTime sessionStartTime;

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
    private DateTimeFormatter loginFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        initializeController();
        loadDashboardData();
        startClockUpdates();
        setupPermissions();
        setupLanguageListener();
        
        // Registrar la escena actual con ThemeService cuando se cargue
        javafx.application.Platform.runLater(() -> {
            Scene currentScene = welcomeLabel.getScene();
            if (currentScene != null) {
                ThemeService.getInstance().registerScene(currentScene);
            }
        });
        
        // Registrar listener para cambios de idioma
        LanguageService.getInstance().addLanguageChangeListener(newLanguage -> {
            System.out.println("✅ Idioma cambiado a: " + newLanguage);
        });
    }

    private void initializeServices() {
        sessionManager = SessionManager.getInstance();
        authService = AuthenticationService.getInstance();
        usuarioDAO = new UsuarioDAOImpl();
        rolDAO = new RolDAO();
        syncService = SyncService.getInstance();
        configService = ConfiguracionService.getInstance();
        sessionStartTime = sessionManager.getLoginTime();
    }

    @Override
    public void initializeController() {
        LanguageService langService = LanguageService.getInstance();
        Usuario currentUser = sessionManager.getCurrentUser();
        Rol currentRole = sessionManager.getCurrentRole();

        if (currentUser != null) {
            String nombreCompleto = currentUser.getNombreCompleto() != null && !currentUser.getNombreCompleto().isEmpty() ?
                currentUser.getNombreCompleto() : currentUser.getUsername();

            welcomeLabel.setText(langService.get("dashboard.bienvenido") + ", " + nombreCompleto);
            lblCurrentUser.setText(langService.get("usuarios.usuario") + ": " + currentUser.getUsername());

            if (currentUser.getUltimoAcceso() != null) {
                lblLastLogin.setText(langService.get("dashboard.ultima_sincro") + ": " + currentUser.getUltimoAcceso().format(loginFormatter));
            } else {
                lblLastLogin.setText("Primera sesión");
            }
        } else {
            welcomeLabel.setText(langService.get("dashboard.bienvenido") + " al Sistema");
            lblCurrentUser.setText(langService.get("usuarios.usuario") + ": Invitado");
            lblLastLogin.setText("Sin sesión activa");
        }

        if (currentRole != null) {
            lblUserRole.setText(currentRole.getNombre());
        } else {
            lblUserRole.setText("Sin rol asignado");
        }

        updateDateTime();
        loadSystemInfo();
    }

    private void loadDashboardData() {
        loadUserStatistics();
        checkDatabaseConnection();
        loadSyncStatus();
        loadConfigurationStatus();
    }

    private void loadUserStatistics() {
        try {
            List<Usuario> usuarios = usuarioDAO.findAll();
            long activeUsers = usuarios.stream().filter(Usuario::isActivo).count();
            lblTotalUsers.setText(String.valueOf(activeUsers));

            List<Rol> roles = rolDAO.findAll();
            lblTotalRoles.setText(String.valueOf(roles.size()));

        } catch (Exception e) {
            lblTotalUsers.setText("Error");
            lblTotalRoles.setText("Error");
            System.err.println("Error loading user statistics: " + e.getMessage());
        }
    }

    private void checkDatabaseConnection() {
        actualizarEstadoConexion();
    }

    private void actualizarEstadoConexion() {
        try {
            SupabaseDatabaseConnection dbConnection = SupabaseDatabaseConnection.getInstance();
            boolean isConnected = dbConnection.testConnection();

            if (isConnected) {
                lblConnectionStatus.setText("●");
                lblConnectionStatus.setStyle("-fx-font-size: 20px; -fx-text-fill: #27ae60;");
                lblConnectionText.setText("Conectado");
                lblConnectionDetails.setText("Base de datos operativa");
            } else {
                lblConnectionStatus.setText("●");
                lblConnectionStatus.setStyle("-fx-font-size: 20px; -fx-text-fill: #e74c3c;");
                lblConnectionText.setText("Desconectado");
                lblConnectionDetails.setText("Sin conexión a la base de datos");
            }
        } catch (Exception e) {
            lblConnectionStatus.setText("●");
            lblConnectionStatus.setStyle("-fx-font-size: 20px; -fx-text-fill: #f39c12;");
            lblConnectionText.setText("Error");
            lblConnectionDetails.setText("Error al verificar conexión");
            System.err.println("Error checking database connection: " + e.getMessage());
        }
    }

    private void loadSyncStatus() {
        try {
            List<SyncService.SyncLogEntry> logs = syncService.getSyncHistory(1);
            if (!logs.isEmpty()) {
                SyncService.SyncLogEntry lastSync = logs.get(0);
                if (lastSync.fechaFin != null) {
                    long minutesAgo = ChronoUnit.MINUTES.between(lastSync.fechaFin, LocalDateTime.now());
                    if (minutesAgo < 60) {
                        lblLastSync.setText("Hace " + minutesAgo + " min");
                    } else {
                        long hoursAgo = minutesAgo / 60;
                        lblLastSync.setText("Hace " + hoursAgo + " hrs");
                    }
                } else {
                    lblLastSync.setText("En proceso");
                }
            } else {
                lblLastSync.setText("Sin sincronizar");
            }
        } catch (Exception e) {
            lblLastSync.setText("No disponible");
        }
    }

    private void loadConfigurationStatus() {
        try {
            String appVersion = configService.getConfigValue("app.version", "1.0.0");
            lblSystemVersion.setText("v" + appVersion);
            lblConfigStatus.setText("Configurado");
        } catch (Exception e) {
            lblConfigStatus.setText("No configurado");
        }
    }

    private void loadSystemInfo() {
        try {
            String appName = configService.getConfigValue("app.nombre", "Sistema de Gestión de Inventario");
            String appVersion = configService.getConfigValue("app.version", "1.0.0");

            lblSystemVersion.setText("v" + appVersion);
            lblDatabaseInfo.setText("Supabase PostgreSQL");
            lblServerInfo.setText("JavaFX " + System.getProperty("javafx.version", "24"));
            lblEnvironment.setText("Producción");

        } catch (Exception e) {
            System.err.println("Error loading system info: " + e.getMessage());
        }
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        lblCurrentTime.setText(now.format(timeFormatter));
        lblCurrentDate.setText(now.format(dateFormatter));
    }

    private void updateSessionDuration() {
        if (sessionStartTime != null) {
            LocalDateTime now = LocalDateTime.now();
            long seconds = ChronoUnit.SECONDS.between(sessionStartTime, now);

            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;

            lblSessionDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
        }
    }

    private void startClockUpdates() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateDateTime();
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();

        sessionTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateSessionDuration();
        }));
        sessionTimeline.setCycleCount(Animation.INDEFINITE);
        sessionTimeline.play();
    }

    private void setupPermissions() {
        boolean canManageUsers = sessionManager.hasPermission("usuarios", "read");
        boolean canManageRoles = sessionManager.hasPermission("roles", "read");
        boolean canManageConfig = sessionManager.hasPermission("configuracion", "read");
        boolean canSync = sessionManager.hasPermission("sincronizacion", "execute");

        if (btnUsuarios != null) btnUsuarios.setDisable(!canManageUsers);
        if (btnRoles != null) btnRoles.setDisable(!canManageRoles);
        if (btnConfiguracion != null) btnConfiguracion.setDisable(!canManageConfig);
        if (btnSync != null) btnSync.setDisable(!canSync);
        if (btnSeguridad != null) btnSeguridad.setDisable(!sessionManager.isAdmin());

        // Añadir tooltips localizados a los botones deshabilitados
        try {
            String tooltipText = com.app.service.LanguageService.getInstance().get("access.denied.tooltip");
            if (tooltipText == null) tooltipText = "Acceso restringido";

            if (btnUsuarios != null && btnUsuarios.isDisable()) btnUsuarios.setTooltip(new Tooltip(tooltipText));
            if (btnRoles != null && btnRoles.isDisable()) btnRoles.setTooltip(new Tooltip(tooltipText));
            if (btnConfiguracion != null && btnConfiguracion.isDisable()) btnConfiguracion.setTooltip(new Tooltip(tooltipText));
            if (btnSeguridad != null && btnSeguridad.isDisable()) btnSeguridad.setTooltip(new Tooltip(tooltipText));
            if (btnSync != null && btnSync.isDisable()) btnSync.setTooltip(new Tooltip(tooltipText));
        } catch (Exception e) {
            // No bloquear si falla la localización de la tooltip
            System.err.println("Error aplicando tooltips de acceso: " + e.getMessage());
        }

        // Log de diagnóstico: imprimir rol actual y permisos para ayudar a depurar por qué un admin no tiene acceso
        try {
            com.app.model.Rol currentRole = sessionManager.getCurrentRole();
            if (currentRole != null) {
                String roleName = currentRole.getNombre();
                int nivel = currentRole.getNivelAcceso();
                boolean sessAdmin = sessionManager.isAdmin();
                String permisos = currentRole.getPermisos() != null ? currentRole.getPermisos().toString() : "<no-permisos>";
                logger.info("[PERMISSIONS DEBUG] Rol='{}' Nivel={} session.isAdmin={} Permisos={}", roleName, nivel, sessAdmin, permisos);
            } else {
                logger.info("[PERMISSIONS DEBUG] currentRole es null");
            }
        } catch (Exception ex) {
            logger.warn("Error al obtener datos de rol para depuración: {}", ex.getMessage());
        }
    }

    @FXML
    public void handleUsuarios(Event event) {
        if (!AccessControlUtil.checkAndWarn("usuarios", "read", "No tiene permisos para acceder a Usuarios.\nSolo puede usar la sincronización en la nube.")) {
            return;
        }
        navigateToView("/com/app/view/UsuariosView.fxml", "Gestión de Usuarios", 1100, 750);
    }

    @FXML
    public void handleUsuariosClick(Event event) {
        handleUsuarios(event);
    }

    @FXML
    private void handleRoles(Event event) {
        if (!AccessControlUtil.checkAndWarn("roles", "read", "No tiene permisos para acceder a Roles.\nSolo puede usar la sincronización en la nube.")) {
            return;
        }
        navigateToView("/com/app/view/RolesView.fxml", "Gestión de Roles", 1000, 700);
    }

    @FXML
    private void handleRolesClick(Event event) {
        handleRoles(event);
    }

    @FXML
    private void handleSync(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sincronización con la Nube");
        alert.setHeaderText("¿Desea sincronizar datos con la nube?");
        alert.setContentText("Seleccione la dirección de sincronización:");

        ButtonType btnEnviar = new ButtonType("Enviar a Nube");
        ButtonType btnRecibir = new ButtonType("Recibir de Nube");
        ButtonType btnCancelar = ButtonType.CANCEL;

        alert.getButtonTypes().setAll(btnEnviar, btnRecibir, btnCancelar);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnEnviar || response == btnRecibir) {
                List<String> tables = List.of("productos", "ventas", "pedidos", "clientes");

                SyncService.SyncResult result;
                if (response == btnEnviar) {
                    result = syncService.syncDataToCloud(tables);
                } else {
                    result = syncService.syncDataFromCloud(tables);
                }

                Alert resultAlert = new Alert(
                    result.success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
                );
                resultAlert.setTitle("Resultado de Sincronización");
                resultAlert.setHeaderText(result.success ? "Sincronización Exitosa" : "Error en Sincronización");
                resultAlert.setContentText(result.message + "\nRegistros procesados: " + result.recordsProcessed);
                resultAlert.showAndWait();

                loadSyncStatus();
            }
        });
    }

    @FXML
    private void handleSyncClick(Event event) {
        handleSync(event);
    }

    @FXML
    private void handleSyncNow(Event event) {
        handleSync(event);
    }

    @FXML
    private void handleConfiguracion(Event event) {
        if (!AccessControlUtil.checkAndWarn("configuracion", "read", "No tiene permisos para acceder a la Configuración.\nSolo puede usar la sincronización en la nube.")) {
            return;
        }
        navigateToView("/com/app/view/ConfiguracionView.fxml", "Configuración del Sistema", 1000, 800);
    }

    @FXML
    private void handleConfiguracionClick(Event event) {
        handleConfiguracion(event);
    }

    @FXML
    private void handleSeguridad(Event event) {
        if (!AccessControlUtil.checkAdminOrWarn("Solo administradores pueden acceder al panel de seguridad.\nSolo puede usar la sincronización en la nube.")) {
            return;
        }
        navigateToView("/com/app/view/SeguridadView.fxml", "Panel de Seguridad", 1000, 800);
    }

    @FXML
    private void handleSeguridadClick(Event event) {
        handleSeguridad(event);
    }

    @FXML
    private void handleLogout(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Está seguro que desea cerrar sesión?");
        alert.setContentText("Todos los cambios no guardados se perderán.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stopTimelines();
                authService.logout();

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/LoginView.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) btnLogout.getScene().getWindow();
                    Scene scene = new Scene(root, 900, 800);
                    stage.setTitle("Login - Sistema de Gestión");
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.centerOnScreen();

                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlert("Error de Navegación", "No se pudo cargar la pantalla de login: " + e.getMessage());
                }
            }
        });
    }

    private void navigateToView(String fxmlPath, String title, int width, int height) {
        try {
            stopTimelines();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            Scene scene = new Scene(root, width, height);

            stage.setTitle(title + " - Sistema de Gestión");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error de Navegación", "No se pudo cargar " + title + ": " + e.getMessage());
        }
    }

    private void stopTimelines() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        if (sessionTimeline != null) {
            sessionTimeline.stop();
        }
    }

    public void refreshDashboard() {
        loadDashboardData();
    }

    private void setupLanguageListener() {
        LanguageService langService = LanguageService.getInstance();
        langService.addLanguageChangeListener(newLanguage -> {
            Platform.runLater(this::updateUITexts);
        });
    }

    private void updateUITexts() {
        LanguageService langService = LanguageService.getInstance();
        Usuario currentUser = sessionManager.getCurrentUser();
        Rol currentRole = sessionManager.getCurrentRole();

        if (currentUser != null) {
            String nombreCompleto = currentUser.getNombreCompleto() != null && !currentUser.getNombreCompleto().isEmpty() ?
                currentUser.getNombreCompleto() : currentUser.getUsername();
            welcomeLabel.setText(langService.get("dashboard.bienvenido") + ", " + nombreCompleto);
            lblCurrentUser.setText(langService.get("usuarios.usuario") + ": " + currentUser.getUsername());
        } else {
            welcomeLabel.setText(langService.get("dashboard.bienvenido") + " al Sistema");
            lblCurrentUser.setText(langService.get("usuarios.usuario") + ": Invitado");
        }

        if (currentRole != null) {
            lblUserRole.setText(currentRole.getNombre());
        }

        lblLastLogin.setText(langService.get("dashboard.ultima_sincro") + ": " +
            (currentUser != null && currentUser.getUltimoAcceso() != null ?
                currentUser.getUltimoAcceso().format(loginFormatter) : "N/A"));

        lblTotalUsers.setText(String.valueOf(lblTotalUsers.getText().isEmpty() ? "0" : lblTotalUsers.getText()));
        lblTotalRoles.setText(String.valueOf(lblTotalRoles.getText().isEmpty() ? "0" : lblTotalRoles.getText()));

        lblConnectionText.setText(lblConnectionText.getText());
        lblConnectionDetails.setText(lblConnectionDetails.getText());
        lblLastSync.setText(lblLastSync.getText());
        lblSessionDuration.setText(lblSessionDuration.getText());
        lblConfigStatus.setText(lblConfigStatus.getText());
    }
}
