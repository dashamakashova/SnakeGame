package com.snakegame.ui;

import com.snakegame.core.GameMode;
import java.util.Scanner;

/**
 * Класс для взаимодействия с пользователем через консоль.
 * Обрабатывает ввод команд и запускает соответствующий режим игры.
 */
public class ConsoleInterface {

    private Scanner scanner;
    private boolean isRunning = true;

    /**
     * Конструктор консольного интерфейса.
     */
    public ConsoleInterface() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Запускает консольный интерфейс.
     * Ожидает ввод команд от пользователя и обрабатывает их.
     */
    public void start() {
        System.out.println(" ДОБРО ПОЖАЛОВАТЬ В ИГРУ 'ЗМЕЙКА' ");
        System.out.println("Доступные команды:");
        System.out.println("1. 'Хочу играть!' - запустить интерактивный режим");
        System.out.println("2. 'Я наблюдатель' - запустить неинтерактивный режим (графический)");
        System.out.println("3. 'выход' - завершить программу");

        while (isRunning) {
            System.out.print("\nВведите команду: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("выход")) {
                exitProgram();
                break;
            } else if (input.equalsIgnoreCase("Хочу играть!") || input.startsWith("Хочу играть!")) {
                startInteractiveMode();
            } else if (input.equalsIgnoreCase("Я наблюдатель")) {
                startObserverMode();
            } else {
                System.out.println("Неизвестная команда. Попробуйте еще раз.");
            }
        }

        scanner.close();
    }

    /**
     * Запускает интерактивный режим игры с графическим интерфейсом.
     */
    private void startInteractiveMode() {
        System.out.println("Запуск интерактивного режима...");
        System.out.println("Управление: стрелки клавиатуры (↑, ↓, ←, →)");
        System.out.println("Пауза: клавиша P");
        System.out.println("Откроется новое окно с игрой...");

        // Запускаем игру через GameLauncher
        GameLauncher.launchGame(GameMode.INTERACTIVE);
    }

    /**
     * Запускает режим наблюдателя с графическим интерфейсом.
     * Змейка движется автоматически, пользователь наблюдает.
     */
    private void startObserverMode() {
        System.out.println("Запуск режима наблюдателя...");
        System.out.println("Змейка движется автоматически");
        System.out.println("Вы можете наблюдать за игрой");
        System.out.println("Пауза: клавиша P");
        System.out.println("Откроется новое окно с игрой...");

        // Запускаем игру через GameLauncher
        GameLauncher.launchGame(GameMode.NON_INTERACTIVE);
    }

    /**
     * Завершает программу.
     */
    private void exitProgram() {
        System.out.println("Завершение программы...");
        isRunning = false;
        GameLauncher.stopGame();
    }
}