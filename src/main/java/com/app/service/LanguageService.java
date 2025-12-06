package com.app.service;

import java.util.*;

/**
 * Servicio para manejar idioma/localización de la aplicación.
 * Soporta: Español, English, Português, Français
 */
public class LanguageService {
    
    private static LanguageService instance;
    private String currentLanguage = "Español";
    private final Map<String, Map<String, String>> translations = new HashMap<>();
    private final List<LanguageChangeListener> listeners = new ArrayList<>();

    public interface LanguageChangeListener {
        void onLanguageChanged(String newLanguage);
    }

    private LanguageService() {
        initializeTranslations();
        // Cargar idioma guardado
        ConfiguracionService configService = ConfiguracionService.getInstance();
        this.currentLanguage = configService.getConfigValue("app.idioma", "Español");
    }

    public static LanguageService getInstance() {
        if (instance == null) {
            synchronized (LanguageService.class) {
                if (instance == null) {
                    instance = new LanguageService();
                }
            }
        }
        return instance;
    }

    private void initializeTranslations() {
        // Español
        Map<String, String> es = new HashMap<>();
        es.put("app.title", "Gestión de Inventario");
        es.put("app.name", "Gestión de Inventario");
        
        // Menu
        es.put("menu.usuarios", "Usuarios");
        es.put("menu.roles", "Roles");
        es.put("menu.seguridad", "Seguridad");
        es.put("menu.configuracion", "Configuración");
        es.put("menu.reportes", "Reportes");
        es.put("menu.pedidos", "Pedidos");
        es.put("menu.dashboard", "Dashboard");
        
        // Botones
        es.put("btn.nuevo", "Nuevo");
        es.put("btn.editar", "Editar");
        es.put("btn.eliminar", "Eliminar");
        es.put("btn.guardar", "Guardar");
        es.put("btn.cancelar", "Cancelar");
        es.put("btn.buscar", "Buscar");
        es.put("btn.refrescar", "Refrescar");
        es.put("btn.volver", "🏠 Regresar al Dashboard");
        es.put("btn.exportar", "Exportar a Excel");
        es.put("btn.importar", "Importar");
        es.put("btn.aceptar", "Aceptar");
        es.put("btn.rechazar", "Rechazar");
        es.put("btn.cerrar", "Cerrar");
        
        // Mensajes
        es.put("msg.exito", "Operación completada exitosamente");
        es.put("msg.error", "Error");
        es.put("msg.advertencia", "Advertencia");
        es.put("msg.confirmacion", "¿Está seguro?");
        es.put("msg.cargando", "Cargando...");
        es.put("msg.guardando", "Guardando...");
        es.put("msg.eliminando", "Eliminando...");
        es.put("msg.sin_datos", "Sin datos disponibles");
        
        // Login
        es.put("login.titulo", "Inicio de Sesión");
        es.put("login.usuario", "Usuario");
        es.put("login.password", "Contraseña");
        es.put("login.entrar", "Entrar");
        es.put("login.error", "Usuario o contraseña incorrectos");
        es.put("login.requerido", "Campo requerido");
        
        // Configuración
        es.put("config.tema", "Tema de la Interfaz");
        es.put("config.tema.claro", "Tema Claro");
        es.put("config.tema.oscuro", "Tema Oscuro");
        es.put("config.idioma", "Idioma");
        es.put("config.guardar", "Guardar Cambios");
        es.put("config.basedatos", "Base de Datos");
        es.put("config.conexion", "Conexión");
        es.put("config.backup", "Respaldo");
        es.put("config.seguridad", "Seguridad");
        es.put("config.backup.manual", "Respaldo Manual");
        es.put("config.backup.automatico", "Respaldo Automático");
        es.put("config.optimize", "Optimizar Base de Datos");
        es.put("config.clean_cache", "Limpiar Caché");
        es.put("config.dos", "DOS (Sistema Operativo)");
        
        // Dashboard
        es.put("dashboard.titulo", "Panel de Control");
        es.put("dashboard.bienvenido", "Bienvenido");
        es.put("dashboard.rol", "Rol");
        es.put("dashboard.usuarios_totales", "Usuarios Totales");
        es.put("dashboard.roles_totales", "Roles Totales");
        es.put("dashboard.eventos_recientes", "Eventos Recientes");
        es.put("dashboard.sincronizacion", "Sincronización en la Nube");
        es.put("dashboard.iniciar_sincro", "Iniciar Sincronización");
        es.put("dashboard.sincronizando", "Sincronizando...");
        es.put("dashboard.ultima_sincro", "Última Sincronización");
        es.put("dashboard.estado", "Estado");
        es.put("dashboard.gestion_usuarios", "Gestión de Usuarios");
        es.put("dashboard.gestion_roles", "Gestión de Roles");
        es.put("dashboard.gestion_seguridad", "Gestión de Seguridad");
        es.put("dashboard.acceso_denegado", "Acceso denegado a esta funcionalidad");
        
        // Usuarios
        es.put("usuarios.titulo", "Gestión de Usuarios");
        es.put("usuarios.nombre", "Nombre");
        es.put("usuarios.usuario", "Usuario");
        es.put("usuarios.email", "Correo Electrónico");
        es.put("usuarios.rol", "Rol");
        es.put("usuarios.estado", "Estado");
        es.put("usuarios.activo", "Activo");
        es.put("usuarios.inactivo", "Inactivo");
        es.put("usuarios.fecha_creacion", "Fecha de Creación");
        es.put("usuarios.nuevo_usuario", "Nuevo Usuario");
        es.put("usuarios.editar_usuario", "Editar Usuario");
        es.put("usuarios.eliminar_usuario", "Eliminar Usuario");
        es.put("usuarios.confirmar_eliminar", "¿Seguro que desea eliminar este usuario?");
        es.put("usuarios.usuario_creado", "Usuario creado exitosamente");
        es.put("usuarios.usuario_actualizado", "Usuario actualizado exitosamente");
        es.put("usuarios.usuario_eliminado", "Usuario eliminado exitosamente");
        es.put("usuarios.error_crear", "Error al crear el usuario");
        es.put("usuarios.error_actualizar", "Error al actualizar el usuario");
        es.put("usuarios.error_eliminar", "Error al eliminar el usuario");
        es.put("usuarios.password_temporal", "Contraseña temporal");
        es.put("usuarios.contrasena", "Contraseña");
        es.put("usuarios.confirmar_contrasena", "Confirmar Contraseña");
        es.put("usuarios.contrasenas_no_coinciden", "Las contraseñas no coinciden");
        
        // Roles
        es.put("roles.titulo", "Gestión de Roles");
        es.put("roles.nombre", "Nombre");
        es.put("roles.descripcion", "Descripción");
        es.put("roles.permisos", "Permisos");
        es.put("roles.nivel_acceso", "Nivel de Acceso");
        es.put("roles.nuevo_rol", "Nuevo Rol");
        es.put("roles.editar_rol", "Editar Rol");
        es.put("roles.eliminar_rol", "Eliminar Rol");
        es.put("roles.confirmar_eliminar", "¿Seguro que desea eliminar este rol?");
        es.put("roles.rol_creado", "Rol creado exitosamente");
        es.put("roles.rol_actualizado", "Rol actualizado exitosamente");
        es.put("roles.rol_eliminado", "Rol eliminado exitosamente");
        es.put("roles.error_crear", "Error al crear el rol");
        es.put("roles.error_actualizar", "Error al actualizar el rol");
        es.put("roles.error_eliminar", "Error al eliminar el rol");
        es.put("roles.admin", "Administrador");
        es.put("roles.usuario", "Usuario");
        es.put("roles.invitado", "Invitado");
        
        // Seguridad
        es.put("seguridad.titulo", "Panel de Seguridad");
        es.put("seguridad.eventos_auditoria", "Eventos de Auditoría");
        es.put("seguridad.tipo_evento", "Tipo de Evento");
        es.put("seguridad.usuario", "Usuario");
        es.put("seguridad.fecha", "Fecha");
        es.put("seguridad.detalles", "Detalles");
        es.put("seguridad.login", "Inicio de Sesión");
        es.put("seguridad.logout", "Cierre de Sesión");
        es.put("seguridad.crear", "Crear");
        es.put("seguridad.actualizar", "Actualizar");
        es.put("seguridad.eliminar", "Eliminar");
        es.put("seguridad.acceso_denegado", "Acceso Denegado");
        es.put("seguridad.exportar_eventos", "Exportar Eventos");
        es.put("seguridad.autenticacion_dos_factores", "Autenticación de Dos Factores");
        es.put("seguridad.configurar_2fa", "Configurar 2FA");
        es.put("seguridad.habilitar_2fa", "Habilitar 2FA");
        es.put("seguridad.deshabilitar_2fa", "Deshabilitar 2FA");
        es.put("seguridad.codigo_2fa", "Código 2FA");
        es.put("seguridad.bloqueo_sesion", "Bloqueo de Sesión");
        es.put("seguridad.intentos_fallidos", "Intentos Fallidos");
        es.put("seguridad.sesion_bloqueada", "Sesión Bloqueada");
        es.put("seguridad.cambiar_contrasena", "Cambiar Contraseña");
        
        // Validación
        es.put("validation.requerido", "Este campo es requerido");
        es.put("validation.email_invalido", "Correo electrónico inválido");
        es.put("validation.usuario_existe", "El usuario ya existe");
        es.put("validation.usuario_no_existe", "El usuario no existe");
        es.put("validation.contrasena_corta", "La contraseña debe tener al menos 8 caracteres");
        es.put("validation.contrasena_debil", "La contraseña es muy débil");
        
        // Confirmación
        es.put("confirm.eliminar_usuario", "¿Desea eliminar este usuario? Esta acción no se puede deshacer.");
        es.put("confirm.eliminar_rol", "¿Desea eliminar este rol? Esta acción no se puede deshacer.");
        es.put("confirm.cambiar_idioma", "El idioma cambiará inmediatamente");
        es.put("confirm.cambiar_tema", "El tema cambiará inmediatamente");
        
        // Acceso
        es.put("access.denied.title", "Acceso denegado");
        es.put("access.denied.tooltip", "Acceso restringido. Contacte al administrador.");
        es.put("access.denied.admin.only", "Esta funcionalidad solo está disponible para administradores");
        es.put("access.denied.permission", "No tiene permisos para realizar esta acción");
        
        // Reportes
        es.put("reportes.titulo", "Reportes");
        es.put("reportes.generar", "Generar Reporte");
        es.put("reportes.descargar", "Descargar");
        es.put("reportes.usuarios", "Reporte de Usuarios");
        es.put("reportes.actividad", "Reporte de Actividad");
        
        translations.put("Español", es);

        // English
        Map<String, String> en = new HashMap<>();
        en.put("app.title", "Inventory Management");
        en.put("app.name", "Inventory Management");
        
        // Menu
        en.put("menu.usuarios", "Users");
        en.put("menu.roles", "Roles");
        en.put("menu.seguridad", "Security");
        en.put("menu.configuracion", "Settings");
        en.put("menu.reportes", "Reports");
        en.put("menu.pedidos", "Orders");
        en.put("menu.dashboard", "Dashboard");
        
        // Buttons
        en.put("btn.nuevo", "New");
        en.put("btn.editar", "Edit");
        en.put("btn.eliminar", "Delete");
        en.put("btn.guardar", "Save");
        en.put("btn.cancelar", "Cancel");
        en.put("btn.buscar", "Search");
        en.put("btn.refrescar", "Refresh");
        en.put("btn.volver", "🏠 Back to Dashboard");
        en.put("btn.exportar", "Export to Excel");
        en.put("btn.importar", "Import");
        en.put("btn.aceptar", "Accept");
        en.put("btn.rechazar", "Reject");
        en.put("btn.cerrar", "Close");
        
        // Messages
        en.put("msg.exito", "Operation completed successfully");
        en.put("msg.error", "Error");
        en.put("msg.advertencia", "Warning");
        en.put("msg.confirmacion", "Are you sure?");
        en.put("msg.cargando", "Loading...");
        en.put("msg.guardando", "Saving...");
        en.put("msg.eliminando", "Deleting...");
        en.put("msg.sin_datos", "No data available");
        
        // Login
        en.put("login.titulo", "Login");
        en.put("login.usuario", "Username");
        en.put("login.password", "Password");
        en.put("login.entrar", "Login");
        en.put("login.error", "Invalid username or password");
        en.put("login.requerido", "This field is required");
        
        // Settings
        en.put("config.tema", "Theme");
        en.put("config.tema.claro", "Light Theme");
        en.put("config.tema.oscuro", "Dark Theme");
        en.put("config.idioma", "Language");
        en.put("config.guardar", "Save Changes");
        en.put("config.basedatos", "Database");
        en.put("config.conexion", "Connection");
        en.put("config.backup", "Backup");
        en.put("config.seguridad", "Security");
        en.put("config.backup.manual", "Manual Backup");
        en.put("config.backup.automatico", "Automatic Backup");
        en.put("config.optimize", "Optimize Database");
        en.put("config.clean_cache", "Clean Cache");
        en.put("config.dos", "DOS (Operating System)");
        
        // Dashboard
        en.put("dashboard.titulo", "Control Panel");
        en.put("dashboard.bienvenido", "Welcome");
        en.put("dashboard.rol", "Role");
        en.put("dashboard.usuarios_totales", "Total Users");
        en.put("dashboard.roles_totales", "Total Roles");
        en.put("dashboard.eventos_recientes", "Recent Events");
        en.put("dashboard.sincronizacion", "Cloud Synchronization");
        en.put("dashboard.iniciar_sincro", "Start Synchronization");
        en.put("dashboard.sincronizando", "Synchronizing...");
        en.put("dashboard.ultima_sincro", "Last Synchronization");
        en.put("dashboard.estado", "Status");
        en.put("dashboard.gestion_usuarios", "User Management");
        en.put("dashboard.gestion_roles", "Role Management");
        en.put("dashboard.gestion_seguridad", "Security Management");
        en.put("dashboard.acceso_denegado", "Access denied to this feature");
        
        // Users
        en.put("usuarios.titulo", "User Management");
        en.put("usuarios.nombre", "Name");
        en.put("usuarios.usuario", "Username");
        en.put("usuarios.email", "Email");
        en.put("usuarios.rol", "Role");
        en.put("usuarios.estado", "Status");
        en.put("usuarios.activo", "Active");
        en.put("usuarios.inactivo", "Inactive");
        en.put("usuarios.fecha_creacion", "Creation Date");
        en.put("usuarios.nuevo_usuario", "New User");
        en.put("usuarios.editar_usuario", "Edit User");
        en.put("usuarios.eliminar_usuario", "Delete User");
        en.put("usuarios.confirmar_eliminar", "Are you sure you want to delete this user?");
        en.put("usuarios.usuario_creado", "User created successfully");
        en.put("usuarios.usuario_actualizado", "User updated successfully");
        en.put("usuarios.usuario_eliminado", "User deleted successfully");
        en.put("usuarios.error_crear", "Error creating user");
        en.put("usuarios.error_actualizar", "Error updating user");
        en.put("usuarios.error_eliminar", "Error deleting user");
        en.put("usuarios.password_temporal", "Temporary password");
        en.put("usuarios.contrasena", "Password");
        en.put("usuarios.confirmar_contrasena", "Confirm Password");
        en.put("usuarios.contrasenas_no_coinciden", "Passwords do not match");
        
        // Roles
        en.put("roles.titulo", "Role Management");
        en.put("roles.nombre", "Name");
        en.put("roles.descripcion", "Description");
        en.put("roles.permisos", "Permissions");
        en.put("roles.nivel_acceso", "Access Level");
        en.put("roles.nuevo_rol", "New Role");
        en.put("roles.editar_rol", "Edit Role");
        en.put("roles.eliminar_rol", "Delete Role");
        en.put("roles.confirmar_eliminar", "Are you sure you want to delete this role?");
        en.put("roles.rol_creado", "Role created successfully");
        en.put("roles.rol_actualizado", "Role updated successfully");
        en.put("roles.rol_eliminado", "Role deleted successfully");
        en.put("roles.error_crear", "Error creating role");
        en.put("roles.error_actualizar", "Error updating role");
        en.put("roles.error_eliminar", "Error deleting role");
        en.put("roles.admin", "Administrator");
        en.put("roles.usuario", "User");
        en.put("roles.invitado", "Guest");
        
        // Security
        en.put("seguridad.titulo", "Security Panel");
        en.put("seguridad.eventos_auditoria", "Audit Events");
        en.put("seguridad.tipo_evento", "Event Type");
        en.put("seguridad.usuario", "User");
        en.put("seguridad.fecha", "Date");
        en.put("seguridad.detalles", "Details");
        en.put("seguridad.login", "Login");
        en.put("seguridad.logout", "Logout");
        en.put("seguridad.crear", "Create");
        en.put("seguridad.actualizar", "Update");
        en.put("seguridad.eliminar", "Delete");
        en.put("seguridad.acceso_denegado", "Access Denied");
        en.put("seguridad.exportar_eventos", "Export Events");
        en.put("seguridad.autenticacion_dos_factores", "Two-Factor Authentication");
        en.put("seguridad.configurar_2fa", "Configure 2FA");
        en.put("seguridad.habilitar_2fa", "Enable 2FA");
        en.put("seguridad.deshabilitar_2fa", "Disable 2FA");
        en.put("seguridad.codigo_2fa", "2FA Code");
        en.put("seguridad.bloqueo_sesion", "Session Lock");
        en.put("seguridad.intentos_fallidos", "Failed Attempts");
        en.put("seguridad.sesion_bloqueada", "Session Blocked");
        en.put("seguridad.cambiar_contrasena", "Change Password");
        
        // Validation
        en.put("validation.requerido", "This field is required");
        en.put("validation.email_invalido", "Invalid email");
        en.put("validation.usuario_existe", "User already exists");
        en.put("validation.usuario_no_existe", "User does not exist");
        en.put("validation.contrasena_corta", "Password must be at least 8 characters long");
        en.put("validation.contrasena_debil", "Password is too weak");
        
        // Confirmation
        en.put("confirm.eliminar_usuario", "Do you want to delete this user? This action cannot be undone.");
        en.put("confirm.eliminar_rol", "Do you want to delete this role? This action cannot be undone.");
        en.put("confirm.cambiar_idioma", "Language will change immediately");
        en.put("confirm.cambiar_tema", "Theme will change immediately");
        
        // Access
        en.put("access.denied.title", "Access denied");
        en.put("access.denied.tooltip", "Restricted access. Contact your administrator.");
        en.put("access.denied.admin.only", "This feature is only available for administrators");
        en.put("access.denied.permission", "You do not have permission to perform this action");
        
        // Reports
        en.put("reportes.titulo", "Reports");
        en.put("reportes.generar", "Generate Report");
        en.put("reportes.descargar", "Download");
        en.put("reportes.usuarios", "Users Report");
        en.put("reportes.actividad", "Activity Report");
        
        translations.put("English", en);

        // Português
        Map<String, String> pt = new HashMap<>();
        pt.put("app.title", "Gestão de Inventário");
        pt.put("app.name", "Gestão de Inventário");
        
        // Menu
        pt.put("menu.usuarios", "Usuários");
        pt.put("menu.roles", "Funções");
        pt.put("menu.seguridad", "Segurança");
        pt.put("menu.configuracion", "Configurações");
        pt.put("menu.reportes", "Relatórios");
        pt.put("menu.pedidos", "Pedidos");
        pt.put("menu.dashboard", "Painel");
        
        // Botões
        pt.put("btn.nuevo", "Novo");
        pt.put("btn.editar", "Editar");
        pt.put("btn.eliminar", "Excluir");
        pt.put("btn.guardar", "Salvar");
        pt.put("btn.cancelar", "Cancelar");
        pt.put("btn.buscar", "Pesquisar");
        pt.put("btn.refrescar", "Atualizar");
        pt.put("btn.volver", "🏠 Voltar ao Painel");
        pt.put("btn.exportar", "Exportar para Excel");
        pt.put("btn.importar", "Importar");
        pt.put("btn.aceptar", "Aceitar");
        pt.put("btn.rechazar", "Rejeitar");
        pt.put("btn.cerrar", "Fechar");
        
        // Mensagens
        pt.put("msg.exito", "Operação concluída com sucesso");
        pt.put("msg.error", "Erro");
        pt.put("msg.advertencia", "Aviso");
        pt.put("msg.confirmacion", "Você tem certeza?");
        pt.put("msg.cargando", "Carregando...");
        pt.put("msg.guardando", "Salvando...");
        pt.put("msg.eliminando", "Excluindo...");
        pt.put("msg.sin_datos", "Sem dados disponíveis");
        
        // Login
        pt.put("login.titulo", "Login");
        pt.put("login.usuario", "Usuário");
        pt.put("login.password", "Senha");
        pt.put("login.entrar", "Entrar");
        pt.put("login.error", "Usuário ou senha inválidos");
        pt.put("login.requerido", "Este campo é obrigatório");
        
        // Configuração
        pt.put("config.tema", "Tema");
        pt.put("config.tema.claro", "Tema Claro");
        pt.put("config.tema.oscuro", "Tema Escuro");
        pt.put("config.idioma", "Idioma");
        pt.put("config.guardar", "Salvar Alterações");
        pt.put("config.basedatos", "Banco de Dados");
        pt.put("config.conexion", "Conexão");
        pt.put("config.backup", "Backup");
        pt.put("config.seguridad", "Segurança");
        pt.put("config.backup.manual", "Backup Manual");
        pt.put("config.backup.automatico", "Backup Automático");
        pt.put("config.optimize", "Otimizar Banco de Dados");
        pt.put("config.clean_cache", "Limpar Cache");
        pt.put("config.dos", "DOS (Sistema Operacional)");
        
        // Dashboard
        pt.put("dashboard.titulo", "Painel de Controle");
        pt.put("dashboard.bienvenido", "Bem-vindo");
        pt.put("dashboard.rol", "Função");
        pt.put("dashboard.usuarios_totales", "Total de Usuários");
        pt.put("dashboard.roles_totales", "Total de Funções");
        pt.put("dashboard.eventos_recientes", "Eventos Recentes");
        pt.put("dashboard.sincronizacion", "Sincronização em Nuvem");
        pt.put("dashboard.iniciar_sincro", "Iniciar Sincronização");
        pt.put("dashboard.sincronizando", "Sincronizando...");
        pt.put("dashboard.ultima_sincro", "Última Sincronização");
        pt.put("dashboard.estado", "Status");
        pt.put("dashboard.gestion_usuarios", "Gerenciamento de Usuários");
        pt.put("dashboard.gestion_roles", "Gerenciamento de Funções");
        pt.put("dashboard.gestion_seguridad", "Gerenciamento de Segurança");
        pt.put("dashboard.acceso_denegado", "Acesso negado a este recurso");
        
        // Usuários
        pt.put("usuarios.titulo", "Gerenciamento de Usuários");
        pt.put("usuarios.nombre", "Nome");
        pt.put("usuarios.usuario", "Usuário");
        pt.put("usuarios.email", "Email");
        pt.put("usuarios.rol", "Função");
        pt.put("usuarios.estado", "Status");
        pt.put("usuarios.activo", "Ativo");
        pt.put("usuarios.inactivo", "Inativo");
        pt.put("usuarios.fecha_creacion", "Data de Criação");
        pt.put("usuarios.nuevo_usuario", "Novo Usuário");
        pt.put("usuarios.editar_usuario", "Editar Usuário");
        pt.put("usuarios.eliminar_usuario", "Excluir Usuário");
        pt.put("usuarios.confirmar_eliminar", "Tem certeza que deseja excluir este usuário?");
        pt.put("usuarios.usuario_creado", "Usuário criado com sucesso");
        pt.put("usuarios.usuario_actualizado", "Usuário atualizado com sucesso");
        pt.put("usuarios.usuario_eliminado", "Usuário excluído com sucesso");
        pt.put("usuarios.error_crear", "Erro ao criar usuário");
        pt.put("usuarios.error_actualizar", "Erro ao atualizar usuário");
        pt.put("usuarios.error_eliminar", "Erro ao excluir usuário");
        pt.put("usuarios.password_temporal", "Senha temporária");
        pt.put("usuarios.contrasena", "Senha");
        pt.put("usuarios.confirmar_contrasena", "Confirmar Senha");
        pt.put("usuarios.contrasenas_no_coinciden", "As senhas não correspondem");
        
        // Funções
        pt.put("roles.titulo", "Gerenciamento de Funções");
        pt.put("roles.nombre", "Nome");
        pt.put("roles.descripcion", "Descrição");
        pt.put("roles.permisos", "Permissões");
        pt.put("roles.nivel_acceso", "Nível de Acesso");
        pt.put("roles.nuevo_rol", "Nova Função");
        pt.put("roles.editar_rol", "Editar Função");
        pt.put("roles.eliminar_rol", "Excluir Função");
        pt.put("roles.confirmar_eliminar", "Tem certeza que deseja excluir esta função?");
        pt.put("roles.rol_creado", "Função criada com sucesso");
        pt.put("roles.rol_actualizado", "Função atualizada com sucesso");
        pt.put("roles.rol_eliminado", "Função excluída com sucesso");
        pt.put("roles.error_crear", "Erro ao criar função");
        pt.put("roles.error_actualizar", "Erro ao atualizar função");
        pt.put("roles.error_eliminar", "Erro ao excluir função");
        pt.put("roles.admin", "Administrador");
        pt.put("roles.usuario", "Usuário");
        pt.put("roles.invitado", "Convidado");
        
        // Segurança
        pt.put("seguridad.titulo", "Painel de Segurança");
        pt.put("seguridad.eventos_auditoria", "Eventos de Auditoria");
        pt.put("seguridad.tipo_evento", "Tipo de Evento");
        pt.put("seguridad.usuario", "Usuário");
        pt.put("seguridad.fecha", "Data");
        pt.put("seguridad.detalles", "Detalhes");
        pt.put("seguridad.login", "Login");
        pt.put("seguridad.logout", "Logout");
        pt.put("seguridad.crear", "Criar");
        pt.put("seguridad.actualizar", "Atualizar");
        pt.put("seguridad.eliminar", "Excluir");
        pt.put("seguridad.acceso_denegado", "Acesso Negado");
        pt.put("seguridad.exportar_eventos", "Exportar Eventos");
        pt.put("seguridad.autenticacion_dos_factores", "Autenticação de Dois Fatores");
        pt.put("seguridad.configurar_2fa", "Configurar 2FA");
        pt.put("seguridad.habilitar_2fa", "Habilitar 2FA");
        pt.put("seguridad.deshabilitar_2fa", "Desabilitar 2FA");
        pt.put("seguridad.codigo_2fa", "Código 2FA");
        pt.put("seguridad.bloqueo_sesion", "Bloqueio de Sessão");
        pt.put("seguridad.intentos_fallidos", "Tentativas Falhadas");
        pt.put("seguridad.sesion_bloqueada", "Sessão Bloqueada");
        pt.put("seguridad.cambiar_contrasena", "Alterar Senha");
        
        // Validação
        pt.put("validation.requerido", "Este campo é obrigatório");
        pt.put("validation.email_invalido", "Email inválido");
        pt.put("validation.usuario_existe", "Usuário já existe");
        pt.put("validation.usuario_no_existe", "Usuário não existe");
        pt.put("validation.contrasena_corta", "A senha deve ter pelo menos 8 caracteres");
        pt.put("validation.contrasena_debil", "A senha é muito fraca");
        
        // Confirmação
        pt.put("confirm.eliminar_usuario", "Deseja excluir este usuário? Esta ação não pode ser desfeita.");
        pt.put("confirm.eliminar_rol", "Deseja excluir esta função? Esta ação não pode ser desfeita.");
        pt.put("confirm.cambiar_idioma", "O idioma será alterado imediatamente");
        pt.put("confirm.cambiar_tema", "O tema será alterado imediatamente");
        
        // Acesso
        pt.put("access.denied.title", "Acesso negado");
        pt.put("access.denied.tooltip", "Acesso restrito. Contate o administrador.");
        pt.put("access.denied.admin.only", "Este recurso está disponível apenas para administradores");
        pt.put("access.denied.permission", "Você não tem permissão para realizar esta ação");
        
        // Relatórios
        pt.put("reportes.titulo", "Relatórios");
        pt.put("reportes.generar", "Gerar Relatório");
        pt.put("reportes.descargar", "Baixar");
        pt.put("reportes.usuarios", "Relatório de Usuários");
        pt.put("reportes.actividad", "Relatório de Atividades");
        
        translations.put("Português", pt);

        // Français
        Map<String, String> fr = new HashMap<>();
        fr.put("app.title", "Gestion des Inventaires");
        fr.put("app.name", "Gestion des Inventaires");
        
        // Menu
        fr.put("menu.usuarios", "Utilisateurs");
        fr.put("menu.roles", "Rôles");
        fr.put("menu.seguridad", "Sécurité");
        fr.put("menu.configuracion", "Paramètres");
        fr.put("menu.reportes", "Rapports");
        fr.put("menu.pedidos", "Commandes");
        fr.put("menu.dashboard", "Tableau de bord");
        
        // Boutons
        fr.put("btn.nuevo", "Nouveau");
        fr.put("btn.editar", "Modifier");
        fr.put("btn.eliminar", "Supprimer");
        fr.put("btn.guardar", "Enregistrer");
        fr.put("btn.cancelar", "Annuler");
        fr.put("btn.buscar", "Rechercher");
        fr.put("btn.refrescar", "Actualiser");
        fr.put("btn.volver", "🏠 Retour au Tableau de bord");
        fr.put("btn.exportar", "Exporter vers Excel");
        fr.put("btn.importar", "Importer");
        fr.put("btn.aceptar", "Accepter");
        fr.put("btn.rechazar", "Rejeter");
        fr.put("btn.cerrar", "Fermer");
        
        // Messages
        fr.put("msg.exito", "Opération complétée avec succès");
        fr.put("msg.error", "Erreur");
        fr.put("msg.advertencia", "Avertissement");
        fr.put("msg.confirmacion", "Êtes-vous sûr?");
        fr.put("msg.cargando", "Chargement...");
        fr.put("msg.guardando", "Enregistrement...");
        fr.put("msg.eliminando", "Suppression...");
        fr.put("msg.sin_datos", "Aucune donnée disponible");
        
        // Login
        fr.put("login.titulo", "Connexion");
        fr.put("login.usuario", "Nom d'utilisateur");
        fr.put("login.password", "Mot de passe");
        fr.put("login.entrar", "Connexion");
        fr.put("login.error", "Nom d'utilisateur ou mot de passe invalide");
        fr.put("login.requerido", "Ce champ est obligatoire");
        
        // Paramètres
        fr.put("config.tema", "Thème");
        fr.put("config.tema.claro", "Thème Clair");
        fr.put("config.tema.oscuro", "Thème Sombre");
        fr.put("config.idioma", "Langue");
        fr.put("config.guardar", "Enregistrer les modifications");
        fr.put("config.basedatos", "Base de Données");
        fr.put("config.conexion", "Connexion");
        fr.put("config.backup", "Sauvegarde");
        fr.put("config.seguridad", "Sécurité");
        fr.put("config.backup.manual", "Sauvegarde Manuelle");
        fr.put("config.backup.automatico", "Sauvegarde Automatique");
        fr.put("config.optimize", "Optimiser la Base de Données");
        fr.put("config.clean_cache", "Nettoyer le Cache");
        fr.put("config.dos", "DOS (Système d'Exploitation)");
        
        // Tableau de bord
        fr.put("dashboard.titulo", "Panneau de Contrôle");
        fr.put("dashboard.bienvenido", "Bienvenue");
        fr.put("dashboard.rol", "Rôle");
        fr.put("dashboard.usuarios_totales", "Nombre Total d'Utilisateurs");
        fr.put("dashboard.roles_totales", "Nombre Total de Rôles");
        fr.put("dashboard.eventos_recientes", "Événements Récents");
        fr.put("dashboard.sincronizacion", "Synchronisation Cloud");
        fr.put("dashboard.iniciar_sincro", "Démarrer la Synchronisation");
        fr.put("dashboard.sincronizando", "Synchronisation en cours...");
        fr.put("dashboard.ultima_sincro", "Dernière Synchronisation");
        fr.put("dashboard.estado", "Statut");
        fr.put("dashboard.gestion_usuarios", "Gestion des Utilisateurs");
        fr.put("dashboard.gestion_roles", "Gestion des Rôles");
        fr.put("dashboard.gestion_seguridad", "Gestion de la Sécurité");
        fr.put("dashboard.acceso_denegado", "Accès refusé à cette fonctionnalité");
        
        // Utilisateurs
        fr.put("usuarios.titulo", "Gestion des Utilisateurs");
        fr.put("usuarios.nombre", "Nom");
        fr.put("usuarios.usuario", "Utilisateur");
        fr.put("usuarios.email", "Email");
        fr.put("usuarios.rol", "Rôle");
        fr.put("usuarios.estado", "Statut");
        fr.put("usuarios.activo", "Actif");
        fr.put("usuarios.inactivo", "Inactif");
        fr.put("usuarios.fecha_creacion", "Date de Création");
        fr.put("usuarios.nuevo_usuario", "Nouvel Utilisateur");
        fr.put("usuarios.editar_usuario", "Modifier l'Utilisateur");
        fr.put("usuarios.eliminar_usuario", "Supprimer l'Utilisateur");
        fr.put("usuarios.confirmar_eliminar", "Êtes-vous sûr que vous souhaitez supprimer cet utilisateur?");
        fr.put("usuarios.usuario_creado", "Utilisateur créé avec succès");
        fr.put("usuarios.usuario_actualizado", "Utilisateur mis à jour avec succès");
        fr.put("usuarios.usuario_eliminado", "Utilisateur supprimé avec succès");
        fr.put("usuarios.error_crear", "Erreur lors de la création de l'utilisateur");
        fr.put("usuarios.error_actualizar", "Erreur lors de la mise à jour de l'utilisateur");
        fr.put("usuarios.error_eliminar", "Erreur lors de la suppression de l'utilisateur");
        fr.put("usuarios.password_temporal", "Mot de passe temporaire");
        fr.put("usuarios.contrasena", "Mot de passe");
        fr.put("usuarios.confirmar_contrasena", "Confirmer le Mot de passe");
        fr.put("usuarios.contrasenas_no_coinciden", "Les mots de passe ne correspondent pas");
        
        // Rôles
        fr.put("roles.titulo", "Gestion des Rôles");
        fr.put("roles.nombre", "Nom");
        fr.put("roles.descripcion", "Description");
        fr.put("roles.permisos", "Permissions");
        fr.put("roles.nivel_acceso", "Niveau d'Accès");
        fr.put("roles.nuevo_rol", "Nouveau Rôle");
        fr.put("roles.editar_rol", "Modifier le Rôle");
        fr.put("roles.eliminar_rol", "Supprimer le Rôle");
        fr.put("roles.confirmar_eliminar", "Êtes-vous sûr que vous souhaitez supprimer ce rôle?");
        fr.put("roles.rol_creado", "Rôle créé avec succès");
        fr.put("roles.rol_actualizado", "Rôle mis à jour avec succès");
        fr.put("roles.rol_eliminado", "Rôle supprimé avec succès");
        fr.put("roles.error_crear", "Erreur lors de la création du rôle");
        fr.put("roles.error_actualizar", "Erreur lors de la mise à jour du rôle");
        fr.put("roles.error_eliminar", "Erreur lors de la suppression du rôle");
        fr.put("roles.admin", "Administrateur");
        fr.put("roles.usuario", "Utilisateur");
        fr.put("roles.invitado", "Invité");
        
        // Sécurité
        fr.put("seguridad.titulo", "Panneau de Sécurité");
        fr.put("seguridad.eventos_auditoria", "Événements d'Audit");
        fr.put("seguridad.tipo_evento", "Type d'Événement");
        fr.put("seguridad.usuario", "Utilisateur");
        fr.put("seguridad.fecha", "Date");
        fr.put("seguridad.detalles", "Détails");
        fr.put("seguridad.login", "Connexion");
        fr.put("seguridad.logout", "Déconnexion");
        fr.put("seguridad.crear", "Créer");
        fr.put("seguridad.actualizar", "Mettre à jour");
        fr.put("seguridad.eliminar", "Supprimer");
        fr.put("seguridad.acceso_denegado", "Accès Refusé");
        fr.put("seguridad.exportar_eventos", "Exporter les Événements");
        fr.put("seguridad.autenticacion_dos_factores", "Authentification à Deux Facteurs");
        fr.put("seguridad.configurar_2fa", "Configurer 2FA");
        fr.put("seguridad.habilitar_2fa", "Activer 2FA");
        fr.put("seguridad.deshabilitar_2fa", "Désactiver 2FA");
        fr.put("seguridad.codigo_2fa", "Code 2FA");
        fr.put("seguridad.bloqueo_sesion", "Verrouillage de Session");
        fr.put("seguridad.intentos_fallidos", "Tentatives Échouées");
        fr.put("seguridad.sesion_bloqueada", "Session Verrouillée");
        fr.put("seguridad.cambiar_contrasena", "Changer le Mot de passe");
        
        // Validation
        fr.put("validation.requerido", "Ce champ est obligatoire");
        fr.put("validation.email_invalido", "Email invalide");
        fr.put("validation.usuario_existe", "L'utilisateur existe déjà");
        fr.put("validation.usuario_no_existe", "L'utilisateur n'existe pas");
        fr.put("validation.contrasena_corta", "Le mot de passe doit contenir au moins 8 caractères");
        fr.put("validation.contrasena_debil", "Le mot de passe est trop faible");
        
        // Confirmation
        fr.put("confirm.eliminar_usuario", "Voulez-vous supprimer cet utilisateur? Cette action ne peut pas être annulée.");
        fr.put("confirm.eliminar_rol", "Voulez-vous supprimer ce rôle? Cette action ne peut pas être annulée.");
        fr.put("confirm.cambiar_idioma", "La langue changera immédiatement");
        fr.put("confirm.cambiar_tema", "Le thème changera immédiatement");
        
        // Accès
        fr.put("access.denied.title", "Accès refusé");
        fr.put("access.denied.tooltip", "Accès restreint. Contactez l'administrateur.");
        fr.put("access.denied.admin.only", "Cette fonctionnalité n'est disponible que pour les administrateurs");
        fr.put("access.denied.permission", "Vous n'avez pas la permission d'effectuer cette action");
        
        // Rapports
        fr.put("reportes.titulo", "Rapports");
        fr.put("reportes.generar", "Générer un Rapport");
        fr.put("reportes.descargar", "Télécharger");
        fr.put("reportes.usuarios", "Rapport des Utilisateurs");
        fr.put("reportes.actividad", "Rapport d'Activité");
        
        translations.put("Français", fr);
    }

    /**
     * Obtiene la traducción de una clave
     */
    public String get(String key) {
        Map<String, String> currentLangMap = translations.get(currentLanguage);
        if (currentLangMap == null) {
            currentLangMap = translations.get("Español"); // fallback
        }
        return currentLangMap.getOrDefault(key, key); // devuelve la clave si no existe
    }

    /**
     * Cambia el idioma actual
     */
    public void setLanguage(String language) {
        if (!translations.containsKey(language)) {
            throw new IllegalArgumentException("Idioma no válido: " + language);
        }

        this.currentLanguage = language;

        // Notificar a todos los listeners
        for (LanguageChangeListener listener : listeners) {
            listener.onLanguageChanged(language);
        }

        // Guardar preferencia
        ConfiguracionService.getInstance().setConfigValue("app.idioma", language);
    }

    /**
     * Obtiene el idioma actual
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Obtiene lista de idiomas disponibles
     */
    public List<String> getAvailableLanguages() {
        return new ArrayList<>(translations.keySet());
    }

    /**
     * Registra un listener para cambios de idioma
     */
    public void addLanguageChangeListener(LanguageChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Desregistra un listener
     */
    public void removeLanguageChangeListener(LanguageChangeListener listener) {
        listeners.remove(listener);
    }
}
