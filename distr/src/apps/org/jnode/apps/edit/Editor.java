/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.apps.edit;

import charva.awt.BorderLayout;
import charva.awt.Color;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charvax.swing.JFileChooser;
import charvax.swing.JFrame;
import charvax.swing.JMenu;
import charvax.swing.JMenuBar;
import charvax.swing.JMenuItem;
import charvax.swing.JOptionPane;
import charvax.swing.JPanel;
import charvax.swing.JScrollPane;
import charvax.swing.JTextArea;
import charvax.swing.ListSelectionModel;
import charvax.swing.border.TitledBorder;
import gnu.java.security.action.GetPropertyAction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.log4j.Logger;

/**
 * @author Levente S\u00e1ntha
 */
public class Editor extends JFrame {
    private static Logger logger = Logger.getLogger(Editor.class);
    private JTextArea textArea;
    private JFileChooser fc;
    private String directory;
    private File file;
    private TitledBorder border;

    private Editor(final File file) {
        super("JNote - JNode Text Editor");
        this.file = file;
        setBackground(Color.black);
        setForeground(Color.cyan);
        JPanel panel = (JPanel) getContentPane();
        setJMenuBar(createMenu());
        panel.setLayout(new BorderLayout());
        textArea = new JTextArea("", 19, 76);
        textArea.setForeground(Color.cyan);
        border = new TitledBorder("");
        border.setTitleColor(Color.cyan);
        JScrollPane sp = new JScrollPane(textArea);
        sp.setViewportBorder(border);
        sp.setForeground(Color.cyan);
        panel.add(sp, BorderLayout.CENTER);
        directory = (String) AccessController.doPrivileged(new GetPropertyAction("user.dir"));
        if (file != null) {
            Boolean exists = (Boolean) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return file.exists();
                }
            });

            if (exists)
                readFile(file);
            else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    //empty
                }
            }

            updateTitle(file.getName());
            textArea.requestFocus();
        }
        setLocation(0, 0);
        setSize(80, 24);
        validate();
    }

    private JMenuBar createMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic('F');
        JMenuItem new_ = new JMenuItem("New");
        new_.setMnemonic('N');
        new_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new_();
            }
        });
        file.add(new_);
        file.addSeparator();
        JMenuItem open = new JMenuItem("Open...");
        open.setMnemonic('O');
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        file.add(open);
        JMenuItem save = new JMenuItem("Save");
        save.setMnemonic('S');
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        file.add(save);
        JMenuItem saveAs = new JMenuItem("Save As..");
        saveAs.setMnemonic('A');
        saveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        file.add(saveAs);
        file.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic('x');
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        file.add(exit);
        mb.add(file);
        return mb;
    }

    private void new_() {
        file = null;
        updateTitle("New file");
        textArea.setText("");
        textArea.requestFocus();
    }

    private void open() {
        initFileChooser();
        fc.setDialogTitle("Open file");
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {
            file = fc.getSelectedFile();
            updateTitle(file.getName());
            readFile(file);
            textArea.requestFocus();
        }
    }

    private void initFileChooser() {
        if (fc == null) {
            fc = new JFileChooser(directory);
            fc.setBackground(Color.cyan);
            fc.setForeground(Color.black);
            fc.setFileSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
    }

    private void readFile(final File file) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = new byte[fis.available()];
                    fis.read(data);
                    textArea.setText(new String(data));
                    fis.close();
                } catch (FileNotFoundException fnfe) {
                    JOptionPane.showMessageDialog(Editor.this, "File not found: " + file);
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(Editor.this, "Error opening file: " + file);
                }
                return null;
            }
        });
    }

    private void save() {
        if (file == null)
            saveAs();
        else
            writeFile(file);
        requestFocus();
    }

    private void writeFile(final File file) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    FileWriter fw = new FileWriter(file);
                    fw.write(textArea.getText());
                    fw.flush();
                    fw.close();
                } catch (FileNotFoundException fnfe) {
                    JOptionPane.showMessageDialog(Editor.this, "File not found: " + file);
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(Editor.this, "Error saving file: " + file);
                } catch (Exception x) {
                    String msg = "Unexpected error wile saving file: " + file;
                    logger.error(msg, x);
                    JOptionPane.showMessageDialog(Editor.this, msg);
                }
                return null;
            }
        });
    }


    private void saveAs() {
        initFileChooser();
        fc.setDialogTitle("Save file");
        if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(this)) {
            file = fc.getSelectedFile();
            updateTitle(file.getName());
            writeFile(file);
        }
    }

    private void exit() {
        hide();
        Toolkit.getDefaultToolkit().close();
    }

    private void updateTitle(String title) {
        border.setTitle(title);
    }

    static void editFile(File file) {
        Toolkit.getDefaultToolkit().register();
        Editor ed = new Editor(file);
        ed.setVisible(true);
    }

    /**
     * Startup method.
     */
    public static void main(String[] argv) {
        editFile(null);
    }
}
