import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BotTest {
    private Ball[][] board;
    private int[] next;
    private Bot bot;

    @BeforeEach
    void setUp() {
        board = new Ball[Config.SIZE][Config.SIZE];
        next = new int[Config.BALLS];
        bot = new Bot(board, next, new Random(1));
    }

    // Проверяет, что средний уровень добавляет 3 шарика.
    @Test
    void midThree() {
        Config.level = Config.MID;
        setNext(1, 2, 3);

        bot.add(3);

        assertEquals(3, ballCnt());
    }

    // Проверяет, что Bot добавляет только нужное количество шариков.
    @Test
    void midCount() {
        Config.level = Config.MID;
        setNext(1, 2, 3);

        bot.add(2);

        assertEquals(2, ballCnt());
    }

    // Проверяет, что лёгкий уровень может достроить одну линию двумя шариками.
    @Test
    void easySameLine() {
        Config.level = Config.EASY;
        putRow(0, 0, 3, 1);
        setNext(1, 1, 2);

        bot.add(3);

        assertColor(0, 3, 1);
        assertColor(0, 4, 1);
    }

    // Проверяет, что лёгкий уровень может помочь двум разным линиям.
    @Test
    void easyTwoLines() {
        Config.level = Config.EASY;
        putRow(0, 0, 3, 1);
        putRow(2, 0, 3, 2);
        setNext(1, 2, 3);

        bot.add(3);

        assertColor(0, 3, 1);
        assertColor(2, 3, 2);
    }

    // Проверяет, что лёгкий уровень может помочь трём разным линиям.
    @Test
    void easyThreeLines() {
        Config.level = Config.EASY;
        putRow(0, 0, 3, 1);
        putRow(2, 0, 3, 2);
        putRow(4, 0, 3, 3);
        setNext(1, 2, 3);

        bot.add(3);

        assertColor(0, 3, 1);
        assertColor(2, 3, 2);
        assertColor(4, 3, 3);
    }

    // Проверяет, что лёгкий уровень не помогает, если нужного цвета нет.
    @Test
    void easyNoColor() {
        Config.level = Config.EASY;
        putRow(0, 0, 3, 1);
        setNext(2, 3, 4);

        bot.add(3);

        assertEquals(3, colorCnt(1));
    }

    // Проверяет, что лёгкий уровень не добавляет больше запрошенного числа.
    @Test
    void easyCount() {
        Config.level = Config.EASY;
        putRow(0, 0, 3, 1);
        setNext(1, 1, 1);

        bot.add(2);

        assertEquals(5, ballCnt());
        assertEquals(5, colorCnt(1));
    }

    // Проверяет, что линия из 4 получает приоритет перед линией из 3.
    @Test
    void easyPriority() {
        Config.level = Config.EASY;
        putRow(0, 0, 4, 1);
        putRow(2, 0, 3, 2);
        setNext(1, 3, 3);

        bot.add(3);

        assertColor(0, 4, 1);
        assertEquals(3, colorCnt(2));
    }

    // Проверяет, что сложный уровень закрывает линию другим цветом.
    @Test
    void hardBlock() {
        Config.level = Config.HARD;
        putRow(0, 0, 3, 1);
        setNext(2, 1, 3);

        bot.add(3);

        assertNotNull(board[0][3]);
        assertNotEquals(1, board[0][3].color());
    }

    // Проверяет, что сложный уровень закрывает линию из 4 в первую очередь.
    @Test
    void hardPriority() {
        Config.level = Config.HARD;
        putRow(0, 0, 4, 1);
        setNext(2, 3, 4);

        bot.add(3);

        assertNotNull(board[0][4]);
        assertNotEquals(1, board[0][4].color());
    }

    // Проверяет, что сложный уровень использует только цвета из next.
    @Test
    void hardOnlyNext() {
        Config.level = Config.HARD;
        putRow(0, 0, 3, 1);
        setNext(1, 1, 1);

        bot.add(3);

        assertEquals(6, ballCnt());
        assertEquals(6, colorCnt(1));
    }

    private void setNext(int a, int b, int c) {
        next[0] = a;
        next[1] = b;
        next[2] = c;
    }

    private void put(int row, int col, int color) {
        board[row][col] = new Ball(color);
    }

    private void putRow(int row, int from, int len, int color) {
        for (int col = from; col < from + len; col++) {
            put(row, col, color);
        }
    }

    private void assertColor(int row, int col, int color) {
        assertNotNull(board[row][col]);
        assertEquals(color, board[row][col].color());
    }

    private int ballCnt() {
        int count = 0;

        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                if (board[row][col] != null) {
                    count++;
                }
            }
        }

        return count;
    }

    private int colorCnt(int color) {
        int count = 0;

        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                if (hasColor(row, col, color)) {
                    count++;
                }
            }
        }

        return count;
    }

    private boolean hasColor(int row, int col, int color) {
        return board[row][col] != null &&
                board[row][col].color() == color;
    }
}