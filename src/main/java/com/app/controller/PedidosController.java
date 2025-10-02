package com.app.controller;

import com.app.model.Cliente;
import com.app.model.Pedido;
import com.app.model.Producto;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Controller class for the Orders view
 */
public class PedidosController extends BaseController implements Initializable {

    @FXML
    private TableView<Pedido> pedidosTable;

    @FXML
    private TableColumn<Pedido, Long> idColumn;

    @FXML
    private TableColumn<Pedido, LocalDateTime> fechaColumn;

    @FXML
    private TableColumn<Pedido, String> clienteColumn;

    @FXML
    private TableColumn<Pedido, String> estadoColumn;

    @FXML
    private TableColumn<Pedido, Integer> itemsColumn;

    @FXML
    private TableColumn<Pedido, Double> totalColumn;

    @FXML
    private ComboBox<String> estadoComboBox;

    @FXML
    private Button updateStatusButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    @FXML
    private Button inventarioButton;

    @FXML
    private Button ventasButton;

    @FXML
    private Button reportesButton;

    @FXML
    private TextArea detailsTextArea;

    @FXML
    private Label selectedOrderLabel;

    private ObservableList<Pedido> pedidos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
        setupTableColumns();
        loadSampleData();
        setupEventHandlers();
    }

    @Override
    public void initializeController() {
        updateStatusButton.setDisable(true);
        viewDetailsButton.setDisable(true);
        
        // Setup status combo box
        estadoComboBox.setItems(FXCollections.observableArrayList(
            Pedido.ESTADO_PENDIENTE,
            Pedido.ESTADO_PROCESANDO,
            Pedido.ESTADO_ENVIADO,
            Pedido.ESTADO_ENTREGADO,
            Pedido.ESTADO_CANCELADO
        ));
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        clienteColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCliente().getNombre()
            )
        );
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        itemsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getTotalItems()
            ).asObject()
        );
        totalColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().calculateTotal()
            ).asObject()
        );
        
        // Add custom cell factory for status column with colored labels
        estadoColumn.setCellFactory(column -> {
            return new TableCell<Pedido, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);

                        // Apply color based on status
                        switch (item) {
                            case "ENTREGADO":
                                setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-background-radius: 4;");
                                break;
                            case "PENDIENTE":
                                setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold; -fx-background-radius: 4;");
                                break;
                            case "PROCESANDO":
                                setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold; -fx-background-radius: 4;");
                                break;
                            case "ENVIADO":
                                setStyle("-fx-background-color: #cce5ff; -fx-text-fill: #004085; -fx-font-weight: bold; -fx-background-radius: 4;");
                                break;
                            case "CANCELADO":
                                setStyle("-fx-background-color: #f5c6cb; -fx-text-fill: #721c24; -fx-font-weight: bold; -fx-background-radius: 4;");
                                break;
                            default:
                                setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #383d41; -fx-font-weight: bold; -fx-background-radius: 4;");
                                break;
                        }
                    }
                }
            };
        });

        pedidosTable.setItems(pedidos);
    }

    private void setupEventHandlers() {
        pedidosTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateStatusButton.setDisable(false);
                viewDetailsButton.setDisable(false);
                estadoComboBox.setValue(newSelection.getEstado());
                selectedOrderLabel.setText("Order #" + newSelection.getId());
                showOrderDetails(newSelection);
            } else {
                updateStatusButton.setDisable(true);
                viewDetailsButton.setDisable(true);
                estadoComboBox.setValue(null);
                selectedOrderLabel.setText("No order selected");
                detailsTextArea.clear();
            }
        });
    }

    private void loadSampleData() {
        // Create sample clients
        Cliente cliente1 = new Cliente(1L, "Juan Pérez", "555-0101", "juan@email.com");
        Cliente cliente2 = new Cliente(2L, "María García", "555-0102", "maria@email.com");
        Cliente cliente3 = new Cliente(3L, "Carlos López", "555-0103", "carlos@email.com");

        // Create sample products
        Producto producto1 = new Producto(1L, "Laptop Dell", "Electronics", 899.99, 15);
        Producto producto2 = new Producto(2L, "Mouse Logitech", "Electronics", 29.99, 50);
        Producto producto3 = new Producto(3L, "Monitor 24\"", "Electronics", 199.99, 12);

        // Create sample orders
        pedidos.addAll(
            new Pedido(1L, LocalDateTime.now().minusDays(2), cliente1, 
                Pedido.ESTADO_PENDIENTE, Arrays.asList(producto1, producto2)),
            new Pedido(2L, LocalDateTime.now().minusDays(1), cliente2, 
                Pedido.ESTADO_PROCESANDO, Arrays.asList(producto3)),
            new Pedido(3L, LocalDateTime.now().minusHours(5), cliente3, 
                Pedido.ESTADO_ENVIADO, Arrays.asList(producto1, producto3)),
            new Pedido(4L, LocalDateTime.now().minusHours(2), cliente1, 
                Pedido.ESTADO_ENTREGADO, Arrays.asList(producto2, producto3)),
            new Pedido(5L, LocalDateTime.now().minusMinutes(30), cliente2, 
                Pedido.ESTADO_PENDIENTE, Arrays.asList(producto1, producto2, producto3))
        );
    }

    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        Pedido selected = pedidosTable.getSelectionModel().getSelectedItem();
        String newStatus = estadoComboBox.getValue();
        
        if (selected != null && newStatus != null) {
            String oldStatus = selected.getEstado();
            selected.setEstado(newStatus);
            pedidosTable.refresh();
            
            //Alert boton ACTUALIZAR ESTADO
            showInfoAlert("Estado actualizado", 
                "Pedido #" + selected.getId() + " estado cambiado de " + 
                oldStatus + " a " + newStatus);
            
            showOrderDetails(selected);
        }
    }

    @FXML
    private void handleViewDetails(ActionEvent event) {
        Pedido selected = pedidosTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showOrderDetails(selected);
        }
    }

    //Alertas al presionas boton ACTUALIZAR
    @FXML
    private void handleRefresh(ActionEvent event) {
        // TODO: Reload data from database
        pedidosTable.refresh();
        showInfoAlert("Actualizado", "La lista de pedidos ha sido actualizada.");
    }

    @FXML
    private void handleInventario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/InventarioView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) inventarioButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("Inventory - JavaFX Application");
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error de navegación", "No es posible cargar inventario: " + e.getMessage());
        }
    }

    @FXML
    private void handleVentas(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/VentasView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ventasButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("Sales - JavaFX Application");
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not load sales: " + e.getMessage());
        }
    }

    @FXML
    private void handleReportes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/ReportesView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) reportesButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("Reports - JavaFX Application");
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not load reports: " + e.getMessage());
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

    private void showOrderDetails(Pedido pedido) {
        StringBuilder details = new StringBuilder();
        details.append("Order Details\n");
        details.append("=============\n\n");
        details.append("Order ID: ").append(pedido.getId()).append("\n");
        details.append("Date: ").append(pedido.getFecha()).append("\n");
        details.append("Client: ").append(pedido.getCliente().getNombre()).append("\n");
        details.append("Phone: ").append(pedido.getCliente().getTelefono()).append("\n");
        details.append("Email: ").append(pedido.getCliente().getEmail()).append("\n");
        details.append("Status: ").append(pedido.getEstado()).append("\n\n");
        
        details.append("Products:\n");
        details.append("---------\n");
        for (Producto producto : pedido.getListaProductos()) {
            details.append("• ").append(producto.getNombre())
                   .append(" - $").append(String.format("%.2f", producto.getPrecio()))
                   .append(" (").append(producto.getCategoria()).append(")\n");
        }
        
        details.append("\nTotal Items: ").append(pedido.getTotalItems()).append("\n");
        details.append("Total Amount: $").append(String.format("%.2f", pedido.calculateTotal()));
        
        detailsTextArea.setText(details.toString());
    }

    public void refreshOrders() {
        // TODO: Implement actual data refresh from database
        pedidosTable.refresh();
    }
}