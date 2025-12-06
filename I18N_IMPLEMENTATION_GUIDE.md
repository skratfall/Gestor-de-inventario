# Guía de Internacionalización en FXML - Patrón de Implementación

## Descripción General
La internacionalización se implementa en 3 pasos:
1. **FXML**: Agregar `fx:id` a los elementos que necesitan traducción
2. **Controller**: Inyectar los elementos con `@FXML` y crear método `updateUITexts()`
3. **Listener**: Registrar listener para cambios de idioma

## Paso 1: Actualizar el FXML

### Agregar fx:id a Labels y Botones
```xml
<!-- ANTES -->
<Label text="Bienvenido" />

<!-- DESPUÉS -->
<Label fx:id="lblWelcome" text="Bienvenido" />
```

### Patrón completo de formulario
```xml
<VBox spacing="10.0">
    <Label fx:id="lblUsername" text="Usuario" />
    <TextField fx:id="usernameField" promptText="Ingresa tu usuario" />
</VBox>
```

## Paso 2: Actualizar el Controller

### Inyectar elementos FXML
```java
@FXML
private Label lblWelcome;

@FXML
private TextField usernameField;
```

### Método updateUITexts()
```java
private void updateUITexts() {
    I18nUtil.setLabelText(lblWelcome, "login.titulo");
    I18nUtil.setPromptText(usernameField, "login.usuario");
}
```

### Initialize con listener
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    updateUITexts();
    
    // ... otros inicios ...
    
    // Registrar listener para cambios de idioma
    LanguageService.getInstance().addLanguageChangeListener(newLanguage -> {
        Platform.runLater(this::updateUITexts);
    });
}
```

## Paso 3: Métodos Disponibles en I18nUtil

```java
// Establecer texto de Label
I18nUtil.setLabelText(label, "clave.traduccion");

// Establecer texto de Botón
I18nUtil.setButtonText(button, "btn.guardar");

// Establecer promptText en campos
I18nUtil.setPromptText(textField, "login.usuario");

// Establecer Tooltip
I18nUtil.setTooltip(node, "tooltip.save");

// Obtener traducción simple
String text = I18nUtil.get("clave");

// Obtener con formateo
String formatted = I18nUtil.get("msg.usuario.creado", "Juan");
```

## Claves de Traducción Disponibles

### Login
- `login.titulo` - Título de login
- `login.usuario` - Etiqueta de usuario
- `login.password` - Etiqueta de contraseña
- `login.entrar` - Botón entrar
- `login.error` - Mensaje de error

### Botones comunes
- `btn.nuevo` - Nuevo
- `btn.editar` - Editar
- `btn.eliminar` - Eliminar
- `btn.guardar` - Guardar
- `btn.cancelar` - Cancelar
- `btn.buscar` - Buscar

### Módulos
- `usuarios.titulo` - Título de usuarios
- `roles.titulo` - Título de roles
- `seguridad.titulo` - Título de seguridad
- `dashboard.titulo` - Título de dashboard

## Ejemplo Completo: LoginController

```java
@FXML
private Label lblWelcome;

@FXML
private TextField usernameField;

@FXML
private PasswordField passwordField;

@Override
public void initialize(URL location, ResourceBundle resources) {
    updateUITexts();
    
    // Listener para cambios de idioma
    LanguageService.getInstance().addLanguageChangeListener(newLanguage -> {
        Platform.runLater(this::updateUITexts);
    });
}

private void updateUITexts() {
    I18nUtil.setLabelText(lblWelcome, "login.titulo");
    I18nUtil.setPromptText(usernameField, "login.usuario");
    I18nUtil.setPromptText(passwordField, "login.password");
}
```

## Vistas a Actualizar

1. **LoginView.fxml** - ✅ Hecho (Ejemplo)
2. **RegisterView.fxml** - Labels y campos de registro
3. **DashboardView.fxml** - Títulos, botones, labels
4. **UsuariosView.fxml** - Títulos de columnas, botones
5. **RolesView.fxml** - Títulos de columnas, botones
6. **SeguridadView.fxml** - Títulos, etiquetas
7. **ConfiguracionView.fxml** - Secciones, campos

## Consejos Prácticos

### Para Labels estáticos
```xml
<Label fx:id="lblTitle" text="Placeholder" />
```
El texto será reemplazado por `I18nUtil.setLabelText()` en el controller.

### Para campos de entrada
```xml
<TextField fx:id="txtName" promptText="Placeholder" />
```
El promptText será reemplazado por `I18nUtil.setPromptText()`.

### Para Tooltips
```java
I18nUtil.setTooltip(button, "tooltip.guardar");
```

### Para composiciones complejas
Si necesitas múltiples idiomas en un Label (ej: "Usuario: John"):
```java
String userName = "John";
I18nUtil.setLabelText(lblUser, "usuarios.usuario") + ": " + userName;
// O mejor:
String template = I18nUtil.get("msg.usuario.es", userName);
```

## Testing

Después de actualizar cada vista:
1. Ejecuta `mvn clean compile`
2. Prueba cambiar de idioma en tiempo real
3. Verifica que todos los textos se actualicen dinámicamente

## Notas

- Los emojis se pueden mantener en FXML o en las claves de traducción
- Es recomendable mantener los fx:id descriptivos (ej: `lblUsername` en lugar de `lbl1`)
- Agrupa las claves por módulo para facilitar mantenimiento
