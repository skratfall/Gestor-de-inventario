# 🎨 Guía Rápida de Componentes UI

## Referencia Rápida de Clases CSS

### 📌 BOTONES

```xml
<!-- Botón Primario (Azul) -->
<Button styleClass="button-primary" text="Guardar" />

<!-- Botón Secundario (Gris) -->
<Button styleClass="button-secondary" text="Editar" />

<!-- Botón Éxito (Verde) -->
<Button styleClass="button-success" text="Confirmar" />

<!-- Botón Peligro (Rojo) -->
<Button styleClass="button-danger" text="Eliminar" />

<!-- Botón Advertencia (Naranja) -->
<Button styleClass="button-warning" text="Atención" />

<!-- Botón Info (Azul claro) -->
<Button styleClass="button-info" text="Información" />

<!-- Botón Cancelar (Gris) -->
<Button styleClass="button-cancel" text="Cancelar" />
```

### 🔤 CAMPOS DE TEXTO

```xml
<!-- Campo de texto normal -->
<TextField styleClass="text-field" promptText="Ingresa..." />

<!-- Campo de contraseña -->
<PasswordField styleClass="password-field" promptText="Contraseña..." />

<!-- Campo de búsqueda -->
<TextField styleClass="search-field" promptText="Buscar..." />

<!-- Campo de login -->
<TextField styleClass="login-field" promptText="Usuario..." />
```

### 📊 TABLAS

```xml
<!-- Tabla con estilos profesionales -->
<TableView styleClass="table-view">
    <columns>
        <TableColumn text="Nombre" prefWidth="150" />
        <TableColumn text="Email" prefWidth="200" />
        <TableColumn text="Estado" prefWidth="100" />
    </columns>
</TableView>
```

### 📦 TARJETAS

```xml
<!-- Tarjeta estándar -->
<VBox styleClass="card" spacing="10">
    <Label text="Contenido de tarjeta" />
</VBox>

<!-- Dashboard Card - Primaria -->
<VBox styleClass="dashboard-card, admin-card-primary" spacing="10">
    <Label text="Usuarios Activos" />
</VBox>

<!-- Dashboard Card - Éxito -->
<VBox styleClass="dashboard-card, admin-card-success" spacing="10">
    <Label text="Roles Configurados" />
</VBox>

<!-- Dashboard Card - Info -->
<VBox styleClass="dashboard-card, admin-card-info" spacing="10">
    <Label text="Conexión" />
</VBox>

<!-- Dashboard Card - Advertencia -->
<VBox styleClass="dashboard-card, admin-card-warning" spacing="10">
    <Label text="Sincronización" />
</VBox>

<!-- Tarjeta de sección -->
<VBox styleClass="section-card" spacing="10">
    <Label text="Configuración" />
</VBox>
```

### 🏷️ ETIQUETAS Y TEXTO

```xml
<!-- Título de página -->
<Label styleClass="page-title" text="Panel de Control" />

<!-- Título de módulo -->
<Label styleClass="module-title" text="Gestión de Usuarios" />

<!-- Subtítulo de módulo -->
<Label styleClass="module-subtitle" text="Administre usuarios del sistema" />

<!-- Título de sección -->
<Label styleClass="section-title" text="Información del Sistema" />

<!-- Subtítulo de sección -->
<Label styleClass="section-subtitle" text="Datos adicionales" />

<!-- Etiqueta de información -->
<Label styleClass="info-label" text="Este es un texto de información" />

<!-- Etiqueta total -->
<Label styleClass="total-label" text="Total: 10 registros" />
```

### 🎯 ENCABEZADOS

```xml
<!-- Encabezado superior -->
<HBox styleClass="top-header">
    <Label styleClass="page-title" text="Bienvenido" />
</HBox>

<!-- Encabezado de módulo -->
<VBox styleClass="module-header">
    <Label styleClass="module-title" text="Usuarios" />
    <Label styleClass="module-subtitle" text="Gestione usuarios..." />
</VBox>
```

### ✅ CONTROLES

```xml
<!-- Checkbox -->
<CheckBox text="Activar notificaciones" />

<!-- ComboBox -->
<ComboBox styleClass="combo-box" promptText="Selecciona una opción" />

<!-- Separador -->
<Separator />

<!-- Separador vertical -->
<Separator orientation="VERTICAL" />
```

### 🎨 ALERTAS Y ESTADOS

```xml
<!-- Alerta de éxito -->
<VBox styleClass="alert-success">
    <Label text="Operación completada exitosamente" />
</VBox>

<!-- Alerta de error -->
<VBox styleClass="alert-error">
    <Label text="Error en la operación" />
</VBox>

<!-- Alerta de advertencia -->
<VBox styleClass="alert-warning">
    <Label text="Advertencia importante" />
</VBox>

<!-- Alerta de información -->
<VBox styleClass="alert-info">
    <Label text="Información importante" />
</VBox>

<!-- Estado activo -->
<Label styleClass="state-active" text="Activo" />

<!-- Estado inactivo -->
<Label styleClass="state-inactive" text="Inactivo" />

<!-- Estado pendiente -->
<Label styleClass="state-pending" text="Pendiente" />
```

### 🖼️ LAYOUT COMPONENTES

```xml
<!-- Contenedor principal -->
<VBox styleClass="main-content">
    <Label text="Contenido principal" />
</VBox>

<!-- Scroll pane personalizado -->
<ScrollPane styleClass="main-content-scroll">
    <VBox styleClass="main-content">
        <Label text="Contenido scrollable" />
    </VBox>
</ScrollPane>

<!-- Tarjeta de acciones rápidas -->
<VBox styleClass="quick-actions-card">
    <VBox styleClass="action-card">
        <Label text="Acción" />
    </VBox>
</VBox>

<!-- Tarjeta de información -->
<VBox styleClass="info-card">
    <Label text="Información" />
</VBox>
```

