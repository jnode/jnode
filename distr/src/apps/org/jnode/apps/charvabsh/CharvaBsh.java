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

package org.jnode.apps.charvabsh;

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;
import charva.awt.BorderLayout;
import charva.awt.Color;
import charva.awt.Container;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charva.awt.event.KeyAdapter;
import charva.awt.event.KeyEvent;
import charvax.swing.BoxLayout;
import charvax.swing.JFrame;
import charvax.swing.JLabel;
import charvax.swing.JMenu;
import charvax.swing.JMenuBar;
import charvax.swing.JMenuItem;
import charvax.swing.JOptionPane;
import charvax.swing.JPanel;
import charvax.swing.JScrollPane;
import charvax.swing.JTextArea;
import charvax.swing.border.TitledBorder;
import gnu.java.io.NullOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.naming.NameNotFoundException;
import org.jnode.shell.CommandShell;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellUtils;

/**
 * A charva based beanshell interface.
 */
public class CharvaBsh {
    static String example = "int i=0;\nfor (i=0;i<2;i++)\nprint(i+\".\");\n return i;\n";

    /**
     * Startup method.
     */
    public static void main(String[] args) {
        Toolkit.getDefaultToolkit().register();
        System.err.println("Starting Charva Shell");
        CharvaShell testwin = new CharvaShell();
        testwin.show();
    }

    static class CharvaShell extends JFrame {
        private JPanel bshPanel = new JPanel();
        private JPanel messagePanel = new JPanel();

        private JLabel topLabel;
        private JLabel bottomLabel;
        private JTextArea editor;
        private JTextArea output;
        private Interpreter interpreter;
        private static final int NUM_TRACE = 5;

