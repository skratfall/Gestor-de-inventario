package com.app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the Dashboard view
 */
public class DashboardController extends BaseController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalSalesLabel;

    @FXML
    private Label pendingOrdersLabel;

    @FXML
    private Button inventarioButton;

    @FXML
    private Button ventasButton;

    @FXML
    private Button pedidosButton;

    @FXML
    private Button reportesButton;

    @FXML
    private Button btnUsuarios;

    @FXML
    private Button btnSync;

    @FXML
    private Button btnConfiguracion;

    @FXML
    private Button logoutButton;

    // --- ReportesView: referencia al gráfico ---
    @FXML
    private PieChart categoryPieChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
        loadDashboardData();
    }

    @Override
    public void initializeController() {
        com.app.security.SessionManager sessionManager = com.app.security.SessionManager.getInstance();
        com.app.model.Usuario currentUser = sessionManager.getCurrentUser();

        if (currentUser != null) {
            String nombreCompleto = currentUser.getNombreCompleto() != null ?
                currentUser.getNombreCompleto() : currentUser.getUsername();
            welcomeLabel.setText("Bienvenido, " + nombreCompleto);
        } else {
            welcomeLabel.setText("Bienvenido al Panel de Control");
        }

        loadDashboardData();
        setupPermissions();
    }

    private void setupPermissions() {
        com.app.security.SessionManager sessionManager = com.app.security.SessionManager.getInstance();

        inventarioButton.setDisable(!sessionManager.hasPermission("productos", "read"));
        ventasButton.setDisable(!sessionManager.hasPermission("ventas", "read"));
        pedidosButton.setDisable(!sessionManager.hasPermission("pedidos", "read"));
        reportesButton.setDisable(!sessionManager.hasPermission("reportes", "read"));

        if (btnUsuarios != null) {
            btnUsuarios.setDisable(!sessionManager.hasPermission("usuarios", "read"));
            btnUsuarios.setOnAction(this::handleUsuarios);
        }

        if (btnSync != null) {
            btnSync.setDisable(!sessionManager.hasPermission("sincronizacion", "execute"));
            btnSync.setOnAction(this::handleSync);
        }

        if (btnConfiguracion != null) {
            btnConfiguracion.setDisable(!sessionManager.hasPermission("configuracion", "read"));
            btnConfiguracion.setOnAction(this::handleConfiguracion);
        }
    }

    @FXML
    private void handleUsuarios(ActionEvent event) {
        navigateToView("/com/app/view/UsuariosView.fxml", "User Management", 900, 700);
    }

    @FXML
    private void handleSync(ActionEvent event) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sincronización con la Nube");
        alert.setHeaderText("¿Desea sincronizar datos con la nube?");
        alert.setContentText("Seleccione la dirección de sincronización:");

        javafx.scene.control.ButtonType btnEnviar = new javafx.scene.control.ButtonType("Enviar a Nube");
        javafx.scene.control.ButtonType btnRecibir = new javafx.scene.control.ButtonType("Recibir de Nube");
        javafx.scene.control.ButtonType btnCancelar = javafx.scene.control.ButtonType.CANCEL;

        alert.getButtonTypes().setAll(btnEnviar, btnRecibir, btnCancelar);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnEnviar || response == btnRecibir) {
                com.app.service.SyncService syncService = com.app.service.SyncService.getInstance();
                java.util.List<String> tables = java.util.Arrays.asList("productos", "ventas", "pedidos", "clientes");

                com.app.service.SyncService.SyncResult result;
                if (response == btnEnviar) {
                    result = syncService.syncDataToCloud(tables);
                } else {
                    result = syncService.syncDataFromCloud(tables);
                }

                javafx.scene.control.Alert resultAlert = new javafx.scene.control.Alert(
                    result.success ? javafx.scene.control.Alert.AlertType.INFORMATION : javafx.scene.control.Alert.AlertType.ERROR
                );
                resultAlert.setTitle("Resultado de Sincronización");
                resultAlert.setHeaderText(result.success ? "Sincronización Exitosa" : "Error en Sincronización");
                resultAlert.setContentText(result.message + "\nRegistros procesados: " + result.recordsProcessed);
                resultAlert.showAndWait();
            }
        });
    }

    @FXML
    private void handleConfiguracion(ActionEvent event) {
        showInfoAlert("Configuración", "Módulo de configuración en desarrollo");
    }

    private void loadDashboardData() {
        totalSalesLabel.setText("$12,450.00");
        pendingOrdersLabel.setText("23");


        // Inicializar PieChart aquí para que se vea en el Dashboard
        if (categoryPieChart != null) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Electrónica", 40),
                new PieChart.Data("Ropa", 25),
                new PieChart.Data("Hogar", 20),
                new PieChart.Data("Otros", 15)
            );

            categoryPieChart.setData(pieChartData);
            categoryPieChart.setLegendVisible(true);
            categoryPieChart.setLabelsVisible(true);
        }
    }

    @FXML
    private void handleInventario(ActionEvent event) {
        navigateToView("/com/app/view/InventarioView.fxml", "Inventory Management", 1000, 700);
    }

    @FXML
    private void handleVentas(ActionEvent event) {
        navigateToView("/com/app/view/VentasView.fxml", "Sales Management", 900, 800);
    }

    @FXML
    private void handlePedidos(ActionEvent event) {
        navigateToView("/com/app/view/PedidosView.fxml", "Orders Management", 1000, 800);
    }

    @FXML
    private void handleReportes(ActionEvent event) {
        navigateToView("/com/app/view/ReportesView.fxml", "Reports", 1000, 800);

        // Simulación de carga de datos en el gráfico
        if (categoryPieChart != null) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Electrónica", 40),
                new PieChart.Data("Ropa", 25),
                new PieChart.Data("Hogar", 20),
                new PieChart.Data("Otros", 15)
            );

            categoryPieChart.setData(pieChartData);
            categoryPieChart.setLegendVisible(true);
            categoryPieChart.setLabelsVisible(true);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        com.app.service.AuthenticationService authService = com.app.service.AuthenticationService.getInstance();
        authService.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 800);
            stage.setTitle("Login - JavaFX Application");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not load login view: " + e.getMessage());
        }
    }

    private void navigateToView(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) inventarioButton.getScene().getWindow();
            Scene scene = new Scene(root, width, height);

            stage.setTitle(title + " - JavaFX Application");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not load " + title.toLowerCase() + ": " + e.getMessage());
        }
    }

    public void refreshDashboard() {
        loadDashboardData();
    }
}
