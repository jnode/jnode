package org.jnode.driver.virtual;

import org.jnode.driver.Device;

/**
 * @author Levente S\u00e1ntha
 */
public class VirtualDevice extends Device {
    private final String initialName;

    public VirtualDevice(String id) {
        super(null, id);
        this.initialName = id;
    }

    String getInitialName() {
        return initialName;
    }
}
