package com.snakegame.utils;

import com.snakegame.config.GameConfig;

/**
 * Класс таймера для отслеживания времени игры.
 * Обеспечивает завершение игры по истечении максимального времени.
 */
public class GameTimer {

    private long startTime;
    private final long gameDuration; // в секундах
    private boolean isRunning;

    /**
     * Конструктор таймера.
     * Инициализирует таймер с длительностью из конфигурации.
     */
    public GameTimer() {
        GameConfig config = GameConfig.getInstance();
        this.gameDuration = config.getGameDuration();
        this.isRunning = false;
    }

    /**
     * Запускает таймер.
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.isRunning = true;
    }

    /**
     * Останавливает таймер.
     */
    public void stop() {
        this.isRunning = false;
    }

    /**
     * Проверяет, истекло ли время игры.
     * @return true, если время истекло, иначе false
     */
    public boolean isTimeUp() {
        if (!isRunning) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - startTime) / 1000;

        return elapsedSeconds >= gameDuration;
    }

    /**
     * Получает оставшееся время игры.
     * @return оставшееся время в секундах
     */
    public long getRemainingTime() {
        if (!isRunning) {
            return gameDuration;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - startTime) / 1000;

        return Math.max(0, gameDuration - elapsedSeconds);
    }

    /**
     * Сбрасывает таймер.
     */
    public void reset() {
        this.isRunning = false;
    }
}