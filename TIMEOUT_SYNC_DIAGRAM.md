# 🔄 Cambio de Timeout - Diagrama de Sincronización

## Arquitectura del Sistema de Timeout

```
╔══════════════════════════════════════════════════════════════════════════╗
║                          CAPA DE PRESENTACIÓN (UI)                       ║
║                                                                          ║
║                    SeguridadView.fxml                                    ║
║                    ┌──────────────────────────────┐                      ║
║                    │  ComboBox cmbTiempoBloqueo   │                      ║
║                    │  Options:                    │                      ║
║                    │  - 5 minutos                 │                      ║
║                    │  - 15 minutos                │                      ║
║                    │  - 30 minutos (default)      │                      ║
║                    │  - 60 minutos                │                      ║
║                    │  - 120 minutos               │                      ║
║                    └─────────────┬────────────────┘                      ║
║                                  │                                       ║
║                       [Guardar Cambios]                                  ║
║                                  │                                       ║
╚══════════════════════════════════╪═══════════════════════════════════════╝
                                  │
                                  ▼
╔══════════════════════════════════════════════════════════════════════════╗
║                    CAPA DE CONTROLADORES (JAVAFX)                        ║
║                                                                          ║
║              SeguridadController.java                                    ║
║              ┌─────────────────────────────────────────┐                 ║
║              │ @FXML handleGuardarConfiguracion()     │                 ║
║              │                                         │                 ║
║              │ 1. Obtener valor de ComboBox:           │                 ║
║              │    String selected = cmbTiempoBloqueo   │                 ║
║              │                      .getValue();       │                 ║
║              │                                         │                 ║
║              │ 2. Convertir a minutos:                 │                 ║
║              │    int minutes =                        │                 ║
║              │      convertirTextoAMinutos(selected);  │                 ║
║              │                                         │                 ║
║              │ 3. Guardar en servicio:                 │                 ║
║              │    seguridadService.setSessionTimeout(  │                 ║
║              │      minutes);                          │                 ║
║              │                                         │                 ║
║              │ 4. Mostrar confirmación:                │                 ║
║              │    mostrarInfo("Guardado exitosamente") │                 ║
║              │                                         │                 ║
║              │ 5. Registrar en auditoría:              │                 ║
║              │    EventoSeguridad guardado             │                 ║
║              └──────────────────┬──────────────────────┘                 ║
║                                 │                                        ║
╚═════════════════════════════════╪════════════════════════════════════════╝
                                  │
                                  ▼
╔══════════════════════════════════════════════════════════════════════════╗
║                    CAPA DE SERVICIOS (LÓGICA)                            ║
║                                                                          ║
║          SeguridadService.java                                           ║
║          ┌──────────────────────────────────────────────┐                ║
║          │ public void setSessionTimeout(int minutes) │                 ║
║          │ {                                            │                ║
║          │   // 📍 PASO 1: Guardar en preferencias      │                ║
║          │   preferences.putInt(                        │                ║
║          │     "session.timeout",                       │                ║
║          │     minutes                                  │                ║
║          │   );  ← Persistencia                         │                ║
║          │                                              │                ║
║          │   // 📍 PASO 2: Sincronizar con sesión       │                ║
║          │   sessionManager                             │                ║
║          │     .setSessionTimeoutMinutes(minutes);      │                ║
║          │   ← ✨ APLICAR CAMBIO EN TIEMPO REAL        │                ║
║          │                                              │                ║
║          │   // 📍 PASO 3: Auditar cambio              │                ║
║          │   registrarEvento(                           │                ║
║          │     "Timeout cambiado a " + minutes + " min" │                ║
║          │   );  ← Tabla EventoSeguridad               │                ║
║          │ }                                            │                ║
║          └──────────────────┬───────────────────────────┘                ║
║                             │                                            ║
║                             ├─────────────────────────────┐              ║
║                             │                             │              ║
║                    [Preferences]            [SessionManager]            ║
║                    ┌─────────────┐          ┌─────────────┐              ║
║                    │ Disk        │          │ Memory      │              ║
║                    │ Storage     │          │ (Singleton) │              ║
║                    │ session.    │          │             │              ║
║                    │ timeout=N   │          │ timeout=N   │              ║
║                    └─────────────┘          └──────┬──────┘              ║
║                                                    │                     ║
╚════════════════════════════════════════════════════╪═════════════════════╝
                                                    │
                    ┌───────────────────────────────┴────────────────┐
                    │                                                │
                    ▼                                                ▼
╔═════════════════════════════════╗      ╔══════════════════════════════╗
║   SessionManager.java            ║      ║ InactivityMonitor.java       ║
║   (Singleton)                    ║      ║ (Running in Background)      ║
║                                  ║      ║                              ║
║ sessionTimeoutMinutes = N        ║      ║ ┌────────────────────────┐   ║
║                                  ║      ║ │ Timeline:             │   ║
║ public int                       ║      ║ │ cada 10 segundos      │   ║
║ getSessionTimeoutMinutes() {     ║      ║ │                       │   ║
║   return sessionTimeoutMinutes;  ║      ║ │ checkInactivity() {   │   ║
║ }                                ║      ║ │   int timeout =       │   ║
║                                  ║      ║ │   sessionManager      │   ║
║ public void                      ║      ║ │   .getSessionTimeout()║   ║
║ setSessionTimeoutMinutes         ║      ║ │   ← LEE VALOR ACTUAL  │   ║
║ (int minutes) {                  ║      ║ │                       │   ║
║   this.sessionTimeoutMinutes     ║      ║ │   if (inactiveTime    │   ║
║     = minutes;                   ║      ║ │       >= timeout) {   │   ║
║ }                                ║      ║ │     lockSession();    │   ║
║                                  ║      ║ │   }                   │   ║
║                                  ║      ║ │ }                     │   ║
║                                  ║      ║ └────────────────────────┘   ║
║                                  ║      ║                              ║
╚═════════════════════════════════╝      ╚══════════════════════════════╝
                                              │
                    ┌─────────────────────────┘
                    │
                    ▼
╔═════════════════════════════════════════════════════════════════════════╗
║                          RESULTADO FINAL                               ║
║                                                                        ║
║  ✅ Timeout actualizado en memoria (SessionManager)                   ║
║  ✅ Próxima verificación de inactividad usará el nuevo timeout       ║
║  ✅ Si inactividad >= nuevo timeout → Bloquea sesión               ║
║  ✅ Cambio aplicado sin reinicio de app                            ║
║                                                                        ║
╚═════════════════════════════════════════════════════════════════════════╝
```

