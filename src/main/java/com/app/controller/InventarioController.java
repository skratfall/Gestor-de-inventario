package com.app.controller;

import javafx.beans.property.SimpleStringProperty;
import com.app.model.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InventarioController {

    @FXML private TableView<Producto> productosTable;
    @FXML private TableColumn<Producto, Long> idColumn;
    @FXML private TableColumn<Producto, String> nombreColumn;
    @FXML private TableColumn<Producto, String> categoriaColumn;
    @FXML private TableColumn<Producto, Double> precioColumn;
    @FXML private TableColumn<Producto, Integer> stockColumn;
    @FXML private TableColumn<Producto, String> statusColumn;

    @FXML private TextField searchField;
    @FXML private TextField nombreField;
    @FXML private TextField categoriaField;
    @FXML private TextField precioField;
    @FXML private TextField stockField;

    @FXML private VBox productFormCard;
    @FXML private Label formTitleLabel;
    @FXML private Button saveButton;
    @FXML private Button clearFormButton;
    @FXML private Button cancelFormButton;
    @FXML private Button backButton;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> filteredProductos;

    private Producto productoEditando = null;

    @FXML
    public void initialize() {
        // Vincular columnas
        idColumn.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        nombreColumn.setCellValueFactory(cell -> cell.getValue().nombreProperty());
        categoriaColumn.setCellValueFactory(cell -> cell.getValue().categoriaProperty());
        precioColumn.setCellValueFactory(cell -> cell.getValue().precioProperty().asObject());
        stockColumn.setCellValueFactory(cell -> cell.getValue().stockProperty().asObject());
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

        // Datos iniciales
        productos.addAll(
                new Producto(1L, "Laptop Lenovo", "Electrónica", 2500.0, 10),
                new Producto(2L, "Teclado Mecánico", "Accesorios", 120.0, 25),
                new Producto(3L, "Silla Gamer", "Muebles", 600.0, 5)
        );

        // Filtro de búsqueda
        filteredProductos = new FilteredList<>(productos, p -> true);
        productosTable.setItems(filteredProductos);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtro = newVal.toLowerCase().trim();
            filteredProductos.setPredicate(producto ->
                    producto.getNombre().toLowerCase().contains(filtro) ||
                    producto.getCategoria().toLowerCase().contains(filtro) ||
                    String.valueOf(producto.getId()).contains(filtro)
            );
        });

        // Botones del formulario
        saveButton.setOnAction(e -> handleSave());
        clearFormButton.setOnAction(e -> clearForm());
        cancelFormButton.setOnAction(e -> toggleForm(false));

        toggleForm(false); // Form oculto al inicio
    }

    // ➕ Mostrar formulario para agregar
    @FXML
    private void handleAdd(ActionEvent event) {
        productoEditando = null;
        formTitleLabel.setText("Agregar Nuevo Producto");
        toggleForm(true);
        clearForm();
    }

    // ✏️ Mostrar formulario con datos para editar
    @FXML
    private void handleUpdate(ActionEvent event) {
        Producto selected = productosTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            productoEditando = selected;
            formTitleLabel.setText("Editar Producto");

            nombreField.setText(selected.getNombre());
            categoriaField.setText(selected.getCategoria());
            precioField.setText(String.valueOf(selected.getPrecio()));
            stockField.setText(String.valueOf(selected.getStock()));

            toggleForm(true);
        } else {
            showWarning("Seleccione un producto para editar.");
        }
    }

    // 🗑️ Eliminar producto
    @FXML
    private void handleDelete(ActionEvent event) {
        Producto selected = productosTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar este producto?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    productos.remove(selected);
                }
            });
        } else {
            showWarning("Seleccione un producto para eliminar.");
        }
    }

    @FXML
    private void handleCancelForm(ActionEvent event) {
        clearForm();
        toggleForm(false);
    }

    // Guardar (Agregar o Editar)
    private void handleSave() {
        if (!validateForm()) return;

        try {
            String nombre = nombreField.getText().trim();
            String categoria = categoriaField.getText().trim();
            double precio = Double.parseDouble(precioField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());

            if (productoEditando == null) {
                // Nuevo
                Producto nuevo = new Producto((long) (productos.size() + 1), nombre, categoria, precio, stock);
                productos.add(nuevo);
                showInfo("Producto agregado con éxito.");
            } else {
                // Editar
                productoEditando.setNombre(nombre);
                productoEditando.setCategoria(categoria);
                productoEditando.setPrecio(precio);
                productoEditando.setStock(stock);
                productosTable.refresh();
                showInfo("Producto actualizado con éxito.");
            }

            clearForm();
            toggleForm(false);

        } catch (NumberFormatException e) {
            showError("Ingrese valores válidos para precio y stock.");
        }
    }

    // 🔙 Método que faltaba
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/DashboardView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Dashboard - JavaFX Application");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo cargar el Dashboard: " + e.getMessage());
        }
    }

    // Helpers
    private void toggleForm(boolean show) {
        productFormCard.setVisible(show);
        productFormCard.setManaged(show);
    }

    private void clearForm() {
        nombreField.clear();
        categoriaField.clear();
        precioField.clear();
        stockField.clear();
    }

    private boolean validateForm() {
        return !nombreField.getText().isEmpty()
                && !categoriaField.getText().isEmpty()
                && !precioField.getText().isEmpty()
                && !stockField.getText().isEmpty();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).show();
    }

    private void showWarning(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).show();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).show();
    }
}
