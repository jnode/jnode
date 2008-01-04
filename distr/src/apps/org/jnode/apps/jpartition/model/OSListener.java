package org.jnode.apps.jpartition.model;

interface OSListener {

	void deviceAdded(Device addedDevice);

	void deviceRemoved(Device removedDevice);

}
