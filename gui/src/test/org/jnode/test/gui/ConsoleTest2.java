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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import javax.naming.NameNotFoundException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.swing.JTextAreaTextScreen;
import org.jnode.driver.console.textscreen.TextScreenConsole;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ConsoleTest2 {

    static class ConsoleFrame extends JFrame {
        //private PrintStream savedOut;
        //private JTextArea textArea;
        private JTextAreaTextScreen screen;
        private ConsoleManager manager;
        private TextConsole console;

        public ConsoleFrame() throws ConsoleException, ShellException, NameNotFoundException {
            super("System.out");

            screen = new JTextAreaTextScreen(80, 24);
            manager = new TextScreenConsoleManager();
            console = new TextScreenConsole(
                manager, "test", screen, ConsoleManager.CreateOptions.TEXT);
            manager.focus(console);

            CommandShell commandShell = new CommandShell(console);
            new Thread(commandShell).start();
            ShellUtils.getShellManager().registerShell(commandShell);

            //textArea = new JTextArea( "System.out:" );

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(screen.getTextArea(), BorderLayout.CENTER);
            //savedOut = new PrintStream( new TextAreaOutputStream( textArea ) );
        }

//        public void show() {
//            super.show( );
//            claimPrintStreams();
//        }

//        private void claimPrintStreams() {
//            System.out.println( "Claiming print streams." );
//            AccessController.doPrivileged( new PrivilegedAction() {
//                public Object run() {
//                    System.setOut( savedOut );
//                    System.setErr( savedOut );
//                    return null;
//                }
//            } );
//            System.out.println( "Claimed print streams." );
//        }
    }


    public static void main(String[] args) {
        try {
            final ConsoleFrame frame = new ConsoleFrame();
            frame.getRootPane().setDoubleBuffered(false);
            frame.setLocation(100, 100);
            frame.setSize(400, 400);
            JButton button = new JButton("Button");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    System.out.println("Button pressed.");
                }
            });
            frame.getContentPane().add(button, BorderLayout.SOUTH);
            JMenuBar mb = new JMenuBar();
            JMenu menu = new JMenu("JMenu test");
            JMenuItem mi = new JMenuItem("JMenuItem test");
            mb.add(menu);
            menu.add(mi);
            frame.setJMenuBar(mb);
            frame.validate();
            frame.show();

//            int count=0;
//            Timer t=new Timer( 1000,new ActionListener() {
//                public void actionPerformed( ActionEvent event ) {
//                    System.out.println( "X" );
//                }
//            } );
//            t.start();
//            comp.requestFocus();
            System.out.println("Showed ConsoleTest frame.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @author epr
     * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
     */
    public static class TextAreaOutputStream extends OutputStream {

        private JTextArea textArea;

        public TextAreaOutputStream(JTextArea console) {
            this.textArea = console;
        }

        public void write(int b) throws IOException {
            textArea.append("" + (char) b);
            if (b == '\n') {
                textArea.repaint();
            }
        }
    }

}
