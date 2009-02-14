package org.jnode.pluginlist;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.Enumeration;
import java.util.HashSet;
import java.io.File;

/**
 *
 */
class PluginListEditor {
    private JFrame frame;
    private PluginTreePane leftPane;
    private PluginTreePane rightPane;
    private JSplitPane splitPane;
    private PluginRepository repository;

    Set<Plugin> getSelectedPlugins(JTree tree) {
        Set<Plugin> set = new HashSet<Plugin>();
        final TreePath[] patha = tree.getSelectionPaths();
        if (patha != null && patha.length > 0) {
            for (TreePath path : patha) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object object = node.getUserObject();
                if ((object instanceof Plugin))
                    set.add((Plugin) object);
            }
        }

        return set;
    }

    public PluginListEditor(PluginRepository pluginRepository, String fileName) {
        repository = pluginRepository;
        frame = new JFrame();
        updateTitle(fileName);
        splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveOnExit();
            }
        });
        leftPane = new PluginTreePane(repository.getAvailableModel());
        leftPane.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectPlugin(getSelectedPlugins(leftPane.tree));
                }
            }
        });
        rightPane = new PluginTreePane(repository.getSelectedModel());
        rightPane.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    deselectPlugin(getSelectedPlugins(rightPane.tree));
                }
            }
        });
        splitPane.setLeftComponent(leftPane);
        splitPane.setRightComponent(rightPane);
        JMenuBar mb = new JMenuBar();
        frame.setJMenuBar(mb);
        JMenu file = new JMenu("File");
        mb.add(file);
        JMenuItem save = new JMenuItem("Save");
        file.add(save);
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Main.save();
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(frame, x.getMessage(), "Error saving file",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JMenuItem saveAs = new JMenuItem("Save as...");
        file.add(saveAs);
        saveAs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Main.saveAs(frame);
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(frame, x.getMessage(), "Error saving file",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JMenuItem openFile = new JMenuItem("Open plugin list...");
        file.add(openFile);
        openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File plf = Main.openFile(frame);
                    if (plf != null)
                        Main.reloadPluginList(plf);
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(frame, x.getMessage(), "Error opening plugin list",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JMenuItem openDir = new JMenuItem("Set project directory...");
        file.add(openDir);
        openDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File dir = Main.openDirectory(frame);
                    if (dir != null)
                        Main.reloadRepository(dir);
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(frame, x.getMessage(), "Error selecting project directory",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        file.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        file.add(exit);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveOnExit();
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }

    void reload(PluginRepository repo) {
        repository = repo;
        leftPane.tree.setModel(new DefaultTreeModel(new RootNode(repository.getAvailableModel())));
        rightPane.tree.setModel(new DefaultTreeModel(new RootNode(repository.getSelectedModel())));
    }

    private void saveOnExit() {
        if (Main.isUnsaved()) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame,
                "The plugin list was modified. Save changes?", "Save changes?", JOptionPane.YES_NO_OPTION)) {
                try {
                    Main.save();
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(frame, x.getMessage(), "Error saving file",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deselectPlugin(Set<Plugin> plugins) {
        Set<Plugin> usedby = new HashSet<Plugin>();
        for (Plugin p : plugins) {
            for (Plugin u : p.allUsed())
                if (repository.isSelected(u.getId()))
                    usedby.add(u);
        }

        if (!usedby.isEmpty()) {
            StringBuilder sb = new StringBuilder("<html><b>The following plugins will be removed:</b><br/>");
            for (Plugin p : usedby) {
                sb.append(p.getId()).append("<br/>");
            }
            sb.append("</html>");
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(splitPane, sb.toString()))
                return;
        }
        Set<Plugin> moved = repository.deselect(plugins);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) rightPane.tree.getModel().getRoot();
        Enumeration en = root.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode nod = (DefaultMutableTreeNode) en.nextElement();
            if (moved.contains(nod.getUserObject())) {
                TreeNode node1 = nod.getParent();
                if (node1 != null) {
                    ((Syncable) nod.getParent()).sync();
                    ((DefaultTreeModel) rightPane.tree.getModel()).reload(node1);
                }
            }
        }

        ((Syncable) ((DefaultTreeModel) leftPane.tree.getModel()).getRoot()).sync();
        root = (DefaultMutableTreeNode) leftPane.tree.getModel().getRoot();
        en = root.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode nod = (DefaultMutableTreeNode) en.nextElement();
            if (moved.contains(nod.getUserObject())) {
                ((DefaultTreeModel) leftPane.tree.getModel()).reload(nod.getParent());
            }
        }
    }

    private void selectPlugin(Set<Plugin> plugins) {
        Set<Plugin> requires = new HashSet<Plugin>();
        for (Plugin p : plugins) {
            for (Plugin u : p.allRequired())
                if (!repository.isSelected(u.getId()))
                    requires.add(u);
        }

        if (!requires.isEmpty()) {
            StringBuilder sb = new StringBuilder("<html><b>The following plugins will be selected:</b><br/>");
            for (Plugin p : requires) {
                sb.append(p.getId()).append("<br/>");
            }
            sb.append("</html>");
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(splitPane, sb.toString()))
                return;
        }
        Set<Plugin> moved = repository.select(plugins);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) leftPane.tree.getModel().getRoot();
        Enumeration en = root.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode nod = (DefaultMutableTreeNode) en.nextElement();
            if (moved.contains(nod.getUserObject())) {
                TreeNode node1 = nod.getParent();
                if (node1 != null) {
                    ((Syncable) nod.getParent()).sync();
                    ((DefaultTreeModel) leftPane.tree.getModel()).reload(node1);
                }
            }
        }

        ((Syncable) ((DefaultTreeModel) rightPane.tree.getModel()).getRoot()).sync();
        root = (DefaultMutableTreeNode) rightPane.tree.getModel().getRoot();
        en = root.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode nod = (DefaultMutableTreeNode) en.nextElement();
            if (moved.contains(nod.getUserObject())) {
                ((DefaultTreeModel) rightPane.tree.getModel()).reload(nod.getParent());
            }
        }
    }

    void start() {
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        splitPane.setDividerLocation(0.5);
    }

    void updateTitle(String fileName) {
        frame.setTitle("Plugin list editor: " + fileName);
    }
}
