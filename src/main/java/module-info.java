module javafx.modular.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    
    exports com.app;
    exports com.app.controller;
    exports com.app.model;
}