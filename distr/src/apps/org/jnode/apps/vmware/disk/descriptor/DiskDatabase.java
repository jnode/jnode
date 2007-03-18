package org.jnode.apps.vmware.disk.descriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class DiskDatabase {
	private AdapterType adapterType;
	private int sectors;
	private int heads;
	private int cylinders;
	public AdapterType getAdapterType() {
		return adapterType;
	}
	public void setAdapterType(AdapterType adapterType) {
		this.adapterType = adapterType;
	}
	public int getSectors() {
		return sectors;
	}
	public void setSectors(int sectors) {
		this.sectors = sectors;
	}
	public int getHeads() {
		return heads;
	}
	public void setHeads(int heads) {
		this.heads = heads;
	}
	public int getCylinders() {
		return cylinders;
	}
	public void setCylinders(int cylinders) {
		this.cylinders = cylinders;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DiskDatabase))
		{
			return false;
		}
		
		DiskDatabase d = (DiskDatabase) obj;

		return (this.adapterType == d.adapterType) &&
				(this.sectors == d.sectors) &&
				(this.heads == d.heads) &&
				(this.cylinders == d.cylinders);
	}
	
	@Override
	public String toString() {
		return "DiskDatabase[adapterType:"+adapterType+
				",sectors:"+sectors+
				",heads:"+heads+
				",cylinders:"+cylinders+"]";
	}
}
