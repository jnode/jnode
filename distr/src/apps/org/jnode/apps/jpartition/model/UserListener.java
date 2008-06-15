package org.jnode.apps.jpartition.model;

public interface UserListener {

    void selectionChanged(Device selectedDevice);

    void deviceAdded(String name);

    void deviceRemoved(String name);

}
