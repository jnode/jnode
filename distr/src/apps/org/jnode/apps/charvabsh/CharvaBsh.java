/*Copyright, Sam Reid, 2003.*/
package org.jnode.apps.charvabsh;

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;
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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: Sam Reid
 * Date: Mar 19, 2004
 * Time: 10:47:29 PM
 * Copyright (c) Mar 19, 2004 by Sam Reid
 */
public class CharvaBsh {
    static String example = "int i=0;\nfor (i=0;i<2;i++)\nprint(i+\".\");\n return i;\n";

    public static void main( String[] args ) {
        Toolkit.getDefaultToolkit().register();
        System.err.println( "Starting Charva Shell" );
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

        void showSaveDialog() {
            String s = JOptionPane.showInputDialog( this, "Enter a filename to save", "Save what file", JOptionPane.QUESTION_MESSAGE );
            if( s == null ) {
                output.append( "\nFile Save Cancelled." );
            }
            else {
                output.append( "\nSaving file=" + s );
                try {
                    saveText( s );
                }
                catch( MalformedURLException e ) {
                    e.printStackTrace();
                    output.append( e.getMessage() );
                }
                catch( IOException e ) {
                    e.printStackTrace();
                }
            }
        }

        void showLoadDialog() {
            String s = JOptionPane.showInputDialog( this, "Enter a filename to load", "Load what file", JOptionPane.QUESTION_MESSAGE );
            if( s == null ) {
                output.append( "\nFile Load Cancelled." );
            }
            else {
                output.append( "\nLoading file=" + s );
                try {
                    loadText( s );
                }
                catch( MalformedURLException e ) {
                    e.printStackTrace();
                    output.append( e.getMessage() );
                }
                catch( IOException e ) {
                    e.printStackTrace();
                }
            }
        }

        void loadText( String filename ) throws IOException {
            final URL url = new URL( filename );
            final InputStream is = url.openConnection().getInputStream();
            try {
                int ch;
                final StringBuffer buf = new StringBuffer();
                while( ( ch = is.read() ) >= 0 ) {
                    buf.append( (char)ch );
                }
                editor.setText( buf.toString() );
                repaint();
            }

            finally {
                is.close();
            }

        }

        void saveText( String filename ) throws IOException {
            final URL url = new URL( filename );
            final OutputStream os = url.openConnection().getOutputStream();
            String text = editor.getText();

            try {
                for( int i = 0; i < text.length(); i++ ) {
                    char ch = text.charAt( i );
                    os.write( ch );
                }
                os.flush();
            }
            finally {
                os.close();
            }
        }

        void debug( String text ) {
            output.setText( text );
            repaint();
        }

        void done() {
            debug( "Called done in CharvaBsh." );
            hide();
            debug( "Calling close" );
            Toolkit.getDefaultToolkit().close();
            System.err.println( "Finished with Done" );
        }

        void requestShell() {
            Toolkit.getDefaultToolkit().unregister();
            debug( "Finished requesting shell" );
        }

        public CharvaShell() {
            super( "Charva Beanshell v1.0" );
            setForeground( Color.green );
            setBackground( Color.black );

            Container cp = super.getContentPane();
            cp.setLayout( new BorderLayout() );

            addMenuBar();

            messagePanel.setLayout( new BoxLayout( messagePanel, BoxLayout.Y_AXIS ) );
            topLabel = new JLabel( "F12[evaluate], F1[clear-output], F5[clear-editor]" );
            messagePanel.add( topLabel );
            bottomLabel = new JLabel( "" );
            messagePanel.add( bottomLabel );

            cp.add( messagePanel, BorderLayout.SOUTH );

            editor = new JTextArea( example, 8, 75 );
            JScrollPane scrollEditor = new JScrollPane( editor );
            scrollEditor.setViewportBorder( new TitledBorder( "Beanshell Editor" ) );

            output = new JTextArea( "BSH output", 7, 75 );
            JScrollPane scrollOutput = new JScrollPane( output );
            scrollOutput.setViewportBorder( new TitledBorder( "Beanshell Output" ) );

            bshPanel.setLayout( new BorderLayout() );
            bshPanel.add( scrollEditor, BorderLayout.NORTH );
            bshPanel.add( scrollOutput, BorderLayout.SOUTH );
            cp.add( bshPanel, BorderLayout.CENTER );

            editor.addKeyListener( new KeyAdapter() {
                public void keyPressed( KeyEvent ke ) {
                    if( ke.getKeyCode() == KeyEvent.VK_F12 ) {
                        evaluateText();
                        repaint();
                    }
                    if( ke.getKeyCode() == KeyEvent.VK_F1 ) {
                        output.setText( "" );
                        output.repaint();
                    }
                    if( ke.getKeyCode() == KeyEvent.VK_F5 ) {
                        editor.setText( "" );
                        editor.repaint();
                    }
                }
            } );
            editor.requestFocus();

//            BSHOutputAdapter out = new BSHOutputAdapter(output);
            ConsoleInterface bshConsole = new BSHConsole( output );
            interpreter = new Interpreter( bshConsole );
            interpreter.setClassLoader( Thread.currentThread().getContextClassLoader() );
            try {
                interpreter.set( "interpreter", interpreter );
            }
            catch( EvalError evalError ) {
                writeError( evalError );
            }
            setLocation( 0, 0 );
            setSize( 80, 24 );
            validate();
        }

        private void addMenuBar() {
            JMenuBar menubar = new JMenuBar();
            JMenu jMenuFile = new JMenu( "File" );
            jMenuFile.setMnemonic( 'F' );

            JMenuItem exit = new JMenuItem( "Exit" );
            exit.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent ae_ ) {
                    done();
                }
            } );

            JMenuItem loadItem = new JMenuItem( "Load" );
            loadItem.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent ae_ ) {
                    showLoadDialog();
                }
            } );
            JMenuItem saveItem = new JMenuItem( "Save" );
            saveItem.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent ae_ ) {
                    showSaveDialog();
                }
            } );

            JMenuItem eval = new JMenuItem( "Evaluate" );
            eval.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent ae_ ) {
                    evaluateText();
                    repaint();
                }
            } );
            menubar.add( jMenuFile );
            jMenuFile.add( eval );
            jMenuFile.add( exit );
            jMenuFile.add( saveItem );
            jMenuFile.add( loadItem );

            setJMenuBar( menubar );
        }

        private void evaluateText() {
            interpreter.setClassLoader( Thread.currentThread().getContextClassLoader() );
            String sourcecode = editor.getText();
//            System.out.println( "Evaluating source string=" + sourcecode );
            topLabel.setText( "Evaluating..." );
            repaint();
//            Interpreter interpreter = new Interpreter();
            try {
                Object out = interpreter.eval( sourcecode );
                if( out != null ) {
                    bottomLabel.setText( "Result=" + out );
                }
                else {
                    bottomLabel.setText( "Null Result." );
                }
                topLabel.setText( "Press F12 to Evaluate." );
                repaint();
            }
            catch( EvalError evalError ) {
                writeError( evalError );
            }
        }

        private void writeError( EvalError evalError ) {

            StringWriter wr = new StringWriter();
            evalError.printStackTrace( new PrintWriter( wr ) );
//                String errorStr = wr.toString();
            output.append( "Evaluation Error: " + evalError.getMessage() + "\n" );
            output.append( "in line number: " + evalError.getErrorLineNumber() + "\n" );
            output.append( evalError.getScriptStackTrace() + "\n" );
            output.append( evalError.getErrorText() );
//                output.append( errorStr );
        }
    }

    static class BSHConsole implements ConsoleInterface {
        BSHOutputAdapter out;
        Reader reader = new StringReader( "456" );

        public BSHConsole( JTextArea output ) {
            this.out = new BSHOutputAdapter( output );
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

        public void println( Object o ) {
            out.println( o );
        }

        public void print( Object o ) {
            out.print( o );
        }

        public void error( Object o ) {
            out.print( o );
        }

    }

    static class BSHOutputAdapter extends PrintStream {
        JTextArea target;

        public BSHOutputAdapter( JTextArea target ) {
            this( target, new NullOutputStream() );
        }

        public BSHOutputAdapter( JTextArea target, OutputStream out ) {
            super( out );
            this.target = target;
        }

        public void print( String s ) {
            target.append( s );
            target.repaint();
        }

        public void println( String s ) {
            target.append( s + "\n" );
            target.repaint();
        }

        public void println( Object obj ) {
            if( obj == null ) {
                println( "null" );
            }
            else {
                println( obj.toString() );
            }
        }

        public void print( Object obj ) {
            if( obj == null ) {
                print( "null" );
            }
            else {
                print( obj.toString() );
            }
        }

        public void println() {
            print( "\n" );
        }

        public void print( int i ) {
            print( i + "" );
        }

        public void println( int i ) {
            println( "" + i );
        }

    }

}


