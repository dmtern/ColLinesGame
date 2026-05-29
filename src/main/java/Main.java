import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String[] levels = {"Лёгкий", "Средний", "Сложный"};
        int choice = JOptionPane.showOptionDialog(null, "Выберите уровень сложности:",
                "Color Lines", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, levels, levels[1]);

        if (choice == -1) System.exit(0);
        Config.level = choice + 1;

        Game game = new Game();
        GamePanel panel = new GamePanel(game);
        JFrame frame = new JFrame("Color Lines");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        game.start();
        panel.repaint();
        frame.setVisible(true);
    }
}