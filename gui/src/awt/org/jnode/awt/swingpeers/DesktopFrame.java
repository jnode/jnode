/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeToolkit;
import org.jnode.test.gui.BoxWorld;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DesktopFrame extends JFrame {
    private static final Color DESKTOP_BACKGROUND_COLOR = new Color(70, 130, 180);
    private static final String FRAME_ATTRIBUTE_NAME = "JNODE_DESKTOP_FRAME_ATTRIBUTE";
	private final JDesktopPane desktop;
    private final JPopupMenu mainPopup = new JPopupMenu("JNode desktop");
    private final JPopupMenu windowsPopup = new JPopupMenu("Window list");
	private final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Initialize this instance.
	 *
	 */
	public DesktopFrame(Dimension screenSize) {
		super("");
		setSize(screenSize);
        desktop = new JDesktopPane();

        configureMainPopup();
        configureActions();
        desktop.setBackground(DESKTOP_BACKGROUND_COLOR);
        getContentPane().add(desktop);
	}

    private void configureActions() {
        desktop.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent event) {
                System.out.println("desktop - mouse pressed");
                int button = event.getButton();
                if(button == MouseEvent.BUTTON1){
                    System.out.println("desktop - show mainpopup");
                    mainPopup.setLightWeightPopupEnabled(true);
                    mainPopup.show(desktop, event.getX(), event.getY());
                    log.info("LWPopup enabled: " + mainPopup.isLightWeightPopupEnabled());
                }else if(event.isPopupTrigger()){
                    System.out.println("desktop - show windowspopup");
                    windowsPopup.show(desktop, event.getX(), event.getY());
                }
            }
        });
        desktop.addContainerListener(new ContainerListener() {
            public void componentAdded(ContainerEvent event) {
                Component comp = event.getChild();
                if(comp instanceof JInternalFrame){
                    JInternalFrame jif = (JInternalFrame) comp;
                    AbstractAction action = new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            JInternalFrame jif = (JInternalFrame) ((JMenuItem)e.getSource()).getAction().getValue(FRAME_ATTRIBUTE_NAME);
                            try{
                                if(jif.isIcon()) jif.setIcon(false);
                                jif.setSelected(true);
                            }catch(PropertyVetoException x){
                                //ignore
                            }
                            jif.toFront();
                        }
                    };
                    action.putValue(Action.NAME, jif.getTitle());
                    action.putValue(FRAME_ATTRIBUTE_NAME, jif);
                    JMenuItem mi = new JMenuItem(action);
                    windowsPopup.add(mi);
                }
            }

            public void componentRemoved(ContainerEvent event) {
                Component comp = event.getChild();
                if(comp instanceof JInternalFrame){
                    for(int i = windowsPopup.getComponentCount(); i-- > 0;){
                        Component c = windowsPopup.getComponent(i);
                        if(c instanceof JMenuItem){
                            JInternalFrame jif = (JInternalFrame) ((JMenuItem)c).getAction().getValue(FRAME_ATTRIBUTE_NAME);
                            if(jif == comp){
                                windowsPopup.remove(c);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void configureMainPopup() {
        JMenuItem mi = new JMenuItem("BoxWorld");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	runJavaMain("org.jnode.test.gui.BoxWorld");
            }
        });
        mainPopup.add(mi);
        mi = new JMenuItem("Tetris");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               	runJavaMain("org.jnode.test.gui.Tetris");
            }
        });
        mainPopup.add(mi);
        mainPopup.addSeparator();
        mi = new JMenuItem("Close");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JNodeToolkit tk = (JNodeToolkit) Toolkit.getDefaultToolkit();
                tk.decRefCount(true);
            }
        });
        mainPopup.add(mi);
        desktop.add(mainPopup);
        desktop.add(windowsPopup);
    }
    
    private void runJavaMain(String className) {
    	try {
    		final Class cls = Thread.currentThread().getContextClassLoader().loadClass(className);
    		Class[] types = { String[].class };
    		Method m = cls.getMethod("main", types);
    		Object[] args = { new String[0] };
    		m.invoke(null, args);
    	} catch (ClassNotFoundException ex) {
    		log.error(ex);
    	} catch (SecurityException ex) {
    		log.error(ex);
		} catch (NoSuchMethodException ex) {
    		log.error(ex);
		} catch (IllegalAccessException ex) {
    		log.error(ex);
		} catch (InvocationTargetException ex) {
    		log.error(ex);
		}
    }

    /**
	 * @return Returns the desktop.
	 */
	final JDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * @see javax.swing.JFrame#frameInit()
	 */
	protected void frameInit() {
		super.setLayout(new BorderLayout());
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		getRootPane(); // will do set/create
	}
}
