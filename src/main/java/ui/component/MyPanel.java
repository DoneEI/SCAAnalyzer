package ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * @Author: DoneEI
 * @Since: 2021/2/12 12:24 AM
 **/
public class MyPanel extends JPanel {
    private Image img;

    public MyPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    public MyPanel(LayoutManager layoutManager) {
        setLayout(layoutManager);
        setOpaque(false);
    }

    public void setBgImage(Image img) {
        this.img = img;
        repaint();
    }

    public void adjustSize(int width, int height) {
        this.setPreferredSize(new Dimension(width, height));
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}
