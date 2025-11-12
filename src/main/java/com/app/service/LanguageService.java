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
        es.put("menu.usuarios", "Usuarios");
        es.put("menu.roles", "Roles");
        es.put("menu.seguridad", "Seguridad");
        es.put("menu.configuracion", "Configuración");
        es.put("menu.reportes", "Reportes");
        es.put("menu.pedidos", "Pedidos");
        es.put("menu.dashboard", "Dashboard");
        es.put("btn.nuevo", "Nuevo");
        es.put("btn.editar", "Editar");
        es.put("btn.eliminar", "Eliminar");
        es.put("btn.guardar", "Guardar");
        es.put("btn.cancelar", "Cancelar");
        es.put("btn.buscar", "Buscar");
        es.put("btn.refrescar", "Refrescar");
        es.put("btn.volver", "🏠 Regresar al Dashboard");
        es.put("msg.exito", "Operación completada exitosamente");
        es.put("msg.error", "Error");
        es.put("msg.advertencia", "Advertencia");
        es.put("msg.confirmacion", "¿Está seguro?");
        es.put("login.titulo", "Inicio de Sesión");
        es.put("login.usuario", "Usuario");
        es.put("login.password", "Contraseña");
        es.put("login.entrar", "Entrar");
        es.put("config.tema", "Tema de la Interfaz");
        es.put("config.tema.claro", "Tema Claro");
        es.put("config.tema.oscuro", "Tema Oscuro");
        es.put("config.idioma", "Idioma");
        es.put("config.guardar", "Guardar Cambios");
    es.put("access.denied.title", "Acceso denegado");
    es.put("access.denied.tooltip", "Acceso restringido. Contacte al administrador.");
        translations.put("Español", es);

        // English
        Map<String, String> en = new HashMap<>();
        en.put("app.title", "Inventory Management");
        en.put("app.name", "Inventory Management");
        en.put("menu.usuarios", "Users");
        en.put("menu.roles", "Roles");
        en.put("menu.seguridad", "Security");
        en.put("menu.configuracion", "Settings");
        en.put("menu.reportes", "Reports");
        en.put("menu.pedidos", "Orders");
        en.put("menu.dashboard", "Dashboard");
        en.put("btn.nuevo", "New");
        en.put("btn.editar", "Edit");
        en.put("btn.eliminar", "Delete");
        en.put("btn.guardar", "Save");
        en.put("btn.cancelar", "Cancel");
        en.put("btn.buscar", "Search");
        en.put("btn.refrescar", "Refresh");
        en.put("btn.volver", "🏠 Back to Dashboard");
        en.put("msg.exito", "Operation completed successfully");
        en.put("msg.error", "Error");
        en.put("msg.advertencia", "Warning");
        en.put("msg.confirmacion", "Are you sure?");
        en.put("login.titulo", "Login");
        en.put("login.usuario", "Username");
        en.put("login.password", "Password");
        en.put("login.entrar", "Login");
        en.put("config.tema", "Theme");
        en.put("config.tema.claro", "Light Theme");
        en.put("config.tema.oscuro", "Dark Theme");
        en.put("config.idioma", "Language");
        en.put("config.guardar", "Save Changes");
    en.put("access.denied.title", "Access denied");
    en.put("access.denied.tooltip", "Restricted access. Contact your administrator.");
        translations.put("English", en);

        // Português
        Map<String, String> pt = new HashMap<>();
        pt.put("app.title", "Gestão de Inventário");
        pt.put("app.name", "Gestão de Inventário");
        pt.put("menu.usuarios", "Usuários");
        pt.put("menu.roles", "Funções");
        pt.put("menu.seguridad", "Segurança");
        pt.put("menu.configuracion", "Configurações");
        pt.put("menu.reportes", "Relatórios");
        pt.put("menu.pedidos", "Pedidos");
        pt.put("menu.dashboard", "Painel");
        pt.put("btn.nuevo", "Novo");
        pt.put("btn.editar", "Editar");
        pt.put("btn.eliminar", "Excluir");
        pt.put("btn.guardar", "Salvar");
        pt.put("btn.cancelar", "Cancelar");
        pt.put("btn.buscar", "Pesquisar");
        pt.put("btn.refrescar", "Atualizar");
        pt.put("btn.volver", "🏠 Voltar ao Painel");
        pt.put("msg.exito", "Operação concluída com sucesso");
        pt.put("msg.error", "Erro");
        pt.put("msg.advertencia", "Aviso");
        pt.put("msg.confirmacion", "Você tem certeza?");
        pt.put("login.titulo", "Login");
        pt.put("login.usuario", "Usuário");
        pt.put("login.password", "Senha");
        pt.put("login.entrar", "Entrar");
        pt.put("config.tema", "Tema");
        pt.put("config.tema.claro", "Tema Claro");
        pt.put("config.tema.oscuro", "Tema Escuro");
        pt.put("config.idioma", "Idioma");
        pt.put("config.guardar", "Salvar Alterações");
    pt.put("access.denied.title", "Acesso negado");
    pt.put("access.denied.tooltip", "Acesso restrito. Contate o administrador.");
        translations.put("Português", pt);

        // Français
        Map<String, String> fr = new HashMap<>();
        fr.put("app.title", "Gestion des Inventaires");
        fr.put("app.name", "Gestion des Inventaires");
        fr.put("menu.usuarios", "Utilisateurs");
        fr.put("menu.roles", "Rôles");
        fr.put("menu.seguridad", "Sécurité");
        fr.put("menu.configuracion", "Paramètres");
        fr.put("menu.reportes", "Rapports");
        fr.put("menu.pedidos", "Commandes");
        fr.put("menu.dashboard", "Tableau de bord");
        fr.put("btn.nuevo", "Nouveau");
        fr.put("btn.editar", "Modifier");
        fr.put("btn.eliminar", "Supprimer");
        fr.put("btn.guardar", "Enregistrer");
        fr.put("btn.cancelar", "Annuler");
        fr.put("btn.buscar", "Rechercher");
        fr.put("btn.refrescar", "Actualiser");
        fr.put("btn.volver", "🏠 Retour au Tableau de bord");
        fr.put("msg.exito", "Opération complétée avec succès");
        fr.put("msg.error", "Erreur");
        fr.put("msg.advertencia", "Avertissement");
        fr.put("msg.confirmacion", "Êtes-vous sûr?");
        fr.put("login.titulo", "Connexion");
        fr.put("login.usuario", "Nom d'utilisateur");
        fr.put("login.password", "Mot de passe");
        fr.put("login.entrar", "Connexion");
        fr.put("config.tema", "Thème");
        fr.put("config.tema.claro", "Thème Clair");
        fr.put("config.tema.oscuro", "Thème Sombre");
        fr.put("config.idioma", "Langue");
        fr.put("config.guardar", "Enregistrer les modifications");
    fr.put("access.denied.title", "Accès refusé");
    fr.put("access.denied.tooltip", "Accès restreint. Contactez l'administrateur.");
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
