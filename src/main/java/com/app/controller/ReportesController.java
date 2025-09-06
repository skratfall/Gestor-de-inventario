package com.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller class for the Reports view
 */
public class ReportesController extends BaseController implements Initializable {

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button generateSalesReportButton;

    @FXML
    private Button generateInventoryReportButton;

    @FXML
    private Button generateOrdersReportButton;

    @FXML
    private Button exportReportButton;

    @FXML
    private Button backButton;

    @FXML
    private TextArea reportTextArea;

    @FXML
    private Label totalSalesLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label lowStockItemsLabel;

    @FXML
    private Label topProductLabel;

    // Chart placeholders
    @FXML
    private Label salesChartPlaceholder;

    @FXML
    private Label inventoryChartPlaceholder;

    @FXML
    private Label ordersChartPlaceholder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
        setupDatePickers();
        loadSampleStatistics();
    }

    @Override
    public void initializeController() {
        exportReportButton.setDisable(true);
        setupChartPlaceholders();
    }

    private void setupDatePickers() {
        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
    }

    private void setupChartPlaceholders() {
        salesChartPlaceholder.setText("📊 Sales Chart\n(Chart implementation pending)");
        salesChartPlaceholder.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; " +
                                     "-fx-border-radius: 5; -fx-padding: 20; -fx-alignment: center;");

        inventoryChartPlaceholder.setText("📈 Inventory Chart\n(Chart implementation pending)");
        inventoryChartPlaceholder.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; " +
                                         "-fx-border-radius: 5; -fx-padding: 20; -fx-alignment: center;");

        ordersChartPlaceholder.setText("📉 Orders Chart\n(Chart implementation pending)");
        ordersChartPlaceholder.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; " +
                                      "-fx-border-radius: 5; -fx-padding: 20; -fx-alignment: center;");
    }

    private void loadSampleStatistics() {
        // TODO: Replace with actual data from services
        totalSalesLabel.setText("$45,230.50");
        totalOrdersLabel.setText("127");
        lowStockItemsLabel.setText("8");
        topProductLabel.setText("Laptop Dell");
    }

    @FXML
    private void handleGenerateSalesReport(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                showWarningAlert("Date Error", "Start date cannot be after end date.");
                return;
            }
            
            generateSalesReport(startDate, endDate);
            exportReportButton.setDisable(false);
        } else {
            showWarningAlert("Date Error", "Please select both start and end dates.");
        }
    }

    @FXML
    private void handleGenerateInventoryReport(ActionEvent event) {
        generateInventoryReport();
        exportReportButton.setDisable(false);
    }

    @FXML
    private void handleGenerateOrdersReport(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                showWarningAlert("Date Error", "Start date cannot be after end date.");
                return;
            }
            
            generateOrdersReport(startDate, endDate);
            exportReportButton.setDisable(false);
        } else {
            showWarningAlert("Date Error", "Please select both start and end dates.");
        }
    }

    @FXML
    private void handleExportReport(ActionEvent event) {
        // TODO: Implement actual export functionality (PDF, Excel, etc.)
        showInfoAlert("Export", "Report export functionality will be implemented.\n" +
                     "Current report content would be exported to file.");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/DashboardView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("Dashboard - JavaFX Application");
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    private void generateSalesReport(LocalDate startDate, LocalDate endDate) {
        // TODO: Replace with actual data from services
        StringBuilder report = new StringBuilder();
        report.append("SALES REPORT\n");
        report.append("============\n\n");
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
        
        report.append("Summary:\n");
        report.append("--------\n");
        report.append("Total Sales: $45,230.50\n");
        report.append("Number of Transactions: 127\n");
        report.append("Average Sale Amount: $356.15\n");
        report.append("Best Selling Day: ").append(endDate.minusDays(3)).append("\n\n");
        
        report.append("Top Products:\n");
        report.append("-------------\n");
        report.append("1. Laptop Dell - $8,999.00 (10 units)\n");
        report.append("2. Monitor 24\" - $3,999.80 (20 units)\n");
        report.append("3. Keyboard Mechanical - $1,599.75 (20 units)\n");
        report.append("4. Mouse Logitech - $1,199.60 (40 units)\n");
        report.append("5. Webcam HD - $999.80 (20 units)\n\n");
        
        report.append("Daily Breakdown:\n");
        report.append("----------------\n");
        for (int i = 0; i < 7; i++) {
            LocalDate day = endDate.minusDays(i);
            double dailySales = 1500 + (Math.random() * 2000); // Sample data
            report.append(day).append(": $").append(String.format("%.2f", dailySales)).append("\n");
        }
        
        reportTextArea.setText(report.toString());
        showInfoAlert("Report Generated", "Sales report has been generated successfully.");
    }

    private void generateInventoryReport() {
        // TODO: Replace with actual data from services
        StringBuilder report = new StringBuilder();
        report.append("INVENTORY REPORT\n");
        report.append("================\n\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");
        
        report.append("Summary:\n");
        report.append("--------\n");
        report.append("Total Products: 156\n");
        report.append("Total Value: $89,450.30\n");
        report.append("Low Stock Items: 8\n");
        report.append("Out of Stock Items: 2\n\n");
        
        report.append("Stock Levels:\n");
        report.append("-------------\n");
        report.append("Laptop Dell - Stock: 15 - Value: $13,499.85\n");
        report.append("Mouse Logitech - Stock: 50 - Value: $1,499.50\n");
        report.append("Keyboard Mechanical - Stock: 25 - Value: $1,999.75\n");
        report.append("Monitor 24\" - Stock: 12 - Value: $2,399.88\n");
        report.append("Webcam HD - Stock: 30 - Value: $1,499.70\n\n");
        
        report.append("Low Stock Alert:\n");
        report.append("----------------\n");
        report.append("Monitor 24\" - Only 12 units remaining\n");
        report.append("Laptop Dell - Only 15 units remaining\n\n");
        
        report.append("Categories:\n");
        report.append("-----------\n");
        report.append("Electronics: 132 items ($78,230.50)\n");
        report.append("Accessories: 24 items ($11,219.80)\n");
        
        reportTextArea.setText(report.toString());
        showInfoAlert("Report Generated", "Inventory report has been generated successfully.");
    }

    private void generateOrdersReport(LocalDate startDate, LocalDate endDate) {
        // TODO: Replace with actual data from services
        StringBuilder report = new StringBuilder();
        report.append("ORDERS REPORT\n");
        report.append("=============\n\n");
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
        
        report.append("Summary:\n");
        report.append("--------\n");
        report.append("Total Orders: 127\n");
        report.append("Pending Orders: 23\n");
        report.append("Processing Orders: 15\n");
        report.append("Shipped Orders: 31\n");
        report.append("Delivered Orders: 52\n");
        report.append("Cancelled Orders: 6\n\n");
        
        report.append("Order Status Breakdown:\n");
        report.append("-----------------------\n");
        report.append("PENDING: 18.1% (23 orders)\n");
        report.append("PROCESSING: 11.8% (15 orders)\n");
        report.append("SHIPPED: 24.4% (31 orders)\n");
        report.append("DELIVERED: 40.9% (52 orders)\n");
        report.append("CANCELLED: 4.7% (6 orders)\n\n");
        
        report.append("Average Processing Time:\n");
        report.append("------------------------\n");
        report.append("Pending to Processing: 2.3 hours\n");
        report.append("Processing to Shipped: 1.2 days\n");
        report.append("Shipped to Delivered: 3.5 days\n\n");
        
        report.append("Top Customers:\n");
        report.append("--------------\n");
        report.append("Juan Pérez - 8 orders\n");
        report.append("María García - 6 orders\n");
        report.append("Carlos López - 5 orders\n");
        report.append("Ana Martínez - 4 orders\n");
        
        reportTextArea.setText(report.toString());
        showInfoAlert("Report Generated", "Orders report has been generated successfully.");
    }
}