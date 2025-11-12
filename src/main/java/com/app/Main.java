package com.app;

import com.app.service.UsuarioService;
import com.app.service.ThemeService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            UsuarioService usuarioService = UsuarioService.getInstance();
            boolean hasUsuarios = false;
            
            // Intentar verificar si existen usuarios; si falla, asumir que es registro inicial
            try {
                hasUsuarios = usuarioService.hasAnyUsuario();
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo verificar usuarios (conexión a BD): " + e.getMessage());
                System.out.println("Asumiendo registro inicial...");
                hasUsuarios = false;
            }

            String viewPath;
            String title;
            int width;
            int height;

            if (!hasUsuarios) {
                viewPath = "/com/app/view/RegisterView.fxml";
                title = "Registro Inicial - Sistema de Gestión de Inventario";
                width = 500;
                height = 600;
            } else {
                viewPath = "/com/app/view/LoginView.fxml";
                title = "Login - Sistema de Gestión de Inventario";
                width = 900;
                height = 800;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);

            // Registrar la escena con el servicio de tema para aplicar tema guardado
            ThemeService.getInstance().registerScene(scene);

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading application view: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}