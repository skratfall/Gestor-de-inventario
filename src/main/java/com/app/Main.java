package com.app;

import com.app.service.UsuarioService;
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
            boolean hasUsuarios = usuarioService.hasAnyUsuario();

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