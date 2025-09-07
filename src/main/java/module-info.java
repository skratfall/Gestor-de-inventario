module javafx.modular.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    
    exports com.app;
    exports com.app.controller;
    exports com.app.model;
    opens com.app.controller to javafx.fxml;
}