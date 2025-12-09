# 📋 TEMPLATE - Implementar Monitoreo en Controladores

## Pasos Rápidos

### 1. Heredar de BaseController
```java
public class MiNuevoController extends BaseController {
    // ... resto del código
}
```

### 2. Implementar initializeController()
```java
@Override
public void initializeController() {
    // Tu lógica de inicialización aquí
    logger.info("MiNuevoController inicializado");
}
```

### 3. Registrar para Monitoreo
Opción A - Automático (recomendado):
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);  // Usa BaseController
    // Tu lógica adicional si necesitas
}
```

Opción B - Manual (si necesitas control explícito):
```java
@Override
public void initializeController() {
    // Tu código...
    
    // Registrar para monitoreo después de que Scene esté disponible
    Platform.runLater(() -> {
        Scene scene = tuLabelONodo.getScene();
        Stage stage = (Stage) scene.getWindow();
        registerSceneForMonitoring(scene, stage);
    });
}
```

---

## Ejemplo Completo: UsuariosController

```java
package com.app.controller;

import com.app.model.Usuario;
import com.app.service.UsuarioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller para la vista de Usuarios
 * Hereda de BaseController para soporte automático de monitoreo de inactividad
 */
public class UsuariosController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuariosController.class);
    
    @FXML private Label lblTitle;
    @FXML private Label lblTotalUsuarios;
    
    private UsuarioService usuarioService;
    
    /**
     * Implementar initializeController para lógica específica
     * BaseController.initialize() lo llamará automáticamente
     * y registrará el monitoreo después
     */
    @Override
    public void initializeController() {
        logger.info("✓ UsuariosController inicializando...");
        
        usuarioService = UsuarioService.getInstance();
        
        // Cargar datos
        cargarUsuarios();
        
        // Actualizar UI
        actualizarLabels();
        
        logger.info("✓ UsuariosController completamente inicializado");
        logger.info("✓ Monitoreo de inactividad registrado automáticamente");
    }
    
    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.findAll();
            logger.info("Cargados {} usuarios", usuarios.size());
        } catch (Exception e) {
            logger.error("Error cargando usuarios", e);
        }
    }
    
    private void actualizarLabels() {
        lblTitle.setText("Gestión de Usuarios");
        // ... más updates ...
    }
}
```

---

## Arquitectura de Herencia

```
Initializable (JavaFX Interface)
     ▲
     │
     │ implements
     │
BaseController
     ▲
     │
     │ extends
     │
┌─────────────────────────────────┐
│ MiController                     │
├─────────────────────────────────┤
│ @Override                        │
│ public void initializeController()│
│ {                               │
│     // Tu lógica aquí            │
│     // Monitoreo registrado auto │
│ }                               │
└─────────────────────────────────┘
```

---

## ✅ Checklist de Verificación

```javascript
✓ ¿Controller extiende BaseController?
✓ ¿Tiene @FXML Label o Control?
✓ ¿Implementa initializeController()?
✓ ¿Compila sin errores?
✓ ¿Aparece en logs: "registrado en gestor de monitoreo"?
✓ ¿Al desactivar checkbox en Seguridad se detiene monitoreo?
✓ ¿Al activar checkbox en Seguridad se reinicia monitoreo?
✓ ¿A los 30 min de inactividad aparece alerta?
✓ ¿A los 32 min aparece pantalla de bloqueo?
```

---

## Métodos Disponibles desde BaseController

```java
// Registrar manualmente (si Platform.runLater es necesario)
protected void registerSceneForMonitoring(Scene scene, Stage stage)

// Desregistrar (opcional, al cerrar vista)
protected void unregisterSceneFromMonitoring()

// Mostrar alertas
protected void showInfoAlert(String title, String message)
protected void showErrorAlert(String title, String message)
protected void showWarningAlert(String title, String message)

// Inicializar (implementar por ti)
public abstract void initializeController()
```

---

## Casos Especiales

### Controller que Abre Otra Ventana

```java
@Override
public void initializeController() {
    // Monitoreo se registra aquí automáticamente
    logger.info("Controller principal registrado");
}

@FXML
private void abrirVentanaSecundaria() {
    try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/app/view/Secondary.fxml")
        );
        Parent root = loader.load();
        
        Stage newStage = new Stage();
        Scene newScene = new Scene(root);
        newStage.setScene(newScene);
        
        // SecondaryController (si hereda de BaseController) 
        // se registrará automáticamente cuando inicialice
        
        newStage.show();
    } catch (Exception e) {
        logger.error("Error abriendo ventana", e);
    }
}
```

### Controller Modal

```java
@Override
public void initializeController() {
    logger.info("Modal controller registrado para monitoreo");
}

@FXML
private void handleCerrar() {
    // Desregistrar si es necesario
    unregisterSceneFromMonitoring();
    
    // Cerrar modal
    Stage stage = (Stage) btnCerrar.getScene().getWindow();
    stage.close();
}
```

---

## Debugging

### Ver logs de registro:

```bash
# En la consola verás:
✓ MiController registrado en gestor de monitoreo
✓ Monitoreo de inactividad activado globalmente
⚠️ Alerta de inactividad: 2 minutos restantes
🔒 Sesión bloqueada por: Inactividad detectada por 30 minutos
```

### Verificar que está habilitado:

```java
// En cualquier controller
boolean habilitado = InactivityMonitorManager.getInstance().isMonitoringEnabled();
logger.info("Monitoreo habilitado: {}", habilitado);
```

---

## Preguntas Frecuentes

**P: ¿Se registra automáticamente?**
R: Sí, si hereda de BaseController. BaseController.initialize() lo hace automáticamente.

**P: ¿Qué pasa si abro múltiples vistas?**
R: Solo la vista ACTUAL está monitoreada. Cuando abres otra, la anterior se desregistra.

**P: ¿Se persisten los cambios del checkbox?**
R: Sí, se guardan en Preferences del SO.

**P: ¿Funciona si deshabilito el monitoreo?**
R: No. Si está deshabilitado, ninguna vista se monitorea.

**P: ¿Puedo tener monitoreo independiente por vista?**
R: No en este diseño. Es global. Pero puedes modificar InactivityMonitorManager si lo necesitas.

---

## Próximas Mejoras (Opcional)

- [ ] Timeout específico por rol
- [ ] Timeout específico por vista  
- [ ] Notificaciones push antes de bloqueo
- [ ] Lock screen con biometría
- [ ] Dashboard de sesiones activas

---

*Template listo para usar. Copia, pega y adapta a tus controladores.*