---

## 🎯 PATRONES DE USO COMUNES

### Formulario Completo

```xml
<VBox spacing="20" styleClass="card">
    <!-- Título -->
    <Label styleClass="section-title" text="Crear Usuario" />
    
    <!-- Campo de entrada -->
    <VBox spacing="8">
        <Label text="Nombre de usuario" style="-fx-font-weight: 600;" />
        <TextField styleClass="text-field" promptText="Ingresa el usuario..." />
    </VBox>
    
    <!-- Campo de contraseña -->
    <VBox spacing="8">
        <Label text="Contraseña" style="-fx-font-weight: 600;" />
        <PasswordField styleClass="password-field" promptText="Ingresa la contraseña..." />
    </VBox>
    
    <!-- Botones de acción -->
    <HBox spacing="10" alignment="CENTER_RIGHT">
        <Button styleClass="button-cancel" text="Cancelar" />
        <Button styleClass="button-primary" text="Guardar" />
    </HBox>
</VBox>
```

### Panel de Búsqueda y Tabla

```xml
<VBox spacing="15" styleClass="card">
    <!-- Encabezado de búsqueda -->
    <HBox spacing="10" alignment="CENTER_LEFT">
        <TextField styleClass="search-field" promptText="Buscar..." HBox.hgrow="ALWAYS" />
        <Button styleClass="button-info" text="Buscar" />
        <Button styleClass="button-secondary" text="Refrescar" />
    </HBox>
    
    <!-- Tabla -->
    <TableView styleClass="table-view" VBox.vgrow="ALWAYS">
        <columns>
            <TableColumn text="Columna 1" prefWidth="150" />
            <TableColumn text="Columna 2" prefWidth="150" />
            <TableColumn text="Columna 3" prefWidth="150" />
        </columns>
    </TableView>
    
    <!-- Información de tabla -->
    <Label styleClass="total-label" text="Total: 25 registros" />
</VBox>
```

### Dashboard Cards Grid

```xml
<GridPane hgap="20" vgap="20">
    <columnConstraints>
        <ColumnConstraints hgrow="ALWAYS" percentWidth="33.33" />
        <ColumnConstraints hgrow="ALWAYS" percentWidth="33.33" />
        <ColumnConstraints hgrow="ALWAYS" percentWidth="33.33" />
    </columnConstraints>
    
    <!-- Card 1 -->
    <VBox styleClass="dashboard-card, admin-card-primary" spacing="15" GridPane.columnIndex="0" GridPane.rowIndex="0">
        <Label styleClass="admin-card-icon" text="👥" />
        <Label styleClass="card-title" text="Usuarios" />
        <Label styleClass="admin-card-value" text="42" />
    </VBox>
    
    <!-- Card 2 -->
    <VBox styleClass="dashboard-card, admin-card-success" spacing="15" GridPane.columnIndex="1" GridPane.rowIndex="0">
        <Label styleClass="admin-card-icon" text="🔐" />
        <Label styleClass="card-title" text="Roles" />
        <Label styleClass="admin-card-value" text="5" />
    </VBox>
    
    <!-- Card 3 -->
    <VBox styleClass="dashboard-card, admin-card-info" spacing="15" GridPane.columnIndex="2" GridPane.rowIndex="0">
        <Label styleClass="admin-card-icon" text="🌐" />
        <Label styleClass="card-title" text="Conexión" />
        <Label styleClass="admin-card-value" text="Activo" />
    </VBox>
</GridPane>
```

---

## 🎨 PALETA DE COLORES DE REFERENCIA

```
Primary Dark:      #1e3a5f  → Títulos, bordes activos
Primary Light:     #3498db  → Botones primarios, links
Secondary Dark:    #34495e  → Botones secundarios
Fondo Claro:       #f8f9fa  → Fondo general
Blanco:            #ffffff  → Tarjetas, campos
Gris Borde:        #ecf0f1  → Bordes suaves
Gris Texto:        #7f8c8d  → Texto secundario
Éxito:             #27ae60  → Confirmación
Advertencia:       #f39c12  → Atención
Peligro:           #e74c3c  → Error, eliminar
```

---

## 📏 ESPACIADO ESTÁNDAR

```
Extra pequeño:  5px
Pequeño:        8-10px
Normal:         12-15px
Mediano:        15-20px
Grande:         20-25px
Extra grande:   30px+
```

---

## ⚡ EFECTOS Y ANIMACIONES

### Sombra Normal
```xml
effect="dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1.5)"
```

### Sombra Elevada (Hover)
```xml
effect="dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2)"
```

### Sombra Fuerte (Cards)
```xml
effect="dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2)"
```

---

## 📋 CHECKLIST DE ESTILIZACIÓN

Al crear una nueva vista, asegúrate de:

- [ ] Usar `modern-ui.css` en stylesheets
- [ ] Aplicar `styleClass="module-header"` al encabezado
- [ ] Usar `styleClass="table-view"` para tablas
- [ ] Clasificar botones: primary, secondary, danger, warning
- [ ] Usar `styleClass="card"` para contenedores
- [ ] Implementar campos con `search-field` o `login-field`
- [ ] Añadir estilos a etiquetas cuando sea necesario
- [ ] Mantener espaciado consistente (spacing)
- [ ] No usar estilos inline (usar CSS)
- [ ] Verificar contraste y legibilidad

---

**Última actualización:** 9 de diciembre de 2025  
**Estado:** ✅ Completo y funcional
