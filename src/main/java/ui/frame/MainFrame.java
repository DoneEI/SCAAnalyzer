package ui.frame;

import base.enums.SCAEnums;
import base.exception.AnalyzerException;
import base.utils.ExcelUtils;
import base.utils.FileUtils;
import base.utils.StringUtils;
import core.analysis.SCASimilarity;
import core.download.TagCrawl;
import core.download.TagFileDownload;
import core.extraction.SCADetector;
import entity.Project;
import entity.SCA;
import entity.Tag;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import project.ProjectService;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * @Author: DoneEI
 * @Since: 2021/2/17 8:03 下午
 **/
public class MainFrame extends JFrame {
    /**
     * width of this frame
     */
    private static final int width = 1200;

    /**
     * height of this frame
     */
    private static final int height = 750;

    /**
     * the current project
     */
    private Project project;

    /**
     * to mark whether there are tasks working
     */
    private Map<Integer, SwingWorker> workManager;

    /**
     * java swing components
     */
    private WelcomeFrame parent;
    private JPanel panel1;
    private JTree projectJTree;
    private JTabbedPane tabbedPane1;
    private JButton downloadButton;
    private JTable downloadTagTable;
    private JScrollPane projectJScrollPane;
    private JPanel dataDownloadPanel;
    private JButton refreshButton;
    private JScrollPane downloadScrollPane;
    private JTable extractTagTable;
    private JTextPane console;
    private JButton extractButton;
    private JButton importButton;
    private JButton extractRefresh;
    private JButton exportButton;
    private JScrollPane extractScrollPane;
    private JButton downloadImportButton;
    private JPanel dataAnalysisPanel;
    private JComboBox<String> sourceComboBox;
    private JComboBox<String> targetComboBox;
    private JButton analysisButton;
    private JButton analysisRefresh;
    private JLabel analysisTipLabel;

    public MainFrame(Project project, WelcomeFrame parent) {

        this.workManager = new HashMap<>();
        this.parent = parent;
        this.project = project;
        ProjectService.openProject(project);

        setComponents();
        createListener();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);

        this.setSize(width, height);
        this.setFocusable(false);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.add(panel1);
        this.setVisible(true);

