# 🎨 GUÍA DE FUNCIONALIDAD: Temas e Idiomas Dinámicos

## ✨ Características Implementadas

### 1. **Sistema de Temas (Claro/Oscuro)**
- ✅ Cambio dinámico sin reiniciar la aplicación
- ✅ Tema claro: colores claros y fondo blanco
- ✅ Tema oscuro: colores oscuros y fondo gris oscuro
- ✅ Preferencia guardada en base de datos
- ✅ Se aplica automáticamente al iniciar la aplicación

### 2. **Sistema de Idiomas Multilingüe**
- ✅ Español (idioma por defecto)
- ✅ English
- ✅ Português
- ✅ Français
- ✅ Cambio instantáneo en toda la aplicación
- ✅ Preferencia guardada en base de datos

---

## 🚀 Cómo Usar

### Cambiar el Tema

1. **Accede a Configuración:**
   - En el Dashboard, click en "⚙️ Configuración"

2. **Selecciona la Pestaña "Interfaz de Usuario"**

3. **Elige tu Tema:**
   - **Tema Claro:** Fondo blanco, texto oscuro (ideal para durante el día)
   - **Tema Oscuro:** Fondo oscuro, texto claro (ideal para la noche)

4. **Click en "Guardar Cambios"**
   - El tema se aplicará **inmediatamente** a toda la ventana
   - La preferencia se guardará en la base de datos

### Cambiar el Idioma

1. **Accede a Configuración:**
   - En el Dashboard, click en "⚙️ Configuración"

2. **Selecciona la Pestaña "Interfaz de Usuario"**

3. **Despliega el ComboBox "Idioma":**
   - Español
   - English
   - Português
   - Français

4. **Selecciona tu idioma preferido**

5. **Click en "Guardar Cambios"**
   - Todos los textos se actualizarán inmediatamente
   - La preferencia se guardará en la base de datos

---

## 📁 Archivos Modificados/Creados

### Servicios Nuevos:
- `ThemeService.java` - Maneja cambios de tema dinámicamente
- `LanguageService.java` - Maneja traducciones y cambios de idioma

### Archivos CSS Nuevos:
- `theme-light.css` - Estilos para tema claro
- `theme-dark.css` - Estilos para tema oscuro

### Controladores Modificados:
- `ConfiguracionController.java` - Ahora aplica cambios en tiempo real
- `Main.java` - Registra la escena inicial con el servicio de tema

### Base de Datos:
- La configuración se persiste en la tabla `configuracion_sistema`
- Claves usadas:
  - `app.tema` - ("claro" o "oscuro")
  - `app.idioma` - ("Español", "English", "Português", "Français")

---

## 🎯 Detalles Técnicos

### ThemeService
```java
// Cambiar a tema claro
ThemeService.getInstance().setThemeClaro();

// Cambiar a tema oscuro
ThemeService.getInstance().setThemeOscuro();

// Registrar una escena para recibir actualizaciones
ThemeService.getInstance().registerScene(scene);
```

### LanguageService
```java
// Cambiar idioma
LanguageService.getInstance().setLanguage("Español");

// Obtener traducción
String texto = LanguageService.getInstance().get("menu.usuarios");

// Escuchar cambios de idioma
LanguageService.getInstance().addLanguageChangeListener(newLang -> {
    // Actualizar interfaz cuando cambia el idioma
});
```

---

## 💾 Persistencia

- **Tema:** Se guarda en `configuracion_sistema` con clave `app.tema`
- **Idioma:** Se guarda en `configuracion_sistema` con clave `app.idioma`
- **Al reiniciar:** La aplicación carga automáticamente las preferencias guardadas

---

## 🌈 Paleta de Colores

### Tema Claro (Light)
- Fondo: #f0f0f0
- Texto: #1a1a1a
- Primario: #27ae60 (verde)
- Secundario: #3498db (azul)

### Tema Oscuro (Dark)
- Fondo: #2c3e50 (gris oscuro)
- Texto: #ecf0f1 (blanco/gris claro)
- Primario: #1abc9c (turquesa)
- Secundario: #3498db (azul)

---

## ⚡ Próximas Mejoras (Opcionales)

- [ ] Tema personalizado (permitir seleccionar colores)
- [ ] Más idiomas adicionales
- [ ] Sincronización de temas entre dispositivos
- [ ] Cambio automático de tema según hora del día
- [ ] Importar/Exportar configuración

