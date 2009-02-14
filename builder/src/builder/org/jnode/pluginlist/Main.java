package org.jnode.pluginlist;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import java.awt.Component;
import org.jnode.nanoxml.XMLElement;
import javax.swing.JFileChooser;

public class Main {
    private static Preferences prefs = Preferences.userNodeForPackage(Main.class);
    private static File baseDir;
    private static File pluginListFile;
    private static PluginList pluginList;
    private static PluginRepository pluginRepository;
    private static PluginListEditor pluginListEditor;
    private static boolean unsaved;

    static File openDirectory(Component parent) throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Select JNode project root directory");
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(parent)) {
            File ret = fc.getSelectedFile();
            if (ret != null) {
                prefs.put("jnode.base.dir", ret.getCanonicalPath());
                return ret;
            }
        }
        return null;
    }

    static File openFile(Component parent) throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Select JNode plugin list");
        fc.setCurrentDirectory(baseDir);
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(parent)) {
            File ret = fc.getSelectedFile();
            if (ret != null) {
                prefs.put("jnode.pluginlist.file", ret.getCanonicalPath());
                return ret;
            }
        }
        return null;
    }

    static void save() throws Exception {
        pluginList.setPlugins(pluginRepository.getSelectedPluginIds());
        BufferedWriter bw = new BufferedWriter(new FileWriter(pluginListFile));
        pluginList.write(bw);
        bw.flush();
        bw.close();
        unsaved = false;
        pluginListEditor.updateTitle(pluginListFile.getName());
    }

    static void saveAs(Component parent) throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setDialogTitle("Save JNode plugin list as");
        fc.setCurrentDirectory(baseDir);
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(parent)) {
            File ret = fc.getSelectedFile();
            if (ret != null) {
                pluginListFile = ret;
                prefs.put("jnode.pluginlist.file", pluginListFile.getCanonicalPath());
                save();
                pluginListEditor.updateTitle(pluginListFile.getName());
            }
        }
    }

    public static void main(String[] argv) throws Exception {
        String str = prefs.get("jnode.base.dir", null);
        if (str == null || !(baseDir = new File(str)).exists()) {
            baseDir = openDirectory(null);
            if (baseDir == null) {
                System.exit(0);
            }
        }

        str = prefs.get("jnode.pluginlist.file", null);
        if (str == null || !(pluginListFile = new File(str)).exists()) {
            pluginListFile = openFile(null);
            if (pluginListFile == null) {
                System.exit(0);
            }
        }

        pluginList = new PluginList();
        pluginList.read(new FileReader(pluginListFile));

        pluginRepository = new PluginRepository();
        pluginRepository.reload(pluginList);

        pluginListEditor = new PluginListEditor(pluginRepository, pluginListFile.getName());
        pluginListEditor.start();
    }

    static void reloadPluginList(File plf) throws Exception {
        PluginList pl = new PluginList();
        pl.read(new FileReader(plf));
        pluginRepository.reselect(pl);
        pluginListFile = plf;
        pluginList = pl;
        pluginListEditor.reload(pluginRepository);
        pluginListEditor.updateTitle(plf.getName());
    }

    static void reloadRepository(File dir) throws Exception {
        baseDir = dir;
        PluginRepository pr = new PluginRepository();
        pr.reload(pluginList);
        pluginRepository = pr;
        pluginListEditor.reload(pr);
    }

    public static Project readProject(String name, PluginRepository repo) throws Exception {
        Project proj = new Project(name);

        File pdir = new File(baseDir, name);
        File ddir = new File(pdir, "descriptors");
        for (File f : ddir.listFiles()) {
            if (!f.getName().endsWith(".xml"))
                continue;

            Plugin pl = readPlugin(new BufferedReader(new FileReader(f)));
            if (pl != null) {
                if (pl.isSystem()) {
                    repo.addSystemPlugin(pl);
                } else {
                    proj.addPlugin(pl);
                }
            } else {
                //warning
            }
        }

        return proj;
    }


    public static Plugin readPlugin(Reader in) throws Exception {

        final XMLElement root = new XMLElement(new Hashtable(), true, false);
        root.parseFromReader(in);
        String rname = root.getName();
        if (rname.equals("plugin") || rname.equals("fragment")) {
            String id = (String) root.getAttribute("id");
            if (id == null) {
                throw new RuntimeException("Invalid plugin");
            }
            Plugin plug = new Plugin(id);
            String system = (String) root.getAttribute("system");
            if (system != null && "true".equals(system)) {
                plug.setSystem();
            }
            for (XMLElement obj : root.getChildren()) {
                String name = obj.getName();
                if (name.equals("requires")) {
                    for (XMLElement ch : obj.getChildren()) {
                        String xname = ch.getName();
                        if (xname.equals("import")) {
                            String pid = (String) ch.getAttribute("plugin");
                            if (pid != null) {
                                plug.addRecuiredId(pid);
                            }
                        }
                    }
                }
            }
            if (rname.equals("fragment")) {
                String pid = (String) root.getAttribute("plugin-id");
                if (pid == null) {
                    throw new RuntimeException("Invalid fragment");
                }
                plug.addRecuiredId(pid);
            }
            return plug;
        }

        return null;
    }

    static boolean isUnsaved() {
        return unsaved;
    }

    static void setSaved() {
        unsaved = false;
    }

    static void setUnsaved() {
        if (!Main.unsaved) {
            if (pluginListEditor != null) {
                pluginListEditor.updateTitle(pluginListFile.getName() + " (modified)");
            } else {
                return;
            }
        }
        Main.unsaved = true;
    }
}

