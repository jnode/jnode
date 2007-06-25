/*
 * Copyright 1998-2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.jdi;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.io.IOException;

import sun.misc.Service; 			// slated to move to java.util in tiger.
import sun.misc.ServiceConfigurationError;

/* Public for use by com.sun.jdi.Bootstrap */
public class VirtualMachineManagerImpl implements VirtualMachineManagerService {
    private List connectors = new ArrayList();
    private LaunchingConnector defaultConnector = null;
    private List targets = new ArrayList();
    private List connectionListeners = new ArrayList();
    private final ThreadGroup mainGroupForJDI;
    private ResourceBundle messages = null;
    private int vmSequenceNumber = 0;
    private static final int majorVersion = 1;
    private static final int minorVersion = 6;

    private static final Object lock = new Object();
    private static VirtualMachineManagerImpl vmm;

    public static VirtualMachineManager virtualMachineManager() {
        SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    JDIPermission vmmPermission =
		new JDIPermission("virtualMachineManager");
	    sm.checkPermission(vmmPermission);
        }
	synchronized (lock) {
            if (vmm == null) {
		vmm = new VirtualMachineManagerImpl();
            }
	}
        return vmm;
    }

    protected VirtualMachineManagerImpl() {

	/* 
	 * Create a top-level thread group
         */
	ThreadGroup top = Thread.currentThread().getThreadGroup();
	ThreadGroup parent = null;
        while ((parent = top.getParent()) != null) {
	    top = parent;
        }
        mainGroupForJDI = new ThreadGroup(top, "JDI main");

   	/*
	 * Load the connectors
	 */
	Iterator connectors = Service.providers(Connector.class,
						Connector.class.getClassLoader());
	while (connectors.hasNext()) {
	    Connector connector;

	    try {
		connector = (Connector)connectors.next();
	    } catch (ThreadDeath x) {
		throw x;
	    } catch (Exception x) {
		System.err.println(x);
		continue;
	    } catch (Error x) {
		System.err.println(x);
		continue;
	    }

	    addConnector(connector);
	}

	/*
	 * Load any transport services and encapsulate them with 
  	 * an attaching and listening connector.
	 */
	Iterator transportServices = Service.providers(TransportService.class,
						       TransportService.class.getClassLoader());
	while (transportServices.hasNext()) {
	    TransportService transportService;

	    try {
		transportService = (TransportService)transportServices.next();
	    } catch (ThreadDeath x) {
		throw x;
	    } catch (Exception x) {
		System.err.println(x);
		continue;
	    } catch (Error x) {
		System.err.println(x);
		continue;
	    }
	
	    addConnector(GenericAttachingConnector.create(transportService));
	    addConnector(GenericListeningConnector.create(transportService));
	}

	// no connectors found
	if (allConnectors().size() == 0) {
	    throw new Error("no Connectors loaded");
	}

	// Set the default launcher. In order to be compatible
	// 1.2/1.3/1.4 we try to make the default launcher
	// "com.sun.jdi.CommandLineLaunch". If this connector
	// isn't found then we arbitarly pick the first connector.
	//
	boolean found = false;
	List launchers = launchingConnectors();
	Iterator i = launchers.iterator();
	while (i.hasNext()) {
	    LaunchingConnector lc = (LaunchingConnector)i.next();
	    if (lc.name().equals("com.sun.jdi.CommandLineLaunch")) {
		setDefaultConnector(lc);
		found = true;
		break;
	    }
	}
	if (!found && launchers.size() > 0) {
	    setDefaultConnector((LaunchingConnector)launchers.get(0));
	}

    }

    public LaunchingConnector defaultConnector() {
	if (defaultConnector == null) {
	    throw new Error("no default LaunchingConnector");
	}
        return defaultConnector;
    }

    public void setDefaultConnector(LaunchingConnector connector) {
        defaultConnector = connector;
    }

    public List launchingConnectors() {
        List launchingConnectors = new ArrayList(connectors.size());
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Object connector = iter.next();
            if (connector instanceof LaunchingConnector) {
                launchingConnectors.add(connector);
            }
        }
        return Collections.unmodifiableList(launchingConnectors);
    }

    public List attachingConnectors() {
        List attachingConnectors = new ArrayList(connectors.size());
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Object connector = iter.next();
            if (connector instanceof AttachingConnector) {
                attachingConnectors.add(connector);
            }
        }
        return Collections.unmodifiableList(attachingConnectors);
    }

    public List listeningConnectors() {
        List listeningConnectors = new ArrayList(connectors.size());
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Object connector = iter.next();
            if (connector instanceof ListeningConnector) {
                listeningConnectors.add(connector);
            }
        }
        return Collections.unmodifiableList(listeningConnectors);
    }

    public List allConnectors() {
        return Collections.unmodifiableList(connectors);
    }

    public List connectedVirtualMachines() {
        return Collections.unmodifiableList(targets);
    }

    public void addConnector(Connector connector) {
        connectors.add(connector);
    }

    public void removeConnector(Connector connector) {
        connectors.remove(connector);
    }

    public synchronized VirtualMachine createVirtualMachine(
                                        Connection connection,
                                        Process process) throws IOException {

	if (!connection.isOpen()) {
	    throw new IllegalStateException("connection is not open");
	}

        VirtualMachine vm;
	try {
	    vm = new VirtualMachineImpl(this, connection, process,
                                                   ++vmSequenceNumber);
	} catch (VMDisconnectedException e) {
	    throw new IOException(e.getMessage());
	}
   	targets.add(vm);
        return vm;
    }

    public VirtualMachine createVirtualMachine(Connection connection) throws IOException {
        return createVirtualMachine(connection, null);
    }

    public void addVirtualMachine(VirtualMachine vm) {
        targets.add(vm);
    }

    void disposeVirtualMachine(VirtualMachine vm) {
        targets.remove(vm);
    }

    public int majorInterfaceVersion() {
        return majorVersion;
    }

    public int minorInterfaceVersion() {
        return minorVersion;
    }

    ThreadGroup mainGroupForJDI() {
	return mainGroupForJDI;
    }

    String getString(String key) {
        if (messages == null) { 
            messages = ResourceBundle.getBundle("com.sun.tools.jdi.resources.jdi");
        }
        return messages.getString(key);
    }

}
