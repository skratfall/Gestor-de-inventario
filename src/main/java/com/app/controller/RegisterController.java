package com.app.controller;

import com.app.dao.RolDAO;
import com.app.model.Rol;
import com.app.service.UsuarioService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField nombreCompletoField;

    @FXML
    private Button registerButton;

    @FXML
    private Button cancelButton;

    private UsuarioService usuarioService;
    private RolDAO rolDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.usuarioService = UsuarioService.getInstance();
        this.rolDAO = new RolDAO();
        setupValidation();

        javafx.application.Platform.runLater(() -> {
            if (registerButton != null && registerButton.getScene() != null) {
                Stage stage = (Stage) registerButton.getScene().getWindow();
                stage.setWidth(500);
                stage.setHeight(600);
                stage.centerOnScreen();
            }
        });
    }

    private void setupValidation() {
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        nombreCompletoField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        validateForm();
    }

    private void validateForm() {
        boolean isValid = !usernameField.getText().trim().isEmpty() &&
                         !passwordField.getText().trim().isEmpty() &&
                         !confirmPasswordField.getText().trim().isEmpty() &&
                         !emailField.getText().trim().isEmpty() &&
                         !nombreCompletoField.getText().trim().isEmpty();
        registerButton.setDisable(!isValid);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();
        String nombreCompleto = nombreCompletoField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || nombreCompleto.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Todos los campos son obligatorios.");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Las contraseñas no coinciden.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El formato del email no es válido.");
            return;
        }

        try {
            Optional<Rol> adminRolOpt = rolDAO.findByNombre("ADMIN");
            if (!adminRolOpt.isPresent()) {
                showAlert(Alert.AlertType.ERROR, "Error del Sistema",
                    "No se encontró el rol ADMIN en el sistema. Por favor, contacte al soporte técnico.");
                return;
            }

            Rol adminRol = adminRolOpt.get();
            boolean success = usuarioService.createUsuarioAsSystem(
                username, password, email, nombreCompleto, adminRol.getId(), true
            );

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso",
                    "El usuario administrador ha sido creado exitosamente.\n\nAhora puede iniciar sesión.");
                navigateToLogin();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de Registro",
                    "No se pudo crear el usuario. El nombre de usuario o email pueden estar en uso.");
            }

        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error del Sistema",
                "Ocurrió un error al registrar el usuario: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 800);
            stage.setTitle("Login - Sistema de Gestión de Inventario");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Navegación",
                "No se pudo cargar la pantalla de login: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void clearForm() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.clear();
        nombreCompletoField.clear();
        usernameField.requestFocus();
    }
}
