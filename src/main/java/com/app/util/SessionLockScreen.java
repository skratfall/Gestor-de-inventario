package com.app.util;

import com.app.security.SessionManager;
import com.app.service.AuthenticationService;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Pantalla de bloqueo de sesión por inactividad
 */
public class SessionLockScreen {
    private final Stage stage;
    private final SessionManager sessionManager;
    private final AuthenticationService authService;

    public SessionLockScreen(Stage primaryStage) {
        this.stage = primaryStage;
        this.sessionManager = SessionManager.getInstance();
        this.authService = AuthenticationService.getInstance();
    }

    /**
     * Muestra la pantalla de bloqueo
     */
    public void show(String reason) {
        VBox root = createLockScreen(reason);
        
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/css/ModernStyles.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Sesión Bloqueada");
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> event.consume()); // Prevenir cierre
        stage.show();
    }

    /**
     * Crea la interfaz de la pantalla de bloqueo
     */
    private VBox createLockScreen(String reason) {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 40;");
        root.setAlignment(Pos.CENTER);

        // Icono de bloqueo
        Label lockIcon = new Label("🔒");
        lockIcon.setStyle("-fx-font-size: 72px;");

        // Título
        Label title = new Label("Sesión Bloqueada");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Usuario
        String username = sessionManager.getCurrentUser() != null ? 
            sessionManager.getCurrentUser().getUsername() : "Usuario";
        Label userLabel = new Label("Usuario: " + username);
        userLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Razón del bloqueo
        Label reasonLabel = new Label(reason);
        reasonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-wrap-text: true;");
        reasonLabel.setWrapText(true);

        // Campo de contraseña
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Ingrese su contraseña para desbloquear");
        passwordField.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");

        // Botón desbloquear
        Button unlockButton = new Button("Desbloquear");
        unlockButton.setStyle(
            "-fx-padding: 10px 40px; " +
            "-fx-font-size: 14px; " +
            "-fx-background-color: #27ae60; " +
            "-fx-text-fill: white; " +
            "-fx-cursor: hand;" +
            "-fx-border-radius: 5;"
        );

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        unlockButton.setOnAction(event -> {
            String password = passwordField.getText();
            if (attemptUnlock(password)) {
                stage.close();
            } else {
                errorLabel.setText("❌ Contraseña incorrecta");
                passwordField.clear();
            }
        });

        // Permitir enter
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                unlockButton.fire();
            }
        });

        // Botón logout
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.setStyle(
            "-fx-padding: 10px 40px; " +
            "-fx-font-size: 14px; " +
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-cursor: hand;" +
            "-fx-border-radius: 5;"
        );

        logoutButton.setOnAction(event -> logout());

        // Espaciador
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Botones
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(unlockButton, logoutButton);

        root.getChildren().addAll(
            lockIcon,
            title,
            userLabel,
            reasonLabel,
            new Separator(),
            passwordField,
            errorLabel,
            spacer,
            buttonBox
        );

        return root;
    }

    /**
     * Intenta desbloquear con la contraseña proporcionada
     */
    private boolean attemptUnlock(String password) {
        try {
            String username = sessionManager.getCurrentUser().getUsername();
            
            // Validar contraseña usando login
            boolean isValid = authService.login(username, password);
            
            if (isValid) {
                // Resetear el temporizador de inactividad
                sessionManager.updateLastActivity();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cierra sesión
     */
    private void logout() {
        authService.logout();
        stage.close();
        System.exit(0);
    }
}
