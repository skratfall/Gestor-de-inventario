package com.app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller class for the Reports view
 */
public class ReportesController extends BaseController implements Initializable {

    // Date Pickers
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    // Buttons
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

    // Text Area
    @FXML
    private TextArea reportTextArea;

    // Statistics Labels
    @FXML
    private Label totalSalesLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label lowStockItemsLabel;
    @FXML
    private Label topProductLabel;

    // Sales Tab Labels
    @FXML
    private Label currentMonthSalesLabel;
    @FXML
    private Label previousMonthSalesLabel;
    @FXML
    private Label salesGrowthLabel;
    @FXML
    private Label reportStatusLabel;

    // Products Tab Labels
    @FXML
    private Label topProduct1Label;
    @FXML
    private Label topProduct1QtyLabel;
    @FXML
    private Label topProduct2Label;
    @FXML
    private Label topProduct2QtyLabel;
    @FXML
    private Label topProduct3Label;
    @FXML
    private Label topProduct3QtyLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label activeProductsLabel;
    @FXML
    private Label lowStockCountLabel;

    // Charts
    @FXML
    private BarChart<String, Number> salesTrendsChart;
    @FXML
    private PieChart topProductsChart;

    // TableViews (si las agregas en el futuro)
    // @FXML
    // private TableView<ReportData> salesTable;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
        setupDatePickers();
        setupCharts();
        loadSampleStatistics();
        setupEventHandlers();
    }

    @Override
    public void initializeController() {
        exportReportButton.setDisable(true);
        reportStatusLabel.setText("Ready to generate reports");
    }

    private void setupDatePickers() {
        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
    }

    private void setupCharts() {
        setupSalesTrendsChart();
        setupTopProductsChart();
    }

    private void setupSalesTrendsChart() {
        // Sample data for sales trends
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Sales");
        
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        for (int i = 0; i < months.length; i++) {
            double sales = 8000 + (Math.random() * 7000); // Random sales between 8000-15000
            series.getData().add(new XYChart.Data<>(months[i], sales));
        }
        
        salesTrendsChart.getData().add(series);
        salesTrendsChart.setLegendVisible(false);
    }

    private void setupTopProductsChart() {
        // Sample data for top products
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Laptop Dell", 45),
            new PieChart.Data("Monitor 24\"", 32),
            new PieChart.Data("Mouse Logitech", 28),
            new PieChart.Data("Keyboard Mechanical", 22),
            new PieChart.Data("Webcam HD", 18)
        );
        
        topProductsChart.setData(pieChartData);
        topProductsChart.setTitle("Top Selling Products");
    }

    private void setupEventHandlers() {
        // Listen for date changes to enable/disable generate buttons
        startDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            validateDateRange();
        });
        
        endDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            validateDateRange();
        });
    }

    private void validateDateRange() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        boolean valid = startDate != null && endDate != null && !startDate.isAfter(endDate);
        generateSalesReportButton.setDisable(!valid);
        generateOrdersReportButton.setDisable(!valid);
    }

    private void loadSampleStatistics() {
        // Statistics Cards
        totalSalesLabel.setText("$45,230.50");
        totalOrdersLabel.setText("127");
        lowStockItemsLabel.setText("8");
        topProductLabel.setText("Laptop Dell");

        // Sales Summary
        currentMonthSalesLabel.setText("$12,450.00");
        previousMonthSalesLabel.setText("$10,230.50");
        salesGrowthLabel.setText("+21.7%");

        // Top Products
        topProduct1Label.setText("Laptop Dell");
        topProduct1QtyLabel.setText("45 units");
        topProduct2Label.setText("Monitor 24\"");
        topProduct2QtyLabel.setText("32 units");
        topProduct3Label.setText("Mouse Logitech");
        topProduct3QtyLabel.setText("28 units");

        // Product Performance
        totalProductsLabel.setText("156");
        activeProductsLabel.setText("148");
        lowStockCountLabel.setText("8");
    }

    @FXML
    private void handleGenerateSalesReport(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (validateDates(startDate, endDate)) {
            generateSalesReport(startDate, endDate);
            exportReportButton.setDisable(false);
            reportStatusLabel.setText("Sales report generated - " + LocalDate.now().format(dateFormatter));
        }
    }

    @FXML
    private void handleGenerateInventoryReport(ActionEvent event) {
        generateInventoryReport();
        exportReportButton.setDisable(false);
        reportStatusLabel.setText("Inventory report generated - " + LocalDate.now().format(dateFormatter));
    }

    @FXML
    private void handleGenerateOrdersReport(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (validateDates(startDate, endDate)) {
            generateOrdersReport(startDate, endDate);
            exportReportButton.setDisable(false);
            reportStatusLabel.setText("Orders report generated - " + LocalDate.now().format(dateFormatter));
        }
    }

    @FXML
    private void handleExportReport(ActionEvent event) {
        if (reportTextArea.getText().isEmpty()) {
            showWarningAlert("Export Error", "No report content to export. Please generate a report first.");
            return;
        }
        
        // TODO: Implement actual export functionality (PDF, Excel, CSV)
        String reportType = "Sales"; // Determine based on current tab/content
        if (reportTextArea.getText().contains("INVENTORY REPORT")) reportType = "Inventory";
        if (reportTextArea.getText().contains("ORDERS REPORT")) reportType = "Orders";
        
        showInfoAlert("Export Feature", reportType + " report export functionality will be implemented.\n" +
                     "The current report content would be exported to file.");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateToDashboard();
    }

    private boolean validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            showWarningAlert("Date Error", "Please select both start and end dates.");
            return false;
        }
        
        if (startDate.isAfter(endDate)) {
            showWarningAlert("Date Error", "Start date cannot be after end date.");
            return false;
        }
        
        return true;
    }

    private void generateSalesReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        report.append("SALES REPORT\n");
        report.append("============\n\n");
        report.append("Period: ").append(startDate.format(dateFormatter))
              .append(" to ").append(endDate.format(dateFormatter)).append("\n");
        report.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
        
        report.append("SUMMARY\n");
        report.append("-------\n");
        report.append("Total Sales: $45,230.50\n");
        report.append("Number of Transactions: 127\n");
        report.append("Average Sale Amount: $356.15\n");
        report.append("Best Selling Day: ").append(endDate.minusDays(3).format(dateFormatter)).append("\n\n");
        
        report.append("TOP PRODUCTS\n");
        report.append("------------\n");
        report.append("1. Laptop Dell - $8,999.00 (10 units)\n");
        report.append("2. Monitor 24\" - $3,999.80 (20 units)\n");
        report.append("3. Keyboard Mechanical - $1,599.75 (20 units)\n");
        report.append("4. Mouse Logitech - $1,199.60 (40 units)\n");
        report.append("5. Webcam HD - $999.80 (20 units)\n\n");
        
        report.append("DAILY BREAKDOWN (Last 7 Days)\n");
        report.append("-----------------------------\n");
        for (int i = 0; i < 7; i++) {
            LocalDate day = endDate.minusDays(i);
            double dailySales = 1500 + (Math.random() * 2000);
            report.append(day.format(dateFormatter)).append(": $")
                  .append(String.format("%.2f", dailySales)).append("\n");
        }
        
        report.append("\nRECOMMENDATIONS\n");
        report.append("---------------\n");
        report.append("• Focus on promoting Laptop Dell (highest revenue)\n");
        report.append("• Consider restocking Mouse Logitech (best seller)\n");
        report.append("• Monitor sales trends for seasonal adjustments\n");
        
        reportTextArea.setText(report.toString());
        showInfoAlert("Report Generated", "Sales report has been generated successfully.");
    }

    private void generateInventoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("INVENTORY REPORT\n");
        report.append("================\n\n");
        report.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
        
        report.append("SUMMARY\n");
        report.append("-------\n");
        report.append("Total Products: 156\n");
        report.append("Total Inventory Value: $89,450.30\n");
        report.append("Low Stock Items (<15 units): 8\n");
        report.append("Out of Stock Items: 2\n\n");
        
        report.append("STOCK LEVELS\n");
        report.append("------------\n");
        report.append("Laptop Dell - Stock: 15 - Value: $13,499.85\n");
        report.append("Mouse Logitech - Stock: 50 - Value: $1,499.50\n");
        report.append("Keyboard Mechanical - Stock: 25 - Value: $1,999.75\n");
        report.append("Monitor 24\" - Stock: 12 - Value: $2,399.88\n");
        report.append("Webcam HD - Stock: 30 - Value: $1,499.70\n\n");
        
        report.append("LOW STOCK ALERT\n");
        report.append("---------------\n");
        report.append("• Monitor 24\" - Only 12 units remaining (Reorder needed)\n");
        report.append("• Laptop Dell - Only 15 units remaining (Monitor closely)\n");
        report.append("• Keyboard Mechanical - 25 units (Adequate)\n\n");
        
        report.append("CATEGORY BREAKDOWN\n");
        report.append("------------------\n");
        report.append("Electronics: 132 items ($78,230.50)\n");
        report.append("Accessories: 24 items ($11,219.80)\n\n");
        
        report.append("ACTION ITEMS\n");
        report.append("------------\n");
        report.append("1. Reorder Monitor 24\" (Minimum 20 units)\n");
        report.append("2. Review Laptop Dell sales forecast\n");
        report.append("3. Consider promotions for slow-moving items\n");
        
        reportTextArea.setText(report.toString());
        showInfoAlert("Report Generated", "Inventory report has been generated successfully.");
    }

    private void generateOrdersReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        report.append("ORDERS REPORT\n");
        report.append("=============\n\n");
        report.append("Period: ").append(startDate.format(dateFormatter))
              .append(" to ").append(endDate.format(dateFormatter)).append("\n");
        report.append("Generated: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");
        
        report.append("SUMMARY\n");
        report.append("-------\n");
        report.append("Total Orders: 127\n");
        report.append("Pending Orders: 23\n");
        report.append("Processing Orders: 15\n");
        report.append("Shipped Orders: 31\n");
        report.append("Delivered Orders: 52\n");
        report.append("Cancelled Orders: 6\n\n");
        
        report.append("ORDER STATUS BREAKDOWN\n");
        report.append("----------------------\n");
        report.append("PENDING: 18.1% (23 orders)\n");
        report.append("PROCESSING: 11.8% (15 orders)\n");
        report.append("SHIPPED: 24.4% (31 orders)\n");
        report.append("DELIVERED: 40.9% (52 orders)\n");
        report.append("CANCELLED: 4.7% (6 orders)\n\n");
        
        report.append("PERFORMANCE METRICS\n");
        report.append("-------------------\n");
        report.append("Average Processing Time: 2.3 hours\n");
        report.append("Average Shipping Time: 1.2 days\n");
        report.append("Average Delivery Time: 3.5 days\n");
        report.append("Order Accuracy Rate: 98.4%\n");
        report.append("Customer Satisfaction: 4.8/5.0\n\n");
        
        report.append("TOP CUSTOMERS\n");
        report.append("-------------\n");
        report.append("1. Juan Pérez - 8 orders ($3,599.20)\n");
        report.append("2. María García - 6 orders ($2,699.40)\n");
        report.append("3. Carlos López - 5 orders ($2,249.50)\n");
        report.append("4. Ana Martínez - 4 orders ($1,799.60)\n\n");
        
        report.append("RECOMMENDATIONS\n");
        report.append("---------------\n");
        report.append("• Reduce pending orders (currently 23)\n");
        report.append("• Follow up on cancelled orders (6 cases)\n");
        report.append("• Implement loyalty program for top customers\n");
        
        reportTextArea.setText(report.toString());
        showInfoAlert("Report Generated", "Orders report has been generated successfully.");
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/DashboardView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("Dashboard - JavaFX Application");
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Could not load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Inner class for report data (if you decide to use TableView)
    public static class ReportData {
        private final SimpleStringProperty category;
        private final SimpleDoubleProperty value;
        
        public ReportData(String category, double value) {
            this.category = new SimpleStringProperty(category);
            this.value = new SimpleDoubleProperty(value);
        }
        
        public String getCategory() { return category.get(); }
        public double getValue() { return value.get(); }
    }
}