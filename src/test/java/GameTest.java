import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    private Game game;

    @BeforeEach
    void setUp() {
        Config.level = Config.MID;
        game = new Game();
    }

    // Проверяет, что новое поле полностью пустое.
    @Test
    void emptyBoard() {
        assertEquals(81, game.freeCnt());
    }

    // Проверяет, что старт игры добавляет первые 6 шариков.
    @Test
    void startGame() {
        game.start();

        assertEquals(75, game.freeCnt());
    }

    // Проверяет, что нельзя ходить из пустой клетки.
    @Test
    void emptySource() {
        assertFalse(game.canMove(0, 0, 0, 1));
    }

    // Проверяет, что нельзя ходить в занятую клетку.
    @Test
    void busyTarget() {
        put(0, 0, 1);
        put(0, 1, 2);

        assertFalse(game.canMove(0, 0, 0, 1));
    }

    // Проверяет, что диагональный ход запрещён.
    @Test
    void diagonalMove() {
        put(0, 0, 1);

        assertFalse(game.canMove(0, 0, 1, 1));
    }

    // Проверяет, что ход по пустой строке разрешён.
    @Test
    void clearRow() {
        put(0, 0, 1);

        assertTrue(game.canMove(0, 0, 0, 3));
    }

    // Проверяет, что шарик на пути блокирует ход по строке.
    @Test
    void blockedRow() {
        put(0, 0, 1);
        put(0, 1, 2);

        assertFalse(game.canMove(0, 0, 0, 3));
    }

    // Проверяет, что ход по пустому столбцу разрешён.
    @Test
    void clearCol() {
        put(0, 0, 1);

        assertTrue(game.canMove(0, 0, 3, 0));
    }

    // Проверяет, что шарик на пути блокирует ход по столбцу.
    @Test
    void blockedCol() {
        put(0, 0, 1);
        put(1, 0, 2);

        assertFalse(game.canMove(0, 0, 3, 0));
    }

    // Проверяет, что нельзя ходить за пределы поля.
    @Test
    void outsideBoard() {
        put(0, 0, 1);

        assertFalse(game.canMove(0, 0, -1, 0));
        assertFalse(game.canMove(0, 0, 0, 9));
    }

    // Проверяет, что нельзя ходить в ту же самую клетку.
    @Test
    void sameCell() {
        put(0, 0, 1);

        assertFalse(game.canMove(0, 0, 0, 0));
    }

    // Проверяет, что неправильный ход не меняет поле.
    @Test
    void invalidMove() {
        put(0, 0, 1);
        put(0, 1, 2);
        int before = game.freeCnt();

        assertFalse(game.move(0, 0, 0, 3));
        assertEquals(before, game.freeCnt());
        assertColor(0, 0, 1);
        assertColor(0, 1, 2);
    }

    // Проверяет удаление горизонтальной линии из 5 шариков.
    @Test
    void horizontalLine() {
        putRow(0, 0, 5, 2);

        assertTrue(game.delLines());
        assertEquals(10, game.score());
        assertRowEmpty(0, 0, 5);
    }

    // Проверяет удаление вертикальной линии из 5 шариков.
    @Test
    void verticalLine() {
        putCol(0, 0, 5, 3);

        assertTrue(game.delLines());
        assertEquals(10, game.score());
        assertColEmpty(0, 0, 5);
    }

    // Проверяет удаление диагонали слева направо.
    @Test
    void diagonalLine() {
        for (int i = 0; i < 5; i++) {
            put(i, i, 4);
        }

        assertTrue(game.delLines());
        assertEquals(10, game.score());
    }

    // Проверяет удаление диагонали справа налево.
    @Test
    void backDiagonalLine() {
        for (int i = 0; i < 5; i++) {
            put(i, 4 - i, 5);
        }

        assertTrue(game.delLines());
        assertEquals(10, game.score());
    }

    // Проверяет, что линия из 4 шариков не удаляется.
    @Test
    void shortLine() {
        putRow(0, 0, 4, 1);

        assertFalse(game.delLines());
        assertEquals(0, game.score());
        assertColor(0, 0, 1);
        assertColor(0, 3, 1);
    }

    // Проверяет, что линия длиннее 5 тоже удаляется полностью.
    @Test
    void longLine() {
        putRow(0, 0, 6, 2);

        assertTrue(game.delLines());
        assertEquals(12, game.score());
        assertRowEmpty(0, 0, 6);
    }

    // Проверяет, что пересекающиеся линии удаляются без двойного счёта.
    @Test
    void crossLines() {
        putRow(0, 0, 5, 1);
        putCol(2, 0, 5, 1);

        assertTrue(game.delLines());
        assertEquals(18, game.score());
        assertEquals(81, game.freeCnt());
    }

    // Проверяет, что если линии нет, поле и счёт не меняются.
    @Test
    void noLine() {
        putRow(0, 0, 4, 1);

        assertFalse(game.delLines());
        assertEquals(0, game.score());
    }

    // Проверяет, что после собранной линии новые шарики не добавляются.
    @Test
    void moveWithLine() {
        putRow(0, 0, 4, 1);
        put(1, 4, 1);

        assertTrue(game.move(1, 4, 0, 4));
        assertEquals(81, game.freeCnt());
        assertEquals(10, game.score());
    }

    // Проверяет, что после обычного хода добавляются 3 новых шарика.
    @Test
    void moveNoLine() {
        put(0, 0, 1);

        assertTrue(game.move(0, 0, 0, 1));
        assertEquals(77, game.freeCnt());
    }

    // Проверяет, что при почти полном поле добавляются только свободные клетки.
    @Test
    void almostFull() {
        fillBoard();
        game.board()[8][7] = null;
        game.board()[8][8] = null;

        assertFalse(game.addBalls());
        assertEquals(0, game.freeCnt());
        assertTrue(game.isFull());
    }

    private void put(int row, int col, int color) {
        game.board()[row][col] = new Ball(color);
    }

    private void putRow(int row, int from, int len, int color) {
        for (int col = from; col < from + len; col++) {
            put(row, col, color);
        }
    }

    private void putCol(int col, int from, int len, int color) {
        for (int row = from; row < from + len; row++) {
            put(row, col, color);
        }
    }

    private void assertColor(int row, int col, int color) {
        assertNotNull(game.board()[row][col]);
        assertEquals(color, game.board()[row][col].color());
    }

    private void assertRowEmpty(int row, int from, int len) {
        for (int col = from; col < from + len; col++) {
            assertNull(game.board()[row][col]);
        }
    }

    private void assertColEmpty(int col, int from, int len) {
        for (int row = from; row < from + len; row++) {
            assertNull(game.board()[row][col]);
        }
    }

    private void fillBoard() {
        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                put(row, col, 1);
            }
        }
    }
}