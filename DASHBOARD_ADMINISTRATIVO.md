# Dashboard Administrativo - Módulo Desktop

## Descripción General

El Dashboard Administrativo es la interfaz central del módulo Desktop del Sistema de Gestión de Inventario. Está diseñado específicamente para funciones administrativas y de supervisión del sistema, eliminando las vistas operacionales de ventas, pedidos e inventario que pertenecen a otros módulos.

## Características Principales

### 1. Gestión de Usuarios y Roles

#### Gestión de Usuarios
- **Acceso**: Tarjeta "Usuarios Activos" o menú lateral "👥 Gestión de Usuarios"
- **Funcionalidades**:
  - Visualización de total de usuarios activos en tiempo real
  - Acceso directo al módulo completo de gestión de usuarios
  - CRUD completo de usuarios
  - Asignación de roles
  - Restablecimiento de contraseñas
  - Control de estados (activo/inactivo)

#### Gestión de Roles
- **Acceso**: Tarjeta "Roles Configurados" o menú lateral "🔐 Gestión de Roles"
- **Funcionalidades** (en desarrollo):
  - Visualización de roles configurados
  - Creación de nuevos roles
  - Asignación de permisos por módulo
  - Gestión de accesos del sistema

### 2. Sincronización con la Nube

#### Estado de Conexión
- **Indicador Visual**: Tarjeta "Conexión a Supabase"
- **Estados**:
  - 🟢 **Conectado**: Base de datos operativa
  - 🔴 **Desconectado**: Sin conexión a la base de datos
  - 🟠 **Error**: Error al verificar conexión

#### Sincronización de Datos
- **Acceso**:
  - Tarjeta "Última Sincronización" → Botón "Sincronizar Ahora"
  - Menú lateral "🔄 Sincronización"
  - Acciones Rápidas → "Sincronizar"

- **Funcionalidades**:
  - Sincronización bidireccional (Enviar/Recibir)
  - Registro de última sincronización
  - Historial de operaciones de sincronización
  - Contador de registros procesados
  - Notificaciones de resultado

### 3. Seguridad y Control de Accesos

#### Indicador de Rol Actual
- **Ubicación**: Sidebar, debajo del logo
- **Información mostrada**: Nombre del rol del usuario actual (ej: "ADMIN", "VENDEDOR", "USUARIO")

#### Panel de Usuario
- **Ubicación**: Header superior derecho
- **Información mostrada**:
  - Nombre de usuario actual
  - Última sesión/Primera sesión
  - Fecha y hora actual en tiempo real
  - Duración de sesión activa

#### Panel de Seguridad
- **Acceso**: Menú lateral "🛡️ Seguridad" (solo ADMIN)
- **Funcionalidades** (en desarrollo):
  - Auditoría de accesos
  - Registro de actividades
  - Gestión de sesiones activas
  - Políticas de contraseñas
  - Logs del sistema

### 4. Configuración Avanzada del Sistema

#### Acceso a Configuración
- **Ubicación**:
  - Tarjeta "Configuración"
  - Menú lateral "⚙️ Configuración"
  - Solo visible para usuarios con permisos de configuración

#### Parámetros Configurables
- Nombre y versión del sistema
- Parámetros de sincronización
- Timeout de sesión
- Configuración de impuestos
- Alertas de stock bajo
- Endpoints de API
- Y más...

## Diseño Visual

