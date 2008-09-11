/*
 * $Id$
 */
package org.jnode.apps.commander;

import charva.awt.BorderLayout;
import charva.awt.Color;
import charva.awt.EventQueue;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charva.awt.event.KeyEvent;
import charva.awt.event.ScrollEvent;
import charvax.swing.JButton;
import charvax.swing.JFrame;
import charvax.swing.JList;
import charvax.swing.JOptionPane;
import charvax.swing.JPanel;
import charvax.swing.JScrollPane;
import charvax.swing.border.TitledBorder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Charva based file manager.
 * @author Levente S\u00e1ntha
 */
public class CharvaCommander extends JFrame {
    private Pane currentPane;
    private Pane otherPane;
    private boolean exitted;

    /**
     * Startup method.
     */
    public static void main(String[] argv) {
        CharvaCommander cmd = null;
        try {
            Toolkit.getDefaultToolkit().register();
            cmd = new CharvaCommander();
            cmd.setVisible(true);
            //Toolkit.getDefaultToolkit().waitTillFinished();
        } catch (Exception e) {
            e.printStackTrace();
            if (cmd != null)
                cmd.exit();
            else
                Toolkit.getDefaultToolkit().close();
        }
    }

    private CharvaCommander() {
        this._insets = new Insets(0, 0, 0, 0);
        setBackground(Color.black);
        setForeground(Color.cyan);
        JPanel content = (JPanel) getContentPane();
        new ButtonPanel();

        JPanel panel = new JPanel();
        content.add(panel, BorderLayout.CENTER);
        panel.setLayout(null);

        File startf = new File(".");
        String path = startf.getAbsolutePath();
        try {
            path = startf.getCanonicalPath();
        } catch (IOException e) {
            //ignore
        }

        currentPane = new Pane(38, 21);
        currentPane.setPath(path);
        currentPane.setLocation(0, 0);
        panel.add(currentPane);

        otherPane = new Pane(38, 21);
        otherPane.setPath(path);
        otherPane.setLocation(40, 0);
        panel.add(otherPane);

        setLocation(0, 0);
        setSize(80, 24);
        validate();

        currentPane.list.requestFocus();
    }

    private class ButtonPanel extends JPanel implements ActionListener {
        {
            getContentPane().add(this, BorderLayout.SOUTH);
        }

        //final JButton help = createButton("1 Help", KeyEvent.VK_F1);
        final JButton mkfile = createButton("F2 New", KeyEvent.VK_F2);
        final JButton view = createButton("F3 View", KeyEvent.VK_F3);
        final JButton edit = createButton("F4 Edit", KeyEvent.VK_F4);
        final JButton copy = createButton("F5 Copy", KeyEvent.VK_F5);
        final JButton move = createButton("F6 Move", KeyEvent.VK_F6);
        final JButton mkdir = createButton("F7 MkDir", KeyEvent.VK_F7);
        final JButton delte = createButton("F8 Delete", KeyEvent.VK_F8);
        final JButton chdir = createButton("F9 ChDir", KeyEvent.VK_F9);
        final JButton exit = createButton("F10 Exit", KeyEvent.VK_F10);

        JButton createButton(String title, int accel) {
            JButton b = new MyButton(title);
            this.add(b);
            b.setMnemonic(accel);
            b.addActionListener(this);
            return b;
        }

        public void actionPerformed(ActionEvent ae) {
            try {
                Object source = ae.getSource();
                //if (source == help) {
                //} else
                if (source == mkfile) {
                    mkfile();
                } else if (source == view) {
                    view();
                } else if (source == edit) {
                    edit();
                } else if (source == copy) {
                    copy();
                } else if (source == move) {
                    move();
                } else if (source == mkdir) {
                    mkdir();
                } else if (source == delte) {
                    delete();
                } else if (source == chdir) {
                    chdir();
                } else if (source == exit) {
                    exit();
                }
            } finally {
                if (!exitted)
                    currentPane.requestFocus();
            }
        }
    }

    private void move() {
        String sel = currentPane.getSelection();
        if (sel == null)
            return;

        int conf = JOptionPane
            .showConfirmDialog(this, sel + " to " + otherPane.getPath(), "Move ?", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION)
            return;

        try {
            File source = new File(currentPane.getPath(), sel);
            if (copyRec(source, new File(otherPane.getPath(), sel), true))
                delete0(source);
        } finally {
            currentPane.setPath(currentPane.getPath());
            currentPane.repaint();
            otherPane.setPath(otherPane.getPath());
            otherPane.repaint();
        }
    }

    private void view() {
        String sel = currentPane.getSelection();
        if (sel == null)
            return;

        File f = new File(currentPane.getPath(), sel);
        if (!f.isFile())
            return;

        callViewer(f);
    }

