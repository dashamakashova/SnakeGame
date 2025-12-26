package com.snakegame.core;

/**
 * Класс, представляющий точку на игровом поле.
 * Используется для хранения координат элементов игры.
 */
public class Point {

    private int x;
    private int y;

    /**
     * Конструктор точки с заданными координатами.
     * @param x координата X
     * @param y координата Y
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Получает координату X точки.
     * @return координата X
     */
    public int getX() {
        return x;
    }

    /**
     * Устанавливает координату X точки.
     * @param x новая координата X
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Получает координату Y точки.
     * @return координата Y
     */
    public int getY() {
        return y;
    }

    /**
     * Устанавливает координату Y точки.
     * @param y новая координата Y
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Проверяет, совпадает ли данная точка с другой точкой.
     * @param other другая точка для сравнения
     * @return true, если точки совпадают, иначе false
     */
    public boolean equals(Point other) {
        return this.x == other.x && this.y == other.y;
    }

    /**
     * Создает копию текущей точки.
     * @return новая точка с теми же координатами
     */
    public Point copy() {
        return new Point(this.x, this.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}