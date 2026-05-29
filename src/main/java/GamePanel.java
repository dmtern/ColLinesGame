import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private Game game;

    private int startRow = -1;
    private int startCol = -1;
    private boolean drag = false;

    public GamePanel(Game game) {
        this.game = game;
        setBackground(new Color(240, 240, 240));
        setPreferredSize(new Dimension(Config.PANEL_W, Config.PANEL_H));

        MouseAdapter mouse = mouse();
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private MouseAdapter mouse() {
        return new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                press(e);
            }

            public void mouseReleased(MouseEvent e) {
                release(e);
            }
        };
    }

    private void press(MouseEvent e) {
        int row = e.getY() / Config.CELL;
        int col = e.getX() / Config.CELL;

        if (inField(row, col) && game.board()[row][col] != null) {
            startRow = row;
            startCol = col;
            drag = true;
            repaint();
        }
    }

    private void release(MouseEvent e) {
        int row = e.getY() / Config.CELL;
        int col = e.getX() / Config.CELL;

        if (drag && inField(row, col)) {
            game.move(startRow, startCol, row, col);
        }

        clearDrag();
        repaint();
        checkEnd();
    }

    private void clearDrag() {
        startRow = -1;
        startCol = -1;
        drag = false;
    }

    private boolean inField(int row, int col) {
        return row >= 0 && row < Config.SIZE &&
                col >= 0 && col < Config.SIZE;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        field(g);
        info(g);
    }

    private void field(Graphics g) {
        for (int row = 0; row < Config.SIZE; row++) {
            for (int col = 0; col < Config.SIZE; col++) {
                cell(g, row, col);
            }
        }
    }

    private void cell(Graphics g, int row, int col) {
        int x = col * Config.CELL;
        int y = row * Config.CELL;

        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(x, y, Config.CELL, Config.CELL);

        if (drag && row == startRow && col == startCol) {
            mark(g, x, y);
        }

        drawCellBall(g, row, col, x, y);
    }

    private void mark(Graphics g, int x, int y) {
        g.setColor(new Color(200, 255, 200));
        g.fillRect(x + 1, y + 1, Config.CELL - 1, Config.CELL - 1);
    }

    private void drawCellBall(Graphics g, int row, int col, int x, int y) {
        Ball ball = game.board()[row][col];

        if (ball != null) {
            ball(g, x, y, Config.CELL, ball.color());
        }
    }

    private void info(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Счёт: " + game.score(), 10, Config.INFO_Y);
        g.drawString("Далее:", 150, Config.INFO_Y);

        nextBalls(g);
        hint(g);
    }

    private void nextBalls(Graphics g) {
        int[] next = game.next();

        for (int i = 0; i < Config.BALLS; i++) {
            int x = Config.NEXT_X + i * (Config.NEXT_SIZE + 5);
            ball(g, x, Config.NEXT_Y, Config.NEXT_SIZE, next[i]);
        }
    }

    private void hint(Graphics g) {
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.setColor(Color.GRAY);
        g.drawString("Зажмите шарик и перетащите", 10, Config.INFO_Y + 20);
    }

    private void ball(Graphics g, int x, int y, int size, int color) {
        Color[] arr = colors();
        int pad = size / 8;

        g.setColor(arr[color]);
        g.fillOval(x + pad, y + pad, size - pad * 2, size - pad * 2);
        g.setColor(Color.DARK_GRAY);
        g.drawOval(x + pad, y + pad, size - pad * 2, size - pad * 2);
    }

    private Color[] colors() {
        return new Color[] {
                null, Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.ORANGE
        };
    }

    private void checkEnd() {
        if (!game.isFull()) {
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Игра окончена!\nВаш счёт: " + game.score(),
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
    }
}