    private void mkfile() {
        String str = JOptionPane.showInputDialog(this, "File name: ", "New file", JOptionPane.OK_CANCEL_OPTION);
        if (str == null || str.trim().length() == 0)
            return;

        File file = new File(currentPane.getPath(), str);
        if (file.exists()) {
            int conf = JOptionPane
                .showConfirmDialog(this, file.getName(), "Continue? File already exits:", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION)
                return;
        }

        callEditor(file);
    }

    private void callViewer(File file) {
        try {
            Class.forName("org.jnode.apps.editor.TextEditor").getMethod("main", String[].class).
                invoke(null, new Object[]{new String[]{file.getAbsolutePath(), "ro"}});
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Viewer not found", "Warning", JOptionPane.DEFAULT_OPTION);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION);
        }

    }

    private void edit() {
        String sel = currentPane.getSelection();
        if (sel == null)
            return;

        File f = new File(currentPane.getPath(), sel);
        if (!f.isFile())
            return;

        callEditor(f);
    }

    private void callEditor(File file) {
        try {
            Class.forName("org.jnode.apps.editor.TextEditor").getMethod("main", String[].class).
                invoke(null, new Object[]{new String[]{file.getAbsolutePath()}});
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Editor not found", "Warning", JOptionPane.DEFAULT_OPTION);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION);
        }
    }

    private void chdir() {
        String str =
            JOptionPane.showInputDialog(this, "Path to directory: ", "Change directory", JOptionPane.OK_CANCEL_OPTION);
        if (str == null || str.trim().length() == 0)
            return;

        File f = str.charAt(0) == '/' ? new File(str) :
            new File(currentPane.getPath(), str);

        if (!f.exists())
            JOptionPane.showMessageDialog(this, f.getAbsolutePath(), "Invalid directory", JOptionPane.DEFAULT_OPTION);
        else {
            if (!f.isDirectory())
                f = f.getParentFile();

            currentPane.setPath(f.getAbsolutePath());
            currentPane.repaint();
        }
    }

    private void delete() {
        String sel = currentPane.getSelection();
        if (sel == null)
            return;

        int conf = JOptionPane.showConfirmDialog(this, sel, "Delete ?", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION)
            return;

        File f = new File(currentPane.getPath(), sel);
        if (f.isDirectory() && f.list().length > 0) {
            conf = JOptionPane
                .showConfirmDialog(this, "Directory is not empty: " + sel, "Delete ?", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION)
                return;
        }

        try {
            delete0(new File(currentPane.getPath(), sel));
        } finally {
            currentPane.setPath(currentPane.getPath());
            currentPane.repaint();
        }
    }

    private void delete0(File f) {
        if (f.isDirectory())
            for (String s : f.list())
                delete0(new File(f.getAbsolutePath(), s));

        f.delete();
    }

    private void notImplemented() {
        JOptionPane.showMessageDialog(this, "NOT IMPLEMENTED!", null, JOptionPane.DEFAULT_OPTION);
        currentPane.requestFocus();
    }

    private void copy() {
        String sel = currentPane.getSelection();
        if (sel == null)
            return;

        int conf = JOptionPane
            .showConfirmDialog(this, sel + " to " + otherPane.getPath(), "Copy ?", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION)
            return;

        try {
            copyRec(new File(currentPane.getPath(), sel), new File(otherPane.getPath(), sel), true);
        } finally {
            otherPane.setPath(otherPane.getPath());
            otherPane.repaint();
        }
    }

    private boolean copyRec(File from, File to, boolean ask) {
        if (from.isFile()) {
            return copy0(from, to, ask);
        } else if (from.isDirectory()) {
            if (ask && to.exists()) {
                int conf = JOptionPane.showConfirmDialog(this, to.toString(), "Overwrite ?", JOptionPane.YES_NO_OPTION);
                if (conf != JOptionPane.YES_OPTION)
                    return false;
            } else
                to.mkdir();

            for (String name : from.list())
                if (!copyRec(new File(from.getAbsolutePath(), name), new File(to.getAbsolutePath(), name), false))
                    return false;

            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Unsupported file type in source.");
            return false;
        }
    }

