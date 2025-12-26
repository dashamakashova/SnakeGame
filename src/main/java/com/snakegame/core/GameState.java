package com.snakegame.core;

/**
 * Перечисление состояний игры.
 */
public enum GameState {
    RUNNING,    // Игра выполняется
    PAUSED,     // Игра на паузе
    GAME_OVER,  // Игра завершена (проигрыш)
    WIN,        // Игра завершена (выигрыш)
    DRAW,       // Ничья (по таймауту)
    NOT_STARTED // Игра еще не началась
}