/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import org.jnode.driver.net.ethernet.spi.BasicEthernetDriver;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.DriverException;
import org.jnode.driver.Device;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.AccessControllerUtils;
import java.security.PrivilegedExceptionAction;

/**
 * @author Levente S\u00e1ntha
 */
public class ViaRhineDriver extends BasicEthernetDriver {
    /**
     * Create a new instance
     * @param config configuartion desc
     */
    public ViaRhineDriver(ConfigurationElement config) {
        this(new ViaRhineFlags(config));
    }

    public ViaRhineDriver(ViaRhineFlags flags) {
        this.flags = flags;
    }

    /**
     * Create a new ViaRhineCore instance
     */
    protected ViaRhineCore newCore(final Device device, final Flags flags)
            throws DriverException, ResourceNotFreeException {
        try {
            return AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction<ViaRhineCore>(){
                public ViaRhineCore run() throws Exception {
                    return new ViaRhineCore(ViaRhineDriver.this, device, device, flags);
                }
            });
        } catch (DriverException ex) {
            throw ex;
        } catch (ResourceNotFreeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DriverException(ex);
        }
    }
}
