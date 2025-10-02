package com.app.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Base controller class with common functionality for all controllers
 */
public abstract class BaseController {

    /**
     * Show an information alert dialog
     */
    protected void showInfoAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Show an error alert dialog
     */
    protected void showErrorAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Show a warning alert dialog
     */
    protected void showWarningAlert(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    /**
     * Generic method to show alert dialogs
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //klok manito
    /**
     * Method to be implemented by child controllers for initialization logic
     */
    public abstract void initializeController();

    public void start(Stage primaryStage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }
}