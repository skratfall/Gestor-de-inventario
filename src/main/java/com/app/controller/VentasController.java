package com.app.controller;

import com.app.model.Cliente;
import com.app.model.Producto;
import com.app.model.Venta;
import com.app.model.CarritoItem;
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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private TableView<CarritoItem> carritoTable;

    @FXML
    private TableColumn<CarritoItem, String> nombreColumn;

    @FXML
    private TableColumn<CarritoItem, String> categoriaColumn;

    @FXML
    private TableColumn<CarritoItem, Double> precioColumn;

    @FXML
    private TableColumn<CarritoItem, Integer> cantidadColumn;

    @FXML
    private TableColumn<CarritoItem, Double> subtotalColumn;

    @FXML
    private Button addToCartButton;

    @FXML
    private Button removeFromCartButton;

    @FXML
    private Label totalLabel;

    @FXML
    private Label todaySalesLabel;

    @FXML
    private Label todaySummaryLabel;

    @FXML
    private Label weekSummaryLabel;

    @FXML
    private Label monthSummaryLabel;

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
    private TableColumn<Venta, String> fechaColumn;

    @FXML
    private TableColumn<Venta, String> clienteColumn;

    @FXML
    private TableColumn<Venta, Double> ventaTotalColumn;

    @FXML
    private TableColumn<Venta, Void> accionesColumn;

    private ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private ObservableList<Producto> productos = FXCollections.observableArrayList();
    private ObservableList<CarritoItem> carrito = FXCollections.observableArrayList();
    private ObservableList<Venta> ventas = FXCollections.observableArrayList();
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
        setupTableColumns();
        loadSampleData();
        setupEventHandlers();
        updateSalesSummary();
    }

    @Override
    public void initializeController() {
        processSaleButton.setDisable(true);
        removeFromCartButton.setDisable(true);
        updateTotal();
    }

    private void setupTableColumns() {
        // Cart table columns
        nombreColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        categoriaColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProducto().getCategoria()));
        precioColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getProducto().getPrecio()).asObject());
        cantidadColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getCantidad()).asObject());
        subtotalColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getSubtotal()).asObject());
        
        carritoTable.setItems(carrito);

        // Sales table columns
        ventaIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fechaColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFecha().format(dateFormatter)));
        clienteColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCliente().getNombre()));
        ventaTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        // Actions column
        accionesColumn.setCellFactory(param -> new TableCell<Venta, Void>() {
            private final Button detailsButton = new Button("View");
            
            {
                detailsButton.getStyleClass().add("btn");
                detailsButton.getStyleClass().add("btn-primary");
                detailsButton.setPrefWidth(70);
                detailsButton.setOnAction(event -> {
                    Venta venta = getTableView().getItems().get(getIndex());
                    showVentaDetails(venta);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
        
        ventasTable.setItems(ventas);
    }

    private void setupEventHandlers() {
        carritoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            removeFromCartButton.setDisable(newSelection == null);
        });

        carrito.addListener((javafx.collections.ListChangeListener<CarritoItem>) change -> {
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
        List<Producto> productosVenta1 = new ArrayList<>(productos.subList(0, 2));
        Venta sampleVenta = new Venta(1L, LocalDateTime.now().minusDays(1), 
            clientes.get(0), productosVenta1, 929.98);
        ventas.add(sampleVenta);
        
        // Add more sample sales for better visualization
        List<Producto> productosVenta2 = new ArrayList<>(productos.subList(1, 3));
        ventas.add(new Venta(2L, LocalDateTime.now().minusHours(3), 
            clientes.get(1), productosVenta2, 109.98));
            
        List<Producto> productosVenta3 = new ArrayList<>(productos.subList(3, 5));
        ventas.add(new Venta(3L, LocalDateTime.now().minusHours(1), 
            clientes.get(2), productosVenta3, 249.98));
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        Producto selected = productoComboBox.getValue();
        if (selected != null) {
            if (selected.isAvailable()) {
                // Check if product already exists in cart
                CarritoItem existingItem = carrito.stream()
                    .filter(item -> item.getProducto().getId().equals(selected.getId()))
                    .findFirst()
                    .orElse(null);
                
                if (existingItem != null) {
                    // Increase quantity if product already in cart
                    if (existingItem.getCantidad() < selected.getStock()) {
                        existingItem.incrementarCantidad();
                        carritoTable.refresh();
                        showInfoAlert("Cart Updated", "Product quantity increased!");
                    } else {
                        showWarningAlert("Stock Limit", "Cannot add more of this product. Stock limit reached.");
                    }
                } else {
                    // Add new item to cart
                    carrito.add(new CarritoItem(selected, 1));
                    showInfoAlert("Success", "Product added to cart!");
                }
            } else {
                showWarningAlert("Stock Warning", "Product is out of stock!");
            }
        } else {
            showWarningAlert("Selection Error", "Please select a product to add.");
        }
    }

    @FXML
    private void handleRemoveFromCart(ActionEvent event) {
        CarritoItem selected = carritoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getCantidad() > 1) {
                // Decrease quantity if more than 1
                selected.decrementarCantidad();
                carritoTable.refresh();
                showInfoAlert("Cart Updated", "Product quantity decreased!");
            } else {
                // Remove item if quantity is 1
                carrito.remove(selected);
                showInfoAlert("Success", "Product removed from cart!");
            }
        }
    }

    @FXML
    private void handleProcessSale(ActionEvent event) {
        Cliente selectedCliente = clienteComboBox.getValue();
        if (selectedCliente != null && !carrito.isEmpty()) {
            try {
                // Create sale items list
                List<Producto> productosVenta = new ArrayList<>();
                for (CarritoItem item : carrito) {
                    for (int i = 0; i < item.getCantidad(); i++) {
                        productosVenta.add(item.getProducto());
                    }
                }
                
                Venta newVenta = new Venta(
                    (long) (ventas.size() + 1),
                    LocalDateTime.now(),
                    selectedCliente,
                    productosVenta,
                    calculateTotal()
                );
                
                ventas.add(newVenta);
                
                // Update product stock
                for (CarritoItem item : carrito) {
                    Producto producto = item.getProducto();
                    if (producto.getStock() >= item.getCantidad()) {
                        producto.decreaseStock(item.getCantidad());
                    } else {
                        showErrorAlert("Stock Error", "Not enough stock for: " + producto.getNombre());
                        return;
                    }
                }
                
                clearCart();
                updateSalesSummary();
                showInfoAlert("Success", "Sale processed successfully!\nTotal: $" + 
                    String.format("%.2f", newVenta.getTotal()));
                
            } catch (Exception e) {
                showErrorAlert("Processing Error", "Could not process sale: " + e.getMessage());
                e.printStackTrace();
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
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
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
            showErrorAlert("Navigation Error", "Could not load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearCart() {
        carrito.clear();
        clienteComboBox.setValue(null);
        productoComboBox.setValue(null);
    }

    private double calculateTotal() {
        return carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum();
    }

    private void updateTotal() {
        double total = calculateTotal();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
    
    private void updateSalesSummary() {
        // Calculate today's sales
        double todaySales = ventas.stream()
            .filter(venta -> venta.getFecha().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
            .mapToDouble(Venta::getTotal)
            .sum();
        
        todaySalesLabel.setText(String.format("Today's Sales: $%.2f", todaySales));
        todaySummaryLabel.setText(String.format("$%.2f", todaySales));
        
        // Calculate weekly and monthly sales (simplified for demo)
        double weeklySales = ventas.stream()
            .mapToDouble(Venta::getTotal)
            .sum() * 1.5; // Simulated data
        
        double monthlySales = ventas.stream()
            .mapToDouble(Venta::getTotal)
            .sum() * 5; // Simulated data
            
        weekSummaryLabel.setText(String.format("$%.2f", weeklySales));
        monthSummaryLabel.setText(String.format("$%.2f", monthlySales));
    }
    
    private void showVentaDetails(Venta venta) {
        StringBuilder details = new StringBuilder();
        details.append("Sale ID: ").append(venta.getId()).append("\n");
        details.append("Date: ").append(venta.getFecha().format(dateFormatter)).append("\n");
        details.append("Customer: ").append(venta.getCliente().getNombre()).append("\n");
        details.append("Total: $").append(String.format("%.2f", venta.getTotal())).append("\n");
        details.append("Items: ").append(venta.getTotalItems()).append("\n\n");
        details.append("Products:\n");
        
        // Usamos getListaProductos() en lugar de getProductos()
        for (Producto producto : venta.getListaProductos()) {
            details.append("- ").append(producto.getNombre())
                  .append(" ($").append(String.format("%.2f", producto.getPrecio())).append(")\n");
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sale Details");
        alert.setHeaderText("Sale Information");
        alert.setContentText(details.toString());
        alert.showAndWait();
    }


}