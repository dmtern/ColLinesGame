public class Ball {
    private int color;

    public Ball(int color) {
        if (color < 1 || color > Config.COLORS) {
            color = 1;
        }

        this.color = color;
    }

    public int color() {
        return color;
    }
}