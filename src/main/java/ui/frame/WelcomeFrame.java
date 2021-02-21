package ui.frame;

import base.exception.AnalyzerException;
import base.utils.StringUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import entity.Project;
import project.ProjectService;
import ui.component.MyButton;
import ui.component.MyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedList;
import java.util.Objects;

/**
 * @Author: DoneEI
 * @Since: 2021/2/16 9:23 下午
 **/
public class WelcomeFrame extends JFrame {
    /**
     * width of this frame
     */
    private static final int width = 800;

    /**
     * height of this frame
     */
    private static final int height = 500;

    /**
     * mark the current history item when the mouse enters
     */
    private JPanel curItem;

    /**
     * history project
     */
    private LinkedList<Project> history;

    /**
     * java swing
     */
    private JPanel historyPanel;
    private JPanel panel1;
    private JComboBox<String> frameworkComboBox;
    private JTextField fileLoc;
    private JButton openFile;
    private JButton cancelButton;
    private JButton createButton;
    private MyPanel first = new MyPanel();
    private JPanel second;
    private MyPanel allPanel = new MyPanel();
    private CardLayout cardLayout = new CardLayout();

    public WelcomeFrame() {
        loadHistory();

        allPanel.setLayout(cardLayout);

        first.add(historyPanel = historyPanel(), BorderLayout.WEST);
        first.add(importPanel(), BorderLayout.EAST);

        allPanel.add(first);
        allPanel.add(second);

        this.add(allPanel);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);

