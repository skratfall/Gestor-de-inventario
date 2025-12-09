# 🎯 GUÍA VISUAL - Sistema Global de Monitoreo de Inactividad

## 1. Flujo de Activación/Desactivación

### Paso 1: Admin abre Seguridad
```
DASHBOARD ABIERTO
│
├─ Usuario hace click en 🛡️ Seguridad
│
└─> SEGURIDAD ABIERTO
    ├─ BaseController.initialize() [Automático]
    │  ├─ initializeController()
    │  └─ registerScene() → Monitor activo
    │
    └─ Admin ve SeguridadView
       ├─ Título: "Gestión de Seguridad"
       ├─ ComboBox: Tiempo de bloqueo (5-120 min)
       ├─ CheckBox: ☑ Bloqueo por Inactividad (ACTIVO)
       └─ Botón: Guardar Cambios
```

### Paso 2: Admin Desactiva Monitoreo
```
┌─────────────────────────────────────┐
│ Seguridad View                      │
├─────────────────────────────────────┤
│ ☑ Bloqueo por Inactividad  [CLICK]  │
│                    ↓                │
│ ☐ Bloqueo por Inactividad  [Nueva] │
└──────────────┬──────────────────────┘
               │
    [onChange Listener]
               │
               ▼
    ┌──────────────────────────┐
    │ SeguridadController      │
    │ handleInactivityMonitoring
    │ Toggle(false)            │
    └──────────────┬───────────┘
                   │
                   ▼
    ┌────────────────────────────────┐
    │ InactivityMonitorManager       │
    │ .setMonitoringEnabled(false)   │
    └──────────────┬─────────────────┘
                   │
      ┌────────────┴────────────┐
      │                         │
      ▼                         ▼
   Detener          Guardar en Preferences
   Monitor Actual   (persiste entre sesiones)
   
   Result: 🔴 Monitoreo DESHABILITADO
```

### Paso 3: Admin Reactiva Monitoreo
```
Admin hace click en checkbox nuevamente
    │
    ▼
☑ Bloqueo por Inactividad

    │ onChange Listener
    ▼
SeguridadController.handleInactivityMonitoring
Toggle(true)
    │
    ▼
InactivityMonitorManager.setMonitoringEnabled(true)
    │
    ├─ Cambio guardado en Preferences
    ├─ Notifica listeners: Estado cambió
    └─ Próximas vistas se registrarán

Result: 🟢 Monitoreo HABILITADO
```

---

## 2. Ciclo de Vida de un Controller

### Ejemplo: UsuariosController

```
┌─────────────────────────────────────────┐
│ ANTES: UsuariosController               │
├─────────────────────────────────────────┤
│ public class UsuariosController         │
│   extends BaseController {              │
│                                         │
│   @Override                             │
│   public void initializeController() {  │
│     usuarioService = ...                │
│     cargarUsuarios();                   │
│     actualizarLabels();                 │
│   }                                     │
│ }                                       │
└─────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│ EJECUCIÓN AUTOMÁTICA (por BaseController.initialize)│
├──────────────────────────────────────────────────────┤
│ 1️⃣ Platform.runLater({                             │
│     // Asegurar que Scene esté disponible           │
│                                                     │
│     2️⃣ initializeController()                      │
│        └─ Tu código aquí (cargarUsuarios, etc)     │
│                                                     │
│     3️⃣ registerSceneForMonitoring()                │
│        └─ Si está habilitado:                      │
│           - Obtiene Scene y Stage                  │
│           - Llama InactivityMonitorManager         │
│           - Monitor registrado ✓                   │
│    })                                              │
└──────────────────────────────────────────────────────┘

RESULTADO:
┌──────────────────────────────────────┐
│ Monitor activo en UsuariosController  │
│ detectando inactividad del usuario    │
└──────────────────────────────────────┘
```

---

## 3. Cambio de Vistas (Sin Interrumpción)

