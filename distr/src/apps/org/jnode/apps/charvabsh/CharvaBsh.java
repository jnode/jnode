/*Copyright, Sam Reid, 2003.*/
package org.jnode.apps.charvabsh;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ConsoleInterface;
import charva.awt.BorderLayout;
import charva.awt.Color;
import charva.awt.Container;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charva.awt.event.KeyAdapter;
import charva.awt.event.KeyEvent;
import charvax.swing.*;
import charvax.swing.border.TitledBorder;
import gnu.java.io.NullOutputStream;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * User: Sam Reid
 * Date: Mar 19, 2004
 * Time: 10:47:29 PM
 * Copyright (c) Mar 19, 2004 by Sam Reid
 */
public class CharvaBsh {
    static String example = "int i=0;\nfor (i=0;i<3;i++)\nprint(i+\".\");\n return i;\n";

    public static void main(String[] args) {
        System.err.println("Starting Charva Shell");
        CharvaShell testwin = new CharvaShell();
        testwin.show();
        Toolkit.getDefaultToolkit().waitTillFinished();
        System.err.println("Finished Charva Shell");
    }

    static class CharvaShell extends JFrame {
        JPanel bshPanel = new JPanel();
        JPanel messagePanel = new JPanel();

        private JLabel topLabel;
        private JLabel bottomLabel;
        private JTextArea editor;
        private JTextArea output;
        Interpreter interpreter;

        void done() {
            hide();
            Toolkit.getDefaultToolkit().close();
            Toolkit.getDefaultToolkit().waitTillFinished();
            System.err.println("Finished Charva Shell");
        }

        public CharvaShell() {
            super("Charva Beanshell v1.0");
            setForeground(Color.green);
            setBackground(Color.black);

            Container cp = super.getContentPane();
            cp.setLayout(new BorderLayout());

            addMenuBar();

            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            topLabel = new JLabel("F12 To evaluate.");
            messagePanel.add(topLabel);
            bottomLabel = new JLabel("");
            messagePanel.add(bottomLabel);

            cp.add(messagePanel, BorderLayout.SOUTH);

            editor = new JTextArea(example, 8, 60);
            JScrollPane scrollEditor = new JScrollPane(editor);
            scrollEditor.setViewportBorder(new TitledBorder("Beanshell Editor"));

            output = new JTextArea("BSH output", 5, 60);
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
                }
            });
            editor.requestFocus();

//            BSHOutputAdapter out = new BSHOutputAdapter(output);
            ConsoleInterface bshConsole=new BSHConsole(output);
            interpreter = new Interpreter(bshConsole);
//            interpreter.setOut(out);
//            interpreter.setErr(out);

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

            setJMenuBar(menubar);
        }

        private void evaluateText() {
            String sourcecode = editor.getText();
            System.out.println("Evaluating source string=" + sourcecode);
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
                evalError.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        }
    }
    static class BSHConsole implements ConsoleInterface{
        BSHOutputAdapter out;
        private JTextArea output;
        Reader reader=new StringReader("456");

        public BSHConsole(JTextArea output) {
            this.out=new BSHOutputAdapter(output);
            this.output=output;
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
            if (obj == null)
                println("null");
            else
                println(obj.toString());
        }

        public void print(Object obj) {
            if (obj == null)
                print("null");
            else
                print(obj.toString());
        }

        public void println() {
            print("\n");
        }

        public void print(int i) {
            print(i+"");
        }

        public void println(int i) {
            println(""+i);
        }

    }

}