### Paleta de Colores
- **Primario**: Azul (#3498db, #2980b9)
- **Secundario**: Gris oscuro (#2c3e50, #34495e)
- **Éxito**: Verde (#27ae60, #229954)
- **Advertencia**: Naranja (#f39c12, #e67e22)
- **Peligro**: Rojo (#e74c3c, #c0392b)
- **Fondo**: Gris claro (#f8f9fa, #ecf0f1)

### Componentes del Layout

#### Sidebar (Izquierda)
- **Ancho**: 280px
- **Color**: Degradado azul-gris oscuro
- **Contenido**:
  - Logo del proyecto (180x180px)
  - Título "Panel Administrativo"
  - Rol del usuario
  - Menú de navegación con íconos
  - Botón de cerrar sesión

#### Header (Superior)
- **Contenido**:
  - Mensaje de bienvenida personalizado
  - Información del usuario actual
  - Última sesión
  - Fecha y hora en tiempo real

#### Área Central
- **Secciones**:
  1. **Estado del Sistema** (6 tarjetas en grid 3x2):
     - Usuarios Activos
     - Roles Configurados
     - Conexión a Supabase
     - Última Sincronización
     - Sesión Actual
     - Configuración

  2. **Acciones Rápidas** (4 tarjetas en grid horizontal):
     - Nuevo Usuario
     - Gestionar Roles
     - Sincronizar
     - Seguridad

  3. **Información del Sistema** (4 tarjetas horizontales):
     - Versión del Sistema
     - Base de Datos
     - Servidor de Aplicación
     - Entorno

### Efectos y Animaciones

#### Tarjetas Interactivas
- **Hover**:
  - Elevación de sombra
  - Cambio de color de fondo
  - Cambio de color de borde
  - Cursor pointer

#### Reloj en Tiempo Real
- Actualización cada segundo
- Formatos:
  - Hora: "hh:mm:ss a" (ej: 10:30:45 AM)
  - Fecha: "EEE, dd MMM yyyy" (ej: Lun, 29 Oct 2025)
  - Login: "dd/MM/yyyy hh:mm a" (ej: 29/10/2025 10:30 AM)

#### Temporizador de Sesión
- Actualización cada segundo
- Formato: HH:MM:SS (ej: 01:23:45)
- Cuenta desde el inicio de sesión

## Seguridad y Permisos

### Control de Acceso por Rol

El Dashboard implementa control de acceso granular basado en el sistema de permisos:

```
ADMIN:
✅ Gestión de Usuarios (read, create, update, delete)
✅ Gestión de Roles (read, create, update, delete)
✅ Sincronización (execute)
✅ Configuración (read, update)
✅ Seguridad (read)

VENDEDOR:
✅ Gestión de Usuarios (read)
❌ Gestión de Roles
✅ Sincronización (execute)
❌ Configuración
❌ Seguridad

USUARIO:
❌ Gestión de Usuarios
❌ Gestión de Roles
❌ Sincronización
❌ Configuración
❌ Seguridad
```

### Validaciones de Sesión

- **Timeout automático**: 30 minutos (configurable)
- **Validación continua**: Cada acción valida que la sesión esté activa
- **Cierre seguro**: Limpieza completa de datos de sesión
- **Confirmación de logout**: Alerta antes de cerrar sesión

## Integración con Servicios

### AuthenticationService
- Login y logout
- Validación de sesión activa
- Obtención de usuario y rol actual

### UsuarioService
- Estadísticas de usuarios
- Gestión completa de usuarios

### RolDAO
- Obtención de roles configurados
- Validación de permisos

### SyncService
- Sincronización bidireccional
- Historial de sincronizaciones
- Estado de última sincronización

### ConfiguracionService
- Lectura de parámetros del sistema
- Información de versión
- Configuración de aplicación

### SupabaseDatabaseConnection
- Verificación de conexión
- Estado de base de datos

## Navegación

### Desde Dashboard hacia:
- **Gestión de Usuarios**: `/com/app/view/UsuariosView.fxml`
- **Login** (al cerrar sesión): `/com/app/view/LoginView.fxml`

### Hacia Dashboard desde:
- **LoginView**: Después de autenticación exitosa

## Requisitos Técnicos

### Dependencias
- JavaFX 24
- Java 17+
- PostgreSQL JDBC Driver
- BCrypt para cifrado
- Gson para JSON
- Apache HttpClient5 para REST

### Conexión a Base de Datos
- Supabase PostgreSQL
- Variables de entorno:
  - `VITE_SUPABASE_URL`
  - `VITE_SUPABASE_SUPABASE_ANON_KEY`
  - `SUPABASE_DB_PASSWORD`

## Mejoras Futuras

### Módulos en Desarrollo
1. **Gestión de Roles Completa**: Interfaz para crear y modificar roles
2. **Panel de Seguridad**: Auditoría completa de accesos
3. **Configuración Avanzada**: UI para editar parámetros del sistema
4. **Dashboard Analytics**: Gráficos y estadísticas del sistema
5. **Notificaciones en Tiempo Real**: Alertas y notificaciones push
6. **Backup Automático**: Respaldo programado de datos
7. **Monitor de Rendimiento**: Métricas de performance del sistema

### Optimizaciones Planificadas
- Cache de estadísticas para mejorar rendimiento
- Lazy loading de componentes pesados
- Compresión de datos en sincronización
- Paginación de historial de sincronización
- Exportación de logs a CSV/PDF
- Temas personalizables (claro/oscuro)

## Resolución de Problemas

### Dashboard no carga datos
1. Verificar conexión a Supabase
2. Validar credenciales en `.env`
3. Revisar logs de consola
4. Confirmar que el usuario tiene sesión activa

### Botones deshabilitados
- **Causa**: Permisos insuficientes
- **Solución**: Verificar rol del usuario y permisos asignados

### Reloj no actualiza
- **Causa**: Timeline no inicializado o detenido
- **Solución**: Verificar que `startClockUpdates()` se ejecute

### Sincronización falla
- **Causa**: Endpoint no configurado o no disponible
- **Solución**: Configurar `sync.endpoint_url` en configuración del sistema

## Soporte y Contacto

Para reportar problemas o sugerencias sobre el Dashboard Administrativo, contactar al equipo de desarrollo.

---

**Versión**: 1.0.0
**Última actualización**: Octubre 2025
**Módulo**: Desktop Administrativo
