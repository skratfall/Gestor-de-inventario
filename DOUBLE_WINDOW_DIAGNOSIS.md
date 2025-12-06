# 🔍 Diagnóstico - Problema de Doble Ventana

## Problema Reportado
El usuario reporta que aparecen DOS ventanas cuando se ejecuta la aplicación, en lugar de una sola.
- ✅ NO se ve `[STAGE #2]` en los logs
- ✅ Pero SÍ se ven visualmente dos ventanas

## Investigación: Segunda Iteración

Esta iteración añade rastreo más profundo para detectar:
1. **Window.getWindows()** - TODAS las ventanas de JavaFX visibles (no solo Stage creados)
2. **Monitor en background** - Detecta cambios en el número de ventanas
3. **Contador de show() calls** - Rastrean cada llamada a `show()`
4. **Stack traces detallados** - Para identificar desde dónde se muestra cada ventana

## Cambios Realizados (v2)

### Archivos Modificados
- `StageDebugger.java` - Mejorado con:
  - Detección de Stage mostrado dos veces (`shownStages` Set)
  - Contador de llamadas a `show()`
  - Stack traces más profundos (hasta 10 niveles)
  
- `Main.java` - Mejorado con:
  - `countVisibleStages()` - Enumera TODAS las ventanas visibles
  - `startWindowMonitor()` - Thread daemon que monitorea cambios cada 2 segundos
  - `countVisibleStagesQuiet()` - Versión sin logs para el monitor
  - Más información antes de `show()` (width, height, resizable)

## Salida Esperada en Consola

```
📋 Starting application...
🚀 DEBUG: Main.start() called - primaryStage: 1234567
🆔 [STAGE #1] Created: Main - Primary Stage - hashCode: 1234567
✅ DEBUG: About to call primaryStage.show()
   Stage width: 900.0
   Stage height: 800.0
   Stage resizable: false
📞 [SHOW CALL #1] show() called from: Main.start() - PRIMARY SHOW CALL
📍 Call stack:
   [3] com.app.Main.start(Main.java:XX)
   [4] javafx.graphics...
✅ DEBUG: primaryStage.show() completed successfully
   Stages currently visible: 1
   📌 Visible Stage: Login - Sistema de Gestión de Inventario (hash: 1234567)
🔔 [MONITOR] Cambio detectado si aparece segunda ventana aquí
```

## Cómo Ejecutar y Diagnosticar

### Opción A: Línea de comandos PowerShell

```powershell
cd "c:\Users\usuario\Documents\Gestor-de-inventario"
mvn clean javafx:run
```

### Opción B: NetBeans (F6)

Presiona F6 y observa la consola de salida

### Opción C: VS Code

Presiona F5 para debugging

## Qué Buscar Esta Vez

1. **Después de "show() completed successfully":**
   - ¿Dice `Stages currently visible: 1` o `Stages currently visible: 2`?

2. **En el MONITOR (después de 2-4 segundos):**
   - ¿Ves un mensaje de `[MONITOR] Cambio detectado`?
   - ¿Aparecen múltiples `📌 Visible Stage` cuando solo deberían ser uno?

3. **En los SHOW CALLS:**
   - ¿Hay un `📞 [SHOW CALL #2]` aparecer inesperadamente?

4. **En el Stack Trace:**
   - ¿De dónde viene la llamada? (qué método/línea)

## Próximos Pasos

**Por favor ejecuta la aplicación nuevamente y comparte:**

1. ✅ La salida COMPLETA de la consola desde inicio hasta que aparezcan las dos ventanas
2. ✅ Si ves algún `[MONITOR] Cambio detectado` - copia ese parte también
3. ✅ Describe visualmente qué ves (¿dos ventanas idénticas? ¿una encima de otra? ¿tamaños diferentes?)
4. ✅ ¿En qué momento aparecen? (al iniciar, después de login, etc.)

## Hipótesis Actuales

1. **Doble renderizado de JavaFX**: El mismo Stage se está pintando dos veces
2. **Bug del driver gráfico**: Windows/GPU mostrando la misma ventana duplicada
3. **Listener no capturado**: Un listener en algún lado llamando `show()` nuevamente
4. **Problema de decoraciones de ventana**: Windows está mostrando dos instancias (improbable)

---

**Estado**: ✅ BUILD SUCCESS - Listo para depuración v2
**Próximo paso**: Ejecuta y comparte los logs completos

