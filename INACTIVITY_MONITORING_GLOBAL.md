# 🔒 Monitoreo de Inactividad Global - Guía de Implementación

## 📋 Descripción General

El sistema de monitoreo de inactividad ahora funciona en **TODAS las vistas** y se controla mediante un **checkbox en el panel de Seguridad**.

### Características Principales:
✅ **Global**: Una única instancia que monitorea cualquier vista abierta  
✅ **Centralizado**: Control único desde `InactivityMonitorManager`  
✅ **Configurable**: Activar/desactivar desde Seguridad  
✅ **Automático**: Se registra en cada vista automáticamente  
✅ **Sin Reinicio**: Cambios en tiempo real

---

## 🏗️ Arquitectura

```
┌──────────────────────────────────────────────┐
│   InactivityMonitorManager (Singleton)       │
│   ├─ Gestiona instancia global               │
│   ├─ Controla activar/desactivar             │
│   ├─ Registra/desregistra escenas            │
│   └─ Notifica cambios de estado              │
└──────────────────┬───────────────────────────┘
                   │
       ┌───────────┼───────────┐
       │           │           │
       ▼           ▼           ▼
    Dashboard   Usuarios    Seguridad
    (registrada) (registrada) (registrada)
       │           │           │
       └───────────┼───────────┘
                   │
              [Monitoreo Global]
              - Detecta inactividad
              - Muestra alertas
              - Bloquea sesión
```

---

## 📁 Archivos Creados/Modificados

### 1. InactivityMonitorManager.java (NUEVO)
**Ubicación**: `src/main/java/com/app/security/InactivityMonitorManager.java`

**Responsabilidad**: Gestor global de monitoreo

**Métodos Principales**:
```java
// Obtener instancia global
public static InactivityMonitorManager getInstance()

// Registrar una vista para monitoreo
public void registerScene(Scene scene, Stage stage)

// Desregistrar una vista
public void unregisterScene()

// Activar/Desactivar monitoreo
public void setMonitoringEnabled(boolean enabled)

// Verificar si está habilitado
public boolean isMonitoringEnabled()
```

### 2. SeguridadService.java (ACTUALIZADO)
**Cambios**:
- Agregadas constantes: `PREF_INACTIVITY_MONITORING_ENABLED`
- Nuevos métodos:
  ```java
  public void setInactivityMonitoringEnabled(boolean enabled)
  public boolean isInactivityMonitoringEnabled()
  ```

### 3. SeguridadController.java (ACTUALIZADO)
**Cambios**:
- Importado `InactivityMonitorManager`
- Agregado listener al checkbox `chkBloqueoSesion`
- Nuevo método: `handleInactivityMonitoringToggle(boolean enabled)`
- Se sincroniza con el manager cuando cambia el checkbox

### 4. BaseController.java (MEJORADO)
**Cambios**:
- Implementa `Initializable` directamente
- Método final `initialize()` que llama `initializeController()`
- Métodos helper para registrar/desregistrar monitoreo:
  ```java
  protected void registerSceneForMonitoring(Scene scene, Stage stage)
  protected void unregisterSceneFromMonitoring()
  ```

### 5. DashboardController.java (ACTUALIZADO)
**Cambios**:
- Importado `InactivityMonitorManager`
- Eliminado `InactivityMonitor` local
- Método `setupInactivityMonitoring()` ahora usa manager global
- Llamado desde `Platform.runLater()` para asegurar que Scene esté disponible

---

## 🔄 Flujo de Funcionamiento

### A. Usuario Habilita/Deshabilita Monitoreo en Seguridad

```
┌──────────────────────────────────────┐
│ SeguridadView (UI)                   │
│ ☑ Bloqueo por Inactividad            │
└───────────────┬──────────────────────┘
                │ [CheckBox cambia]
                ▼
┌──────────────────────────────────────┐
│ SeguridadController                  │
│ handleInactivityMonitoringToggle(b)  │
└───────────────┬──────────────────────┘
                │
                ▼
┌──────────────────────────────────────┐
│ InactivityMonitorManager             │
│ setMonitoringEnabled(true/false)     │
├──────────────────────────────────────┤
│ ├─ Actualiza isMonitoringEnabled     │
│ ├─ Inicia/Detiene monitor actual     │
│ └─ Notifica listeners de cambio      │
└──────────────────────────────────────┘
```

### B. Usuario Abre una Nueva Vista

