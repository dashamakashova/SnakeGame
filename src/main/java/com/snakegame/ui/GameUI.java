package com.snakegame.ui;

import com.snakegame.config.GameConfig;
import com.snakegame.core.*;
import com.snakegame.utils.GameTimer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

/**
 * Класс графического интерфейса игры "Змейка" на JavaFX.
 * Отвечает за отрисовку игрового поля и обработку пользовательского ввода.
 */
public class GameUI {

    private final GameMode gameMode;
    private Snake snake;
    private GameState gameState;
    private GameTimer gameTimer;
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private final int cellSize;
    private final int gridSize;
    private Stage stage;
    private boolean gameStarted = false;
    private boolean isPaused = false;

    /**
     * Конструктор графического интерфейса.
     * @param gameMode режим игры (интерактивный или неинтерактивный)
     */
    public GameUI(GameMode gameMode) {
        this.gameMode = gameMode;
        this.snake = new Snake();
        this.gameState = GameState.NOT_STARTED;
        this.gameTimer = new GameTimer();

        GameConfig config = GameConfig.getInstance();
        this.gridSize = config.getGridSize();
        this.cellSize = Math.min(config.getWindowWidth(), config.getWindowHeight()) / gridSize;
    }

    /**
     * Запускает графический интерфейс в указанном окне.
     * @param stage окно для отображения игры
     */
    public void start(Stage stage) {
        this.stage = stage;

        try {
            GameConfig config = GameConfig.getInstance();

            // Создаем игровое поле
            Pane root = new Pane();
            canvas = new Canvas(config.getWindowWidth(), config.getWindowHeight());
            gc = canvas.getGraphicsContext2D();
            root.getChildren().add(canvas);

            Scene scene = createScene(root, config);

            String title = "Змейка - ";
            if (gameMode == GameMode.INTERACTIVE) {
                title += "Игровой режим (управление стрелками)";
            } else {
                title += "Режим наблюдателя (автоматическая игра)";
            }
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            // Инициализация игрового цикла
            initializeGameLoop();

            // Рисуем начальный экран
            drawStartScreen();

            // Обработка закрытия окна
            stage.setOnCloseRequest(event -> {
                stopGame();
            });

            // Запускаем игру с задержкой
            startGameWithDelay();

        } catch (Exception e) {
            System.err.println("Ошибка инициализации графического интерфейса: " + e.getMessage());
        }
    }

