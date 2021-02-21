package ui.frame;

import base.utils.StringUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * self-defined Console in GUI
 * 
 * @Author: DoneEI
 * @Since: 2021/2/17 10:16 下午
 **/
public class Console {
    private static JTextPane console;

    public static void bind(JTextPane textPane) {
        console = textPane;

        console.setEditable(false);
    }

    public static void log(String str) {
        if (console != null && StringUtils.isNotEmpty(str)) {
            insertDocument(str, null);
            console.paintImmediately(console.getBounds());
        }
    }

    public static void error(String error) {
        if (console != null && StringUtils.isNotEmpty(error)) {
            insertDocument(error, new Color(0xFF6B68));
            console.paintImmediately(console.getBounds());
        }
    }

    public static String getExceptionMsg(Exception e) {
        if (e != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(byteArrayOutputStream));

            return byteArrayOutputStream.toString();
        }

        return "";
    }

    private static void insertDocument(String text, Color textColor) {
        SimpleAttributeSet set = new SimpleAttributeSet();

        if (textColor != null) {
            StyleConstants.setForeground(set, textColor);
        }

        StyleConstants.setFontSize(set, 14);
        Document doc = console.getDocument();

        try {
            doc.insertString(doc.getLength(), text + '\n', set);
        } catch (BadLocationException e) {

        }
    }
}
