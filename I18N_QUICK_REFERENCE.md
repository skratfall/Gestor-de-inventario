# Checklist Rápida: Actualización de FXML a i18n

## Plantilla de 5 Pasos

### FXML - Agregar fx:id
```
Antes:  <Label text="Usuario" />
Después: <Label fx:id="lblUsername" text="Usuario" />
```

### Controller - Inyectar
```java
@FXML private Label lblUsername;
```

### Controller - Método updateUITexts()
```java
private void updateUITexts() {
    I18nUtil.setLabelText(lblUsername, "usuarios.usuario");
}
```

### Controller - Initialize
```java
@Override
public void initialize(...) {
    updateUITexts();
    LanguageService.getInstance().addLanguageChangeListener(lang -> 
        Platform.runLater(this::updateUITexts)
    );
}
```

### Compilar y Probar
```bash
mvn clean compile
```

---

## Vistas a Actualizar (Prioridad)

### ✅ Completadas
- [x] LoginView.fxml (Ejemplo)

### 🟡 En Progreso
- [ ] DashboardView.fxml (Botones, labels principales)
- [ ] ConfiguracionView.fxml (Secciones, campos)

### 📋 Por Hacer
- [ ] UsuariosView.fxml (Tabla, botones)
- [ ] RolesView.fxml (Tabla, botones)
- [ ] SeguridadView.fxml (Labels, botones)
- [ ] RegisterView.fxml (Campos, botones)

---

## Comandos Útiles

### Buscar Labels sin fx:id en FXML
```bash
grep -n 'text="' DashboardView.fxml | grep -v 'fx:id'
```

### Buscar Buttons sin fx:id
```bash
grep -n '<Button' DashboardView.fxml | grep -v 'fx:id'
```

### Compilar solo
```bash
mvn compile -DskipTests
```

### Ejecutar aplicación
```bash
mvn javafx:run
```

---

## Claves de Traducción por Vista

### Dashboard
- dashboard.titulo
- dashboard.bienvenido
- dashboard.usuarios_totales
- dashboard.roles_totales
- dashboard.estado
- btn.usuarios → "👥 Gestión de Usuarios"
- btn.roles → "🔐 Gestión de Roles"

### Usuarios
- usuarios.titulo
- usuarios.nombre
- usuarios.usuario
- usuarios.email
- usuarios.rol
- btn.nuevo
- btn.editar
- btn.eliminar

### Roles
- roles.titulo
- roles.nombre
- roles.descripcion
- roles.permisos

### Seguridad
- seguridad.titulo
- seguridad.eventos_auditoria
- seguridad.exportar_eventos

### Configuración
- config.tema
- config.idioma
- config.guardar
