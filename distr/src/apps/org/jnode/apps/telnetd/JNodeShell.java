package org.jnode.apps.telnetd;

import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;

import org.apache.log4j.Logger;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.textscreen.ScrollableTextScreenConsole;
import org.jnode.shell.CommandShell;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class JNodeShell implements Shell {
    private static final RemoteConsoleManager CONSOLE_MANAGER;

    static {
        try {
            CONSOLE_MANAGER = new RemoteConsoleManager();
        } catch (ConsoleException e) {
            throw new RuntimeException("can't create RemoteConsoleManager", e);
        }
    }

    private static final Logger log = Logger.getLogger(JNodeShell.class);

    private CommandShell commandShell;
    private Connection connection;
    private TerminalIO terminalIO;
    private ScrollableTextScreenConsole console;

    // private Editfield m_EF;

    /**
     * Method that runs a shell
     * 
     * @param con Connection that runs the shell.
     */
    public void run(Connection con) {
        try {
            connection = con;
            // mycon.setNextShell("nothing");
            terminalIO = (TerminalIO) connection.getTerminalIO();

            // dont forget to register listener
            connection.addConnectionListener(this);

            // clear the screen and start from zero
            terminalIO.eraseScreen();
            terminalIO.homeCursor();

            // We just read any key
            terminalIO
                    .write("Welcome to JNode telnet server. Thanks for connecting.\r\n"
                            + "You can type any jnode command and " 
                            + "press enter **2 times**(it's a bug!!!) to execute it.\r\n"
                            + "Use the exit command to logout!\r\n"); // some
            // output
            terminalIO.flush();

            final String name = connection.getConnectionData().getHostName();
            synchronized (CONSOLE_MANAGER) {
                CONSOLE_MANAGER.setTerminalIO(terminalIO);
                console =
                        (ScrollableTextScreenConsole) CONSOLE_MANAGER.createConsole(name,
                                ConsoleManager.CreateOptions.TEXT |
                                        ConsoleManager.CreateOptions.SCROLLABLE);
            }
            CONSOLE_MANAGER.focus(console);

            // final RemoteTextScreen screen = new RemoteTextScreen(terminalIO);
            // final ScrollableTextScreen scrollScreen =
            // screen.createCompatibleScrollableBufferScreen(terminalIO.getRows()*10);
            // console = new ScrollableTextScreenConsole(CONSOLE_MANAGER, name,
            // scrollScreen, ConsoleManager.CreateOptions.TEXT
            // | ConsoleManager.CreateOptions.SCROLLABLE);
            // CONSOLE_MANAGER.registerConsole(console);

            // InputStream in = new RemoteConsoleInputStream(terminalIO,
            // console);
            // PrintStream out = new PrintStream(new
            // RemoteConsoleOutputStream(terminalIO));
            // PrintStream err = out;
            // commandShell = new JNodeCommandShell(this, console, in, out,
            // err);
            commandShell = new JNodeCommandShell(console, this);

            commandShell.run();
            /*
             * boolean done = false; while (!done) { int i = m_IO.read(); if (i ==
             * -1 || i == -2) { log.debug("Input(Code):" + i); done = true; } if
             * (i == 10) { done = true; } else if (i == 117) {
             * 
             * ConnectionData cd = m_Connection.getConnectionData(); //output
             * header m_IO.write(BasicTerminalIO.CRLF + "DEBUG: Active
             * Connection" + BasicTerminalIO.CRLF);
             * m_IO.write("------------------------" + BasicTerminalIO.CRLF);
             * 
             * //output connection data m_IO.write("Connected from: " +
             * cd.getHostName() + "[" + cd.getHostAddress() + ":" + cd.getPort() +
             * "]" + BasicTerminalIO.CRLF); m_IO.write("Guessed Locale: " +
             * cd.getLocale() + BasicTerminalIO.CRLF);
             * m_IO.write(BasicTerminalIO.CRLF); //output negotiated terminal
             * properties m_IO.write("Negotiated Terminal Type: " +
             * cd.getNegotiatedTerminalType() + BasicTerminalIO.CRLF);
             * m_IO.write("Negotiated Columns: " + cd.getTerminalColumns() +
             * BasicTerminalIO.CRLF); m_IO.write("Negotiated Rows: " +
             * cd.getTerminalRows() + BasicTerminalIO.CRLF);
             * 
             * //output of assigned terminal instance (the cast is a hack,
             * please //do not copy for other TCommands, because it would break
             * the //decoupling of interface and implementation!
             * m_IO.write(BasicTerminalIO.CRLF); m_IO.write("Assigned Terminal
             * instance: " + ((TerminalIO) m_IO).getTerminal());
             * m_IO.write(BasicTerminalIO.CRLF); m_IO.write("Environment: " +
             * cd.getEnvironment().toString());
             * m_IO.write(BasicTerminalIO.CRLF); //output footer
             * m_IO.write("-----------------------------------------------" +
             * BasicTerminalIO.CRLF + BasicTerminalIO.CRLF);
             * 
             * m_IO.flush();
             *  } else if (i == 'e') { //run editfield test Label l = new
             * Label(m_IO, "testedit", "TestEdit: "); m_EF = new Editfield(m_IO,
             * "edit", 50); m_EF.registerInputFilter(new InputFilter() {
             * 
             * public int filterInput(int key) throws java.io.IOException { if
             * (key == 't') { try { m_EF.setValue("Test"); } catch
             * (BufferOverflowException e) { } return InputFilter.INPUT_HANDLED; }
             * else if (key == 'c') { m_EF.clear(); return
             * InputFilter.INPUT_HANDLED; } else {
             * 
             * return key; } } }); l.draw(); m_EF.run(); } else if (i == 116) {
             * //run test sequence
             * 
             * Pager pg = new Pager(m_IO); pg.setShowPosition(true);
             * pg.page(logo + logo + logo + logo + logo + logo + logo + logo +
             * logo + logo + logo);
             * 
             * Label l = new Label(m_IO, "label1"); l.setText("Hello World!");
             * l.setLocation(new Point(1, 5)); l.draw(); m_IO.flush();
             * 
             * m_IO.homeCursor(); m_IO.eraseScreen(); Titlebar tb = new
             * Titlebar(m_IO, "title 1"); tb.setTitleText("MyTitle");
             * tb.setAlignment(Titlebar.ALIGN_CENTER);
             * tb.setBackgroundColor(ColorHelper.BLUE);
             * tb.setForegroundColor(ColorHelper.YELLOW); tb.draw();
             * 
             * 
             * Statusbar sb = new Statusbar(m_IO, "status 1");
             * sb.setStatusText("MyStatus");
             * sb.setAlignment(Statusbar.ALIGN_LEFT);
             * sb.setBackgroundColor(ColorHelper.BLUE);
             * sb.setForegroundColor(ColorHelper.YELLOW); sb.draw();
             * 
             * m_IO.flush();
             * 
             * m_IO.setCursor(2, 1);
             * 
             * Selection sel = new Selection(m_IO, "selection 1"); String[] tn =
             * TerminalManager.getReference().getAvailableTerminals();
             * 
             * for (int n = 0; n < tn.length; n++) { sel.addOption(tn[n]); }
             * 
             * sel.setLocation(1, 10); sel.run();
             * 
             * Checkbox cb = new Checkbox(m_IO, "checkbox 1"); cb.setText("Check
             * me !"); cb.setLocation(1, 12); cb.run();
             * 
             * Editfield ef = new Editfield(m_IO, "editfield 1", 20);
             * ef.setLocation(1, 13); ef.run(); try { ef.setValue("SETVALUE!"); }
             * catch (Exception ex) { } Editfield ef2 = new Editfield(m_IO,
             * "editfield 2", 8); ef2.setLocation(1, 14);
             * ef2.setPasswordField(true); ef2.run();
             * 
             * log.debug("Your secret password was:" + ef2.getValue());
             * m_IO.flush();
             * 
             * //clear the screen and start from zero m_IO.eraseScreen();
             * m_IO.homeCursor(); //myio.flush(); Titlebar tb2 = new
             * Titlebar(m_IO, "title 1"); tb2.setTitleText("jEditor v0.1");
             * tb2.setAlignment(Titlebar.ALIGN_LEFT);
             * tb2.setBackgroundColor(ColorHelper.BLUE);
             * tb2.setForegroundColor(ColorHelper.YELLOW); tb2.draw();
             * 
             * Statusbar sb2 = new Statusbar(m_IO, "status 1");
             * sb2.setStatusText("Status");
             * sb2.setAlignment(Statusbar.ALIGN_LEFT);
             * sb2.setBackgroundColor(ColorHelper.BLUE);
             * sb2.setForegroundColor(ColorHelper.YELLOW); sb2.draw();
             * 
             * m_IO.setCursor(2, 1);
             * 
             * Editarea ea = new Editarea(m_IO, "area", m_IO.getRows() - 2,
             * 100); m_IO.flush(); ea.run(); log.debug(ea.getValue());
             * 
             * m_IO.eraseScreen(); m_IO.homeCursor(); m_IO.write("Dummy Shell.
             * Please press enter to logout!\r\n"); m_IO.flush();
             *  } //the next line is for debug reasons else
             * log.debug("Input(Code):" + i); } m_IO.homeCursor();
             * m_IO.eraseScreen(); m_IO.write("Goodbye!.\r\n\r\n");
             * m_IO.flush();
             */

        } catch (Exception ex) {
            log.error("run()", ex);
        }
    }// run

    void close() {
        try {
            CONSOLE_MANAGER.unregisterConsole(console);

            terminalIO.homeCursor();
            terminalIO.eraseScreen();
            terminalIO.write("Goodbye!.\r\n\r\n");
            terminalIO.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this implements the ConnectionListener!
    public void connectionTimedOut(ConnectionEvent ce) {
        try {
            terminalIO.write("CONNECTION_TIMEDOUT");
            terminalIO.flush();
            // close connection
            connection.close();
            console.close();
        } catch (Exception ex) {
            log.error("connectionTimedOut()", ex);
        }
    }// connectionTimedOut

    public void connectionIdle(ConnectionEvent ce) {
        try {
            terminalIO.write("CONNECTION_IDLE");
            terminalIO.flush();
        } catch (java.io.IOException e) {
            log.error("connectionIdle()", e);
        }

    }// connectionIdle

    public void connectionLogoutRequest(ConnectionEvent ce) {
        try {
            terminalIO.write("CONNECTION_LOGOUTREQUEST");
            terminalIO.flush();
            console.close();
        } catch (Exception ex) {
            log.error("connectionLogoutRequest()", ex);
        }
    }// connectionLogout

    public void connectionSentBreak(ConnectionEvent ce) {
        try {
            terminalIO.write("CONNECTION_BREAK");
            terminalIO.flush();
            console.close();
        } catch (Exception ex) {
            log.error("connectionSentBreak()", ex);
        }
    }// connectionSentBreak

    public static Shell createShell() {
        return new JNodeShell();
    }// createShell

    // Constants
    // private static final String logo =
    // "/***\n" +
    // "* \n" +
    // "* TelnetD library (embeddable telnet daemon)\n" +
    // "* Copyright (C) 2000-2005 Dieter Wimberger\n" +
    // "***/\n" +
    // "A looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
    // "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
    // "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
    // "ng line!";
}
