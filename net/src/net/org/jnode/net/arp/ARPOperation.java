package org.jnode.net.arp;


public enum ARPOperation {
	ARP_REQUEST (1),
    ARP_REPLY (2),
    RARP_REQUEST (3),
    RARP_REPLY (4);
	
	private int id;
	
	private ARPOperation(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public static ARPOperation getType(int id){
		for(ARPOperation t : ARPOperation.values()){
			return t;
		}
		return null;
	}
}