---

## Secuencia Temporal

```
TIEMPO         USUARIO                     SISTEMA
──────────────────────────────────────────────────────────────────────

T=0:00         ▶ Abre Seguridad           SessionManager.timeout = 30 min
               ▶ Ve ComboBox timeout      

T=0:05         ▶ Selecciona 5 minutos     Estado sin cambios
               ▶ Click "Guardar"          

T=0:07         ◀ Respuesta OK              SeguridadService.setSessionTimeout(5)
                                          ▶ preferences.putInt("timeout", 5)
                                          ▶ sessionManager.setSessionTimeoutMinutes(5)
                                          ▶ registrarEvento(...)
                                          SessionManager.timeout = 5 min ✅

T=0:10         ▶ Cierra Seguridad,        InactivityMonitor.checkInactivity():
               ▶ Vuelve a Dashboard       timeout = sessionManager.getTimeout() = 5
               
T=0:20         [Usuario inactivo]         Timer sigue contando...
                                          Inactividad = 13 minutos

T=3:20         ▶ Aparece advertencia      InactivityMonitor:
                                          timeoutMinutos = 5
                                          inactiveMinutos = 3
                                          ¡Tiempo restante = 2 min! 
                                          ⚠️ Mostrar alerta

T=5:20         ▶ SIN INTERACCIÓN          InactivityMonitor:
                                          inactiveMinutos = 5
                                          inactiveMinutos >= timeout(5)
                                          🔒 BLOQUEAR SESIÓN

T=5:22         🔒 Pantalla de bloqueo      SessionLockScreen mostrada
               ▶ Usuario ingresa password  Validar credenciales
               ▶ Sesión desbloqueada      lastActivityTime = ahora (reset)

T=5:25         ▶ Vuelve al trabajo        Timer de inactividad reinicia
                                          Próximo bloqueo en 5 min...
```

