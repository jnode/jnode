package org.jnode.driver.console.swing;

import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.Console;
import org.jnode.driver.textscreen.swing.SwingTextScreenManager;
import org.jnode.driver.textscreen.swing.SwingPcTextScreen;
import org.jnode.driver.DeviceManager;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Levente S\u00e1ntha
 */
public class SwingTextScreenConsoleManager extends TextScreenConsoleManager {
    private JFrame frame;
    private SwingTextScreenManager textScreenManager;

    public SwingTextScreenConsoleManager() throws ConsoleException {

    }

    protected void openInput(DeviceManager devMan) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                SwingPcTextScreen systemScreen = getTextScreenManager().getSystemScreen();
                final JComponent screen = systemScreen.getScreenComponent();
                initializeKeyboard(systemScreen.getKeyboardDevice());
                addPointerDevice(systemScreen.getPointerDevice());
                frame = new JFrame("Console");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter(){
                    public void windowClosed(WindowEvent e) {
                        closeAll();
                    }
                });
                frame.setLayout(new BorderLayout());
                frame.add(screen, BorderLayout.CENTER);
                frame.pack();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frame.setVisible(true);
                        screen.requestFocus();
                    }
                });

                return null;
            }
        });
    }

    public void unregisterConsole(Console console) {
        super.unregisterConsole(console);
        if(getFocus() == null)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textScreenManager.getSystemScreen().close();
                frame.dispose();
            }
        });
    }

    protected SwingTextScreenManager getTextScreenManager() {
        if(textScreenManager == null){
            this.textScreenManager = new SwingTextScreenManager();
        }
        return textScreenManager;
    }

    public JFrame getFrame() {
        return frame;
    }
}
