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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

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
        Usuario currentUser = sessionManager.getCurrentUser();
        Rol currentRole = sessionManager.getCurrentRole();

        if (currentUser != null) {
            String nombreCompleto = currentUser.getNombreCompleto() != null && !currentUser.getNombreCompleto().isEmpty() ?
                currentUser.getNombreCompleto() : currentUser.getUsername();

            welcomeLabel.setText("Bienvenido, " + nombreCompleto);
            lblCurrentUser.setText("Usuario: " + currentUser.getUsername());

            if (currentUser.getUltimoAcceso() != null) {
                lblLastLogin.setText("Última sesión: " + currentUser.getUltimoAcceso().format(loginFormatter));
            } else {
                lblLastLogin.setText("Primera sesión");
            }
        } else {
            welcomeLabel.setText("Bienvenido al Sistema");
            lblCurrentUser.setText("Usuario: Invitado");
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
        boolean canManageConfig = sessionManager.hasPermission("configuracion", "read");
        boolean canSync = sessionManager.hasPermission("sincronizacion", "execute");

        if (btnUsuarios != null) btnUsuarios.setDisable(!canManageUsers);
        if (btnRoles != null) btnRoles.setDisable(!canManageUsers);
        if (btnConfiguracion != null) btnConfiguracion.setDisable(!canManageConfig);
        if (btnSync != null) btnSync.setDisable(!canSync);
        if (btnSeguridad != null) btnSeguridad.setDisable(!sessionManager.isAdmin());
    }

    @FXML
    public void handleUsuarios(Event event) {
        navigateToView("/com/app/view/UsuariosView.fxml", "Gestión de Usuarios", 1100, 750);
    }

    @FXML
    public void handleUsuariosClick(Event event) {
        handleUsuarios(event);
    }

    @FXML
    private void handleRoles(Event event) {
        showInfoAlert("Gestión de Roles", "El módulo de gestión de roles estará disponible próximamente.\n\nPodrá configurar:\n- Crear nuevos roles\n- Asignar permisos por módulo\n- Gestionar accesos del sistema");
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
        showInfoAlert("Configuración del Sistema",
            "El módulo de configuración avanzada estará disponible próximamente.\n\n" +
            "Podrá configurar:\n" +
            "- Parámetros generales del sistema\n" +
            "- Conexiones y endpoints\n" +
            "- Apariencia y personalización\n" +
            "- Opciones de seguridad");
    }

    @FXML
    private void handleConfiguracionClick(Event event) {
        handleConfiguracion(event);
    }

    @FXML
    private void handleSeguridad(Event event) {
        showInfoAlert("Panel de Seguridad",
            "El módulo de seguridad estará disponible próximamente.\n\n" +
            "Características:\n" +
            "- Auditoría de accesos\n" +
            "- Registro de actividades\n" +
            "- Gestión de sesiones activas\n" +
            "- Políticas de contraseñas\n" +
            "- Logs del sistema");
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
}
