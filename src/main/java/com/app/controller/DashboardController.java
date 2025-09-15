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
        // Initialize dashboard components
        welcomeLabel.setText("Bienvenido al Panel de Control");
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        // TODO: Load actual data from services
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/LoginView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
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
