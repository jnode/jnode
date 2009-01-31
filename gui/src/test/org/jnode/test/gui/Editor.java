/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.test.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
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

    public Editor(File file) {
        super("JNote");
        this.file = file;
        setBackground(Color.black);
        setForeground(Color.cyan);
        JPanel panel = (JPanel) getContentPane();
        setJMenuBar(createMenu());
        panel.setLayout(new BorderLayout());
        textArea = new JTextArea();
        JScrollPane sp = new JScrollPane(textArea);
        sp.setViewportBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        panel.add(sp, BorderLayout.CENTER);
        if (file != null) {
            directory = file.getParent();
            readFile(file);
            updateTitle(file.getName());
            textArea.requestFocus();
        } else {
            new_();
        }
        setLocation(0, 0);
        setSize(500, 500);
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
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = new byte[fis.available()];
                    fis.read(data);
                    textArea.setText(new String(data));
                    textArea.setCaretPosition(0);
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
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
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
        setVisible(false);
    }

    private void updateTitle(String title) {
        setTitle("JNote - " + title);
    }

    static void editFile(File file) {
        Editor ed = new Editor(file);
        ed.setVisible(true);
    }

    public static void main(String[] argv) {
        editFile(null);
    }
}