        void showSaveDialog() {
            String s = JOptionPane
                .showInputDialog(this, "Enter a filename to save", "Save what file", JOptionPane.QUESTION_MESSAGE);
            if (s == null) {
                output.append("\nFile Save Cancelled.");
            } else {
                output.append("\nSaving file=" + s);
                try {
                    saveText(s);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    output.append(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void showLoadDialog() {
            String s = JOptionPane
                .showInputDialog(this, "Enter a filename to load", "Load what file", JOptionPane.QUESTION_MESSAGE);
            if (s == null) {
                output.append("\nFile Load Cancelled.");
            } else {
                output.append("\nLoading file=" + s);
                try {
                    loadText(s);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    output.append(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void loadText(String filename) throws IOException {
            final URL url = new URL(filename);
            final InputStream is = url.openConnection().getInputStream();
            try {
                int ch;
                final StringBuffer buf = new StringBuffer();
                while ((ch = is.read()) >= 0) {
                    buf.append((char) ch);
                }
                editor.setText(buf.toString());
                repaint();
            } finally {
                is.close();
            }

        }

        void saveText(String filename) throws IOException {
            final URL url = new URL(filename);
            final OutputStream os = url.openConnection().getOutputStream();
            String text = editor.getText();

            try {
                for (int i = 0; i < text.length(); i++) {
                    char ch = text.charAt(i);
                    os.write(ch);
                }
                os.flush();
            } finally {
                os.close();
            }
        }

        void debug(String text) {
            output.setText(text);
            repaint();
        }

        void done() {
            debug("Called done in CharvaBsh.");
            hide();
            debug("Calling close");
            Toolkit.getDefaultToolkit().close();
            System.err.println("Finished with Done");
        }

        void requestShell() {
            Toolkit.getDefaultToolkit().unregister();
            debug("Finished requesting shell");
        }

        public CharvaShell() {
            super("Charva Beanshell v1.0");
            setForeground(Color.green);
            setBackground(Color.black);

            Container cp = super.getContentPane();
            cp.setLayout(new BorderLayout());

            addMenuBar();

            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            topLabel = new JLabel("F12[evaluate], F1[clear-output], F5[clear-editor]");
            messagePanel.add(topLabel);
            bottomLabel = new JLabel("");
            messagePanel.add(bottomLabel);

            cp.add(messagePanel, BorderLayout.SOUTH);

            editor = new JTextArea(example, 8, 75);
            JScrollPane scrollEditor = new JScrollPane(editor);
            scrollEditor.setViewportBorder(new TitledBorder("Beanshell Editor"));

            output = new JTextArea("BSH output", 7, 75);
            output.setLineWrap(true);

            JScrollPane scrollOutput = new JScrollPane(output);
            scrollOutput.setViewportBorder(new TitledBorder("Beanshell Output"));

            bshPanel.setLayout(new BorderLayout());
            bshPanel.add(scrollEditor, BorderLayout.NORTH);
            bshPanel.add(scrollOutput, BorderLayout.SOUTH);
            cp.add(bshPanel, BorderLayout.CENTER);

            editor.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent ke) {
                    if (ke.getKeyCode() == KeyEvent.VK_F12) {
                        evaluateText();
                        repaint();
                    }
                    if (ke.getKeyCode() == KeyEvent.VK_F1) {
                        output.setText("");
                        output.repaint();
                    }
                    if (ke.getKeyCode() == KeyEvent.VK_F5) {
                        editor.setText("");
                        editor.repaint();
                    }
                }
            });
            editor.requestFocus();

//            BSHOutputAdapter out = new BSHOutputAdapter(output);
            ConsoleInterface bshConsole = new BSHConsole(output);
            interpreter = new Interpreter(bshConsole);
            interpreter.setClassLoader(Thread.currentThread().getContextClassLoader());

            CharvaBshCommandInvoker shell = new CharvaBshCommandInvoker();
            try {
                interpreter.set("interpreter", interpreter);
                interpreter.set("shell", shell);
            } catch (EvalError evalError) {
                writeError(evalError);
            }
            setLocation(0, 0);
            setSize(80, 24);
            validate();
        }

        private void addMenuBar() {
            JMenuBar menubar = new JMenuBar();
            JMenu jMenuFile = new JMenu("File");
            jMenuFile.setMnemonic('F');

            JMenuItem exit = new JMenuItem("Exit");
            exit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae_) {
                    done();
                }
            });

            JMenuItem loadItem = new JMenuItem("Load");
            loadItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae_) {
                    showLoadDialog();
                }
            });
            JMenuItem saveItem = new JMenuItem("Save");
            saveItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae_) {
                    showSaveDialog();
                }
            });

            JMenuItem eval = new JMenuItem("Evaluate");
            eval.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae_) {
                    evaluateText();
                    repaint();
                }
            });
            menubar.add(jMenuFile);
            jMenuFile.add(eval);
            jMenuFile.add(exit);
            jMenuFile.add(saveItem);
            jMenuFile.add(loadItem);

            setJMenuBar(menubar);
        }

        private void evaluateText() {
            interpreter.setClassLoader(Thread.currentThread().getContextClassLoader());
            String sourcecode = editor.getText();
//            System.out.println( "Evaluating source string=" + sourcecode );
            topLabel.setText("Evaluating...");
            repaint();
//            Interpreter interpreter = new Interpreter();
            try {
                Object out = interpreter.eval(sourcecode);
                if (out != null) {
                    bottomLabel.setText("Result=" + out);
                } else {
                    bottomLabel.setText("Null Result.");
                }
                topLabel.setText("Press F12 to Evaluate.");
                repaint();
            } catch (EvalError evalError) {
                writeError(evalError);
            }
        }

        private void writeError(EvalError evalError) {
            output.append("<Evaluation Error>\n" + evalError);
            if (evalError instanceof TargetError) {
                TargetError te = (TargetError) evalError;
                StringWriter wr = new StringWriter();
                Throwable target = te.getTarget();
                target.printStackTrace(new PrintWriter(wr));
                String text = wr.toString();
                output.append("\n" + text);
            }
        }
    }

    static class BSHConsole implements ConsoleInterface {
        BSHOutputAdapter out;
        Reader reader = new StringReader("456");

        public BSHConsole(JTextArea output) {
            this.out = new BSHOutputAdapter(output);
        }

        public Reader getIn() {
            return reader;
        }

        public PrintStream getOut() {
            return out;
        }

        public PrintStream getErr() {
            return out;
        }

        public void println(Object o) {
            out.println(o);
        }

        public void print(Object o) {
            out.print(o);
        }

        public void error(Object o) {
            out.print(o);
        }

    }

    static class BSHOutputAdapter extends PrintStream {
        JTextArea target;

        public BSHOutputAdapter(JTextArea target) {
            this(target, new NullOutputStream());
        }

        public BSHOutputAdapter(JTextArea target, OutputStream out) {
            super(out);
            this.target = target;
        }

        public void print(String s) {
            target.append(s);
            target.repaint();
        }

        public void println(String s) {
            target.append(s + "\n");
            target.repaint();
        }

        public void println(Object obj) {
            if (obj == null) {
                println("null");
            } else {
                println(obj.toString());
            }
        }

        public void print(Object obj) {
            if (obj == null) {
                print("null");
            } else {
                print(obj.toString());
            }
        }

        public void println() {
            print("\n");
        }

        public void print(int i) {
            print(i + "");
        }

        public void println(int i) {
            println("" + i);
        }

    }

    static class CharvaBshCommandInvoker {
        private Shell shell;

        public CharvaBshCommandInvoker() {
            try {
                shell = ShellUtils.getShellManager().getCurrentShell();
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void invoke(String command) {
            if (shell != null) {
                if (shell instanceof CommandShell) {
                    CommandShell cs = (CommandShell) shell;
                    try {
                        cs.runCommand(command);
                    } catch (ShellException ex) {
                        System.err.println("Command invocation failed: " + ex.getMessage());
                    }
                } else {
                    System.err.println("Shell wasn't a CommandShell: " + shell.getClass());
                }
            } else {
                System.err.println("Shell is null.");
            }
        }
    }

}


