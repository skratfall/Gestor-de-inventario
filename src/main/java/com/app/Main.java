package com.app;

import com.app.service.UsuarioService;
import com.app.service.ThemeService;
import com.app.util.StageDebugger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("🚀 DEBUG: Main.start() called - primaryStage: " + System.identityHashCode(primaryStage));
        StageDebugger.trackStage(primaryStage, "Main - Primary Stage");
        
        try {
            UsuarioService usuarioService = UsuarioService.getInstance();
            boolean hasActiveUsuarios = false;
            
            // Intentar verificar si existen usuarios activos; si falla, asumir que es registro inicial
            try {
                hasActiveUsuarios = usuarioService.hasAnyActiveUsuario();
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo verificar usuarios (conexión a BD): " + e.getMessage());
                System.out.println("Asumiendo registro inicial...");
                hasActiveUsuarios = false;
            }

            String viewPath;
            String title;
            int width;
            int height;

            if (!hasActiveUsuarios) {
                viewPath = "/com/app/view/RegisterView.fxml";
                title = "Registro Inicial - Sistema de Gestión de Inventario";
                width = 900;
                height = 800;
            } else {
                viewPath = "/com/app/view/LoginView.fxml";
                title = "Login - Sistema de Gestión de Inventario";
                width = 900;
                height = 800;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);

            // Registrar la escena con el servicio de tema para aplicar tema guardado
            ThemeService.getInstance().registerScene(scene);

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            System.out.println("✅ DEBUG: About to call primaryStage.show()");
            System.out.println("   Stage width: " + primaryStage.getWidth());
            System.out.println("   Stage height: " + primaryStage.getHeight());
            System.out.println("   Stage resizable: " + primaryStage.isResizable());
            StageDebugger.recordShowCall("Main.start() - PRIMARY SHOW CALL");
            primaryStage.show();
            System.out.println("✅ DEBUG: primaryStage.show() completed successfully");
            System.out.println("   Stages currently visible: " + countVisibleStages());
            
            // Iniciar un monitor de ventanas en background
            startWindowMonitor();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading application view: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("📋 Starting application...");
        launch(args);
    }

    private static int countVisibleStages() {
        // Contar todas las windows visibles de JavaFX
        // Esto es un poco de hack pero útil para depuración
        int count = 0;
        try {
            java.util.Collection<javafx.stage.Window> windows = 
                javafx.stage.Window.getWindows();
            for (javafx.stage.Window w : windows) {
                if (w instanceof javafx.stage.Stage) {
                    javafx.stage.Stage s = (javafx.stage.Stage) w;
                    if (s.isShowing()) {
                        count++;
                        System.out.println("   📌 Visible Stage: " + s.getTitle() + 
                            " (hash: " + System.identityHashCode(s) + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("   ❌ Error counting stages: " + e.getMessage());
        }
        return count;
    }

    private static void startWindowMonitor() {
        // Monitorear cambios en ventanas cada 2 segundos durante los primeros 10 segundos
        Thread monitorThread = new Thread(() -> {
            try {
                int previousCount = 0;
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(2000); // Esperar 2 segundos
                    int currentCount = countVisibleStagesQuiet();
                    if (currentCount != previousCount) {
                        System.out.println("🔔 [MONITOR] Cambio detectado: " + previousCount + 
                            " -> " + currentCount + " ventanas visibles");
                        countVisibleStages(); // Mostrar detalles
                    }
                    previousCount = currentCount;
                }
            } catch (InterruptedException e) {
                // Ignorar
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("WindowMonitor");
        monitorThread.start();
    }

    private static int countVisibleStagesQuiet() {
        try {
            int count = 0;
            java.util.Collection<javafx.stage.Window> windows = 
                javafx.stage.Window.getWindows();
            for (javafx.stage.Window w : windows) {
                if (w instanceof javafx.stage.Stage) {
                    javafx.stage.Stage s = (javafx.stage.Stage) w;
                    if (s.isShowing()) {
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            return -1;
        }
    }
}