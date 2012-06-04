/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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

import gnu.java.security.action.SetPropertyAction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeGraphicsConfiguration;
import org.jnode.awt.JNodeToolkit;
import org.jnode.plugin.ExtensionPoint;

/**
 * @author Levente S\u00e1ntha
 */
public class TaskBar extends JPanel {
    private static final long serialVersionUID = 1L;
    static final Logger log = Logger.getLogger(TaskBar.class);
    private static final Class<?>[] mainTypes = {String[].class};
    JButton startButton;
    JPopupMenu startMenu;
    WindowBar windowBar;
    JMenuItem quitMI;
    JMenuItem haltMI;
    JMenuItem restartMI;
    JMenuItem desktopColorMI;
    Desktop desktop;
    Clock clock;
    JMenuItem[] chgSizeMenuItem;

    public TaskBar(Desktop desk, ExtensionPoint appsEP) {
        this.desktop = desk;
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        layout.setVgap(0);
        setLayout(layout);
        setBorder(new BevelBorder(BevelBorder.RAISED));
        startButton = new JButton("JNode", new ImageIcon(Desktop.loadImage("jnode_icon.png")));
        startButton.setToolTipText("JNode Menu");
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

        gamesMenu.add(createMenuItem("Tetris", "org.jnode.games.tetris.Tetris"));
        gamesMenu.add(createMenuItem("BoxWorld", "org.jnode.games.boxworld.BoxWorld"));
        gamesMenu.add(createMenuItem("Rubik", "org.jnode.games.rubik.Rubik"));

        JMenu toolsMenu = new JMenu("Tools");
        startMenu.add(toolsMenu);

        toolsMenu.add(createMenuItem("Console", "org.jnode.apps.console.SwingConsole"));

        JMenu settingsMenu = new JMenu("Settings");
        startMenu.add(settingsMenu);
        JMenu resMenu = new JMenu("Screen Resolution");
        settingsMenu.add(resMenu);
        
        int i = 0;
        GraphicsConfiguration[] configs = ((JNodeToolkit) Toolkit.getDefaultToolkit()).getConfigurations();
        chgSizeMenuItem = new JMenuItem[configs.length];
        for (GraphicsConfiguration config : configs) {
            Action a = new ChangeScreenResolution((JNodeGraphicsConfiguration) config);
            resMenu.add(new JMenuItem(a));
            chgSizeMenuItem[i++] = new JMenuItem(a);
        }

        JMenu lfMenu = new JMenu("Look & Feel");
        settingsMenu.add(lfMenu);
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        for (i = 0; i < lafs.length; ++i) {
            final UIManager.LookAndFeelInfo laf = lafs[i];
            String name = laf.getName();
            if (!"Metal".equals(name)) {
                JMenuItem item = new JMenuItem(name);
                item.addActionListener(new SetLFAction(laf));
                lfMenu.add(item);
            }
        }

        //todo fix nimbus startup
//        JMenuItem nimbus = new JMenuItem("Nimbus");
//        lfMenu.add(nimbus);
//        nimbus.addActionListener(new SetLFAction("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel") {
//            public void actionPerformed(ActionEvent e) {
//                super.actionPerformed(e);
//            }
//        });

        JMenuItem metal_theme = new JMenuItem("Metal Default");
        lfMenu.add(metal_theme);
        metal_theme.addActionListener(new SetLFAction("javax.swing.plaf.metal.MetalLookAndFeel") {
            public void actionPerformed(ActionEvent e) {
                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                super.actionPerformed(e);
            }
        });

        JMenuItem ocean_theme = new JMenuItem("Metal Ocean");
        lfMenu.add(ocean_theme);
        ocean_theme.addActionListener(new SetLFAction("javax.swing.plaf.metal.MetalLookAndFeel") {
            public void actionPerformed(ActionEvent e) {
                MetalLookAndFeel.setCurrentTheme(new OceanTheme());
                super.actionPerformed(e);
            }
        });
        settingsMenu.add(desktopColorMI = new JMenuItem("Desktop color"));
        final JCheckBoxMenuItem desktopImage = new JCheckBoxMenuItem("Desktop image");
        desktopImage.setSelected(false);
        desktopImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desktop.enableBackgroundImage(desktopImage.isSelected());
            }
        });
        settingsMenu.add(desktopImage);
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
    
    private class SetLFAction implements ActionListener {
        private LookAndFeel lf;
        private final String lfName;

        public SetLFAction(UIManager.LookAndFeelInfo lfInfo) {
            this.lfName = lfInfo.getClassName();
        }

        public SetLFAction(String lfName) {
            this.lfName = lfName;
        }

        public void actionPerformed(ActionEvent e) {
            if (lf == null) {
                try {
                    Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(lfName);
                    this.lf = (LookAndFeel) c.newInstance();
                } catch (Exception ex) {
                    log.error("Error crating look & feel " + lfName, ex);
                    return;
                }
            }

            try {
                UIManager.setLookAndFeel(lf);
            } catch (UnsupportedLookAndFeelException ex) {
                log.error("", ex);
                return;
            }
            final Color bg_color = desktop.desktopPane.getBackground();
            //TODO review this, Classpath plaf code is still instable failures can occure
            //so we try to minimise the effect of a failure
            try {
                SwingUtilities.updateComponentTreeUI(desktop.desktopFrame.getTopLevelRootComponent());
            } catch (Exception x) {
                log.error("", x);
            }
            try {
                SwingUtilities.updateComponentTreeUI(startMenu);
            } catch (Exception x) {
                log.error("", x);
            }
            try {
                SwingUtilities.updateComponentTreeUI(desktop.desktopMenu);
            } catch (Exception x) {
                log.error("", x);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    desktop.desktopPane.setBackground(bg_color);
                }
            });
        }
    }

    class ChangeScreenResolution extends AbstractAction implements Runnable {
        private static final long serialVersionUID = 1L;
        private final JNodeGraphicsConfiguration config;

        public ChangeScreenResolution(JNodeGraphicsConfiguration config) {
            super("Set to " + config.toString());
            this.config = config;
        }

        @Override
        public void run() {
            ((JNodeToolkit) Toolkit.getDefaultToolkit()).changeScreenSize(config);
            AccessController.doPrivileged(new SetPropertyAction("jnode.awt.screensize", config.toString()));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            SwingUtilities.invokeLater(this);
        }
    }

    private JMenuItem createMenuItem(final String label, final String classname) {
        final JMenuItem mi = new JMenuItem(label);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startApp(label, classname, mi);
            }
        });
        return mi;
    }

    final void startApp(final String name, final String className, final JMenuItem mi) {
        try {
            final Runnable runner = new Runnable() {
                public void run() {
                    try {
                        mi.setEnabled(false);
                        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        final Class<?> cls = cl.loadClass(className);
                        final Method main = cls.getMethod("main", mainTypes);
                        final Object[] args = {new String[0]};
                        main.invoke(null, args);
                        mi.setEnabled(true);
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
        private static final long serialVersionUID = 1L;
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

        void stop() {
            timer.cancel();
        }
    }
}
