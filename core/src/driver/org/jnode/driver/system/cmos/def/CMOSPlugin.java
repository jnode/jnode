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

package org.jnode.driver.system.cmos.def;

import java.security.PrivilegedExceptionAction;
import javax.naming.NamingException;
import org.jnode.driver.system.cmos.CMOSService;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.vm.VmSystem;

/**
 * @author epr
 */
public class CMOSPlugin extends Plugin implements CMOSService {

    /**
     * The CMOS accesser
     */
    private CMOS cmos;
    private final RTC rtc = new RTC(this);

    /**
     * Initialize a new instance
     *
     * @param descriptor
     */
    public CMOSPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Gets the value of a CMOS register with a given nr.
     *
     * @param regnr
     * @return The register value
     */
    public int getRegister(int regnr) {
        return cmos.getRegister(regnr);
    }

    /**
     * Start this plugin
     *
     * @throws PluginException
     */
    protected void startPlugin() throws PluginException {
        try {
            final ResourceOwner owner = new SimpleResourceOwner("CMOS");
            cmos = (CMOS) AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ResourceNotFreeException {
                    return new CMOS(owner);
                }
            });
            InitialNaming.bind(NAME, this);
            VmSystem.setRtcService(rtc);
        } catch (ResourceNotFreeException ex) {
            throw new PluginException("Cannot claim IO ports", ex);
        } catch (NamingException ex) {
            throw new PluginException("Cannot register service", ex);
        } catch (Exception ex) {
            throw new PluginException("Unknown exception", ex);
        }
    }

    /**
     * Stop this plugin
     *
     * @throws PluginException
     */
    protected void stopPlugin() throws PluginException {
        InitialNaming.unbind(NAME);
        VmSystem.resetRtcService(rtc);
        cmos.release();
        cmos = null;
    }
}
