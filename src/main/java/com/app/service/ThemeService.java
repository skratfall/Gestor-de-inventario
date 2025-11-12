package com.app.service;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para manejar temas (claro/oscuro) dinámicamente en la aplicación.
 */
public class ThemeService {
    
    private static ThemeService instance;
    private static final String THEME_CLARO = "claro";
    private static final String THEME_OSCURO = "oscuro";
    private String currentTheme = THEME_CLARO;
    private final List<Scene> registeredScenes = new ArrayList<>();

    private ThemeService() {
        // Cargar tema guardado desde configuración
        ConfiguracionService configService = ConfiguracionService.getInstance();
        this.currentTheme = configService.getConfigValue("app.tema", THEME_CLARO);
    }

    public static ThemeService getInstance() {
        if (instance == null) {
            synchronized (ThemeService.class) {
                if (instance == null) {
                    instance = new ThemeService();
                }
            }
        }
        return instance;
    }

    /**
     * Registra una escena para que reciba actualizaciones de tema
     */
    public void registerScene(Scene scene) {
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyThemeToScene(scene, currentTheme);
        }
    }

    /**
     * Cambia el tema a claro
     */
    public void setThemeClaro() {
        setTheme(THEME_CLARO);
    }

    /**
     * Cambia el tema a oscuro
     */
    public void setThemeOscuro() {
        setTheme(THEME_OSCURO);
    }

    /**
     * Cambia el tema globalmente
     */
    public void setTheme(String theme) {
        if (!theme.equals(THEME_CLARO) && !theme.equals(THEME_OSCURO)) {
            throw new IllegalArgumentException("Tema no válido: " + theme);
        }

        this.currentTheme = theme;

        // Aplicar tema a todas las escenas registradas
        for (Scene scene : registeredScenes) {
            applyThemeToScene(scene, theme);
        }

        // Guardar preferencia en configuración
        ConfiguracionService.getInstance().setConfigValue("app.tema", theme);
    }

    /**
     * Obtiene el tema actual
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Aplica el tema a una escena específica
     */
    private void applyThemeToScene(Scene scene, String theme) {
        if (scene == null) {
            System.err.println("⚠️ Scene es nulo, no se puede aplicar tema");
            return;
        }

        // Limpiar estilos anteriores de tema
        scene.getStylesheets().removeIf(url -> url.contains("theme-"));

        // Aplicar tema correspondiente
        String themeUrl;
        try {
            if (THEME_OSCURO.equals(theme)) {
                themeUrl = getClass().getResource("/css/theme-dark.css").toExternalForm();
                System.out.println("✅ Aplicando tema oscuro");
            } else {
                themeUrl = getClass().getResource("/css/theme-light.css").toExternalForm();
                System.out.println("✅ Aplicando tema claro");
            }

            if (!scene.getStylesheets().contains(themeUrl)) {
                scene.getStylesheets().add(themeUrl);
                System.out.println("✅ Tema aplicado a escena: " + theme);
            }
        } catch (NullPointerException e) {
            System.err.println("❌ No se encontró el archivo CSS de tema: " + e.getMessage());
        }
    }

    /**
     * Verifica si el tema actual es oscuro
     */
    public boolean isDarkTheme() {
        return THEME_OSCURO.equals(currentTheme);
    }

    /**
     * Verifica si el tema actual es claro
     */
    public boolean isLightTheme() {
        return THEME_CLARO.equals(currentTheme);
    }
}