---

## Flujo de Datos - Llamadas de Función

```
handleGuardarConfiguracion()
    │
    ├─→ convertirTextoAMinutos("5 minutos")  → 5
    │
    ├─→ seguridadService.setSessionTimeout(5)
    │       │
    │       ├─→ preferences.putInt("session.timeout", 5)
    │       │       └─→ Guardar en disco
    │       │
    │       ├─→ sessionManager.setSessionTimeoutMinutes(5)
    │       │       └─→ sessionTimeoutMinutes = 5 ✅ EN MEMORIA
    │       │
    │       ├─→ registrarEvento(tipo, descripcion)
    │       │       └─→ EventoSeguridadDAO.save()
    │       │           └─→ INSERT INTO eventos_seguridad (...)
    │       │
    │       └─→ return void
    │
    ├─→ mostrarInfo("Guardado exitosamente")
    │
    └─→ cargarEventos()  // Refrescar tabla


checkInactivity() [cada 10 segundos]
    │
    ├─→ sessionManager.getSessionTimeoutMinutes()  ← LEE NUEVO VALOR
    │       └─→ return 5  (cambio aplicado)
    │
    ├─→ ChronoUnit.MINUTES.between(lastActivityTime, now)
    │       └─→ inactiveMinutes = X
    │
    ├─→ if (inactiveMinutes >= 5)  ← Compara con nuevo timeout
    │       │
    │       └─→ lockSession("Inactividad detectada")
    │           └─→ SessionLockScreen.show()
    │
    └─→ else: continuar monitoreando
```

---

## Consistencia de Estado

```
ANTES DEL CAMBIO:
┌─────────────────────────┐
│ Preferences (Disco):    │
│ session.timeout = 30    │
├─────────────────────────┤
│ SessionManager (RAM):   │
│ timeout = 30            │
├─────────────────────────┤
│ InactivityMonitor:      │
│ lee timeout = 30        │
└─────────────────────────┘

       ↓ [Guardar timeout = 5]

DESPUÉS DEL CAMBIO:
┌─────────────────────────┐
│ Preferences (Disco):    │
│ session.timeout = 5 ✅  │ ← Persistente
├─────────────────────────┤
│ SessionManager (RAM):   │
│ timeout = 5 ✅          │ ← En Memoria
├─────────────────────────┤
│ InactivityMonitor:      │
│ lee timeout = 5 ✅      │ ← Próxima verificación
└─────────────────────────┘

✅ CONSISTENCIA GARANTIZADA
- RAM y Disco sincronizados
- InactivityMonitor usa valor actual
- No requiere reinicio
```

---

## Ventajas del Diseño

| Aspecto | Beneficio | Cómo |
|---|---|---|
| **Sin Reinicio** | Cambios inmediatos | Direct update a `SessionManager` |
| **Thread-Safe** | Sincronización segura | Singleton + preferencias sincronizadas |
| **Persistencia** | Timeout se guarda | Preferences del sistema operativo |
| **Auditable** | Registro de cambios | `EventoSeguridad` con timestamp |
| **Flexible** | Valores variables | ComboBox configurable |
| **Eficiente** | Sin overhead** | Solo 1 línea de actualización |

---

## Casos de Uso

### Caso 1: Aumentar Seguridad (Admin detecta amenaza)
```
Admin: "Detecto login fallidos, aumentar timeout"
Acción: Seguridad → 5 minutos → Guardar
Resultado: Próximas sesiones se bloquean más rápido
```

### Caso 2: Sesión Larga (Usuario trabajando)
```
Usuario: "Necesito trabajar 2 horas sin interrupciones"
Admin: Seguridad → 120 minutos → Guardar
Resultado: Sesión se extiende sin forzar logout
```

### Caso 3: Desarrollo/Testing
```
Dev: "Necesito timeout corto para probar bloqueo"
Acción: Seguridad → 2 minutos (si existe) → Guardar
Resultado: Pruebas rápidas de SessionLockScreen
```

---

*Actualizado en sesión actual*
