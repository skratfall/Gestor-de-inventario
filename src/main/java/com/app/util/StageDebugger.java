package com.app.util;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilidad para rastrear creación y visualización de Stages
 */
public class StageDebugger {
    private static int stageCounter = 0;
    private static final Set<Integer> shownStages = new HashSet<>();
    private static int showCallCount = 0;

    public static void trackStage(Stage stage, String name) {
        stageCounter++;
        int id = stageCounter;
        int hash = System.identityHashCode(stage);
        System.out.println("🆔 [STAGE #" + id + "] Created: " + name + " - hashCode: " + hash);
        
        stage.setOnShown(event -> {
            System.out.println("👁️  [STAGE #" + id + "] SHOWN: " + name + " - Title: " + stage.getTitle());
            System.out.println("    hashCode: " + hash);
            if (shownStages.contains(hash)) {
                System.out.println("⚠️  ⚠️  ⚠️  STAGE SHOWN TWICE! ⚠️  ⚠️  ⚠️ ");
            } else {
                shownStages.add(hash);
            }
            printStackTrace();
        });
        
        stage.setOnHidden(event -> {
            System.out.println("❌ [STAGE #" + id + "] HIDDEN: " + name);
        });
        
        // Interceptar show() directamente
        trackShowMethod(stage, id, name);
    }

    private static void trackShowMethod(Stage stage, int id, String name) {
        // Se ejecutará cuando show() sea llamado
        // Ya tenemos el listener de setOnShown arriba
    }

    public static void recordShowCall(String location) {
        showCallCount++;
        System.out.println("📞 [SHOW CALL #" + showCallCount + "] show() called from: " + location);
        printStackTrace();
    }

    private static void printStackTrace() {
        System.out.println("📍 Call stack:");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 3; i < Math.min(10, elements.length); i++) {
            System.out.println("   [" + i + "] " + elements[i]);
        }
    }

    public static void reset() {
        stageCounter = 0;
        showCallCount = 0;
        shownStages.clear();
        System.out.println("🔄 Stage counter reset");
    }
}
