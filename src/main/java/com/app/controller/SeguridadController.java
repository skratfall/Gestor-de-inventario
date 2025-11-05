package com.app.controller;

import com.app.model.EventoSeguridad;
import com.app.model.Usuario;
import com.app.service.SeguridadService;
import com.app.service.UsuarioService;
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
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SeguridadController implements Initializable {

    @FXML private CheckBox chk2FA;
    @FXML private CheckBox chkBloqueoSesion;
    @FXML private ComboBox<String> cmbTiempoBloqueo;
    @FXML private ComboBox<String> cmbIntentosMaximos;
    @FXML private CheckBox chkAuditoriaAccesos;
    @FXML private CheckBox chkAuditoriaRoles;
    @FXML private CheckBox chkAuditoriaConfiguracion;
    
    @FXML private TableView<EventoSeguridad> tableEventos;
    @FXML private TableColumn<EventoSeguridad, String> colFecha;
    @FXML private TableColumn<EventoSeguridad, String> colTipo;
    @FXML private TableColumn<EventoSeguridad, String> colUsuario;
    @FXML private TableColumn<EventoSeguridad, String> colDescripcion;
    
    @FXML private ComboBox<String> cmbTipoEvento;
    @FXML private TextField txtBuscarEvento;
    @FXML private Label lblTotalEventos;

    private SeguridadService seguridadService;
    private UsuarioService usuarioService;
    private ObservableList<EventoSeguridad> eventosData;
    private DateTimeFormatter dateFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        seguridadService = SeguridadService.getInstance();
        usuarioService = UsuarioService.getInstance();
        eventosData = FXCollections.observableArrayList();
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        configurarControles();
        configurarTablaEventos();
        cargarConfiguracion();
        cargarEventos();
    }

    private void configurarControles() {
        // Configurar ComboBox de tiempo de bloqueo
        cmbTiempoBloqueo.setItems(FXCollections.observableArrayList(
            "5 minutos", "10 minutos", "15 minutos", "30 minutos", "1 hora"
        ));

        // Configurar ComboBox de intentos máximos
        cmbIntentosMaximos.setItems(FXCollections.observableArrayList(
            "3 intentos", "5 intentos", "7 intentos", "10 intentos"
        ));

        // Configurar ComboBox de tipo de evento
        cmbTipoEvento.setItems(FXCollections.observableArrayList(
            "Todos", "Accesos", "Roles", "Configuración", "Seguridad"
        ));
        cmbTipoEvento.getSelectionModel().selectFirst();
    }

    private void configurarTablaEventos() {
        colFecha.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFecha().format(dateFormatter)));
            
        colTipo.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getTipo()));
            
        colUsuario.setCellValueFactory(cellData -> {
            String usuarioId = cellData.getValue().getUsuarioId();
            if (usuarioId != null) {
                Optional<Usuario> usuario = usuarioService.findUsuarioById(usuarioId);
                return new SimpleStringProperty(usuario.map(Usuario::getUsername).orElse("N/A"));
            }
            return new SimpleStringProperty("Sistema");
        });
            
        colDescripcion.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDescripcion()));

        tableEventos.setItems(eventosData);
    }

    private void cargarConfiguracion() {
        chk2FA.setSelected(seguridadService.is2FAEnabled());
        
        int sessionTimeout = seguridadService.getSessionTimeout();
        cmbTiempoBloqueo.setValue(convertirMinutosATexto(sessionTimeout));
        
        int maxAttempts = seguridadService.getMaxLoginAttempts();
        cmbIntentosMaximos.setValue(maxAttempts + " intentos");
        
        chkAuditoriaAccesos.setSelected(seguridadService.isAuditAccessEnabled());
        chkAuditoriaRoles.setSelected(seguridadService.isAuditRolesEnabled());
        chkAuditoriaConfiguracion.setSelected(seguridadService.isAuditConfigEnabled());
    }

    private void cargarEventos() {
        List<EventoSeguridad> eventos = seguridadService.obtenerEventos();
        eventosData.clear();
        eventosData.addAll(eventos);
        actualizarTotalEventos();
    }

    @FXML
    private void handleConfigura2FA() {
        if (!chk2FA.isSelected()) {
            mostrarAdvertencia("2FA desactivado", 
                "Debe activar la autenticación de dos factores primero");
            return;
        }

        // Aquí iría la lógica para configurar 2FA
        mostrarInfo("Configuración 2FA", 
            "Esta funcionalidad será implementada próximamente");
    }

    @FXML
    private void handleDesbloquearUsuarios() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Está seguro que desea desbloquear todos los usuarios bloqueados?",
            ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                seguridadService.desbloquearUsuarios();
                mostrarInfo("Usuarios desbloqueados", 
                    "Se han desbloqueado todos los usuarios bloqueados");
            }
        });
    }

    @FXML
    private void handleGuardarConfiguracion() {
        try {
            seguridadService.set2FAEnabled(chk2FA.isSelected());
            seguridadService.setSessionTimeout(convertirTextoAMinutos(cmbTiempoBloqueo.getValue()));
            seguridadService.setMaxLoginAttempts(
                Integer.parseInt(cmbIntentosMaximos.getValue().split(" ")[0]));
            seguridadService.setAuditAccess(chkAuditoriaAccesos.isSelected());
            seguridadService.setAuditRoles(chkAuditoriaRoles.isSelected());
            seguridadService.setAuditConfig(chkAuditoriaConfiguracion.isSelected());

            mostrarInfo("Configuración guardada", 
                "Los cambios han sido guardados exitosamente");
            cargarEventos();
            
        } catch (Exception e) {
            mostrarError("Error", "Error al guardar la configuración: " + e.getMessage());
        }
    }

    @FXML
    private void handleReiniciarConfiguracion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Está seguro que desea reiniciar toda la configuración de seguridad?\n" +
            "Esto restaurará todos los valores a su configuración predeterminada.",
            ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                seguridadService.reiniciarConfiguracion();
                cargarConfiguracion();
                cargarEventos();
                mostrarInfo("Configuración reiniciada", 
                    "La configuración ha sido restaurada a sus valores predeterminados");
            }
        });
    }

    @FXML
    private void handleBuscarEventos() {
        String filtro = txtBuscarEvento.getText().toLowerCase().trim();
        String tipoSeleccionado = cmbTipoEvento.getValue();

        List<EventoSeguridad> eventos;
        if ("Todos".equals(tipoSeleccionado)) {
            eventos = seguridadService.obtenerEventos();
        } else {
            eventos = seguridadService.obtenerEventosPorTipo(tipoSeleccionado.toUpperCase());
        }

        if (!filtro.isEmpty()) {
            eventos = eventos.stream()
                .filter(e -> e.getDescripcion().toLowerCase().contains(filtro))
                .toList();
        }

        eventosData.clear();
        eventosData.addAll(eventos);
        actualizarTotalEventos();
    }

    @FXML
    private void handleRefrescarEventos() {
        txtBuscarEvento.clear();
        cmbTipoEvento.getSelectionModel().selectFirst();
        cargarEventos();
    }

    @FXML
    private void handleExportarEventos() {
        // Aquí iría la lógica para exportar eventos
        mostrarInfo("Exportar eventos", 
            "Esta funcionalidad será implementada próximamente");
    }

    @FXML
    private void handleBackToDashboard(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/app/view/DashboardView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - Gestión de Inventario");
            
        } catch (IOException e) {
            mostrarError("Error", "No se pudo cargar el dashboard: " + e.getMessage());
        }
    }

    private String convertirMinutosATexto(int minutos) {
        if (minutos >= 60) {
            return (minutos / 60) + " hora" + (minutos >= 120 ? "s" : "");
        }
        return minutos + " minutos";
    }

    private int convertirTextoAMinutos(String texto) {
        String[] partes = texto.split(" ");
        int cantidad = Integer.parseInt(partes[0]);
        if (texto.contains("hora")) {
            return cantidad * 60;
        }
        return cantidad;
    }

    private void actualizarTotalEventos() {
        lblTotalEventos.setText("Total: " + eventosData.size() + " eventos");
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}