```
┌──────────────────────────────┐
│ Controller abre vista        │
│ (ej: UsuariosController)     │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│ BaseController.initialize()          │
│ (Método final - se llama siempre)    │
├──────────────────────────────────────┤
│ ├─ Platform.runLater({              │
│ │    initializeController()          │
│ │    registerForInactivityMonitoring()
│ │  })                                │
└───────────────┬────────────────────────┘
                │
                ▼
┌──────────────────────────────────────┐
│ UsuariosController.initializeController()
│ (Lógica específica)                  │
└───────────────┬────────────────────────┘
                │
                ▼
┌──────────────────────────────────────┐
│ registerSceneForMonitoring()         │
│ (Heredado de BaseController)         │
├──────────────────────────────────────┤
│ ├─ Obtiene Scene y Stage             │
│ ├─ Llamada:                          │
│ │  InactivityMonitorManager          │
│ │  .registerScene(scene, stage)      │
│ └─ Inicia monitoreo si está habilitado
└──────────────────────────────────────┘
```

### C. Usuario Permanece Inactivo

```
┌──────────────────────────────┐
│ Usuario inactivo en vista    │
│ (sin mouse ni teclado)       │
└──────────────┬───────────────┘
               │
               ▼
         [Cada 10 segundos]
         InactivityMonitor
         .checkInactivity()
         │
         ├─ ¿Inactividad ≥ timeout?
         │
         ├─ 2 min antes: ⚠️ Alerta
         │
         └─ Timeout alcanzado: 🔒 Bloqueo
```

---

## 💻 Cómo Implementar en Otros Controladores

### Paso 1: Heredar de BaseController

```java
public class MiController extends BaseController {
    
    @FXML private Label miLabel;
    
    @Override
    public void initializeController() {
        // Tu lógica de inicialización aquí
    }
}
```

### Paso 2: Registrar la Escena

En tu método de inicialización específico:

```java
@Override
public void initializeController() {
    // ... tu código ...
    
    // Registrar para monitoreo de inactividad
    Platform.runLater(() -> {
        Scene scene = miLabel.getScene();
        Stage stage = (Stage) scene.getWindow();
        registerSceneForMonitoring(scene, stage);
    });
}
```

O más simple, si tienes un método `initialize` propio:

```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);  // Llama a BaseController.initialize()
    
    // Tu código adicional aquí
}
```

### Paso 3: (Opcional) Desregistrar al Cerrar

```java
@FXML
private void handleClose() {
    unregisterSceneFromMonitoring();
    // Cerrar vista
    Stage stage = (Stage) miLabel.getScene().getWindow();
    stage.close();
}
```

---

## 🧪 Prueba Completa del Sistema

### Escenario de Prueba

```
1. INICIO
   ├─ App arranca
   ├─ Dashboard abierto
   ├─ Monitoreo: HABILITADO (por defecto)
   ├─ Vista registrada para monitoreo
   └─ Timeout: 30 minutos (por defecto)

2. USUARIO NAVEGA
   ├─ Click en "🛡️ Seguridad"
   ├─ Seguridad abierto
   ├─ Seguridad registrado para monitoreo
   ├─ Dashboard desregistrado (nueva escena)
   └─ Monitoreo sigue ACTIVO

3. USUARIO DESACTIVA MONITOREO
   ├─ En Seguridad: Deshabilita checkbox
   ├─ SeguridadController notifica al manager
   ├─ Manager detiene monitoreo actual
   ├─ Cambio se guarda en preferences
   └─ ℹ️ Monitoreo: DESHABILITADO

4. USUARIO NAVEGA (Monitoreo deshabilitado)
   ├─ Click en "Usuarios"
   ├─ Usuarios abierto
   ├─ Manager NO registra (monitoreo deshabilitado)
   └─ ℹ️ Ningún monitoreo activo

5. USUARIO REACTIVA MONITOREO
   ├─ Vuelve a Seguridad
   ├─ Habilita checkbox
   ├─ Manager reactivado
   ├─ Usuarios registrado inmediatamente
   └─ ✅ Monitoreo: ACTIVO de nuevo

6. USUARIO INACTIVO 3 MINUTOS
   ├─ Sin actividad (timeout: 30 min)
   ├─ 28 minutos de inactividad restantes
   └─ Sin alertas aún

7. USUARIO INACTIVO 28 MINUTOS (28 + 3 = 31 total)
   ├─ InactivityMonitor verifica
   ├─ Detecta: inactivo 28 min ≥ timeout 30 min
   ├─ Falta 2 minutos para timeout
   └─ ⚠️ ALERTA: "Por favor, continúe usando..."

8. USUARIO IGNORA ALERTA
   ├─ Otro 2 minutos inactivo
   ├─ Total: 30 minutos de inactividad
   └─ 🔒 SESIÓN BLOQUEADA

9. SESIÓN BLOQUEADA
   ├─ Pantalla de bloqueo mostrada
   ├─ Campos:
   │  ├─ 🔒 ícono de bloqueo
   │  ├─ Razón: "Inactividad detectada"
   │  ├─ PasswordField para desbloquear
   │  └─ Botón "Logout"
   └─ Usuario ingresa contraseña para continuar
```