```
USUARIO EN DASHBOARD
│
├─ Monitor: ✓ Detectando inactividad
├─ Escena: Dashboard registrada
├─ Inactividad: 5 minutos
│
└─ Usuario hace click en "👥 Usuarios"
   │
   └─> CAMBIO DE VISTA
       │
       ├─ BaseController.initialize() [Dashboard finalizado]
       │  └─ registerForInactivityMonitoring()
       │     └─ unregisterScene() [Dashboard desregistrado]
       │
       └─ UsuariosController.initialize() [Nueva vista cargando]
          ├─ initializeController()
          │  └─ loadUsers()
          │
          └─ registerForInactivityMonitoring()
             └─ registerScene() [Usuarios registrado]
                │
                ├─ Monitor cambia de escena
                ├─ Counter se reinicia
                └─ Inactividad: 0 minutos (NEW)

CONTINUIDAD:
┌────────────────────────────┐
│ Monitor SIGUE ACTIVO       │
│ Solo cambió de vista       │
│ Sin interrupciones         │
│ Contador reiniciado ✓      │
└────────────────────────────┘
```

---

## 4. Timeline de Inactividad

```
USUARIO EN CUALQUIER VISTA
├─ Monitoreo habilitado: ✓
├─ Timeout configurado: 30 minutos
│
├─ T=0:00  Usuario abre vista
│          Contador: 0 min de inactividad
│
├─ T=5:00  Usuario navega en pantalla
│          Contador RESET: 0 min
│
├─ T=10:00 Usuario completamente inactivo
│          Contador comienza: 0 min
│
├─ T=25:00 Inactividad: 15 minutos
│          [Sin alertas aún]
│
├─ T=28:00 ⚠️ ALERTA DE ADVERTENCIA
│          Inactividad: 28 minutos
│          Tiempo restante: 2 minutos
│          └─> Alert mostrado: "Por favor continúe..."
│
├─ T=30:00 🔒 SESIÓN BLOQUEADA
│          Inactividad: 30 minutos >= Timeout
│          └─> SessionLockScreen mostrado
│             ├─ Ícono: 🔒
│             ├─ Razón: "Inactividad detectada"
│             ├─ PasswordField: [      ]
│             └─ Botones: [Desbloquear] [Logout]
│
├─ T=30:15 Usuario ingresa contraseña
│          ├─ Validación con authService.login()
│          ├─ Si ✓: Sesión desbloqueada
│          │        Contador reset: 0 min
│          │        Vuelve a la vista anterior
│          └─ Si ✗: Error, requiere reintentar
│
└─ T=30:20 Usuario activo nuevamente
           Contador: 0 min
           Próximo bloqueo en: 30 min (nuevo ciclo)
```

---

## 5. Estados Posibles del Sistema

```
                 ┌─────────────────────┐
                 │   APP START         │
                 │ isMonitoring=true   │
                 └──────────┬──────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
        ┌──────────────┐      ┌──────────────────┐
        │ ENABLED      │      │ DISABLED         │
        │ 🟢           │      │ 🔴              │
        ├──────────────┤      ├──────────────────┤
        │ ✓ Monitor    │      │ ✗ Monitor       │
        │   activo     │      │   detenido      │
        │ ✓ Escenas se │      │ ✗ Escenas NO   │
        │   registran  │      │   registran     │
        │ ✓ Detecta    │      │ ✗ NO detecta   │
        │   inactividad│      │   inactividad   │
        │ ✓ Bloquea    │      │ ✗ NO bloquea   │
        │   sesión     │      │   sesión        │
        └────────┬─────┘      └────────┬────────┘
                 │                      │
      [Admin      │                      │
       desactiva  └──┐  [Admin activa]──┘
       checkbox]     │
                     ▼
                [Cambio guardado]
```

---

## 6. Estructura de Herencia