    /**
     * Запускает игру с задержкой.
     */
    private void startGameWithDelay() {
        new Thread(() -> {
            try {
                // Отсчет до старта
                for (int i = 3; i > 0; i--) {
                    final int count = i;
                    Platform.runLater(() -> {
                        drawStartScreen();
                        gc.setFill(Color.WHITE);
                        gc.setFont(Font.font("Arial", 48));
                        gc.fillText(String.valueOf(count),
                                canvas.getWidth() / 2 - 15,
                                canvas.getHeight() / 2 + 50);
                    });
                    Thread.sleep(1000);
                }

                // Старт игры
                Platform.runLater(() -> {
                    startGame();
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Создает сцену для игры.
     * @param root корневой контейнер
     * @param config конфигурация игры
     * @return созданная сцена
     */
    private Scene createScene(Pane root, GameConfig config) {
        Scene scene = new Scene(root, config.getWindowWidth(), config.getWindowHeight());

        // Обработка нажатий клавиш
        scene.setOnKeyPressed(event -> {
            // Обработка паузы
            if (event.getCode() == KeyCode.P) {
                if (gameState == GameState.RUNNING) {
                    togglePause();
                } else if (gameState == GameState.PAUSED) {
                    togglePause(); // Снимаем с паузы
                }
                return;
            }

            // Старт игры
            if (!gameStarted && gameState == GameState.NOT_STARTED) {
                startGame();
                return;
            }

            // В интерактивном режиме обрабатываем управление
            if (gameMode == GameMode.INTERACTIVE && gameState == GameState.RUNNING) {
                handleKeyPress(event.getCode());
            }

            // Перезапуск игры
            if (event.getCode() == KeyCode.R &&
                    (gameState == GameState.GAME_OVER || gameState == GameState.WIN || gameState == GameState.DRAW)) {
                restartGame();
            }
        });

        return scene;
    }

    /**
     * Инициализирует игровой цикл.
     */
    private void initializeGameLoop() {
        GameConfig config = GameConfig.getInstance();

        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (gameState != GameState.RUNNING) {
                    return;
                }

                // Ограничиваем частоту обновления
                if (now - lastUpdate >= config.getGameSpeed() * 1_000_000L) {
                    updateGame();
                    drawGame();
                    lastUpdate = now;
                }

                // Проверяем таймер
                if (gameTimer.isTimeUp()) {
                    endGame(GameState.DRAW);
                }
            }
        };
    }

    /**
     * Запускает игру.
     */
    private void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            gameState = GameState.RUNNING;
            gameTimer.start();
            gameLoop.start();

            // Обновляем заголовок окна
            updateWindowTitle();
        }
    }

    /**
     * Обновляет состояние игры.
     */
    private void updateGame() {
        try {
            boolean moveSuccessful;

            if (gameMode == GameMode.NON_INTERACTIVE) {
                // Движение в режиме наблюдателя
                moveSuccessful = snake.moveAlive();
            } else {
                // Движение в интерактивном режиме
                moveSuccessful = snake.move();
            }

            // Проверяем результат движения
            if (!moveSuccessful) {
                endGame(GameState.GAME_OVER);
                return;
            }

            // Проверяем победу
            if (snake.isMaxLength()) {
                endGame(GameState.WIN);
            }

        } catch (Exception e) {
            System.err.println("Ошибка при обновлении игры: " + e.getMessage());
            // Игра не должна крашиться при ошибках
            gameState = GameState.PAUSED;
            updateWindowTitle();
        }
    }

    /**
     * Обрабатывает нажатие клавиши.
     * @param keyCode код нажатой клавиши
     */
    private void handleKeyPress(KeyCode keyCode) {
        try {
            switch (keyCode) {
                case UP:
                    snake.setDirection(Direction.UP);
                    break;
                case DOWN:
                    snake.setDirection(Direction.DOWN);
                    break;
                case LEFT:
                    snake.setDirection(Direction.LEFT);
                    break;
                case RIGHT:
                    snake.setDirection(Direction.RIGHT);
                    break;
                default:
                    // Игнорируем другие клавиши
                    break;
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки ввода: " + e.getMessage());
            // Игнорируем ошибки ввода
        }
    }

    /**
     * Переключает состояние паузы.
     */
    private void togglePause() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
            gameTimer.stop();
            isPaused = true;
            updateWindowTitle();
            drawGame(); // Перерисовываем для отображения экрана паузы
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING;
            gameTimer.start();
            isPaused = false;
            updateWindowTitle();
        }
    }

    /**
     * Завершает игру с указанным состоянием.
     * @param endState конечное состояние игры
     */
    private void endGame(GameState endState) {
        gameState = endState;
        gameTimer.stop();
        gameLoop.stop();
        drawGame(); // Отображаем финальный экран
        updateWindowTitle();
    }

    /**
     * Перезапускает игру.
     */
    private void restartGame() {
        gameLoop.stop();
        gameTimer.reset();
        gameStarted = false;
        isPaused = false;

        // Пересоздаем змейку
        snake = new Snake();
        gameState = GameState.NOT_STARTED;

        // Перезапускаем
        drawStartScreen();

        // Запускаем с задержкой
        startGameWithDelay();
    }

    /**
     * Останавливает игру.
     */
    private void stopGame() {
        gameState = GameState.GAME_OVER;
        gameTimer.stop();
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /**
     * Обновляет заголовок окна.
     */
    private void updateWindowTitle() {
        if (stage != null) {
            String status = "";
            switch (gameState) {
                case RUNNING:
                    status = " [Игра идет]";
                    break;
                case PAUSED:
                    status = " [ПАУЗА]";
                    break;
                case GAME_OVER:
                    status = " [Игра окончена]";
                    break;
                case WIN:
                    status = " [ПОБЕДА!]";
                    break;
                case DRAW:
                    status = " [Время вышло]";
                    break;
            }

            String mode = gameMode == GameMode.INTERACTIVE ?
                    "Игровой режим" : "Режим наблюдателя";
            stage.setTitle("Змейка - " + mode + status);
        }
    }

    /**
     * Рисует начальный экран.
     */
    private void drawStartScreen() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 28));
        gc.fillText("ЗМЕЙКА", canvas.getWidth() / 2 - 70, canvas.getHeight() / 2 - 80);

