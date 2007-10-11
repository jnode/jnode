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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ConsoleTest {

    static class ConsoleFrame extends JFrame {
        private PrintStream savedOut;
        private JTextArea textArea;

        public ConsoleFrame() {
            super( "System.out" );
            textArea = new JTextArea( "System.out:" );
            getContentPane().setLayout( new BorderLayout() );
            getContentPane().add( textArea, BorderLayout.CENTER );
            savedOut = new PrintStream( new TextAreaOutputStream( textArea ) );
        }

        public void show(  ) {
            super.show( );
                claimPrintStreams();
        }

        private void claimPrintStreams() {
            System.out.println( "Claiming print streams." );
            AccessController.doPrivileged( new PrivilegedAction<Void>() {
                public Void run() {
                    System.setOut( savedOut );
                    System.setErr( savedOut );
                    return null;
                }
            } );
            System.out.println( "Claimed print streams." );
        }
    }


    public static void main( String[] args ) {
        try {
            final ConsoleFrame frame = new ConsoleFrame();
            frame.getRootPane().setDoubleBuffered( false );
            frame.setLocation( 100, 100 );
            frame.setSize( 400, 400 );
            JButton button = new JButton( "Button" );
            button.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    System.out.println( "Button pressed." );
                }
            } );
            frame.getContentPane().add( button, BorderLayout.SOUTH );
            JMenuBar mb = new JMenuBar();
            JMenu menu = new JMenu( "JMenu test" );
            JMenuItem mi = new JMenuItem( "JMenuItem test" );
            mb.add( menu );
            menu.add( mi );
            frame.setJMenuBar( mb );
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
            System.out.println( "Showed ConsoleTest frame." );
        }
        catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * @author epr
     * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
     */
    public static class TextAreaOutputStream extends OutputStream {

        private JTextArea textArea;

        public TextAreaOutputStream( JTextArea console ) {
            this.textArea = console;
        }

        public void write( int b ) throws IOException {
            textArea.append( "" + (char)b );
            if (b == '\n') {
                textArea.repaint();
            }
        }
    }

}
