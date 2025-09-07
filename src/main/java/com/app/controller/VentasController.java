package com.app.controller;

import com.app.model.Cliente;
import com.app.model.Producto;
import com.app.model.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * Controller class for the Sales view
 */
public class VentasController extends BaseController implements Initializable {

    @FXML
    private ComboBox<Cliente> clienteComboBox;

    @FXML
    private ComboBox<Producto> productoComboBox;

    @FXML
    private TableView<Producto> carritoTable;

    @FXML
    private TableColumn<Producto, String> nombreColumn;

    @FXML
    private TableColumn<Producto, String> categoriaColumn;

    @FXML
    private TableColumn<Producto, Double> precioColumn;

    @FXML
    private Button addToCartButton;

    @FXML
    private Button removeFromCartButton;

    @FXML
    private Label totalLabel;

    @FXML
    private Button processSaleButton;

    @FXML
    private Button clearCartButton;

    @FXML
    private Button backButton;

    @FXML
    private TableView<Venta> ventasTable;

    @FXML
    private TableColumn<Venta, Long> ventaIdColumn;

    @FXML
    private TableColumn<Venta, LocalDateTime> fechaColumn;

    @FXML
    private TableColumn<Venta, String> clienteColumn;

    @FXML
    private TableColumn<Venta, Double> ventaTotalColumn;

    private ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private ObservableList<Producto> productos = FXCollections.observableArrayList();
    private ObservableList<Producto> carrito = FXCollections.observableArrayList();
    private ObservableList<Venta> ventas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
        setupTableColumns();
        loadSampleData();
        setupEventHandlers();
    }

    @Override
    public void initializeController() {
        processSaleButton.setDisable(true);
        removeFromCartButton.setDisable(true);
        updateTotal();
    }

    private void setupTableColumns() {
        // Cart table
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        carritoTable.setItems(carrito);

        // Sales table
        ventaIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        clienteColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCliente().getNombre()
            )
        );
        ventaTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        ventasTable.setItems(ventas);
    }

    private void setupEventHandlers() {
        carritoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            removeFromCartButton.setDisable(newSelection == null);
        });

        carrito.addListener((javafx.collections.ListChangeListener<Producto>) change -> {
            updateTotal();
            processSaleButton.setDisable(carrito.isEmpty() || clienteComboBox.getValue() == null);
        });

        clienteComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            processSaleButton.setDisable(carrito.isEmpty() || newValue == null);
        });
    }

    private void loadSampleData() {
        // Load sample clients
        clientes.addAll(
            new Cliente(1L, "Juan Pérez", "555-0101", "juan@email.com"),
            new Cliente(2L, "María García", "555-0102", "maria@email.com"),
            new Cliente(3L, "Carlos López", "555-0103", "carlos@email.com"),
            new Cliente(4L, "Ana Martínez", "555-0104", "ana@email.com")
        );
        clienteComboBox.setItems(clientes);

        // Load sample products
        productos.addAll(
            new Producto(1L, "Laptop Dell", "Electronics", 899.99, 15),
            new Producto(2L, "Mouse Logitech", "Electronics", 29.99, 50),
            new Producto(3L, "Keyboard Mechanical", "Electronics", 79.99, 25),
            new Producto(4L, "Monitor 24\"", "Electronics", 199.99, 12),
            new Producto(5L, "Webcam HD", "Electronics", 49.99, 30)
        );
        productoComboBox.setItems(productos);

        // Load sample sales
        Venta sampleVenta = new Venta(1L, LocalDateTime.now().minusDays(1), 
            clientes.get(0), productos.subList(0, 2), 929.98);
        ventas.add(sampleVenta);
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        Producto selected = productoComboBox.getValue();
        if (selected != null) {
            if (selected.isAvailable()) {
                carrito.add(selected);
                showInfoAlert("Success", "Product added to cart!");
            } else {
                showWarningAlert("Stock Warning", "Product is out of stock!");
            }
        } else {
            showWarningAlert("Selection Error", "Please select a product to add.");
        }
    }

    @FXML
    private void handleRemoveFromCart(ActionEvent event) {
        Producto selected = carritoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            carrito.remove(selected);
            showInfoAlert("Success", "Product removed from cart!");
        }
    }

    @FXML
    private void handleProcessSale(ActionEvent event) {
        Cliente selectedCliente = clienteComboBox.getValue();
        if (selectedCliente != null && !carrito.isEmpty()) {
            try {
                Venta newVenta = new Venta(
                    (long) (ventas.size() + 1),
                    LocalDateTime.now(),
                    selectedCliente,
                    FXCollections.observableArrayList(carrito),
                    calculateTotal()
                );
                
                ventas.add(newVenta);
                
                // Update product stock (simulation)
                for (Producto producto : carrito) {
                    if (producto.getStock() > 0) {
                        producto.decreaseStock(1);
                    }
                }
                
                clearCart();
                showInfoAlert("Success", "Sale processed successfully!\nTotal: $" + 
                    String.format("%.2f", newVenta.getTotal()));
                
            } catch (Exception e) {
                showErrorAlert("Processing Error", "Could not process sale: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        if (!carrito.isEmpty()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Clear");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Are you sure you want to clear the cart?");
            
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                clearCart();
            }
        }
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

    private void clearCart() {
        carrito.clear();
        clienteComboBox.setValue(null);
        productoComboBox.setValue(null);
    }

    private double calculateTotal() {
        return carrito.stream().mapToDouble(Producto::getPrecio).sum();
    }

    private void updateTotal() {
        double total = calculateTotal();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
}