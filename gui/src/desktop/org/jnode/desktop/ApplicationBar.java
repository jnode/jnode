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
 
package org.jnode.desktop;

import java.awt.AWTError;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ApplicationBar extends JPanel {

    static final Logger log = Logger.getLogger(ApplicationBar.class);

    private final ExtensionPoint ep;

    private static final Class[] mainTypes = {String[].class};

    public ApplicationBar(ExtensionPoint ep) {
        this.ep = ep;
        setLayout(new GridBagLayout());
        setOpaque(false);

        reloadApps();
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

    private void reloadApps() {
        removeAll();
        final Extension[] exts = ep.getExtensions();
        final int cnt = exts.length;
        log.debug("Found " + cnt + " extensions");
        for (int i = 0; i < cnt; i++) {
            final Extension ext = exts[i];

            final ConfigurationElement[] elems = ext.getConfigurationElements();
            log.debug("Found " + elems.length + "ce's");
            for (int k = 0; k < elems.length; k++) {
                final ConfigurationElement ce = elems[k];
                if (ce.getName().equals("application")) {
                    final String name = ce.getAttribute("name");
                    final String className = ce.getAttribute("class");
                    if ((name != null) && (className != null)) {
                        log.debug("Adding app " + name);
                        addApp(name, new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                startApp(name, className);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * @see java.awt.Container#add(java.awt.Component)
     * @deprecated
     */
    public Component add(Component c) {
        throw new AWTError("Use addApp instead");
    }

    public void addApp(String label, ActionListener action) {
        final int cnt = getComponentCount();
        final JButton b = new JButton(label);
        b.addActionListener(action);
        final GridBagConstraints constraints = new GridBagConstraints();
        final int rowCount = 5; // TODO calculate on the fly
        constraints.gridx = cnt % rowCount;
        constraints.gridy = cnt / rowCount;
        constraints.ipadx = 5;
        constraints.ipadx = 5;
        constraints.insets = new Insets(2, 5, 2, 5);
        constraints.fill = GridBagConstraints.BOTH;
        super.add(b, constraints);
    }
}
