# Guía Rápida: Patrón de Actualización de Vistas FXML

## 🎯 Objetivo
Integrar completamente la internacionalización (i18n) en todas las vistas FXML.

## 📝 Patrón Estándar (Copy-Paste)

### 1️⃣ FXML - Agregar fx:id

**Antes:**
```xml
<Label text="Nombre de Usuario" />
<Button text="Guardar" />
```

**Después:**
```xml
<Label fx:id="lblUsername" text="Nombre de Usuario" />
<Button fx:id="btnSave" text="Guardar" />
```

### 2️⃣ Controller - Inyectar elementos

```java
@FXML
private Label lblUsername;

@FXML
private Button btnSave;
```

### 3️⃣ Controller - Método updateUITexts()

```java
private void updateUITexts() {
    I18nUtil.setLabelText(lblUsername, "usuarios.usuario");
    I18nUtil.setButtonText(btnSave, "btn.guardar");
}
```

### 4️⃣ Controller - Initialize con Listener

```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    updateUITexts();
    
    LanguageService.getInstance().addLanguageChangeListener(newLanguage -> {
        Platform.runLater(this::updateUITexts);
    });
}
```

---

## 🔑 Claves de Traducción Recomendadas

### Para UsuariosView
```
usuarios.titulo           → "Gestión de Usuarios"
usuarios.nombre           → "Nombre"
usuarios.usuario          → "Usuario"
usuarios.email            → "Correo Electrónico"
usuarios.rol              → "Rol"
usuarios.estado           → "Estado"
usuarios.nuevo_usuario    → "Nuevo Usuario"
usuarios.editar_usuario   → "Editar Usuario"
btn.nuevo                 → "Nuevo"
btn.editar                → "Editar"
btn.eliminar              → "Eliminar"
btn.guardar               → "Guardar"
btn.cancelar              → "Cancelar"
btn.buscar                → "Buscar"
```

### Para RolesView
```
roles.titulo              → "Gestión de Roles"
roles.nombre              → "Nombre"
roles.descripcion         → "Descripción"
roles.permisos            → "Permisos"
roles.nivel_acceso        → "Nivel de Acceso"
roles.nuevo_rol           → "Nuevo Rol"
roles.editar_rol          → "Editar Rol"
btn.nuevo                 → "Nuevo"
btn.editar                → "Editar"
btn.eliminar              → "Eliminar"
```

### Para SeguridadView
```
seguridad.titulo          → "Panel de Seguridad"
seguridad.eventos_auditoria → "Eventos de Auditoría"
seguridad.tipo_evento     → "Tipo de Evento"
seguridad.usuario         → "Usuario"
seguridad.fecha           → "Fecha"
seguridad.exportar_eventos → "Exportar Eventos"
btn.exportar              → "Exportar a Excel"
```

### Para ConfiguracionView
```
config.tema               → "Tema de la Interfaz"
config.tema.claro         → "Tema Claro"
config.tema.oscuro        → "Tema Oscuro"
config.idioma             → "Idioma"
config.guardar            → "Guardar Cambios"
btn.guardar               → "Guardar"
btn.cancelar              → "Cancelar"
```

### Para RegisterView
```
login.titulo              → "Registro de Usuario"
usuarios.nombre           → "Nombre Completo"
usuarios.usuario          → "Usuario"
login.password            → "Contraseña"
usuarios.email            → "Correo Electrónico"
btn.guardar               → "Registrarse"
btn.cancelar              → "Cancelar"
```

---

## 💻 Imports Necesarios en Controller

```java
import com.app.service.LanguageService;
import com.app.util.I18nUtil;
import javafx.application.Platform;
```

---

## ✅ Checklist para cada Vista

- [ ] Identificar todos los labels/botones con texto hardcodeado
- [ ] Agregar `fx:id="lblXxx"` o `fx:id="btnXxx"` a cada elemento
- [ ] Agregar `@FXML` fields en Controller
- [ ] Crear método `updateUITexts()` con todos los elementos
- [ ] Llamar `updateUITexts()` en `initialize()`
- [ ] Registrar listener de cambio de idioma
- [ ] Agregar imports necesarios
- [ ] Compilar: `mvn compile -DskipTests`
- [ ] Probar cambio de idioma en vivo

---

## 🧪 Testing

Después de actualizar cada vista:

1. Compilar:
```bash
mvn compile -DskipTests
```

2. Ejecutar:
```bash
mvn javafx:run
```

3. Cambiar idioma en Configuración → Verificar que UI se actualice

---

## 📊 Progreso

```
✅ LoginView.fxml - COMPLETO
✅ DashboardView.fxml - PARCIALMENTE
⏳ UsuariosView.fxml - PENDIENTE
⏳ RolesView.fxml - PENDIENTE
⏳ SeguridadView.fxml - PENDIENTE
⏳ ConfiguracionView.fxml - PENDIENTE
⏳ RegisterView.fxml - PENDIENTE
```

---

## 🎓 Ejemplo Completo: UsuariosController

### FXML (fragmento)
```xml
<Label fx:id="lblTitle" text="Gestión de Usuarios" />
<Button fx:id="btnNew" text="Nuevo Usuario" />
<Button fx:id="btnEdit" text="Editar" />
<Button fx:id="btnDelete" text="Eliminar" />
<Button fx:id="btnSave" text="Guardar" />
```

### Controller
```java
@FXML private Label lblTitle;
@FXML private Button btnNew;
@FXML private Button btnEdit;
@FXML private Button btnDelete;
@FXML private Button btnSave;

@Override
public void initialize(URL location, ResourceBundle resources) {
    updateUITexts();
    
    LanguageService.getInstance().addLanguageChangeListener(newLanguage -> {
        Platform.runLater(this::updateUITexts);
    });
}

private void updateUITexts() {
    I18nUtil.setLabelText(lblTitle, "usuarios.titulo");
    I18nUtil.setButtonText(btnNew, "usuarios.nuevo_usuario");
    I18nUtil.setButtonText(btnEdit, "btn.editar");
    I18nUtil.setButtonText(btnDelete, "btn.eliminar");
    I18nUtil.setButtonText(btnSave, "btn.guardar");
}
```

---

## 🆘 Troubleshooting

### Error: "method setupLanguageListener() is undefined"
**Solución**: Asegúrate de haber agregado el método o integrado el listener directamente en `initialize()`.

### Labels no se actualizan al cambiar idioma
**Solución**: Verifica que:
1. Hayas llamado `updateUITexts()` en `initialize()`
2. El listener esté registrado
3. La clave de traducción exista en `LanguageService`

### Compilación falla con "cannot find symbol"
**Solución**: Asegúrate de:
1. Agregar `@FXML` antes de cada field
2. El `fx:id` en FXML coincida con el nombre del field en Java
3. Agregar imports necesarios

---

## 📞 Resumen

**Tiempo estimado por vista**: 10-15 minutos
**Vistas restantes**: 5
**Tiempo total estimado**: 50-75 minutos

¡Listo para comenzar! 🚀
