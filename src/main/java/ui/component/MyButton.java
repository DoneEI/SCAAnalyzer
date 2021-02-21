package ui.component;

import base.utils.StringUtils;

import javax.swing.*;
import java.awt.*;

/**
 * self-defined button
 * 
 * @Author: DoneEI
 * @Since: 2021/2/11 11:54 PM
 **/
public class MyButton extends JButton {

    public MyButton() {
        super();
    }

    public MyButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorder(null);
        setOpaque(false);
        setContentAreaFilled(false);
        setHorizontalTextPosition(SwingConstants.CENTER);
    }

    public MyButton(ImageIcon icon) {

        this.setIcon(icon);

        setBorder(null);
        setOpaque(false);
        setContentAreaFilled(false);
    }

    public MyButton(ImageIcon icon, int width, int height) {

        this.setIcon(adjustIconSize(icon, width, height));

        setBorder(null);
        setOpaque(false);
        setContentAreaFilled(false);
    }

    public void adjustSize(int width, int height) {
        this.setPreferredSize(new Dimension(width, height));
    }

    public ImageIcon adjustIconSize(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);

        return new ImageIcon(newImg);
    }

    public void adjustFont(String name, int style, int size) {
        Font font = getFont();

        setFont(new Font(StringUtils.isNotEmpty(name) ? name : font.getName(), style == -1 ? font.getStyle() : style,
            size == -1 ? font.getSize() : size));
    }
}