        this.setSize(width, height);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
        this.setTitle("Welcome to SCAAnalyzer!");

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    ProjectService.saveHistoryProjects();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(allPanel, "can not save the application info", "Error!",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(e -> cardLayout.previous(allPanel));

        openFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int res = fileChooser.showSaveDialog(allPanel);

            if (res == JFileChooser.APPROVE_OPTION) {
                fileLoc.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }

        });

        createButton.addActionListener(e -> {
            String filePath = fileLoc.getText();
            String[] data = ((String)Objects.requireNonNull(frameworkComboBox.getSelectedItem())).split("/");

            String owner = data[0];
            String framework = data[1];

            if (!StringUtils.isNotEmpty(filePath)) {
                JOptionPane.showMessageDialog(allPanel, "Location can not be an empty string", "Error!",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String frameworkPath = filePath + File.separator + framework;

            File file = new File(frameworkPath);

            if (file.exists()) {
                JOptionPane.showMessageDialog(allPanel,
                    String.format("The %s is not an empty directory", frameworkPath), "Error!",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            cardLayout.previous(allPanel);

            // start a new project and open a new window
            Project project = ProjectService.createNewProject(owner, framework, frameworkPath);
            openProjectWindow(project);
        });

    }

    private void loadHistory() {
        try {
            this.history = ProjectService.getHistory();
        } catch (AnalyzerException ae) {
            this.history = new LinkedList<>();
            JOptionPane.showMessageDialog(allPanel, ae.getErrorMsg(), "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel importPanel() {
        MyPanel panel = new MyPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        panel.adjustSize(getValue(width, 60), height);

        // icon
        MyButton icon = new MyButton(new ImageIcon("src/main/resources/images/SA.png"));
        gridBagLayout.setConstraints(icon, getGridBagConstraints(1, 0, 2, 1, GridBagConstraints.NORTH));

        // fill blank
        JLabel blank = getBlankLabel();
        gridBagLayout.setConstraints(blank, getGridBagConstraints(1, 2, 2, 4, -1));
        panel.add(blank);

        // tool name
        MyButton toolName = new MyButton("\tSCAAnalyzer");
        toolName.adjustFont(null, -1, 32);
        toolName.setForeground(new Color(0xD2D3CE));
        gridBagLayout.setConstraints(toolName, getGridBagConstraints(1, 6, 2, 1, -1));

        // version
        MyButton version = new MyButton("\tVersion 2021.02");
        version.adjustFont(null, -1, 18);
        version.setForeground(Color.GRAY);
        gridBagLayout.setConstraints(version, getGridBagConstraints(1, 7, 2, 1, -1));

        panel.add(icon);
        panel.add(toolName);
        panel.add(version);

        // fill blank
        blank = getBlankLabel();
        gridBagLayout.setConstraints(blank, getGridBagConstraints(1, 8, 2, 3, -1));
        panel.add(blank);

        // import new project button
        MyButton add = new MyButton(new ImageIcon("src/main/resources/images/add.png"));
        MyButton newProject = new MyButton(" New Project");
        newProject.setHorizontalTextPosition(SwingConstants.LEADING);

        newProject.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                newProject.setForeground(new Color(0xA45C14));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newProject.setForeground(null);
            }
        });

        newProject.addActionListener(e -> cardLayout.next(allPanel));

        JPanel impNew = anonymousPanel(110, 30, null, add, newProject);
        gridBagLayout.setConstraints(impNew, getGridBagConstraints(1, 11, 2, 1, -1));
        panel.add(impNew);

        // open project button
        MyButton file = new MyButton(new ImageIcon("src/main/resources/images/file.png"));
        MyButton openProject = new MyButton(" Open Project");
        openProject.setHorizontalTextPosition(SwingConstants.LEADING);

        openProject.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                openProject.setForeground(new Color(0xA45C14));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                openProject.setForeground(null);
            }
        });

        openProject.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int res = fileChooser.showSaveDialog(allPanel);

            if (res == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();

                try {
                    Project project = ProjectService.openProject(path);
                    openProjectWindow(project);
                } catch (AnalyzerException ae) {
                    JOptionPane.showMessageDialog(allPanel, ae.getErrorMsg(), "Error!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel openOld = anonymousPanel(110, 30, null, file, openProject);
        gridBagLayout.setConstraints(openOld, getGridBagConstraints(1, 13, 2, 1, -1));
        panel.add(openOld);

        // fill blank
        blank = getBlankLabel();
        gridBagLayout.setConstraints(blank, getGridBagConstraints(1, 15, 2, 10, -1));
        panel.add(blank);

        // fill blank
        blank = getBlankLabel();
        gridBagLayout.setConstraints(blank, getGridBagConstraints(1, 25, 2, 3, -1));
        panel.add(blank);

        return panel;
    }

    private JPanel historyPanel() {

        MyPanel panel = new MyPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(new Color(0x393939));

        int itemWidth = getValue(width, 40);
        int itemHeight = getValue(height, 10);

        panel.adjustSize(itemWidth, itemHeight * history.size());

        for (int i = 0; i < history.size(); i++) {
            panel.add(historyItem(history.get(i).getFramework(), itemWidth, itemHeight, i));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.putClientProperty("JScrollBar.showButtons", true);
        scrollPane.putClientProperty("JScrollPane.smoothScrolling", true);
        scrollPane.setPreferredSize(new Dimension(itemWidth, itemHeight * history.size()));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        historyPanel = panel;

        JPanel res = anonymousPanel(itemWidth, height, null, scrollPane);
        res.setOpaque(true);
        res.setBackground(new Color(0x393939));

        return res;
    }

    private MyPanel historyItem(String framework, int width, int height, int bindValue) {
        MyPanel panel = new MyPanel();
        panel.setOpaque(true);
        panel.setBackground(new Color(0x393939));
        panel.adjustSize(width, height);

        MyButton item = new MyButton(framework);
        item.adjustSize(getValue(width, 80), height);
        item.adjustFont(null, -1, 14);
        item.setHorizontalTextPosition(SwingConstants.LEADING);

        MyButton del = new MyButton(new ImageIcon("src/main/resources/images/delete.png"));

        del.adjustSize(getValue(width, 20), height);
        item.setActionCommand(String.valueOf(bindValue));
        del.setActionCommand(String.valueOf(bindValue));

        del.addActionListener(e -> {
            history.remove(Integer.parseInt(e.getActionCommand()));
            historyPanelRefresh();
        });

        del.setVisible(false);

        panel.add(item, BorderLayout.WEST);
        panel.add(del, BorderLayout.EAST);

        // add listener
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEnteredAction(panel, del);
            }
        });

        item.addActionListener(e -> {
            int idx = Integer.parseInt(e.getActionCommand());

            Project project = history.get(idx);
            openProjectWindow(project);
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEnteredAction(panel, del);
            }
        });

        return panel;
    }

    private int getValue(int base, int percent) {
        return base * percent / 100;
    }

    private JPanel anonymousPanel(int width, int height, LayoutManager layoutManager, Component... components) {
        JPanel panel = new JPanel();

        if (layoutManager != null) {
            panel.setLayout(layoutManager);
        }

        for (Component c : components) {
            panel.add(c);
        }

        panel.setPreferredSize(new Dimension(width, height));

        return panel;
    }

    private GridBagConstraints getGridBagConstraints(int x, int y, int width, int height, int anchor) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx = x;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = width;
        gridBagConstraints.gridheight = height;

        if (anchor != -1) {
            gridBagConstraints.anchor = anchor;
        }

        return gridBagConstraints;
    }

    private JLabel getBlankLabel() {
        return new JLabel("               ");
    }

    private void openProjectWindow(Project project) {
        // check whether the project is valid
        File file = new File(project.getFilePath());

        if (!file.exists()) {
            JOptionPane.showMessageDialog(allPanel, "The project does not exist!", "Error!", JOptionPane.ERROR_MESSAGE);
            history.remove(project);
            historyPanelRefresh();

            return;
        }

        WelcomeFrame.this.setVisible(false);
        new MainFrame(project, WelcomeFrame.this);
    }

    public void historyPanelRefresh() {
        first.remove(historyPanel);
        first.add(historyPanel = historyPanel(), BorderLayout.WEST);
        first.validate();
    }

    private void mouseEnteredAction(JPanel panel, MyButton button) {
        // reset last history item
        if (curItem != null) {
            curItem.getComponent(1).setVisible(false);
            curItem.setBackground(new Color(0x393939));
        }

        // set the current history item
        curItem = panel;

        button.setVisible(true);
        panel.setBackground(new Color(0x454545));
    }

    /**
     * the following code is generated by IntelliJ IDEA GUI Designer
     */
    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your
     * code!
     * 
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        second = new JPanel();
        second.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        second.add(panel1,
            new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                new Dimension(400, 50), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Location");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Framework");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label2, gbc);
        frameworkComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("microsoft/CNTK");
        defaultComboBoxModel1.addElement("keras-team/keras");
        defaultComboBoxModel1.addElement("apache/incubator-mxnet");
        defaultComboBoxModel1.addElement("eclipse/deeplearning4j");
        defaultComboBoxModel1.addElement("PaddlePaddle/Paddle");
        defaultComboBoxModel1.addElement("BVLC/caffe");
        defaultComboBoxModel1.addElement("tensorflow/tensorflow");
        defaultComboBoxModel1.addElement("pytorch/pytorch");
        defaultComboBoxModel1.addElement("Theano/Theano");
        frameworkComboBox.setModel(defaultComboBoxModel1);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 100;
        panel1.add(frameworkComboBox, gbc);
        fileLoc = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        panel1.add(fileLoc, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer3, gbc);
        openFile = new JButton();
        openFile.setRequestFocusEnabled(false);
        openFile.setSelected(false);
        openFile.setText("...");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(openFile, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer6, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        second.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        panel2.add(cancelButton,
            new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel2.add(spacer7, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel2.add(spacer8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        createButton = new JButton();
        createButton.setText("Create");
        panel2.add(createButton,
            new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel2.add(spacer9, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() {
        return second;
    }

}
