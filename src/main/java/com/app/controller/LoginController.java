package com.app.controller;

import com.app.model.Usuario;
import com.app.service.LanguageService;
import com.app.service.SeguridadService;
import com.app.util.I18nUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the Login view
 */
public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button cancelButton;

    @FXML
    private javafx.scene.control.Label lblWelcome;

    @FXML
    private javafx.scene.control.Label lblSubtitle;

    @FXML
    private javafx.scene.control.Label lblUsername;

    @FXML
    private javafx.scene.control.Label lblPassword;

    @FXML
    private javafx.scene.control.Label lblLoginBtn;

    @FXML
    private javafx.scene.control.Label lblCancelBtn;
    
    @FXML
    private javafx.scene.control.Label lblIntentosRestantes;
    
    private SeguridadService seguridadService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔍 DEBUG: LoginController.initialize() called");
        
        this.seguridadService = SeguridadService.getInstance();
        
        // Inicializar textos con traducciones
        updateUITexts();
        
        // Initialize any required components here
        setupValidation();
        
        // Registrar listener para cambios de idioma
        LanguageService.getInstance().addLanguageChangeListener(newLanguage -> {
            Platform.runLater(this::updateUITexts);
        });
        
        // Listener para mostrar intentos restantes
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            actualizarIntentosRestantes();
        });
    }

    private void updateUITexts() {
        I18nUtil.setLabelText(lblWelcome, "login.titulo");
        I18nUtil.setLabelText(lblUsername, "👤 " + I18nUtil.get("usuarios.usuario"));
        I18nUtil.setLabelText(lblPassword, "🔒 " + I18nUtil.get("login.password"));
        I18nUtil.setLabelText(lblLoginBtn, I18nUtil.get("login.entrar"));
        I18nUtil.setLabelText(lblCancelBtn, I18nUtil.get("btn.cancelar"));
        I18nUtil.setPromptText(usernameField, "login.usuario");
        I18nUtil.setPromptText(passwordField, "login.password");
    }

    private void setupValidation() {
        // Add listeners for form validation
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
            actualizarIntentosRestantes();
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        // Initial validation
        validateForm();
    }

    private void validateForm() {
        boolean isValid = !usernameField.getText().trim().isEmpty() && 
                         !passwordField.getText().trim().isEmpty();
        loginButton.setDisable(!isValid);
    }
    
    private void actualizarIntentosRestantes() {
        try {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                if (lblIntentosRestantes != null) {
                    lblIntentosRestantes.setText("");
                }
                return;
            }
            
            // Verificar si usuario está bloqueado
            if (seguridadService.estaUsuarioBloqueado(username)) {
                if (lblIntentosRestantes != null) {
                    lblIntentosRestantes.setText("❌ Usuario bloqueado por exceso de intentos");
                    lblIntentosRestantes.setStyle("-fx-text-fill: #d32f2f;");
                }
                return;
            }
            
            // Mostrar intentos restantes
            int intentosRestantes = seguridadService.obtenerIntentosRestantes(username);
            int maxIntentos = seguridadService.getMaxLoginAttempts(); // Leer dinámicamente
            
            if (intentosRestantes > 0 && intentosRestantes < maxIntentos) {
                if (lblIntentosRestantes != null) {
                    lblIntentosRestantes.setText("⚠️ Intentos restantes: " + intentosRestantes + "/" + maxIntentos);
                    lblIntentosRestantes.setStyle("-fx-text-fill: #f57c00;");
                }
            } else if (lblIntentosRestantes != null) {
                lblIntentosRestantes.setText("");
            }
        } catch (Exception e) {
            System.err.println("Error actualizando intentos: " + e.getMessage());
            // No lanzar excepción, solo registrar
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please enter both username and password.");
            return;
        }

        // Verificar si usuario está bloqueado ANTES de intentar login
        if (seguridadService.estaUsuarioBloqueado(username)) {
            showAlert("Cuenta Bloqueada", 
                "❌ Esta cuenta ha sido bloqueada por exceso de intentos fallidos.\n\n" +
                "Por favor, contacte al administrador para desbloquearla.");
            passwordField.clear();
            return;
        }

        // TODO: Implement actual authentication logic here
        // For now, we'll simulate a successful login
        if (authenticateUser(username, password)) {
            navigateToDashboard();
        } else {
            // Login fallido - mostrar intentos restantes y máximo
            int intentosRestantes = seguridadService.obtenerIntentosRestantes(username);
            int maxIntentos = seguridadService.getMaxLoginAttempts();
            
            if (intentosRestantes <= 0) {
                showAlert("Cuenta Bloqueada", 
                    "❌ Se han agotado todos los intentos permitidos (" + maxIntentos + ").\n\n" +
                    "Su cuenta ha sido bloqueada por seguridad.\n" +
                    "Contacte al administrador para desbloquearla.");
            } else if (intentosRestantes == 1) {
                showAlert("⚠️ ÚLTIMO INTENTO", 
                    "❌ Credenciales incorrectas.\n\n" +
                    "⚠️ Tiene 1 ÚLTIMO intento restante antes de que su cuenta se bloquee.\n" +
                    "Por favor, verifique su usuario y contraseña.\n\n" +
                    "(Máximo de intentos configurado: " + maxIntentos + ")");
            } else {
                showAlert("Login Fallido", 
                    "❌ Usuario o contraseña incorrectos.\n\n" +
                    "Intentos restantes: " + intentosRestantes + "/" + maxIntentos);
            }
            
            passwordField.clear();
            actualizarIntentosRestantes();
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Close the application
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 900);
            stage.setTitle("Dashboard - JavaFX Application");
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }
    private boolean authenticateUser(String username, String password) {
        com.app.service.AuthenticationService authService = com.app.service.AuthenticationService.getInstance();
        return authService.login(username, password);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to clear form fields
    public void clearForm() {
        usernameField.clear();
        passwordField.clear();
        usernameField.requestFocus();
    }
}