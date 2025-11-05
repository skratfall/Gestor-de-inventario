package com.app.controller;

import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.service.UsuarioService;
import com.app.dao.RolDAO;
import com.app.security.SessionManager;
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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UsuariosController implements Initializable {

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombreCompleto;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colActivo;
    @FXML private TableColumn<Usuario, String> colUltimoAcceso;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotalUsuarios;
    @FXML private Button btnBackToDashboard;
    @FXML private Button btnNuevoUsuario;
    @FXML private Button btnEditarUsuario;
    @FXML private Button btnEliminarUsuario;
    @FXML private Button btnResetPassword;

    private UsuarioService usuarioService;
    private RolDAO rolDAO;
    private SessionManager sessionManager;
    private ObservableList<Usuario> usuariosData;
    private DateTimeFormatter dateFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usuarioService = UsuarioService.getInstance();
        rolDAO = new RolDAO();
        sessionManager = SessionManager.getInstance();
        usuariosData = FXCollections.observableArrayList();
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        setupTableColumns();
        setupPermissions();
        loadUsuarios();
    }

    private void setupTableColumns() {
        colUsername.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getUsername()));

        colNombreCompleto.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNombreCompleto()));

        colEmail.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getEmail()));

        colRol.setCellValueFactory(cellData -> {
            String rolId = cellData.getValue().getRolId();
            Optional<Rol> rol = rolDAO.findById(rolId);
            return new SimpleStringProperty(rol.isPresent() ? rol.get().getNombre() : "N/A");
        });

        colActivo.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isActivo() ? "Activo" : "Inactivo"));

        colUltimoAcceso.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUltimoAcceso() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getUltimoAcceso().format(dateFormatter));
            }
            return new SimpleStringProperty("Nunca");
        });

        tableUsuarios.setItems(usuariosData);
    }

    private void setupPermissions() {
        boolean canCreate = sessionManager.hasPermission("usuarios", "create");
        boolean canUpdate = sessionManager.hasPermission("usuarios", "update");
        boolean canDelete = sessionManager.hasPermission("usuarios", "delete");

        btnNuevoUsuario.setDisable(!canCreate);
        btnEditarUsuario.setDisable(!canUpdate);
        btnEliminarUsuario.setDisable(!canDelete);
        btnResetPassword.setDisable(!sessionManager.isAdmin());
    }

    private void loadUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.findAllUsuarios();
            usuariosData.clear();
            usuariosData.addAll(usuarios);
            lblTotalUsuarios.setText("Total: " + usuarios.size() + " usuarios");
        } catch (Exception e) {
            showError("Error al cargar usuarios", e.getMessage());
        }
    }

    @FXML
    private void handleNuevoUsuario() {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.setHeaderText("Crear nuevo usuario del sistema");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Usuario");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");

        ComboBox<String> cmbRol = new ComboBox<>();
        List<Rol> roles = rolDAO.findAll();
        for (Rol rol : roles) {
            cmbRol.getItems().add(rol.getNombre());
        }
        cmbRol.getSelectionModel().selectFirst();

        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(txtPassword, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Nombre:"), 0, 3);
        grid.add(txtNombre, 1, 3);
        grid.add(new Label("Rol:"), 0, 4);
        grid.add(cmbRol, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                try {
                    return usuarioService.createUsuario(
                        txtUsername.getText(),
                        txtPassword.getText(),
                        txtEmail.getText(),
                        txtNombre.getText(),
                        cmbRol.getValue()
                    );
                } catch (Exception e) {
                    showError("Error al crear usuario", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Usuario> result = dialog.showAndWait();
        if (result.isPresent()) {
            showInfo("Usuario creado", "El usuario ha sido creado exitosamente");
            loadUsuarios();
        }
    }

    @FXML
    private void handleEditarUsuario() {
        Usuario selected = tableUsuarios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Seleccione un usuario", "Por favor seleccione un usuario para editar");
            return;
        }

        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
        dialog.setHeaderText("Modificar datos del usuario");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtUsername = new TextField(selected.getUsername());
        TextField txtEmail = new TextField(selected.getEmail());
        TextField txtNombre = new TextField(selected.getNombreCompleto());
        CheckBox chkActivo = new CheckBox();
        chkActivo.setSelected(selected.isActivo());

        ComboBox<String> cmbRol = new ComboBox<>();
        List<Rol> roles = rolDAO.findAll();
        Optional<Rol> currentRol = rolDAO.findById(selected.getRolId());
        for (Rol rol : roles) {
            cmbRol.getItems().add(rol.getNombre());
        }
        if (currentRol.isPresent()) {
            cmbRol.setValue(currentRol.get().getNombre());
        }

        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(new Label("Nombre:"), 0, 2);
        grid.add(txtNombre, 1, 2);
        grid.add(new Label("Rol:"), 0, 3);
        grid.add(cmbRol, 1, 3);
        grid.add(new Label("Activo:"), 0, 4);
        grid.add(chkActivo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                try {
                    return usuarioService.updateUsuario(
                        selected.getId(),
                        txtUsername.getText(),
                        txtEmail.getText(),
                        txtNombre.getText(),
                        cmbRol.getValue(),
                        chkActivo.isSelected()
                    );
                } catch (Exception e) {
                    showError("Error al actualizar usuario", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Usuario> result = dialog.showAndWait();
        if (result.isPresent()) {
            showInfo("Usuario actualizado", "El usuario ha sido actualizado exitosamente");
            loadUsuarios();
        }
    }

    @FXML
    private void handleEliminarUsuario() {
        Usuario selected = tableUsuarios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Seleccione un usuario", "Por favor seleccione un usuario para eliminar");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar este usuario?");
        alert.setContentText("Usuario: " + selected.getUsername() + "\nEsta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                usuarioService.deleteUsuario(selected.getId());
                showInfo("Usuario eliminado", "El usuario ha sido eliminado exitosamente");
                loadUsuarios();
            } catch (Exception e) {
                showError("Error al eliminar usuario", e.getMessage());
            }
        }
    }

    @FXML
    private void handleResetPassword() {
        Usuario selected = tableUsuarios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Seleccione un usuario", "Por favor seleccione un usuario");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Restablecer Contraseña");
        dialog.setHeaderText("Restablecer contraseña para: " + selected.getUsername());

        ButtonType btnGuardar = new ButtonType("Restablecer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField txtNewPassword = new PasswordField();
        txtNewPassword.setPromptText("Nueva contraseña");
        PasswordField txtConfirmPassword = new PasswordField();
        txtConfirmPassword.setPromptText("Confirmar contraseña");

        grid.add(new Label("Nueva contraseña:"), 0, 0);
        grid.add(txtNewPassword, 1, 0);
        grid.add(new Label("Confirmar:"), 0, 1);
        grid.add(txtConfirmPassword, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                if (!txtNewPassword.getText().equals(txtConfirmPassword.getText())) {
                    showError("Error", "Las contraseñas no coinciden");
                    return null;
                }
                try {
                    usuarioService.resetPassword(selected.getId(), txtNewPassword.getText());
                    return txtNewPassword.getText();
                } catch (Exception e) {
                    showError("Error al restablecer contraseña", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            showInfo("Contraseña restablecida", "La contraseña ha sido restablecida exitosamente");
        }
    }

    @FXML
    private void handleBuscar() {
        String searchText = txtBuscar.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            loadUsuarios();
            return;
        }

        try {
            List<Usuario> allUsuarios = usuarioService.findAllUsuarios();
            List<Usuario> filtered = allUsuarios.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(searchText) ||
                           (u.getNombreCompleto() != null &&
                            u.getNombreCompleto().toLowerCase().contains(searchText)) ||
                           (u.getEmail() != null &&
                            u.getEmail().toLowerCase().contains(searchText)))
                .toList();

            usuariosData.clear();
            usuariosData.addAll(filtered);
            lblTotalUsuarios.setText("Encontrados: " + filtered.size() + " usuarios");
        } catch (Exception e) {
            showError("Error al buscar", e.getMessage());
        }
    }

    @FXML
    private void handleRefrescar() {
        txtBuscar.clear();
        loadUsuarios();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

 @FXML
    public void handleBackToDashboard(Event event) {
        try {
            // Cargar el FXML del Dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/DashboardView.fxml")); // Ajusta la ruta según tu estructura de paquetes
            Parent root = loader.load();
            
            // Obtener el Stage actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Crear nueva escena y asignarla al Stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Opcional: ajustar el título de la ventana
            stage.setTitle("Dashboard - Gestión de Inventario");
            
        } catch (IOException e) {
            e.printStackTrace();
            // Manejo de errores, por ejemplo, mostrar un diálogo de error
        }
    }
}