        gc.setFont(Font.font("Arial", 18));

        if (gameMode == GameMode.INTERACTIVE) {
            gc.fillText("ИГРОВОЙ РЕЖИМ", canvas.getWidth() / 2 - 70, canvas.getHeight() / 2 - 40);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText("Управление: стрелки клавиатуры",
                    canvas.getWidth() / 2 - 120, canvas.getHeight() / 2);
            gc.fillText("Пауза: клавиша P",
                    canvas.getWidth() / 2 - 70, canvas.getHeight() / 2 + 30);

            if (!gameStarted) {
                gc.fillText("Игра начнется через несколько секунд...",
                        canvas.getWidth() / 2 - 150, canvas.getHeight() / 2 + 60);
            } else {
                gc.fillText("Нажмите любую клавишу для начала",
                        canvas.getWidth() / 2 - 140, canvas.getHeight() / 2 + 60);
            }
        } else {
            gc.fillText("РЕЖИМ НАБЛЮДАТЕЛЯ", canvas.getWidth() / 2 - 90, canvas.getHeight() / 2 - 40);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText("Змейка движется автоматически",
                    canvas.getWidth() / 2 - 120, canvas.getHeight() / 2);
            gc.fillText("Вы можете наблюдать за игрой",
                    canvas.getWidth() / 2 - 120, canvas.getHeight() / 2 + 30);

            if (!gameStarted) {
                gc.fillText("Игра начнется через несколько секунд...",
                        canvas.getWidth() / 2 - 150, canvas.getHeight() / 2 + 60);
            }
        }

