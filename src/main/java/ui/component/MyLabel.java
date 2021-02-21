package ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * @Author: DoneEI
 * @Since: 2021/2/12 1:56 AM
 **/
public class MyLabel extends JLabel {

    public MyLabel(String text, Font font) {
        super(text);
        setFont(font);
    }

    public MyLabel(String text, int width, int height) {
        super(text);
        adjustSize(width, height);
    }

    public MyLabel(String text) {
        super(text);
    }

    public MyLabel(Icon image) {
        super(image);
    }

    public void adjustSize(int width, int height) {
        this.setPreferredSize(new Dimension(width, height));
    }

}