        initDownloadTagTable();
        initExtractTagTableTable();
        initAnalysisData();
    }

    private void setComponents() {
        Console.bind(console);

        // root directory
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(new File(project.getFilePath())));
        getTreeChildNode(root);
        projectJTree.setModel(new DefaultTreeModel(root));
        projectJTree.collapseRow(0);

        tabbedPane1.putClientProperty("JTabbedPane.tabClosable", true);

        tabbedPane1.putClientProperty("JTabbedPane.tabCloseCallback", (IntConsumer)tabIndex -> {
            // close tab
            if (tabIndex >= 3) {
                tabbedPane1.removeTabAt(tabIndex);
            } else {
                JOptionPane.showMessageDialog(panel1, "The first three windows are reserved by default");
            }
        });

        downloadScrollPane.putClientProperty("JScrollPane.smoothScrolling", true);
        extractScrollPane.putClientProperty("JScrollPane.smoothScrolling", true);

        Font f = analysisTipLabel.getFont();
        analysisTipLabel.setFont(new Font(f.getName(), f.getStyle(), 18));
    }

    private void initDownloadTagTable() {
        Vector<Object> tagData = new Vector<>();

        Vector<String> header = new Vector<>(Arrays.asList("Tag", "Path", "Choose"));

        DefaultTableModel downloadTagTableModel = new DefaultTableModel(tagData, header) {
            Class<?>[] columnTypes = new Class[] {String.class, String.class, Boolean.class};
            boolean[] columnEditable = new boolean[] {false, false, true};

            public Class<?> getColumnClass(int columnIndex) {
                return this.columnTypes[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return this.columnEditable[columnIndex];
            }
        };

        Map<String, Tag> tagMap = project.getTags();

        if (tagMap != null) {
            for (String t : tagMap.keySet()) {
                String path = "";

                if (StringUtils.isNotEmpty(tagMap.get(t).getTagFilePath())) {
                    path = FileUtils.getRelativePath(tagMap.get(t).getTagFilePath(), project.getFilePath());
                }

                tagData.add(new Vector<>(Arrays.asList(t, path, false)));
            }

        }

        downloadTagTableModel.setDataVector(tagData, header);
        refreshTable(downloadTagTableModel, downloadTagTable);
    }

    private void initExtractTagTableTable() {
        Vector<Object> tagData = new Vector<>();

        Vector<String> header = new Vector<>(Arrays.asList("Tag", "SCA", "Choose"));

        DefaultTableModel extractTagTableModel = new DefaultTableModel(tagData, header) {
            Class<?>[] columnTypes = new Class[] {String.class, String.class, Boolean.class};
            boolean[] columnEditable = new boolean[] {false, false, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return this.columnEditable[columnIndex];
            }

            public Class<?> getColumnClass(int columnIndex) {
                return this.columnTypes[columnIndex];
            }
        };

        Map<String, Tag> tagMap = project.getTags();

        if (tagMap != null) {
            for (String t : tagMap.keySet()) {
                Tag tag = tagMap.get(t);

                if (StringUtils.isNotEmpty(tag.getTagFilePath())) {
                    String sca = "";

                    if (tag.getAssumptions() != null) {
                        sca = "Double click for Show";
                    }

                    tagData.add(new Vector<>(Arrays.asList(t, sca, false)));
                }

            }
        }

        extractTagTableModel.setDataVector(tagData, header);
        refreshTable(extractTagTableModel, extractTagTable);
    }

    private void refreshTable(DefaultTableModel model, JTable table) {
        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFocusable(false);
        table.setPreferredSize(new Dimension(table.getWidth(), table.getRowHeight() * model.getDataVector().size()));

        // cell rendered
        DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
        tcr.setHorizontalAlignment(JLabel.CENTER);
        tcr.setBorder(null);

        table.setDefaultRenderer(Object.class, tcr);

        table.updateUI();
    }

    private void initAnalysisData() {
        sourceComboBox.removeAllItems();
        targetComboBox.removeAllItems();

        for (Tag tag : project.getTags(true).values()) {
            if (tag.getAssumptions() != null && tag.getAssumptions().size() > 0) {
                sourceComboBox.addItem(tag.getVersion());
                targetComboBox.addItem(tag.getVersion());
            }
        }
    }

    private void createListener() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                panel1.requestFocus();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (workManager.size() != 0) {
                        if (JOptionPane.showConfirmDialog(panel1, "There may be task running, would you like to stop?",
                            "Confirm", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {

                            // end all tasks
                            for (SwingWorker worker : workManager.values()) {
                                if (!worker.isCancelled()) {
                                    worker.cancel(true);
                                }
                            }
                        } else {
                            return;
                        }
                    }

                    ProjectService.closeProject(project);
                    MainFrame.this.dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel1, "can not save the project info when closing project",
                        "Error!", JOptionPane.ERROR_MESSAGE);
                }

                parent.setVisible(true);
                parent.historyPanelRefresh();
            }
        });

        projectJTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = projectJTree.getPathForLocation(e.getX(), e.getY());

                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();

                        File f = ((FileNode)node.getUserObject()).getFile();

                        if (f.isFile()) {
                            openFileTab(f.getAbsolutePath(), 0);
                        }

                    }

                }
            }
        });

        projectJTree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                // double check
                TreePath path = event.getPath();

                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();

                // check whether the children of current node have their children
                Enumeration children = node.children();

                while (children.hasMoreElements()) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
                    if (child.isLeaf()) {
                        getTreeChildNode(child);
                    }
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {

            }
        });

        refreshButton.addActionListener(e -> {

            int key = getWorkerKey();

            SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    publish("log", "Start to update tags of this framework");

                    try {
                        List<String> tags = TagCrawl.getTags(project.getOwner(), project.getFramework());

                        System.out.println("ok!");

                        Map<String, Tag> newMap = new LinkedHashMap<>();
                        Map<String, Tag> oldMap = null;

                        if (project.getTags() != null) {
                            oldMap = project.getTags();
                        }

                        for (String t : tags) {
                            if (oldMap == null || !oldMap.containsKey(t)) {
                                newMap.put(t, new Tag(t));
                            } else {
                                newMap.put(t, oldMap.get(t));
                            }
                        }

                        project.setTags(newMap);

                        return true;
                    } catch (AnalyzerException ae) {
                        publish("error", String.format("update tags of framework: %s failed!", project.getFramework()));
                        JOptionPane.showMessageDialog(panel1, ae.getErrorMsg(), "Error!", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e) {
                        publish("error", String.format("update tags of framework: %s failed!", project.getFramework()));
                        publish("error", Console.getExceptionMsg(e));
                        JOptionPane.showMessageDialog(panel1, "System Error! See console log", "Error!",
                            JOptionPane.ERROR_MESSAGE);
                    }

                    return false;
                }

                @Override
                protected void process(List<String> chunks) {
                    swingWorkerIO(chunks);
                }

                @Override
                protected void done() {
                    if (isDone()) {
                        try {
                            if (get()) {
                                // update table
                                initDownloadTagTable();
                                Console.log("Refresh Success!");
                            }
                        } catch (Exception e) {
                            Console
                                .error(String.format("update tags of framework: %s failed!", project.getFramework()));
                        } finally {
                            // release key
                            workManager.remove(key);
                        }

                    }
                }
            };

            registerWorker(key, worker);
        });

        downloadImportButton.addActionListener(e -> {
            List<Integer> toDownloadImport = getSelectedRows(downloadTagTable);

            if (toDownloadImport.size() > 1) {
                JOptionPane.showMessageDialog(panel1, "import only support one file for each time", "Error!",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            int key = getWorkerKey();

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    JFileChooser fileChooser = new JFileChooser();

                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    int res = fileChooser.showSaveDialog(panel1);

                    if (res == JFileChooser.APPROVE_OPTION) {
                        return fileChooser.getSelectedFile().getAbsolutePath();
                    }

                    return null;
                }

                @Override
                protected void done() {
                    if (isDone()) {
                        try {
                            if (get() != null) {
                                int row = toDownloadImport.get(0);
                                String tag = (String)downloadTagTable.getValueAt(row, 0);
                                Tag t = project.getTags().get(tag);

                                t.setTagFilePath(get());

                                downloadTagTable.setValueAt(
                                    FileUtils.getRelativePath(t.getTagFilePath(), project.getFilePath()), row, 1);
                                downloadTagTable.updateUI();

                                Console.log("Import file success!");
                                JOptionPane.showMessageDialog(panel1, "Import file success!", "Tip",
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception e) {
                            Console.log("Import file failed!");
                            JOptionPane.showMessageDialog(panel1, "Import file failed!", "Tip",
                                JOptionPane.ERROR_MESSAGE);
                        } finally {
                            // release key
                            workManager.remove(key);
                        }
                    }
                }
            };

            registerWorker(key, worker);

        });

        downloadButton.addActionListener(e -> {
            List<Integer> toDownload = getSelectedRows(downloadTagTable);

            for (Integer row : toDownload) {
                int key = getWorkerKey();

                SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
                    @Override
                    protected Boolean doInBackground() {
                        publish("log", "Start to download file, total: " + toDownload.size());

                        String saveDir = project.getFilePath() + File.separator + "tags" + File.separator;

                        String tag = (String)downloadTagTable.getValueAt(row, 0);

                        try {
                            publish("log", String.format("Downloading tag: %s", tag));

                            String filePath = saveDir + tag;

                            filePath =
                                TagFileDownload.download(project.getOwner(), project.getFramework(), tag, filePath);

                            Tag t = project.getTags(true).getOrDefault(tag, new Tag(tag));

                            t.setTagFilePath(filePath);

                            project.getTags().put(tag, t);

                            publish("log", String.format("Download tag: %s over!", tag));
                            return true;
                        } catch (Exception e1) {
                            publish("error",
                                String.format("Download Failed, framework: %s, tag: %s", project.getFramework(), tag));
                        }

                        return false;
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        swingWorkerIO(chunks);
                    }

                    @Override
                    protected void done() {
                        if (isDone()) {
                            String tag = (String)downloadTagTable.getValueAt(row, 0);

                            try {
                                if (get()) {
                                    String path = FileUtils.getRelativePath(project.getTags().get(tag).getTagFilePath(),
                                        project.getFilePath());

                                    downloadTagTable.setValueAt(path, row, 1);
                                    downloadTagTable.updateUI();

                                    Console.log(String.format("Download tag: %s over!", tag));
                                }
                            } catch (Exception e) {
                                Console.error(String.format("Download tag: %s failed!", tag));
                                Console.error(Console.getExceptionMsg(e));
                            } finally {
                                // release key
                                workManager.remove(key);
                            }
                        }
                    }
                };

                registerWorker(key, worker);
            }

        });

        extractRefresh.addActionListener(e -> initExtractTagTableTable());

        extractButton.addActionListener(e -> {
            List<Integer> toExtract = getSelectedRows(extractTagTable);

            for (Integer row : toExtract) {
                String tag = (String)extractTagTable.getValueAt(row, 0);
                Tag t = project.getTags().get(tag);

                int key = getWorkerKey();

                SwingWorker<Map<String, List>, String> worker = new SwingWorker<Map<String, List>, String>() {
                    long startTime, endTime;

                    @Override
                    protected Map<String, List> doInBackground() throws Exception {

                        try {
                            publish("log", String.format("Start to extract assumptions from framework: %s, tag: %s",
                                project.getFramework(), tag));

                            startTime = System.currentTimeMillis();
                            SCADetector scaDetector = new SCADetector(project.getFramework(), tag, t.getTagFilePath());
                            endTime = System.currentTimeMillis();

                            return scaDetector.detect();
                        } catch (AnalyzerException ae) {
                            publish("error", String.format("Failed to extract assumptions from framework: %s, tag: %s",
                                project.getFramework(), tag));
                            JOptionPane.showMessageDialog(panel1, ae.getErrorMsg(), "Error!",
                                JOptionPane.ERROR_MESSAGE);
                        } catch (Exception e) {
                            publish("error", String.format("Failed to extract assumptions from framework: %s, tag: %s",
                                project.getFramework(), tag));
                            publish("error", Console.getExceptionMsg(e));
                            JOptionPane.showMessageDialog(panel1, "System Error! See console log", "Error!",
                                JOptionPane.ERROR_MESSAGE);
                        } finally {
                            // release key
                            workManager.remove(key);
                        }

                        return null;
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        swingWorkerIO(chunks);
                    }

                    @Override
                    protected void done() {
                        if (isDone()) {
                            try {
                                Map<String, List> res = get();

                                // report results
                                Map<String, Set<String>> count = new HashMap<>(8);

                                Console.log(String.format("The extraction results of framework: %s, tag: %s",
                                    project.getFramework(), tag));

                                Console.log(String.format("The cost time: %.2f s", (endTime - startTime) / 1000.0));

                                for (Object obj : res.get("SCA")) {
                                    SCA sca = (SCA)obj;

                                    Set<String> c = count.getOrDefault(sca.getKeyword(), new HashSet<>());

                                    c.add(sca.getFilePath() + "(" + sca.getLine() + ")");

                                    count.put(sca.getKeyword(), c);
                                }

                                for (String key : count.keySet()) {
                                    Console.log(key + " : " + count.get(key).size());
                                }

                                Console.log(String.format("The number of unhandled lines: %d",
                                    res.get("unhandledLines").size()));

                                // update
                                t.setAssumptions(res.get("SCA"));
                                t.setUnhandledLines(res.get("unhandledLines"));

                                extractTagTable.setValueAt("Double click for Show", row, 1);
                                extractTagTable.updateUI();
                            } catch (Exception e) {
                                publish("error",
                                    String.format("Failed to extract assumptions from framework: %s, tag: %s",
                                        project.getFramework(), tag));
                                publish("error", Console.getExceptionMsg(e));
                            }
                        }
                    }
                };

                registerWorker(key, worker);
            }
        });

        exportButton.addActionListener(e -> {
            List<Integer> toExport = getSelectedRows(extractTagTable);

            for (Integer row : toExport) {
                int key = getWorkerKey();

                String tag = (String)extractTagTable.getValueAt(row, 0);

                SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {

                        // we assume that the tag is not null
                        Tag t = project.getTags().get(tag);

                        String path = project.getFilePath() + File.separator + "tags" + File.separator + tag;
                        String fileName = project.getFramework() + "-" + tag + ".xlsx";

                        List<SCA> fromSourceCode = new ArrayList<>();
                        List<SCA> fromOthers = new ArrayList<>();

                        for (SCA sca : t.getAssumptions()) {
                            if (StringUtils.equal(sca.getType(), SCAEnums.SOURCE_CODE_COMMENT.getCode())) {
                                fromSourceCode.add(sca);
                            } else {
                                fromOthers.add(sca);
                            }
                        }

                        try {
                            ExcelUtils.simpleWrite(path, fileName, new Class[] {SCA.class, SCA.class, String.class},
                                new String[] {SCAEnums.SOURCE_CODE_COMMENT.getCode(),
                                    SCAEnums.NON_SOURCE_CODE_COMMENT.getCode(), "unhandledLines"},
                                fromSourceCode, fromOthers);

                            return true;
                        } catch (Exception e) {
                            publish("error", "failed to export assumptions of tag: " + tag);
                            publish("error", Console.getExceptionMsg(e));
                        }

                        return false;
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        swingWorkerIO(chunks);
                    }

                    @Override
                    protected void done() {
                        if (isDone()) {
                            try {
                                if (get()) {
                                    Console.log("Successfully export assumptions of tag: " + tag);
                                }
                            } catch (Exception e) {
                                Console.error("failed to export assumptions of tag: " + tag);
                            } finally {
                                // release key
                                workManager.remove(key);
                            }

                        }
                    }
                };

                registerWorker(key, worker);
            }

        });

        importButton.addActionListener(e -> {
            List<Integer> toImport = getSelectedRows(extractTagTable);

            if (toImport.size() > 1) {
                JOptionPane.showMessageDialog(panel1, "import only support one file for each time", "Error!",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            int row = toImport.get(0);
            String tag = (String)extractTagTable.getValueAt(row, 0);
            int key = getWorkerKey();

            SwingWorker<List<SCA>, Void> worker = new SwingWorker<List<SCA>, Void>() {
                @Override
                protected List<SCA> doInBackground() {
                    JFileChooser fileChooser = new JFileChooser();

                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                    int res = fileChooser.showSaveDialog(panel1);

                    if (res == JFileChooser.APPROVE_OPTION) {
                        String path = fileChooser.getSelectedFile().getAbsolutePath();

                        if (!path.endsWith(".xlsx")) {
                            JOptionPane.showMessageDialog(panel1, "import only support excel files", "Error!",
                                JOptionPane.ERROR_MESSAGE);

                            return null;
                        }

                        // for now, we only read SCAs from the first sheet of the excel file
                        return ExcelUtils.simpleRead(path, 0, SCA.class);
                    }

                    return null;
                }

                @Override
                protected void done() {
                    if (isDone()) {
                        try {
                            Tag t = project.getTags().get(tag);

                            t.setAssumptions(get());
                            t.setUnhandledLines(null);

                            Console.log("Import assumptions success!");
                            JOptionPane.showMessageDialog(panel1, "Import assumptions success!", "Tip",
                                JOptionPane.INFORMATION_MESSAGE);

                        } catch (Exception e) {
                            Console.log("Import assumptions failed!");
                            JOptionPane.showMessageDialog(panel1, "Import assumptions failed!", "Tip",
                                JOptionPane.ERROR_MESSAGE);
                        } finally {
                            // release key
                            workManager.remove(key);
                        }
                    }
                }
            };

            registerWorker(key, worker);
        });

        extractTagTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // double click
                if (e.getClickCount() == 2) {
                    int col = extractTagTable.columnAtPoint(e.getPoint());

                    if (col == 1) {
                        int row = extractTagTable.rowAtPoint(e.getPoint());

                        String tag = (String)extractTagTable.getValueAt(row, 0);

                        int idx = tabbedPane1.indexOfTab(tag);

                        if (idx == -1) {
                            openSCATab(project.getTags().get(tag));
                        }
                    }
                }

            }
        });

        analysisRefresh.addActionListener(e -> {
            initAnalysisData();
        });

        analysisButton.addActionListener(e -> {
            String s = (String)sourceComboBox.getSelectedItem();
            String t = (String)targetComboBox.getSelectedItem();

            if (project.getTags() != null) {
                Tag source = project.getTags().getOrDefault(s, null);

                Tag target = project.getTags().getOrDefault(t, null);

                if (source != null && source.getAssumptions() != null && target != null
                    && target.getAssumptions() != null) {

                    int key = getWorkerKey();

                    SwingWorker<String, String> worker = new SwingWorker<String, String>() {
                        @Override
                        protected String doInBackground() {
                            publish("log", String.format("start to analyze assumptions between %s and %s",
                                source.getVersion(), target.getVersion()));

                            List<String> res = SCASimilarity.calculateSimilarity(source.getVersion(),
                                target.getVersion(), source.getAssumptions(), target.getAssumptions());

                            String filePath =
                                project.getFilePath() + File.separator + "tags" + File.separator + source.getVersion()
                                    + File.separator + String.format("similar assumptions between %s and %s.txt",
                                        source.getVersion(), target.getVersion());

                            try {
                                FileUtils.writeFile(filePath, res);

                                return filePath;
                            } catch (IOException ioe) {
                                publish("error", String.format("Failed to get similarity results between %s and %s",
                                    source.getVersion(), target.getVersion()));
                                publish("error", Console.getExceptionMsg(ioe));
                            }

                            return null;
                        }

                        @Override
                        protected void process(List<String> chunks) {
                            swingWorkerIO(chunks);
                        }

                        @Override
                        protected void done() {
                            if (isDone()) {
                                try {
                                    String path = get();

                                    openFileTab(path, 0);

                                } catch (Exception e) {
                                    publish("error", String.format("Failed to get similarity results between %s and %s",
                                        source.getVersion(), target.getVersion()));
                                    publish("error", Console.getExceptionMsg(e));
                                } finally {
                                    // release key
                                    workManager.remove(key);
                                }
                            }
                        }
                    };

                    registerWorker(key, worker);
                }
            }
        });
    }

    private void openSCATab(Tag tag) {
        JPanel panel = new JPanel(new GridLayout(1, 1));

        JScrollPane scrollPane = new JScrollPane();

        Vector<Object> SCAData = new Vector<>();

        Vector<String> header = new Vector<>(Arrays.asList("File", "Line", "Term", "Source", "Assumption"));

        DefaultTableModel model = new DefaultTableModel(SCAData, header) {
            Class<?>[] columnTypes = new Class[] {String.class, String.class, String.class, String.class, String.class};
            boolean[] columnEditable = new boolean[] {false, false, false, false, false};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return this.columnEditable[columnIndex];
            }

            public Class<?> getColumnClass(int columnIndex) {
                return this.columnTypes[columnIndex];
            }
        };

        if (tag.getAssumptions() != null) {
            for (SCA sca : tag.getAssumptions()) {
                SCAData.add(new Vector<>(Arrays.asList(sca.getFilePath(), sca.getLine().toString(), sca.getKeyword(),
                    sca.getType(), sca.getContent())));
            }

        }

        model.setDataVector(SCAData, header);

        JTable table = new JTable();
        table.setRequestFocusEnabled(false);
        table.setPreferredSize(new Dimension(2000, table.getHeight()));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshTable(model, table);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // double click
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());

                    String filePath = tag.getTagFilePath() + File.separator + table.getValueAt(row, 0);
                    int line = Integer.parseInt((String)table.getValueAt(row, 1));

                    openFileTab(filePath, line);
                }
            }
        });

        scrollPane.setPreferredSize(new Dimension(extractScrollPane.getWidth(), extractScrollPane.getHeight()));
        scrollPane.setViewportView(table);
        scrollPane.setRequestFocusEnabled(false);
        scrollPane.putClientProperty("JScrollPane.smoothScrolling", true);

        panel.add(scrollPane);
        panel.setPreferredSize(new Dimension(dataDownloadPanel.getWidth(), dataDownloadPanel.getHeight()));
        panel.setRequestFocusEnabled(false);

        tabbedPane1.addTab(tag.getVersion(), panel);
        tabbedPane1.setSelectedIndex(tabbedPane1.getTabCount() - 1);
    }

    private void openFileTab(String filePath, int line) {
        // check whether line is valid
        line = Math.max(line, 0);

        RSyntaxTextArea textPane = new RSyntaxTextArea();
        textPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        textPane.setBackground(new Color(0x2b2b2b));
        textPane.setForeground(new Color(0xa9b7c6));
        textPane.setCurrentLineHighlightColor(new Color(0x2b2b2b));
        textPane.setRequestFocusEnabled(true);

        RTextScrollPane scrollPane = new RTextScrollPane(textPane, true, new Color(0xa4a3a3));
        scrollPane.getGutter().setBackground(new Color(0x2b2b2b));
        scrollPane.setPreferredSize(new Dimension(dataDownloadPanel.getWidth(), dataDownloadPanel.getHeight()));
        scrollPane.setRequestFocusEnabled(false);
        scrollPane.putClientProperty("JScrollPane.smoothScrolling", true);
        scrollPane.setRequestFocusEnabled(false);

        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.setPreferredSize(new Dimension(dataDownloadPanel.getWidth(), dataDownloadPanel.getHeight()));
        panel.setRequestFocusEnabled(false);

        int key = getWorkerKey();

        int finalLine = line;
        SwingWorker<FileReader, String> worker = new SwingWorker<FileReader, String>() {
            File f = new File(filePath);

            @Override
            protected FileReader doInBackground() {

                if (f.exists()) {
                    try {
                        return new FileReader(f);
                    } catch (IOException ioe) {
                        error(ioe);
                    }

                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                swingWorkerIO(chunks);
            }

            @Override
            protected void done() {
                if (isDone()) {
                    try {
                        FileReader reader;
                        if ((reader = get()) != null) {
                            textPane.read(reader, null);
                            textPane.addLineHighlight(Math.max(finalLine - 1, 0), Color.BLUE);

                            // attempt to let the line be in the view.
                            double off = Math.max(finalLine - 15, 0) / (textPane.getLineCount() * 1.0);
                            int height = scrollPane.getViewport().getViewSize().height;
                            height = (int)(height * off);
                            scrollPane.getViewport().setViewPosition(new Point(0, height));

                            panel.add(scrollPane);
                            tabbedPane1.addTab(f.getName(), panel);
                            tabbedPane1.setSelectedIndex(tabbedPane1.getTabCount() - 1);
                        } else {
                            error(null);
                        }
                    } catch (Exception e) {
                        error(e);
                    } finally {
                        // release key
                        workManager.remove(key);
                    }
                }
            }

            private void error(Exception e) {
                publish("error", String.format("Failed to open the file: %s", filePath));
                publish("error", Console.getExceptionMsg(e));
                JOptionPane.showMessageDialog(panel1, "System Error! See console log", "Error!",
                    JOptionPane.ERROR_MESSAGE);
            }
        };

        registerWorker(key, worker);

        int x = 1;
    }

    private String getFileSyntax(String fileName) {
        if (fileName.endsWith(".java")) {
            return SyntaxConstants.SYNTAX_STYLE_JAVA;
        } else if (fileName.endsWith(".py")) {
            return SyntaxConstants.SYNTAX_STYLE_PYTHON;
        } else if (fileName.endsWith(".c") || fileName.endsWith(".h")) {
            return SyntaxConstants.SYNTAX_STYLE_C;
        } else if (fileName.endsWith(".cpp") || fileName.endsWith(".cc") || fileName.endsWith(".hpp")) {
            return SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
        } else if (fileName.endsWith(".md")) {
            return SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
        } else if (fileName.endsWith(".cmake")) {
            return SyntaxConstants.SYNTAX_STYLE_MAKEFILE;
        } else if (fileName.endsWith(".properties")) {
            return SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
        }

        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    private void getTreeChildNode(DefaultMutableTreeNode node) {
        node.removeAllChildren();

        FileNode parentNode = (FileNode)node.getUserObject();

        if (parentNode == null) {
            return;
        }

        // add child node to current parent node
        if (parentNode.getFile() != null && parentNode.getFile().isDirectory()) {
            File[] files = parentNode.getFile().listFiles();

            if (files != null) {
                for (File file : files) {
                    if (!file.isHidden()) {
                        node.add(new DefaultMutableTreeNode(new FileNode(file)));
                    }
                }
            }
        }
    }

    private List<Integer> getSelectedRows(JTable table) {
        List<Integer> res = new ArrayList<>();

        // last col is choose
        int col = table.getColumnCount() - 1;

        for (int row = 0; row < table.getRowCount(); row++) {

            if (table.getValueAt(row, col) != null && (boolean)table.getValueAt(row, col)) {
                res.add(row);
            }
        }

        return res;
    }

    private void swingWorkerIO(List<String> str) {
        if (str != null && str.size() % 2 == 0) {
            for (int i = 0; i < str.size(); i += 2) {
                if (StringUtils.equal(str.get(i), "error")) {
                    Console.error(str.get(i + 1));
                } else {
                    Console.log(str.get(i + 1));
                }
            }
        }
    }

    private void registerWorker(int key, SwingWorker worker) {
        workManager.put(key, worker);

        worker.execute();
    }

    private int getWorkerKey() {
        int key;
        while (workManager.containsKey(key = (int)(Math.random() * 1000))) {
        }

        return key;
    }

    static class FileNode {

        private File file;

        public FileNode(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
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
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setOpaque(true);
        panel1.setPreferredSize(new Dimension(1200, 750));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel2.setPreferredSize(new Dimension(400, 400));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 200;
        gbc.ipady = 200;
        panel1.add(panel2, gbc);
        panel2.setBorder(BorderFactory.createTitledBorder("Console"));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setRequestFocusEnabled(false);
        panel2.add(scrollPane1, BorderLayout.CENTER);
        console = new JTextPane();
        console.setBackground(new Color(-13948117));
        console.setEditable(true);
        console.setEnabled(true);
        console.setFocusable(true);
        console.setRequestFocusEnabled(true);
        console.setText("");
        scrollPane1.setViewportView(console);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        projectJScrollPane = new JScrollPane();
        projectJScrollPane.setPreferredSize(new Dimension(200, 400));
        projectJScrollPane.setRequestFocusEnabled(false);
        projectJScrollPane.setVerifyInputWhenFocusTarget(false);
        panel3.add(projectJScrollPane, BorderLayout.WEST);
        projectJTree = new JTree();
        projectJTree.setMaximumSize(new Dimension(-1, -1));
        projectJTree.setPreferredSize(new Dimension(500, 500));
        projectJTree.setRequestFocusEnabled(false);
        projectJTree.setVerifyInputWhenFocusTarget(false);
        projectJScrollPane.setViewportView(projectJTree);
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setPreferredSize(new Dimension(80, 400));
        tabbedPane1.setRequestFocusEnabled(false);
        panel3.add(tabbedPane1, BorderLayout.CENTER);
        dataDownloadPanel = new JPanel();
        dataDownloadPanel.setLayout(new BorderLayout(0, 0));
        dataDownloadPanel.setOpaque(true);
        dataDownloadPanel.setPreferredSize(new Dimension(1000, 400));
        dataDownloadPanel.setRequestFocusEnabled(false);
        tabbedPane1.addTab("Data Download", dataDownloadPanel);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        panel4.setOpaque(true);
        panel4.setPreferredSize(new Dimension(1000, 50));
        panel4.setRequestFocusEnabled(false);
        dataDownloadPanel.add(panel4, BorderLayout.SOUTH);
        downloadButton = new JButton();
        downloadButton.setFocusPainted(false);
        downloadButton.setFocusable(false);
        downloadButton.setRequestFocusEnabled(false);
        downloadButton.setText("Download");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(downloadButton, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel4.add(spacer2, gbc);
        refreshButton = new JButton();
        refreshButton.setFocusPainted(false);
        refreshButton.setFocusable(false);
        refreshButton.setRequestFocusEnabled(false);
        refreshButton.setText("Refresh");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(refreshButton, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(spacer4, gbc);
        downloadImportButton = new JButton();
        downloadImportButton.setFocusPainted(false);
        downloadImportButton.setFocusable(false);
        downloadImportButton.setRequestFocusEnabled(false);
        downloadImportButton.setText("Import");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(downloadImportButton, gbc);
        downloadScrollPane = new JScrollPane();
        downloadScrollPane.setPreferredSize(new Dimension(1000, 400));
        downloadScrollPane.setRequestFocusEnabled(false);
        dataDownloadPanel.add(downloadScrollPane, BorderLayout.CENTER);
        downloadTagTable = new JTable();
        downloadTagTable.setPreferredScrollableViewportSize(new Dimension(1000, 400));
        downloadTagTable.setPreferredSize(new Dimension(1000, 400));
        downloadTagTable.setRequestFocusEnabled(false);
        downloadTagTable.setRowSelectionAllowed(false);
        downloadScrollPane.setViewportView(downloadTagTable);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new BorderLayout(0, 0));
        panel5.setPreferredSize(new Dimension(0, 400));
        panel5.setRequestFocusEnabled(false);
        tabbedPane1.addTab("Data Extraction", panel5);
        extractScrollPane = new JScrollPane();
        extractScrollPane.setRequestFocusEnabled(false);
        panel5.add(extractScrollPane, BorderLayout.CENTER);
        extractTagTable = new JTable();
        extractTagTable.setRequestFocusEnabled(false);
        extractTagTable.setRowSelectionAllowed(false);
        extractScrollPane.setViewportView(extractTagTable);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        panel6.setPreferredSize(new Dimension(1000, 50));
        panel6.setRequestFocusEnabled(false);
        panel5.add(panel6, BorderLayout.SOUTH);
        extractButton = new JButton();
        extractButton.setFocusPainted(false);
        extractButton.setFocusable(false);
        extractButton.setText("Extract");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(extractButton, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel6.add(spacer6, gbc);
        importButton = new JButton();
        importButton.setFocusPainted(false);
        importButton.setFocusable(false);
        importButton.setRequestFocusEnabled(false);
        importButton.setText("Import");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(importButton, gbc);
        extractRefresh = new JButton();
        extractRefresh.setFocusPainted(false);
        extractRefresh.setFocusable(false);
        extractRefresh.setRequestFocusEnabled(false);
        extractRefresh.setText("Refresh");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(extractRefresh, gbc);
        exportButton = new JButton();
        exportButton.setFocusPainted(false);
        exportButton.setFocusable(false);
        exportButton.setRequestFocusEnabled(false);
        exportButton.setText("Export");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(exportButton, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(spacer7, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(spacer8, gbc);
        dataAnalysisPanel = new JPanel();
        dataAnalysisPanel.setLayout(new BorderLayout(0, 0));
        dataAnalysisPanel.setPreferredSize(new Dimension(0, 400));
        tabbedPane1.addTab("Data Analysis", dataAnalysisPanel);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        panel7.setFocusable(false);
        panel7.setRequestFocusEnabled(false);
        dataAnalysisPanel.add(panel7, BorderLayout.CENTER);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel7.add(spacer9, gbc);
        final JLabel label1 = new JLabel();
        label1.setFocusable(false);
        label1.setRequestFocusEnabled(false);
        label1.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(label1, gbc);
        analysisRefresh = new JButton();
        analysisRefresh.setAutoscrolls(false);
        analysisRefresh.setFocusPainted(false);
        analysisRefresh.setFocusable(false);
        analysisRefresh.setRequestFocusEnabled(false);
        analysisRefresh.setText("Refresh");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(analysisRefresh, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setFocusable(false);
        label3.setText("Source");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setAutoscrolls(false);
        label4.setFocusable(false);
        label4.setText("Target");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(label4, gbc);
        sourceComboBox = new JComboBox();
        sourceComboBox.setFocusable(false);
        sourceComboBox.setRequestFocusEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 100;
        panel7.add(sourceComboBox, gbc);
        targetComboBox = new JComboBox();
        targetComboBox.setFocusable(false);
        targetComboBox.setRequestFocusEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(targetComboBox, gbc);
        analysisButton = new JButton();
        analysisButton.setFocusPainted(false);
        analysisButton.setFocusable(false);
        analysisButton.setRequestFocusEnabled(false);
        analysisButton.setText("Analysis");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.EAST;
        panel7.add(analysisButton, gbc);
        analysisTipLabel = new JLabel();
        analysisTipLabel.setText("Find all similar assumptions in Target for each assumption in Source");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(analysisTipLabel, gbc);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel7.add(spacer10, gbc);
        final JPanel spacer11 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel7.add(spacer11, gbc);
        final JPanel spacer12 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel7.add(spacer12, gbc);
        final JPanel spacer13 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel7.add(spacer13, gbc);
        final JPanel spacer14 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel7.add(spacer14, gbc);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