        gc.setFont(Font.font("Arial", 12));
        gc.fillText("Перезапуск: R (после окончания игры)",
                canvas.getWidth() / 2 - 140, canvas.getHeight() - 30);
    }

    /**
     * Рисует игровое поле и все элементы игры.
     */
    private void drawGame() {
        // Очищаем холст
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (gameState == GameState.NOT_STARTED) {
            drawStartScreen();
            return;
        }

        // Рисуем сетку
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(0.5);

        for (int i = 0; i <= gridSize; i++) {
            // Вертикальные линии
            gc.strokeLine(i * cellSize, 0, i * cellSize, gridSize * cellSize);
            // Горизонтальные линии
            gc.strokeLine(0, i * cellSize, gridSize * cellSize, i * cellSize);
        }

        // Рисуем еду
        Point food = snake.getFood();
        gc.setFill(Color.RED);
        gc.fillOval(food.getX() * cellSize + 2, food.getY() * cellSize + 2,
                cellSize - 4, cellSize - 4);

        // Рисуем змейку
        List<Point> body = snake.getBody();
        for (int i = 0; i < body.size(); i++) {
            Point segment = body.get(i);

            // Голова зеленого цвета, тело - разных оттенков зеленого
            if (i == 0) {
                gc.setFill(Color.LIMEGREEN); // Голова
            } else {
                // Градиент для тела
                double intensity = 0.5 + 0.5 * (i / (double) body.size());
                gc.setFill(Color.color(0, intensity, 0));
            }

            gc.fillRect(segment.getX() * cellSize + 1, segment.getY() * cellSize + 1,
                    cellSize - 2, cellSize - 2);
        }

        // Рисуем информацию о состоянии игры
        drawGameInfo();

        // Рисуем экран окончания игры или паузы
        if (gameState == GameState.GAME_OVER || gameState == GameState.WIN ||
                gameState == GameState.DRAW) {
            drawEndScreen();
        } else if (gameState == GameState.PAUSED) {
            drawPauseScreen();
        }
    }

    /**
     * Рисует информацию о состоянии игры.
     */
    private void drawGameInfo() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 14));

        // Счет
        gc.fillText("Длина: " + snake.getLength(), 10, 20);

        // Оставшееся время (только во время игры)
        if (gameState == GameState.RUNNING) {
            long remainingTime = gameTimer.getRemainingTime();
            gc.fillText("Время: " + remainingTime + " сек", 10, 40);
        }

        // Режим игры
        String modeText = gameMode == GameMode.INTERACTIVE ?
                "Режим: Игрок" : "Режим: Наблюдатель";
        gc.fillText(modeText, 10, 60);

        // Состояние игры
        String stateText = "";
        switch (gameState) {
            case RUNNING:
                stateText = "Состояние: Игра идет";
                break;
            case PAUSED:
                stateText = "Состояние: ПАУЗА";
                break;
            case GAME_OVER:
                stateText = "Состояние: Игра окончена";
                break;
            case WIN:
                stateText = "Состояние: ПОБЕДА!";
                break;
            case DRAW:
                stateText = "Состояние: Время вышло";
                break;
        }
        gc.fillText(stateText, 10, 80);

        // Инструкции
        if (gameMode == GameMode.INTERACTIVE && gameState == GameState.RUNNING) {
            gc.fillText("Управление: стрелки", canvas.getWidth() - 150, 20);
        }

        if (gameState == GameState.RUNNING || gameState == GameState.PAUSED) {
            gc.fillText("Пауза: P", canvas.getWidth() - 150, 40);
        }

        if (gameState == GameState.GAME_OVER || gameState == GameState.WIN || gameState == GameState.DRAW) {
            gc.fillText("Перезапуск: R", canvas.getWidth() - 150, 60);
        }
    }

    /**
     * Рисует экран паузы.
     */
    private void drawPauseScreen() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 36));
        gc.fillText("ПАУЗА", canvas.getWidth() / 2 - 70, canvas.getHeight() / 2);

        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Нажмите P для продолжения",
                canvas.getWidth() / 2 - 120, canvas.getHeight() / 2 + 40);

        if (gameMode == GameMode.NON_INTERACTIVE) {
            gc.fillText("Змейка движется автоматически",
                    canvas.getWidth() / 2 - 140, canvas.getHeight() / 2 + 80);
        }
    }

    /**
     * Рисует экран окончания игры.
     */
    private void drawEndScreen() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 36));

        String message = "";
        Color messageColor = Color.WHITE;

        switch (gameState) {
            case GAME_OVER:
                message = "ИГРА ОКОНЧЕНА";
                messageColor = Color.RED;
                break;
            case WIN:
                message = "ПОБЕДА!";
                messageColor = Color.GREEN;
                break;
            case DRAW:
                message = "ВРЕМЯ ВЫШЛО!";
                messageColor = Color.YELLOW;
                break;
            default:
                message = "ИГРА ЗАВЕРШЕНА";
                break;
        }

        gc.setFill(messageColor);
        gc.fillText(message, canvas.getWidth() / 2 - 120, canvas.getHeight() / 2 - 50);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText("Итоговая длина змейки: " + snake.getLength(),
                canvas.getWidth() / 2 - 120, canvas.getHeight() / 2);

        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Нажмите R для перезапуска игры",
                canvas.getWidth() / 2 - 140, canvas.getHeight() / 2 + 40);

        gc.setFont(Font.font("Arial", 14));
        gc.fillText("Для выхода закройте окно",
                canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 + 80);
    }
}