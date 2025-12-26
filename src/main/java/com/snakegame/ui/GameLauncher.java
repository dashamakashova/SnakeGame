package com.snakegame.ui;

import com.snakegame.core.GameMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Класс для запуска JavaFX приложения.
 * Обеспечивает однократный запуск JavaFX и переключение режимов.
 */
public class GameLauncher extends Application {

    private static GameMode currentGameMode = GameMode.INTERACTIVE;
    private static GameUI currentGameUI;
    private static Stage primaryStage;
    private static boolean isJavaFXStarted = false;

    /**
     * Запускает JavaFX приложение.
     * @param gameMode режим игры для запуска
     */
    public static void launchGame(GameMode gameMode) {
        currentGameMode = gameMode;

        if (!isJavaFXStarted) {
            // Первый запуск JavaFX
            isJavaFXStarted = true;
            new Thread(() -> Application.launch(GameLauncher.class)).start();
        } else {
            // JavaFX уже запущен, переключаем режим
            Platform.runLater(() -> {
                if (primaryStage != null && primaryStage.isShowing()) {
                    // Закрываем текущее окно
                    primaryStage.close();
                }
                // Создаем новое окно с новым режимом
                createNewGameWindow();
            });
        }
    }

    /**
     * Метод инициализации JavaFX Application.
     * @param stage главное окно приложения
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        createNewGameWindow();
    }

    /**
     * Создает новое игровое окно.
     */
    private static void createNewGameWindow() {
        currentGameUI = new GameUI(currentGameMode);

        Stage newStage = new Stage();
        currentGameUI.start(newStage);

        // Закрываем старое окно если оно есть
        if (primaryStage != null && primaryStage != newStage) {
            primaryStage.close();
        }
        primaryStage = newStage;
    }

    /**
     * Получает текущий режим игры.
     * @return текущий режим игры
     */
    public static GameMode getCurrentGameMode() {
        return currentGameMode;
    }

    /**
     * Останавливает JavaFX приложение.
     */
    public static void stopGame() {
        if (primaryStage != null) {
            Platform.runLater(() -> {
                primaryStage.close();
                Platform.exit();
            });
        }
    }
}