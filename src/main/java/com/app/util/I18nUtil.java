package com.app.util;

import com.app.service.LanguageService;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

/**
 * Utilidad para facilitar la internacionalización (i18n) en JavaFX.
 * Permite enlazar automáticamente claves de traducción a componentes.
 */
public class I18nUtil {

    private static final LanguageService langService = LanguageService.getInstance();

    /**
     * Establece el texto de una etiqueta usando una clave de traducción
     */
    public static void setLabelText(Label label, String key) {
        if (label != null && key != null) {
            label.setText(langService.get(key));
        }
    }

    /**
     * Establece el texto de un botón usando una clave de traducción
     */
    public static void setButtonText(Button button, String key) {
        if (button != null && key != null) {
            button.setText(langService.get(key));
        }
    }

    /**
     * Establece el promptText de un campo de texto usando una clave de traducción
     */
    public static void setPromptText(TextInputControl field, String key) {
        if (field != null && key != null) {
            field.setPromptText(langService.get(key));
        }
    }

    /**
     * Establece el tooltip de un componente usando una clave de traducción
     */
    public static void setTooltip(Node node, String key) {
        if (node != null && key != null) {
            Tooltip tooltip = new Tooltip(langService.get(key));
            Tooltip.install(node, tooltip);
        }
    }

    /**
     * Obtiene una traducción por clave
     */
    public static String get(String key) {
        return langService.get(key);
    }

    /**
     * Obtiene una traducción con formateo
     */
    public static String get(String key, Object... args) {
        String template = langService.get(key);
        return String.format(template, args);
    }
}
