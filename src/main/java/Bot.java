import java.util.Random;

public class Bot {
    private Ball[][] board;
    private int[] next;
    private int addCnt;
    private Random rnd;

    private boolean blockTurn = true;
    private boolean[] used = new boolean[Config.BALLS];

    private LineInfo[] near = new LineInfo[Config.SIZE * Config.SIZE * 8];
    private int nearCnt;

    private int[] dr = {0, 1, 1, 1};
    private int[] dc = {1, 0, 1, -1};

    public Bot(Ball[][] board, int[] next, Random rnd) {
        this.board = board;
        this.next = next;
        this.rnd = rnd;
    }

    public void add(int addCnt) {
        this.addCnt = addCnt;
        clearUsed();

        if (Config.level == Config.EASY) {
            addEasy();
        } else if (Config.level == Config.HARD) {
            addHard();
        } else {
            addMid();
        }
    }

    private void addMid() {
        for (int i = 0; i < addCnt; i++) {
            putRand(next[i]);
            used[i] = true;
        }
    }

    private void addEasy() {
        findNear();

        while (helpLine()) {
            findNear();
        }

        putLeft();
    }

    private void addHard() {
        findNear();

        if (blockTurn) {
            if (blockLine()) {
                blockTurn = false;
            }
        } else {
            blockTurn = true;
        }

        putLeft();
    }

    private void clearUsed() {
        for (int i = 0; i < Config.BALLS; i++) {
            used[i] = false;
        }
    }

    private void putLeft() {
        for (int i = 0; i < addCnt; i++) {
            if (!used[i]) {
                putRand(next[i]);
                used[i] = true;
            }
        }
    }

    private void putRand(int color) {
        int target = rnd.nextInt(emptyCnt());
        int cur = 0;

        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                if (board[row][col] == null && cur++ == target) {
                    board[row][col] = new Ball(color);
                    return;
                }
            }
        }
    }

    private int emptyCnt() {
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

    // лёгкий уровень: пытаемся продолжить линии игрока
    private boolean helpLine() {
        for (int len = 4; len >= 3; len--) {
            for (int i = 0; i < nearCnt; i++) {
                if (near[i].len != len) {
                    continue;
                }

                int id = sameId(near[i].color);

                if (id != -1) {
                    putNear(i, id, near[i].color);
                    return true;
                }
            }
        }

        return false;
    }

    // сложный уровень: через ход пытаемся закрыть линию другим цветом
    private boolean blockLine() {
        for (int len = 4; len >= 3; len--) {
            int count = blockCount(len);

            if (count > 0) {
                return blockOne(len, rnd.nextInt(count));
            }
        }

        return false;
    }

    private int blockCount(int len) {
        int count = 0;

        for (int i = 0; i < nearCnt; i++) {
            if (near[i].len == len && diffId(near[i].color) != -1) {
                count++;
            }
        }

        return count;
    }

    private boolean blockOne(int len, int num) {
        int cur = 0;

        for (int i = 0; i < nearCnt; i++) {
            if (near[i].len != len || diffId(near[i].color) == -1) {
                continue;
            }

            if (cur++ == num) {
                int id = diffId(near[i].color);
                putNear(i, id, next[id]);
                return true;
            }
        }

        return false;
    }

    private void putNear(int nearId, int nextId, int color) {
        int row = near[nearId].row;
        int col = near[nearId].col;

        if (board[row][col] == null) {
            board[row][col] = new Ball(color);
            used[nextId] = true;
        }
    }

    private int sameId(int color) {
        for (int i = 0; i < addCnt; i++) {
            if (!used[i] && next[i] == color) {
                return i;
            }
        }

        return -1;
    }

    private int diffId(int color) {
        for (int i = 0; i < addCnt; i++) {
            if (!used[i] && next[i] != color) {
                return i;
            }
        }

        return -1;
    }

    // поиск мест рядом с линиями из 3-4 шариков
    private void findNear() {
        nearCnt = 0;

        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                if (board[row][col] != null) {
                    scanNear(row, col);
                }
            }
        }
    }

    private void scanNear(int row, int col) {
        int color = board[row][col].color();

        for (int dir = 0; dir < dr.length; dir++) {
            if (!lineStart(row, col, color, dir)) {
                continue;
            }

            int len = lineLen(row, col, color, dir);

            if (len >= 3 && len <= 4) {
                addNear(row + len * dr[dir], col + len * dc[dir], color, len);
                addNear(row - dr[dir], col - dc[dir], color, len);
            }
        }
    }

    private void addNear(int row, int col, int color, int len) {
        if (!inside(row, col) || board[row][col] != null) {
            return;
        }

        if (hasNear(row, col, color, len)) {
            return;
        }

        near[nearCnt++] = new LineInfo(row, col, color, len);
    }

    private boolean hasNear(int row, int col, int color, int len) {
        for (int i = 0; i < nearCnt; i++) {
            if (near[i].row == row && near[i].col == col &&
                    near[i].color == color && near[i].len == len) {
                return true;
            }
        }

        return false;
    }

    private int lineLen(int row, int col, int color, int dir) {
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

    private boolean lineStart(int row, int col, int color, int dir) {
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

    private boolean inside(int row, int col) {
        return row >= 0 && row < Config.SIZE &&
                col >= 0 && col < Config.SIZE;
    }
}