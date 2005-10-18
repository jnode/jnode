package org.jnode.desktop.classic;

import org.apache.log4j.Logger;
import org.jnode.plugin.ExtensionPoint;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
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

    public TaskBar(ExtensionPoint appsEP) {
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        layout.setVgap(0);
        setLayout(layout);
        setBorder(new BevelBorder(BevelBorder.RAISED));
        startButton = new JButton("Start");
        startButton.setBorder(new EmptyBorder(1,3,1,3));
        add(startButton, BorderLayout.WEST);
        startMenu = new JPopupMenu();

        JMenuItem mi = new JMenuItem("AWTDemo");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startApp("Tetris", "org.jnode.test.gui.AWTDemo");
            }
        });
        startMenu.add(mi);

        mi = new JMenuItem("AWTFrameTest");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startApp("Tetris", "org.jnode.test.gui.AWTFrameTest");
            }
        });
        startMenu.add(mi);

        mi = new JMenuItem("SwingTest");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startApp("Tetris", "org.jnode.test.gui.SwingTest");
            }
        });
        startMenu.add(mi);

        JMenu gamesMenu = new JMenu("Games");
        startMenu.add(gamesMenu);

        mi = new JMenuItem("Tetris");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startApp("Tetris", "org.jnode.test.gui.Tetris");
            }
        });
        gamesMenu.add(mi);

        mi = new JMenuItem("BoxWorld");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startApp("Tetris", "org.jnode.test.gui.BoxWorld");
            }
        });
        gamesMenu.add(mi);

        mi = new JMenuItem("Rubik");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startApp("Tetris", "org.jnode.test.gui.Rubik");
            }
        });
        gamesMenu.add(mi);

        /*
        startMenu.addSeparator();

        startMenu.add(desktopColorMI = new JMenuItem("Desktop color"));
        */


        startMenu.addSeparator();
        startMenu.add(quitMI = new JMenuItem("Quit"));
        startMenu.add(haltMI = new JMenuItem("Halt"));
        startMenu.add(restartMI = new JMenuItem("Restart"));
        windowBar = new WindowBar();
        add(windowBar, BorderLayout.CENTER);
        add(new Clock(), BorderLayout.EAST);
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

    private static class Clock extends JLabel {
        Timer timer;
        TimerTask task;

        public Clock() {
            setBorder(new EmptyBorder(1,1,1,5));
            timer = new Timer(true);
            startTimer();
        }

        public void setVisible(boolean v) {
            if (v) {
                if(task == null){
                    startTimer();
                }
            } else {
                if(task != null) {
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
                    int h = calendar.get(Calendar.HOUR_OF_DAY);
                    int m = calendar.get(Calendar.MINUTE);
                    setText((h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m);
                }
            }, 0, 60 * 1000);
        }
    }
}
