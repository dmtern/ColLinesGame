import java.util.Random;

public class Game {
    private Ball[][] board = new Ball[Config.SIZE][Config.SIZE];
    private int[] next = new int[Config.BALLS];
    private int score = 0;

    private Random rnd = new Random();
    private Bot bot = new Bot(board, next, rnd);

    private int[] dr = {0, 1, 1, 1};
    private int[] dc = {1, 0, 1, -1};

    public Game() {
        newNext();
    }

    public Ball[][] board() {
        return board;
    }

    public int[] next() {
        return next;
    }

    public int score() {
        return score;
    }

    public void start() {
        addBalls();
        addBalls();
    }

    private void newNext() {
        for (int i = 0; i < Config.BALLS; i++) {
            next[i] = rnd.nextInt(Config.COLORS) + 1;
        }
    }

    public boolean addBalls() {
        int addCnt = Math.min(Config.BALLS, freeCnt());

        if (addCnt == 0) {
            return false;
        }

        bot.add(addCnt);
        newNext();
        return !isFull();
    }

    public boolean isFull() {
        return freeCnt() == 0;
    }

    int freeCnt() {
        int count = 0;

        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                if (board[row][col] == null) {
                    count++;
                }
            }
        }

        return count;
    }

    public boolean move(int fr, int fc, int tr, int tc) {
        if (!canMove(fr, fc, tr, tc)) {
            return false;
        }

        moveBall(fr, fc, tr, tc);
        finishMove();
        return true;
    }

    private void moveBall(int fr, int fc, int tr, int tc) {
        board[tr][tc] = board[fr][fc];
        board[fr][fc] = null;
    }

    private void finishMove() {
        if (delLines()) {
            return;
        }

        addBalls();
        delLines();
    }

    public boolean canMove(int fr, int fc, int tr, int tc) {
        if (!inside(fr, fc) || !inside(tr, tc)) {
            return false;
        }

        if (fr != tr && fc != tc) {
            return false;
        }

        if (board[fr][fc] == null || board[tr][tc] != null) {
            return false;
        }

        return clearPath(fr, fc, tr, tc);
    }

    boolean clearPath(int fr, int fc, int tr, int tc) {
        if (fr == tr) {
            return clearRow(fr, fc, tc);
        }

        return clearCol(fc, fr, tr);
    }

    private boolean clearRow(int row, int from, int to) {
        int step = to > from ? 1 : -1;

        for (int col = from + step; col != to; col += step) {
            if (board[row][col] != null) {
                return false;
            }
        }

        return true;
    }

    private boolean clearCol(int col, int from, int to) {
        int step = to > from ? 1 : -1;

        for (int row = from + step; row != to; row += step) {
            if (board[row][col] != null) {
                return false;
            }
        }

        return true;
    }

    public boolean delLines() {
        boolean[][] del = new boolean[Config.SIZE][Config.SIZE];

        if (!markLines(del)) {
            return false;
        }

        int delCnt = removeBalls(del);
        score += delCnt * 2;
        return true;
    }

    private boolean markLines(boolean[][] del) {
        boolean found = false;

        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                if (board[row][col] != null && markCell(del, row, col)) {
                    found = true;
                }
            }
        }

        return found;
    }

    private boolean markCell(boolean[][] del, int row, int col) {
        boolean found = false;
        int color = board[row][col].color();

        for (int dir = 0; dir < dr.length; dir++) {
            if (markLine(del, row, col, color, dir)) {
                found = true;
            }
        }

        return found;
    }

    private boolean markLine(boolean[][] del, int row, int col,
                             int color, int dir) {
        if (!lineStart(row, col, color, dir)) {
            return false;
        }

        int len = lineLen(row, col, color, dir);

        if (len < Config.LINE) {
            return false;
        }

        markCells(del, row, col, dir, len);
        return true;
    }

    private void markCells(boolean[][] del, int row, int col,
                           int dir, int len) {
        for (int i = 0; i < len; i++) {
            int r = row + i * dr[dir];
            int c = col + i * dc[dir];

            del[r][c] = true;
        }
    }

    private int removeBalls(boolean[][] del) {
        int count = 0;

        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                if (del[row][col]) {
                    board[row][col] = null;
                    count++;
                }
            }
        }

        return count;
    }

    int lineLen(int row, int col, int color, int dir) {
        int len = 1;
        row += dr[dir];
        col += dc[dir];

        while (inside(row, col) && board[row][col] != null &&
                board[row][col].color() == color) {
            len++;
            row += dr[dir];
            col += dc[dir];
        }

        return len;
    }

    boolean lineStart(int row, int col, int color, int dir) {
        int r = row - dr[dir];
        int c = col - dc[dir];

        if (!inside(r, c)) {
            return true;
        }

        if (board[r][c] == null) {
            return true;
        }

        return board[r][c].color() != color;
    }

    boolean inside(int row, int col) {
        return row >= 0 && row < Config.SIZE &&
                col >= 0 && col < Config.SIZE;
    }
}