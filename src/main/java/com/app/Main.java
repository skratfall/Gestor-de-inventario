package com.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class that launches the JavaFX application
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the LoginView FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/view/LoginView.fxml"));
            Parent root = loader.load();
            
            // Create and configure the scene
            Scene scene = new Scene(root, 400, 300);
            
            // Configure the primary stage
            primaryStage.setTitle("JavaFX Modular Application");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            
            // Show the application
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading LoginView.fxml: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}