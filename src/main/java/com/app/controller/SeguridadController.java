package com.app.controller;

import com.app.model.EventoSeguridad;
import com.app.model.Usuario;
import com.app.security.InactivityMonitorManager;
import com.app.service.SeguridadService;
import com.app.service.UsuarioService;
import com.app.service.LanguageService;
import com.app.util.I18nUtil;
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
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;  // Agregado para logging

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.io.FileOutputStream;
import java.io.File;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class SeguridadController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SeguridadController.class);  // Logger SLF4J

    @FXML private Label lblPanelTitle;
    @FXML private Label lblPanelSubtitle;
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

        updateUITexts();
        configurarControles();
        configurarTablaEventos();
        cargarConfiguracion();
        cargarEventos();
        setupLanguageListener();
        logger.info("SeguridadController inicializado");
    }

    private void setupLanguageListener() {
        LanguageService.getInstance().addLanguageChangeListener(newLanguage -> {
            Platform.runLater(this::updateUITexts);
        });
    }

    private void updateUITexts() {
        I18nUtil.setLabelText(lblPanelTitle, "seguridad.titulo");
        I18nUtil.setLabelText(lblPanelSubtitle, "seguridad.subtitulo");
        
        colFecha.setText(I18nUtil.get("seguridad.fecha"));
        colTipo.setText(I18nUtil.get("seguridad.tipo_evento"));
        colUsuario.setText(I18nUtil.get("usuarios.usuario"));
        colDescripcion.setText(I18nUtil.get("seguridad.descripcion"));
        
        updateEventosLabel();
    }

    private void updateEventosLabel() {
        long total = eventosData.stream().count();
        lblTotalEventos.setText(I18nUtil.get("seguridad.total_eventos") + ": " + total);
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
        
        // Agregar listener para cambios en tiempo real del ComboBox de intentos
        cmbIntentosMaximos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    int intentos = Integer.parseInt(newVal.split(" ")[0]);
                    seguridadService.setMaxLoginAttempts(intentos);
                    logger.info("✓ Intentos máximos actualizados a: " + intentos);
                } catch (Exception e) {
                    logger.error("Error al actualizar intentos máximos", e);
                }
            }
        });

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
                try {
                    Optional<Usuario> usuario = usuarioService.findUsuarioById(usuarioId);
                    return new SimpleStringProperty(usuario.map(Usuario::getUsername).orElse("N/A"));
                } catch (SecurityException e) {
                    logger.warn("Acceso denegado a usuario en tabla: {}", usuarioId);
                    return new SimpleStringProperty("Sin permiso");
                }
            }
            return new SimpleStringProperty("Sistema");
        });
            
        colDescripcion.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDescripcion()));

        tableEventos.setItems(eventosData);
    }

    private void cargarConfiguracion() {
        try {
            chk2FA.setSelected(seguridadService.is2FAEnabled());
            
            // Cargar configuración de monitoreo de inactividad
            chkBloqueoSesion.setSelected(seguridadService.isInactivityMonitoringEnabled());
            // Agregar listener al checkbox
            chkBloqueoSesion.selectedProperty().addListener((obs, oldVal, newVal) -> {
                handleInactivityMonitoringToggle(newVal);
            });
            
            int sessionTimeout = seguridadService.getSessionTimeout();
            cmbTiempoBloqueo.setValue(convertirMinutosATexto(sessionTimeout));
            
            int maxAttempts = seguridadService.getMaxLoginAttempts();
            cmbIntentosMaximos.setValue(maxAttempts + " intentos");
            
            chkAuditoriaAccesos.setSelected(seguridadService.isAuditAccessEnabled());
            chkAuditoriaRoles.setSelected(seguridadService.isAuditRolesEnabled());
            chkAuditoriaConfiguracion.setSelected(seguridadService.isAuditConfigEnabled());
        } catch (Exception e) {
            logger.error("Error cargando configuración", e);
            mostrarError("Error", "Error al cargar la configuración: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el cambio de estado del checkbox de bloqueo por inactividad
     */
    private void handleInactivityMonitoringToggle(boolean enabled) {
        try {
            // Sincronizar con el manager global
            InactivityMonitorManager.getInstance().setMonitoringEnabled(enabled);
            
            if (enabled) {
                logger.info("✓ Monitoreo de inactividad habilitado desde Seguridad");
            } else {
                logger.info("✓ Monitoreo de inactividad deshabilitado desde Seguridad");
            }
        } catch (Exception e) {
            logger.error("Error al cambiar estado de monitoreo", e);
            mostrarError("Error", "Error al cambiar estado del monitoreo: " + e.getMessage());
        }
    }

    private void cargarEventos() {
        try {
            List<EventoSeguridad> eventos = seguridadService.obtenerEventos();
            eventosData.clear();
            eventosData.addAll(eventos);
            actualizarTotalEventos();
        } catch (Exception e) {
            logger.error("Error cargando eventos", e);
            mostrarError("Error", "Error al cargar eventos: " + e.getMessage());
        }
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
                try {
                    seguridadService.desbloquearUsuarios();
                    mostrarInfo("Usuarios desbloqueados", 
                        "Se han desbloqueado todos los usuarios bloqueados");
                } catch (Exception e) {
                    logger.error("Error desbloqueando usuarios", e);
                    mostrarError("Error", "Error al desbloquear usuarios: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleGuardarConfiguracion() {
        try {
            seguridadService.set2FAEnabled(chk2FA.isSelected());
            seguridadService.setSessionTimeout(convertirTextoAMinutos(cmbTiempoBloqueo.getValue()));
            // Nota: setMaxLoginAttempts se guarda automáticamente desde el listener del ComboBox
            seguridadService.setAuditAccess(chkAuditoriaAccesos.isSelected());
            seguridadService.setAuditRoles(chkAuditoriaRoles.isSelected());
            seguridadService.setAuditConfig(chkAuditoriaConfiguracion.isSelected());

            mostrarInfo("Configuración guardada", 
                "Los cambios han sido guardados exitosamente\n\n" +
                "Máximo de intentos: " + seguridadService.getMaxLoginAttempts());
            cargarEventos();
            
        } catch (Exception e) {
            logger.error("Error guardando configuración", e);
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
                try {
                    seguridadService.reiniciarConfiguracion();
                    cargarConfiguracion();
                    cargarEventos();
                    mostrarInfo("Configuración reiniciada", 
                        "La configuración ha sido restaurada a sus valores predeterminados");
                } catch (Exception e) {
                    logger.error("Error reiniciando configuración", e);
                    mostrarError("Error", "Error al reiniciar configuración: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBuscarEventos() {
        try {
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
        } catch (Exception e) {
            logger.error("Error buscando eventos", e);
            mostrarError("Error", "Error al buscar eventos: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefrescarEventos() {
        try {
            txtBuscarEvento.clear();
            cmbTipoEvento.getSelectionModel().selectFirst();
            cargarEventos();
        } catch (Exception e) {
            logger.error("Error refrescando eventos", e);
            mostrarError("Error", "Error al refrescar eventos: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportarEventos() {
        if (eventosData == null || eventosData.isEmpty()) {
            mostrarAdvertencia("Exportar eventos", "No hay eventos para exportar");
            return;
        }

        // Mostrar FileChooser para que el usuario seleccione ubicación y nombre de archivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar eventos de seguridad");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));

        // Intentar abrir en la carpeta Descargas del usuario si existe
        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, "Downloads");
        if (!defaultDir.exists() || !defaultDir.isDirectory()) {
            defaultDir = new File(userHome);
        }
        fileChooser.setInitialDirectory(defaultDir);
        fileChooser.setInitialFileName("eventos_seguridad.xlsx");

        Window window = tableEventos.getScene().getWindow();
        File chosen = fileChooser.showSaveDialog(window);
        if (chosen == null) {
            // Usuario canceló
            return;
        }

        // Crear workbook y hoja
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CreationHelper createHelper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("Eventos de Seguridad");

            // Estilos
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));

            // Cabeceras
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Fecha");
            headerRow.createCell(1).setCellValue("Tipo");
            headerRow.createCell(2).setCellValue("Usuario");
            headerRow.createCell(3).setCellValue("Descripción");

            // Filas
            int rowIdx = 1;
            for (EventoSeguridad ev : eventosData) {
                Row row = sheet.createRow(rowIdx++);
                Cell c0 = row.createCell(0);
                if (ev.getFecha() != null) {
                    c0.setCellValue(ev.getFecha().format(dateFormatter));
                } else {
                    c0.setCellValue("");
                }
                row.createCell(1).setCellValue(ev.getTipo() != null ? ev.getTipo() : "");
                // Usuario: intentar resolver nombre
                String usuarioDisplay = "";
                if (ev.getUsuarioId() != null && !"sistema".equals(ev.getUsuarioId())) {
                    try {
                        Optional<Usuario> u = usuarioService.findUsuarioById(ev.getUsuarioId());
                        usuarioDisplay = u.map(Usuario::getUsername).orElse(ev.getUsuarioId());
                    } catch (Exception ex) {
                        usuarioDisplay = ev.getUsuarioId();
                    }
                } else {
                    usuarioDisplay = "Sistema";
                }
                row.createCell(2).setCellValue(usuarioDisplay);
                row.createCell(3).setCellValue(ev.getDescripcion() != null ? ev.getDescripcion() : "");
            }

            // Auto-ajustar columnas
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar en la ruta seleccionada por el usuario
            File outFile = chosen;
            if (!outFile.getName().toLowerCase().endsWith(".xlsx")) {
                outFile = new File(outFile.getAbsolutePath() + ".xlsx");
            }
            try (FileOutputStream fileOut = new FileOutputStream(outFile)) {
                workbook.write(fileOut);
            }

            mostrarInfo("Exportar eventos", "Eventos exportados a: " + outFile.getAbsolutePath());

        } catch (Exception e) {
            logger.error("Error exportando eventos a XLSX", e);
            mostrarError("Error exportando", "No se pudo exportar eventos: " + e.getMessage());
        }
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
            logger.error("Error cargando dashboard", e);
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