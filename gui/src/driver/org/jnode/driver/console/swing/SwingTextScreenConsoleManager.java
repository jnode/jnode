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
 
package org.jnode.driver.console.swing;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.driver.textscreen.swing.SwingPcTextScreen;
import org.jnode.driver.textscreen.swing.SwingTextScreenManager;

/**
 * @author Levente S\u00e1ntha
 */
public class SwingTextScreenConsoleManager extends TextScreenConsoleManager {
    private JFrame frame;
    private SwingTextScreenManager textScreenManager;

    public SwingTextScreenConsoleManager() throws ConsoleException {

    }

    @Override
    protected void openInput(DeviceManager devMan) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                SwingPcTextScreen systemScreen = getTextScreenManager().getSystemScreen();
                final JComponent screen = systemScreen.getScreenComponent();
                initializeKeyboard(systemScreen.getKeyboardDevice());
                addPointerDevice(systemScreen.getPointerDevice());
                frame = new JFrame("Console");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
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

    @Override
    public void unregisterConsole(Console console) {
        super.unregisterConsole(console);
        if (getFocus() == null)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textScreenManager.getSystemScreen().close();
                    frame.dispose();
                }
            });
    }

    @Override
    protected SwingTextScreenManager getTextScreenManager() {
        if (textScreenManager == null) {
            this.textScreenManager = new SwingTextScreenManager();
        }
        return textScreenManager;
    }

    public JFrame getFrame() {
        return frame;
    }
}
