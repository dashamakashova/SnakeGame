package com.snakegame.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс для загрузки конфигурационных параметров игры из файла.
 * Реализует паттерн Singleton для обеспечения единственного экземпляра.
 */
public class GameConfig {

    private static GameConfig instance;
    private final Properties properties;

    private static final String CONFIG_FILE = "config.properties";
    private static final int DEFAULT_GRID_SIZE = 20;
    private static final int DEFAULT_GAME_SPEED = 150;
    private static final int DEFAULT_GAME_DURATION = 300; // 5 минут в секундах
    private static final int DEFAULT_SNAKE_INITIAL_LENGTH = 3;
    private static final int DEFAULT_WINDOW_WIDTH = 600;
    private static final int DEFAULT_WINDOW_HEIGHT = 600;

    /**
     * Приватный конструктор для реализации Singleton.
     * Загружает конфигурацию из файла.
     */
    private GameConfig() {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Загружает свойства из конфигурационного файла.
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Конфигурационный файл не найден, используются значения по умолчанию");
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
            setDefaultProperties();
        }
    }

    /**
     * Устанавливает значения по умолчанию для конфигурации.
     */
    private void setDefaultProperties() {
        properties.setProperty("grid.size", String.valueOf(DEFAULT_GRID_SIZE));
        properties.setProperty("game.speed", String.valueOf(DEFAULT_GAME_SPEED));
        properties.setProperty("game.duration", String.valueOf(DEFAULT_GAME_DURATION));
        properties.setProperty("snake.initial.length", String.valueOf(DEFAULT_SNAKE_INITIAL_LENGTH));
        properties.setProperty("game.window.width", String.valueOf(DEFAULT_WINDOW_WIDTH));
        properties.setProperty("game.window.height", String.valueOf(DEFAULT_WINDOW_HEIGHT));
    }

    /**
     * Возвращает единственный экземпляр GameConfig.
     * @return экземпляр GameConfig
     */
    public static synchronized GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    /**
     * Получает целочисленное значение свойства.
     * @param propertyName имя свойства
     * @param defaultValue значение по умолчанию
     * @return значение свойства
     */
    private int getIntProperty(String propertyName, int defaultValue) {
        String value = properties.getProperty(propertyName);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Ошибка парсинга свойства " + propertyName + ": " + value);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Получает размер игрового поля.
     * @return размер сетки в клетках
     */
    public int getGridSize() {
        return getIntProperty("grid.size", DEFAULT_GRID_SIZE);
    }

    /**
     * Получает скорость игры.
     * @return скорость игры (задержка между ходами в миллисекундах)
     */
    public int getGameSpeed() {
        return getIntProperty("game.speed", DEFAULT_GAME_SPEED);
    }

    /**
     * Получает максимальную продолжительность игры.
     * @return максимальная продолжительность игры в секундах
     */
    public int getGameDuration() {
        return getIntProperty("game.duration", DEFAULT_GAME_DURATION);
    }

    /**
     * Получает начальную длину змейки.
     * @return начальная длина змейки
     */
    public int getSnakeInitialLength() {
        return getIntProperty("snake.initial.length", DEFAULT_SNAKE_INITIAL_LENGTH);
    }

    /**
     * Получает ширину окна игры.
     * @return ширина окна в пикселях
     */
    public int getWindowWidth() {
        return getIntProperty("game.window.width", DEFAULT_WINDOW_WIDTH);
    }

    /**
     * Получает высоту окна игры.
     * @return высота окна в пикселях
     */
    public int getWindowHeight() {
        return getIntProperty("game.window.height", DEFAULT_WINDOW_HEIGHT);
    }
}