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

package org.jnode.desktop.classic;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeToolkit;
import org.jnode.plugin.ExtensionPoint;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.LookAndFeel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Levente S\u00e1ntha
 */
public class TaskBar extends JPanel {
    final static Logger log = Logger.getLogger(TaskBar.class);
    private final static Class[] mainTypes = {String[].class};
    JButton startButton;
    JPopupMenu startMenu;
    WindowBar windowBar;
    JMenuItem quitMI;
    JMenuItem haltMI;
    JMenuItem restartMI;
    JMenuItem desktopColorMI;
    JMenuItem changeResMI1;
    JMenuItem changeResMI2;
    JMenuItem changeResMI3;
    JMenuItem changeResMI4;
    Clock clock;

    public TaskBar(ExtensionPoint appsEP) {
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        layout.setVgap(0);
        setLayout(layout);
        setBorder(new BevelBorder(BevelBorder.RAISED));
        startButton = new JButton("Start");
        startButton.setBorder(new EmptyBorder(1, 3, 1, 3));

        add(startButton, BorderLayout.WEST);
        startMenu = new JPopupMenu();

        JMenu awtMenu = new JMenu("AWT tests");
        startMenu.add(awtMenu);
        awtMenu.add(createMenuItem("AWTDemo", "org.jnode.test.gui.AWTDemo"));
        awtMenu.add(createMenuItem("AWTFrameTest", "org.jnode.test.gui.AWTFrameTest"));
        awtMenu.add(createMenuItem("AWTMenuBuilderTest", "org.jnode.test.gui.AWTMenuBuilderTest"));
        awtMenu.add(createMenuItem("AWTMenuTest", "org.jnode.test.gui.AWTMenuTest"));
        awtMenu.add(createMenuItem("AWTTest", "org.jnode.test.gui.AWTTest"));
        awtMenu.add(createMenuItem("AWTTest2", "org.jnode.test.gui.AWTTest2"));
        awtMenu.add(createMenuItem("RobotTest", "org.jnode.test.gui.RobotTest"));

        JMenu swingMenu = new JMenu("Swing tests");
        startMenu.add(swingMenu);
        swingMenu.add(createMenuItem("Editor", "org.jnode.test.gui.Editor"));
        swingMenu.add(createMenuItem("SwingTest", "org.jnode.test.gui.SwingTest"));
        swingMenu.add(createMenuItem("JTableTest", "org.jnode.test.gui.JTableTest"));
        swingMenu.add(createMenuItem("JTreeTest", "org.jnode.test.gui.JTreeTest"));
        swingMenu.add(createMenuItem("JInternalFrameTest", "org.jnode.test.gui.JInternalFrameTest"));
        swingMenu.add(createMenuItem("JDPTest", "org.jnode.test.gui.JDPTest"));

        JMenu gamesMenu = new JMenu("Games");
        startMenu.add(gamesMenu);

        gamesMenu.add(createMenuItem("Tetris", "org.jnode.test.gui.Tetris"));
        gamesMenu.add(createMenuItem("BoxWorld", "org.jnode.test.gui.BoxWorld"));
        gamesMenu.add(createMenuItem("Rubik", "org.jnode.test.gui.Rubik"));

        JMenu toolsMenu = new JMenu("Tools");
        startMenu.add(toolsMenu);

        toolsMenu.add(createMenuItem("Console", "org.jnode.apps.console.SwingConsole"));

        JMenu settingsMenu = new JMenu("Settings");
        startMenu.add(settingsMenu);
        settingsMenu.addSeparator();
        settingsMenu.add(desktopColorMI = new JMenuItem("Desktop color"));
        settingsMenu.add(changeResMI1 = new JMenuItem("Set to 640x480/32"));
        settingsMenu.add(changeResMI2 = new JMenuItem("Set to 800x600/32"));
        settingsMenu.add(changeResMI3 = new JMenuItem("Set to 1024x768/32"));
        settingsMenu.add(changeResMI4 = new JMenuItem("Set to 1280x1024/32"));
        settingsMenu.addSeparator();
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        for (int i = 0; i < lafs.length; ++i) {
            final UIManager.LookAndFeelInfo laf = lafs[i];
            JMenuItem item = new JMenuItem(laf.getName());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        Class c = Thread.currentThread().getContextClassLoader().loadClass(laf.getClassName());
                        UIManager.setLookAndFeel((LookAndFeel) c.newInstance());
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    SwingUtilities.updateComponentTreeUI((Component) JNodeToolkit.getJNodeToolkit().getAwtContext());
                }
            });
            settingsMenu.add(item);
        }
        settingsMenu.addSeparator();
        JMenuItem metal_theme = new JMenuItem("Metal");
        settingsMenu.add(metal_theme);
        metal_theme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                try {
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                } catch (UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                SwingUtilities.updateComponentTreeUI((Component) JNodeToolkit.getJNodeToolkit().getAwtContext());
            }
        });
        JMenuItem ocean_theme = new JMenuItem("Ocean");
        settingsMenu.add(ocean_theme);
        ocean_theme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                MetalLookAndFeel.setCurrentTheme(new OceanTheme());
                try {
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                } catch (UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                SwingUtilities.updateComponentTreeUI((Component) JNodeToolkit.getJNodeToolkit().getAwtContext());
            }
        });

        JMenu exitMenu = new JMenu("Exit");
        startMenu.add(exitMenu);
        exitMenu.add(quitMI = new JMenuItem("Quit"));
        exitMenu.add(restartMI = new JMenuItem("Restart"));
        exitMenu.add(haltMI = new JMenuItem("Halt"));
        windowBar = new WindowBar();
        add(windowBar, BorderLayout.CENTER);
        clock = new Clock();
        add(clock, BorderLayout.EAST);
    }

    private JMenuItem createMenuItem(final String label, final String classname) {
        JMenuItem mi = new JMenuItem(label);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startApp(label, classname);
            }
        });
        return mi;
    }

    final void startApp(final String name, final String className) {
        try {
            final Runnable runner = new Runnable() {
                public void run() {
                    try {
                        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        final Class<?> cls = cl.loadClass(className);
                        final Method main = cls.getMethod("main", mainTypes);
                        final Object[] args = {new String[0]};
                        main.invoke(null, args);
                    } catch (SecurityException ex) {
                        log.error("Security exception in starting class " + className, ex);
                    } catch (ClassNotFoundException ex) {
                        log.error("Cannot find class " + className);
                    } catch (NoSuchMethodException ex) {
                        log.error("Cannot find main method in " + className);
                    } catch (IllegalAccessException ex) {
                        log.error("Cannot access main method in " + className);
                    } catch (InvocationTargetException ex) {
                        log.error("Error in " + className, ex.getTargetException());
                    }
                }
            };
            final Thread t = new Thread(runner);
            t.start();
        } catch (SecurityException ex) {
            log.error("Security exception in starting class " + className, ex);
        }
    }

    static class Clock extends JLabel {
        private Timer timer;
        private TimerTask task;

        Clock() {
            setBorder(new EmptyBorder(1, 1, 1, 5));
            timer = new Timer(true);
            startTimer();
        }

        public void setVisible(boolean v) {
            if (v) {
                if (task == null) {
                    startTimer();
                }
            } else {
                if (task != null) {
                    task.cancel();
                    task = null;
                }
            }
            super.setVisible(v);
        }

        private void startTimer() {
            timer.schedule(task = new TimerTask() {
                Calendar calendar = new GregorianCalendar();

                public void run() {
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    final int h = calendar.get(Calendar.HOUR_OF_DAY);
                    final int m = calendar.get(Calendar.MINUTE);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // insure that the modification of swing component
                            // is done in the awt event dispatcher thread
                            setText((h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m);
                        }
                    });
                }
            }, 0, 60 * 1000);
        }

        void stop(){
            timer.cancel();
        }
    }
}
