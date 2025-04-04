package tk.zhangmz;

import javax.swing.*;
import java.awt.*;

public class CellScreen extends JPanel {
    private boolean[][] pixels;
    private final int size;

    public CellScreen(int size) {
        super();
        this.size = size;
        this.pixels = new boolean[size][size];
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int width = getWidth();
        int height = getHeight();
        int xPixel = width / size;
        int yPixel = height / size;
        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[y].length; x++) {
                boolean b = pixels[y][x];
                if (b) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(x * xPixel, y * yPixel, xPixel, yPixel);
            }
        }
    }

    public boolean[][] getPixels() {
        return pixels;
    }

    public void setPixels(boolean[][] pixels) {
        this.pixels = pixels;
    }

    public int getScreenSize() {
        return size;
    }
}