    private boolean copy0(File from, File to, boolean ask) {
        if (from.equals(to)) {
            JOptionPane.showMessageDialog(this, "Source and destination files are the same.");
            return false;
        }
        if (ask && to.exists()) {
            int conf = JOptionPane.showConfirmDialog(this, to.toString(), "Overwrite ?", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION)
                return false;
        }
        try {
            copy0(new FileInputStream(from), new FileOutputStream(to));
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.OK_OPTION);
            to.delete();
            return false;
        }
    }

    private void copy0(FileInputStream in, FileOutputStream out) throws IOException {
        try {
            byte[] buf = new byte[8 * 1024];
            int c;
            while ((c = in.read(buf)) > -1)
                out.write(buf, 0, c);
            out.flush();
        } finally {
            in.close();
            out.close();
        }
    }

    private void mkdir() {
        String str =
            JOptionPane.showInputDialog(this, "Directory name: ", "Create directory", JOptionPane.OK_CANCEL_OPTION);
        if (str != null && str.trim().length() > 0) {
            File file = new File(currentPane.getPath(), str);
            file.mkdir();
            currentPane.setPath(currentPane.getPath());
            currentPane.repaint();
        }
    }

    private void exit() {
        exitted = true;
        hide();
        Toolkit.getDefaultToolkit().close();
    }

    private class Pane extends JScrollPane {
        String path;
        JList list;
        TitledBorder border;
        int w;
        int h;

        Pane(int w, int h) {
            this.w = w;
            this.h = h;
            border = new TitledBorder("");
            border.setTitleColor(Color.cyan);
            list = new JList() {
                @Override
                public void processKeyEvent(KeyEvent ke) {
                    EventQueue evtqueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
                    int keyCode = ke.getKeyCode();
                    if (keyCode == KeyEvent.VK_ENTER) {
                        Object sel = list.getSelectedValue();
                        String s;
                        String n = null;
                        if ("..".equals(sel)) {
                            File file = new File(path);
                            n = file.getName();
                            s = file.getParentFile().getAbsolutePath();
                        } else {
                            String sels = String.valueOf(sel);
                            s = new File(path, sels.substring(1, sels.length())).getAbsolutePath();
                        }
                        int sri = setData(s, n);
                        int dir = _currentRow < sri ? ScrollEvent.UP : ScrollEvent.DOWN;
                        if (sri > -1)
                            _currentRow = sri;
                        setSelectedIndex(_currentRow);
                        evtqueue.postEvent(new ScrollEvent(this, dir, new Point(0, _currentRow)));
                        ke.consume();
                    } else if (keyCode == KeyEvent.VK_HOME) {
                        _currentRow = 0;
                        evtqueue.postEvent(new ScrollEvent(
                            this, ScrollEvent.DOWN, new Point(0, _currentRow)));
                    }
                    super.processKeyEvent(ke);
                    if (keyCode == KeyEvent.VK_UP ||
                        keyCode == KeyEvent.VK_DOWN ||
                        keyCode == KeyEvent.VK_PAGE_UP ||
                        keyCode == KeyEvent.VK_PAGE_DOWN ||
                        keyCode == KeyEvent.VK_END ||
                        keyCode == KeyEvent.VK_HOME) {
                        setSelectedIndex(_currentRow);
                    }
                }

                @Override
                public void requestFocus() {
                    super.requestFocus();
                    handleFocus();
                }
            };
            setViewportView(list);
            list.setVisibleRowCount(h);
            list.setColumns(w);
            this.setViewportBorder(border);
            this.setForeground(Color.cyan);
        }

        private void handleFocus() {
            if (currentPane != this) {
                otherPane = currentPane;
                currentPane = this;
            }
        }

        private int setData(String s, String n) {
            File file = new File(s);
            if (file.isDirectory()) {
                setPath0(s);
                File[] files = file.listFiles();
                String[] names = new String[files.length];

                //prefix dirs with '/'
                int i = 0;
                for (File f : files)
                    names[i++] = f.isDirectory() ? "/" + f.getName() : f.getName();

                Arrays.sort(names);
                //add parent entry: ".."
                if (!"/".equals(s)) {
                    int ln = names.length;
                    String[] names2 = new String[ln + 1];
                    names2[0] = "..";
                    System.arraycopy(names, 0, names2, 1, ln);
                    names = names2;
                }

                list.setListData(names);

                //set list selection
                if (n == null) {
                    list.setSelectedIndex(0);
                    return 0;
                } else {
                    i = 0;
                    n = "/" + n;
                    for (String nn : names) {
                        if (n.equals(nn)) {
                            return i;
                        }
                        i++;
                    }

                    return 0;
                }
            }
            return -1;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            setPath0(path);
            setData(path, null);
        }

        public String getSelection() {
            String sel = (String) list.getSelectedValue();
            if (sel == null || sel.equals(".."))
                return null;

            if (sel.charAt(0) == '/')
                return sel.substring(1);

            return sel;
        }

        private void setPath0(String path) {
            this.path = path;
            setBorder(path);
        }

        private void setBorder(String path) {
            int w = this.w - 2;
            if (w > 0 && path.length() > w) {
                path = path.substring(path.length() - w);
            }
            border.setTitle(path);
        }
    }

    private static class MyButton extends JButton {
        public MyButton() {
            this("");
        }

        public MyButton(String text_) {
            super(text_);
        }

        @Override
        public boolean isFocusTraversable() {
            return false;
        }


        @Override
        protected String getLabelString() {
            return getText();
        }

        public int getWidth() {
            return getLabelString().length();
        }

        @Override
        public void draw(Toolkit toolkit) {
            Point origin = getLocationOnScreen();
            toolkit.setCursor(origin);
            int colorpair = getCursesColor();
            toolkit.addString(getLabelString(), Toolkit.A_REVERSE, colorpair);
        }
    }
}
