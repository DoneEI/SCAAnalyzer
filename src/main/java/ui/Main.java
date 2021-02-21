package ui;

import base.exception.AnalyzerException;
import com.formdev.flatlaf.FlatDarkLaf;
import ui.frame.WelcomeFrame;

import javax.swing.*;

/**
 * Main class
 *
 * @Author: DoneEI
 * @Since: 2021/2/11 11:12 PM
 **/
public class Main {
    public static void main(String[] args) {
        try {
            FlatDarkLaf.install();
            UIManager.setLookAndFeel(new FlatDarkLaf());

            new WelcomeFrame();

        } catch (AnalyzerException ae) {
            System.out.println(ae.getErrorMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
