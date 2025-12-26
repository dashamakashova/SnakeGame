package com.snakegame.core;

import com.snakegame.config.GameConfig;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Класс, представляющий змейку в игре.
 * Отвечает за движение, рост и проверку столкновений змейки.
 */
public class Snake {

    private final LinkedList<Point> body;
    private final int gridSize;
    private final Random random;
    private Point food;
    private Direction direction;
    private Direction nextDirection;
    private int stepsWithoutTurn;
    private final int maxStepsWithoutTurn = 5;

    /**
     * Конструктор змейки.
     * Инициализирует змейку в начальном положении.
     */
    public Snake() {
        GameConfig config = GameConfig.getInstance();
        this.gridSize = config.getGridSize();
        this.random = new Random();
        this.body = new LinkedList<>();
        this.direction = Direction.RIGHT;
        this.nextDirection = Direction.RIGHT;
        this.stepsWithoutTurn = 0;

        // Инициализация начальной позиции змейки
        int initialLength = config.getSnakeInitialLength();
        int startX = gridSize / 4;
        int startY = gridSize / 2;

        for (int i = 0; i < initialLength; i++) {
            body.add(new Point(startX - i, startY));
        }

        generateFood();
    }

    /**
     * Получает текущее направление движения змейки.
     * @return текущее направление движения
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Устанавливает следующее направление движения змейки.
     * Проверяет, не является ли новое направление противоположным текущему.
     * @param direction новое направление движения
     */
    public void setDirection(Direction direction) {
        // Предотвращаем разворот на 180 градусов
        if ((this.direction == Direction.UP && direction != Direction.DOWN) ||
                (this.direction == Direction.DOWN && direction != Direction.UP) ||
                (this.direction == Direction.LEFT && direction != Direction.RIGHT) ||
                (this.direction == Direction.RIGHT && direction != Direction.LEFT)) {
            this.nextDirection = direction;
        }
    }

    /**
     * Двигает змейку на одну клетку в текущем направлении.
     * @return true, если движение успешно, false если произошло столкновение
     */
    public boolean move() {
        // Обновляем текущее направление
        direction = nextDirection;

        // Получаем голову змейки
        Point head = body.getFirst().copy();

        // Вычисляем новую позицию головы
        switch (direction) {
            case UP:
                head.setY(head.getY() - 1);
                break;
            case DOWN:
                head.setY(head.getY() + 1);
                break;
            case LEFT:
                head.setX(head.getX() - 1);
                break;
            case RIGHT:
                head.setX(head.getX() + 1);
                break;
        }

        // Проверяем столкновение со стенами
        if (head.getX() < 0 || head.getX() >= gridSize ||
                head.getY() < 0 || head.getY() >= gridSize) {
            return false;
        }

        // Проверяем столкновение с собственным телом
        for (Point segment : body) {
            if (head.equals(segment)) {
                return false;
            }
        }

        // Добавляем новую голову
        body.addFirst(head);

        // Проверяем, съела ли змейка еду
        if (head.equals(food)) {
            generateFood();
        } else {
            // Удаляем хвост, если еда не была съедена
            body.removeLast();
        }

        return true;
    }

    /**
     * Двигает змейку в режиме наблюдателя.
     * @return true, если движение успешно
     */
    public boolean moveAlive() {
        stepsWithoutTurn++;

        // Частая смена направления
        if (stepsWithoutTurn >= maxStepsWithoutTurn || random.nextDouble() < 0.4) {
            // Пробуем найти безопасное направление
            Direction safeDirection = findSafeDirection();
            if (safeDirection != null && !isOppositeDirection(safeDirection)) {
                setDirection(safeDirection);
                stepsWithoutTurn = 0;
            }
        }

        // Выполняем движение
        return move();
    }

    /**
     * Находит безопасное направление движения.
     * @return безопасное направление или null
     */
    private Direction findSafeDirection() {
        Point head = body.getFirst();

        // Собираем все возможные направления
        Direction[] allDirections = Direction.values();
        List<Direction> safeDirections = new LinkedList<>();

        for (Direction dir : allDirections) {
            if (!isOppositeDirection(dir) && isDirectionSafe(head, dir)) {
                safeDirections.add(dir);
            }
        }

        if (safeDirections.isEmpty()) {
            return null;
        }

        // Выбираем случайное безопасное направление
        return safeDirections.get(random.nextInt(safeDirections.size()));
    }

    /**
     * Проверяет, безопасно ли движение в указанном направлении.
     * @param head позиция головы
     * @param direction направление для проверки
     * @return true, если направление безопасно
     */
    private boolean isDirectionSafe(Point head, Direction direction) {
        Point newHead = head.copy();

        switch (direction) {
            case UP:
                newHead.setY(head.getY() - 1);
                break;
            case DOWN:
                newHead.setY(head.getY() + 1);
                break;
            case LEFT:
                newHead.setX(head.getX() - 1);
                break;
            case RIGHT:
                newHead.setX(head.getX() + 1);
                break;
        }

        // Проверяем границы
        if (newHead.getX() < 0 || newHead.getX() >= gridSize ||
                newHead.getY() < 0 || newHead.getY() >= gridSize) {
            return false;
        }

        // Проверяем столкновение с телом (кроме хвоста, который будет удален)
        for (int i = 0; i < body.size() - 1; i++) {
            if (newHead.equals(body.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Проверяет, является ли направление противоположным текущему.
     * @param direction направление для проверки
     * @return true, если направление противоположное
     */
    private boolean isOppositeDirection(Direction direction) {
        return (this.direction == Direction.UP && direction == Direction.DOWN) ||
                (this.direction == Direction.DOWN && direction == Direction.UP) ||
                (this.direction == Direction.LEFT && direction == Direction.RIGHT) ||
                (this.direction == Direction.RIGHT && direction == Direction.LEFT);
    }

    /**
     * Генерирует новую еду на случайной позиции поля.
     * Убеждается, что еда не появляется на теле змейки.
     */
    private void generateFood() {
        Point newFood;
        boolean onSnake;

        do {
            onSnake = false;
            newFood = new Point(random.nextInt(gridSize), random.nextInt(gridSize));

            // Проверяем, не находится ли еда на змейке
            for (Point segment : body) {
                if (newFood.equals(segment)) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);

        this.food = newFood;
    }

    /**
     * Получает тело змейки.
     * @return список точек, представляющих тело змейки
     */
    public List<Point> getBody() {
        return new LinkedList<>(body);
    }

    /**
     * Получает текущую позицию еды.
     * @return точка с координатами еды
     */
    public Point getFood() {
        return food;
    }

    /**
     * Получает текущую длину змейки.
     * @return длина змейки
     */
    public int getLength() {
        return body.size();
    }

    /**
     * Проверяет, достигла ли змейка максимально возможной длины.
     * @return true, если змейка заполнила всё поле, иначе false
     */
    public boolean isMaxLength() {
        return body.size() >= gridSize * gridSize;
    }
}