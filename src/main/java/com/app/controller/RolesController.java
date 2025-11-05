package com.app.controller;

import com.app.model.Rol;
import com.app.dao.RolDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class RolesController implements Initializable {

    @FXML private TableView<Rol> tableRoles;
    @FXML private TableColumn<Rol, String> colId;
    @FXML private TableColumn<Rol, String> colNombre;
    @FXML private TableColumn<Rol, String> colDescripcion;
    @FXML private TableColumn<Rol, String> colNivelAcceso;
    
    @FXML private TextField txtBuscar;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cmbNivelAcceso;
    @FXML private Label lblTotalRoles;
    
    @FXML private VBox formPane;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnEditarRol;
    @FXML private Button btnEliminarRol;

    private RolDAO rolDAO;
    private ObservableList<Rol> rolesData;
    private Rol rolEditando;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rolDAO = new RolDAO();
        rolesData = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupNivelesAcceso();
        cargarRoles();
        ocultarFormulario();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getId()));
            
        colNombre.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNombre()));
            
        colDescripcion.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDescripcion()));
            
        colNivelAcceso.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.valueOf(cellData.getValue().getNivelAcceso())));

        tableRoles.setItems(rolesData);
    }

    private void setupNivelesAcceso() {
        cmbNivelAcceso.setItems(FXCollections.observableArrayList(
            "1 - Básico",
            "2 - Intermedio",
            "3 - Avanzado",
            "4 - Administrador"
        ));
    }

    private void cargarRoles() {
        try {
            List<Rol> roles = rolDAO.findAll();
            rolesData.clear();
            rolesData.addAll(roles);
            actualizarTotalRoles();
        } catch (Exception e) {
            mostrarError("Error al cargar roles", e.getMessage());
        }
    }

    @FXML
    private void handleNuevoRol() {
        rolEditando = null;
        limpiarFormulario();
        mostrarFormulario("Nuevo Rol");
    }

    @FXML
    private void handleEditarRol() {
        Rol seleccionado = tableRoles.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            rolEditando = seleccionado;
            cargarDatosEnFormulario(seleccionado);
            mostrarFormulario("Editar Rol");
        } else {
            mostrarAdvertencia("Seleccione un rol", "Por favor seleccione un rol para editar");
        }
    }

    @FXML
    private void handleEliminarRol() {
        Rol seleccionado = tableRoles.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar este rol?");
            alert.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    rolDAO.delete(seleccionado.getId());
                    cargarRoles();
                    mostrarInfo("Rol eliminado", "El rol ha sido eliminado exitosamente");
                } catch (Exception e) {
                    mostrarError("Error al eliminar rol", e.getMessage());
                }
            }
        } else {
            mostrarAdvertencia("Seleccione un rol", "Por favor seleccione un rol para eliminar");
        }
    }

    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) {
            mostrarAdvertencia("Datos incompletos", "Por favor complete todos los campos obligatorios");
            return;
        }

        try {
            Rol rol = new Rol();
            if (rolEditando != null) {
                rol.setId(rolEditando.getId());
            }
            
            rol.setNombre(txtNombre.getText().trim());
            rol.setDescripcion(txtDescripcion.getText().trim());
            rol.setNivelAcceso(Integer.parseInt(cmbNivelAcceso.getValue().split(" - ")[0]));

            // Validar duplicados
            if (rolEditando == null && rolDAO.existsByNombre(rol.getNombre())) {
                mostrarAdvertencia("Rol duplicado", "Ya existe un rol con este nombre");
                return;
            }

            if (rolEditando == null) {
                rolDAO.save(rol);
                mostrarInfo("Rol creado", "El rol ha sido creado exitosamente");
            } else {
                rolDAO.update(rol);
                mostrarInfo("Rol actualizado", "El rol ha sido actualizado exitosamente");
            }

            cargarRoles();
            ocultarFormulario();
        } catch (Exception e) {
            mostrarError("Error al guardar rol", e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        ocultarFormulario();
    }

    @FXML
    private void handleBuscar() {
        String filtro = txtBuscar.getText().toLowerCase().trim();
        if (filtro.isEmpty()) {
            cargarRoles();
            return;
        }

        try {
            List<Rol> roles = rolDAO.findAll();
            List<Rol> filtrados = roles.stream()
                .filter(r -> r.getNombre().toLowerCase().contains(filtro) ||
                           r.getDescripcion().toLowerCase().contains(filtro))
                .toList();

            rolesData.clear();
            rolesData.addAll(filtrados);
            actualizarTotalRoles();
        } catch (Exception e) {
            mostrarError("Error al buscar", e.getMessage());
        }
    }

    @FXML
    private void handleRefrescar() {
        txtBuscar.clear();
        cargarRoles();
    }

    @FXML
    private void handleBackToDashboard(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/DashboardView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - Gestión de Inventario");
            
        } catch (IOException e) {
            mostrarError("Error", "No se pudo cargar el dashboard: " + e.getMessage());
        }
    }

    private void mostrarFormulario(String titulo) {
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    private void ocultarFormulario() {
        formPane.setVisible(false);
        formPane.setManaged(false);
        rolEditando = null;
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtDescripcion.clear();
        cmbNivelAcceso.getSelectionModel().clearSelection();
    }

    private void cargarDatosEnFormulario(Rol rol) {
        txtNombre.setText(rol.getNombre());
        txtDescripcion.setText(rol.getDescripcion());
        cmbNivelAcceso.setValue(rol.getNivelAcceso() + " - " + obtenerNivelAccesoTexto(rol.getNivelAcceso()));
    }

    private String obtenerNivelAccesoTexto(int nivel) {
        return switch (nivel) {
            case 1 -> "Básico";
            case 2 -> "Intermedio";
            case 3 -> "Avanzado";
            case 4 -> "Administrador";
            default -> "Desconocido";
        };
    }

    private boolean validarFormulario() {
        return !txtNombre.getText().trim().isEmpty() &&
               !txtDescripcion.getText().trim().isEmpty() &&
               cmbNivelAcceso.getValue() != null;
    }

    private void actualizarTotalRoles() {
        lblTotalRoles.setText("Total: " + rolesData.size() + " roles");
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}