```
java.lang.Object
    ▲
    │
    └─ Initializable [JavaFX Interface]
       ├─ initialize(URL, ResourceBundle)
       │  └─ Llamado automáticamente al cargar FXML
       │
       ▲
       │
       └─ BaseController [NUEVA]
          ├─ initialize() [FINAL - no puede sobrescribirse]
          │  │
          │  ├─ Platform.runLater({
          │  │    initializeController()  [Abstract - implementar]
          │  │    registerForInactivityMonitoring()
          │  │  })
          │  │
          │  └─ [No se puede sobrescribir]
          │
          ├─ protected abstract void initializeController()
          │  └─ Cada controller implementa su lógica
          │
          ├─ protected void registerSceneForMonitoring()
          │  └─ Helper que usa InactivityMonitorManager
          │
          └─ [Métodos útiles]
             ├─ showInfoAlert()
             ├─ showErrorAlert()
             └─ showWarningAlert()

       ▲
       │ extends
       │
       ├─ DashboardController
       │  └─ @Override initializeController()
       │     └─ setupInactivityMonitoring() [Explícito]
       │
       ├─ UsuariosController
       │  └─ @Override initializeController()
       │     └─ Automático (hereda comportamiento)
       │
       ├─ RolesController
       │  └─ @Override initializeController()
       │     └─ Automático (hereda comportamiento)
       │
       └─ TuNuevoController
          └─ @Override initializeController()
             └─ Automático (hereda comportamiento)
```

---

## 7. Tabla de Responsabilidades

| Componente | Responsabilidad | Estado |
|---|---|---|
| **InactivityMonitorManager** | Gestión central global | ✅ Nuevo |
| **BaseController** | Orquestación de init y register | ✅ Mejorado |
| **SeguridadService** | Persistencia de preferencias | ✅ Actualizado |
| **SeguridadController** | Capturar cambios de UI | ✅ Actualizado |
| **InactivityMonitor** | Detectar inactividad (sin cambios) | ✅ Mejorado |
| **SessionManager** | Validar sesión (sin cambios) | ✅ Compatible |
| **SessionLockScreen** | Mostrar pantalla de bloqueo (sin cambios) | ✅ Compatible |

---

## 8. Flujo Resumido de 30 Segundos

```
┌─────────────────────────────────────────────────┐
│ APP ARRANCA                                     │
├─────────────────────────────────────────────────┤
│ 1. InactivityMonitorManager.getInstance()      │
│ 2. Carga preferencias (monitoreo: ON)          │
│ 3. Espera primer controller                    │
└─────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────┐
│ USUARIO LOGIN → DASHBOARD ABIERTO              │
├─────────────────────────────────────────────────┤
│ 1. DashboardController.initializeController()  │
│ 2. registerSceneForMonitoring() [Automático]   │
│ 3. Monitor → ACTIVO en Dashboard               │
└─────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────┐
│ USUARIO NAVEGA                                 │
├─────────────────────────────────────────────────┤
│ 1. Click en "Usuarios"                         │
│ 2. Dashboard desregistrado                     │
│ 3. Usuarios registrado                         │
│ 4. Monitor → ACTIVO en Usuarios                │
│ 5. Contador reiniciado                         │
└─────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────┐
│ INACTIVIDAD DETECTADA (30 MIN)                 │
├─────────────────────────────────────────────────┤
│ 1. Monitor verifica cada 10 seg                │
│ 2. A 28 min: ⚠️ Alerta de advertencia         │
│ 3. A 30 min: 🔒 Sesión bloqueada              │
│ 4. SessionLockScreen mostrado                  │
│ 5. Usuario ingresa contraseña                  │
│ 6. ✓ Sesión desbloqueada                      │
└─────────────────────────────────────────────────┘
```

---

## 9. Checklist de Implementación Rápida

Para cada nuevo controller:

```
☐ class MyController extends BaseController
☐ @Override public void initializeController() { }
☐ Implementar tu lógica en initializeController()
☐ NO sobrescribir initialize() - BaseController lo hace
☐ Compilar sin errores
☐ Listo - Monitoreo registrado automáticamente
```

---

## 10. Verificación en Logs

Cuando todo funciona, verás en consola:

```
✓ MyController registrado en gestor de monitoreo
✓ Monitoreo de inactividad activado globalmente
✓ Dashboard registrado para monitoreo
⚠️ Alerta de inactividad: 2 minutos restantes
🔒 Sesión bloqueada por: Inactividad detectada
```

---

*Sistema Global de Monitoreo - Documentación Visual Completada*