---

## 🔑 Casos de Uso

### Caso 1: Admin Requiere Alta Seguridad

```
Escenario: Detectan intentos de acceso no autorizado

Acción:
1. Admin va a Seguridad
2. Disminuye timeout a 5 minutos
3. Asegura que monitoreo esté habilitado
4. Guardar cambios

Resultado:
- Todas las sesiones se bloquean después de 5 min
- Usuarios deben interactuar constantemente
- Auditoría registra el cambio
```

### Caso 2: Usuario Necesita Sesión Larga

```
Escenario: Dev trabajando en código importante

Acción:
1. Solicita al admin aumentar timeout
2. Admin va a Seguridad
3. Aumenta timeout a 120 minutos
4. Guardar cambios

Resultado:
- Usuario puede trabajar 2 horas sin interrupciones
- Monitoreo sigue activo
- Si sale a tomar café 1 hora, sesión se bloquea
```

### Caso 3: Desactivar Monitoreo Temporalmente

```
Escenario: Testing de sistema, no quieren bloqueos

Acción:
1. Admin va a Seguridad
2. Deshabilita: ☐ Bloqueo por Inactividad
3. Guardar cambios

Resultado:
- Monitoreo completamente deshabilitado
- Las sesiones NO se bloquean por inactividad
- Se sigue permitiendo cierre manual
```

---

## 🐛 Troubleshooting

### "El monitoreo no se registra en la nueva vista"

**Solución**:
```java
// Verificar que el controller herede de BaseController
public class MiController extends BaseController {  ✅
    
    @Override
    public void initializeController() {
        // Esto será llamado automáticamente
    }
}
```

### "El monitoreo no se detiene al desactivar el checkbox"

**Solución**:
- Verificar que `handleInactivityMonitoringToggle()` esté en `SeguridadController`
- Verificar que llame a `InactivityMonitorManager.getInstance().setMonitoringEnabled(enabled)`

### "Usuario no ve alerta de inactividad"

**Solución**:
- Verificar que `InactivityMonitorManager` tenga listeners registrados
- Verificar que `isMonitoringEnabled()` devuelva `true`
- Revisar logs para errores

---

## 📊 Diagrama de Estados

```
┌─────────────────────────────────────┐
│    MONITOREO DESHABILITADO          │
│    (ℹ️ Preferencias guardadas)       │
└────────────┬────────────────────────┘
             │
             │ Admin habilita en Seguridad
             │
             ▼
┌─────────────────────────────────────┐
│    MONITOREO HABILITADO             │
│    ├─ Manager activo                │
│    ├─ Escenas se registran          │
│    └─ Inactividad se monitorea      │
└────────────┬────────────────────────┘
             │
             │ Usuario abre vista
             │
             ▼
┌─────────────────────────────────────┐
│    VISTA REGISTRADA                 │
│    ├─ Scene registrada              │
│    ├─ InactivityMonitor activo      │
│    └─ Contador iniciado             │
└────────────┬────────────────────────┘
             │
             ├─ Usuario interactúa → Reset contador
             │
             ├─ Inactividad < timeout → Continuar
             │
             ├─ Inactividad >= timeout - 2min → ⚠️ Alerta
             │
             └─ Inactividad >= timeout → 🔒 Bloqueo
```

---

## ✅ Checklist de Implementación

Para cada controlador nuevo:

- [ ] ¿Hereda de `BaseController`?
- [ ] ¿Implementa `initializeController()`?
- [ ] ¿Llama `registerSceneForMonitoring()` desde `Platform.runLater()`?
- [ ] ¿Scene y Stage están disponibles cuando se registra?
- [ ] ¿Compilación sin errores?
- [ ] ¿Probado navegando entre vistas?
- [ ] ¿Monitoreo activo visto en logs?

---

## 🔗 Integración con Sincronización

El monitoreo NO interfiere con:
- `SyncService` (sincronización de datos)
- `ThemeService` (cambio de tema)
- `LanguageService` (cambio de idioma)

Solo monitorea inactividad del usuario y bloquea sesión.

---

## 📝 Notas Importantes

1. **Thread-Safety**: Todos los cambios se hacen con `Platform.runLater()` para thread-safety
2. **Singleton**: `InactivityMonitorManager` es singleton - solo existe una instancia
3. **Persistencia**: Los cambios se guardan en `Preferences` del SO
4. **Sin Reinicio**: Los cambios se aplican inmediatamente sin reiniciar la app
5. **Auditoría**: Cada cambio se registra en `EventoSeguridad`

---

*Documentación completada para implementación global de monitoreo de inactividad*
