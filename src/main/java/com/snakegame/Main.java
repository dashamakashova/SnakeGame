package com.snakegame;

import com.snakegame.ui.ConsoleInterface;

/**
 * Главный класс приложения "Змейка".
 * Запускает программу и обрабатывает выбор режима игры.
 */
public class Main {

    /**
     * Точка входа в приложение.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        ConsoleInterface consoleInterface = new ConsoleInterface();
        consoleInterface.start();
    }
}