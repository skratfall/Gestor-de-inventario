package com.app.controller;

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
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the Inventory view
 */
public class InventarioController extends BaseController implements Initializable {

    @FXML
    private TableView<Producto> productosTable;

    @FXML
    private TableColumn<Producto, Long> idColumn;

    @FXML
    private TableColumn<Producto, String> nombreColumn;

    @FXML
    private TableColumn<Producto, String> categoriaColumn;

    @FXML
    private TableColumn<Producto, Double> precioColumn;

    @FXML
    private TableColumn<Producto, Integer> stockColumn;

    @FXML
    private TextField nombreField;

    @FXML
    private TextField categoriaField;

    @FXML
    private TextField precioField;

    @FXML
    private TextField stockField;

    @FXML
    private Button addButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private ObservableList<Producto> productos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
        setupTableColumns();
        loadSampleData();
        setupTableSelection();
    }

    @Override
    public void initializeController() {
        // Initialize form validation
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        productosTable.setItems(productos);
    }

    private void setupTableSelection() {
        productosTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                clearForm();
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    private void loadSampleData() {
        // TODO: Replace with actual data from DAO
        productos.addAll(
            new Producto(1L, "Laptop Dell", "Electronics", 899.99, 15),
            new Producto(2L, "Mouse Logitech", "Electronics", 29.99, 50),
            new Producto(3L, "Keyboard Mechanical", "Electronics", 79.99, 25),
            new Producto(4L, "Monitor 24\"", "Electronics", 199.99, 12),
            new Producto(5L, "Webcam HD", "Electronics", 49.99, 30)
        );
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (validateForm()) {
            try {
                Producto newProducto = new Producto(
                    (long) (productos.size() + 1),
                    nombreField.getText().trim(),
                    categoriaField.getText().trim(),
                    Double.parseDouble(precioField.getText().trim()),
                    Integer.parseInt(stockField.getText().trim())
                );
                
                productos.add(newProducto);
                clearForm();
                showInfoAlert("Success", "Product added successfully!");
                
            } catch (NumberFormatException e) {
                showErrorAlert("Input Error", "Please enter valid numbers for price and stock.");
            }
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        Producto selected = productosTable.getSelectionModel().getSelectedItem();
        if (selected != null && validateForm()) {
            try {
                selected.setNombre(nombreField.getText().trim());
                selected.setCategoria(categoriaField.getText().trim());
                selected.setPrecio(Double.parseDouble(precioField.getText().trim()));
                selected.setStock(Integer.parseInt(stockField.getText().trim()));
                
                productosTable.refresh();
                showInfoAlert("Success", "Product updated successfully!");
                
            } catch (NumberFormatException e) {
                showErrorAlert("Input Error", "Please enter valid numbers for price and stock.");
            }
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Producto selected = productosTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Are you sure you want to delete this product?");
            
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                productos.remove(selected);
                clearForm();
                showInfoAlert("Success", "Product deleted successfully!");
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

    private boolean validateForm() {
        if (nombreField.getText().trim().isEmpty()) {
            showWarningAlert("Validation Error", "Product name is required.");
            return false;
        }
        if (categoriaField.getText().trim().isEmpty()) {
            showWarningAlert("Validation Error", "Category is required.");
            return false;
        }
        if (precioField.getText().trim().isEmpty()) {
            showWarningAlert("Validation Error", "Price is required.");
            return false;
        }
        if (stockField.getText().trim().isEmpty()) {
            showWarningAlert("Validation Error", "Stock is required.");
            return false;
        }
        return true;
    }

    private void populateForm(Producto producto) {
        nombreField.setText(producto.getNombre());
        categoriaField.setText(producto.getCategoria());
        precioField.setText(String.valueOf(producto.getPrecio()));
        stockField.setText(String.valueOf(producto.getStock()));
    }

    private void clearForm() {
        nombreField.clear();
        categoriaField.clear();
        precioField.clear();
        stockField.clear();
        productosTable.getSelectionModel().clearSelection();
    }
}