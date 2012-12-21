package org.jnode.net.ipv4.icmp;

public enum ICMPType {
	ICMP_ECHOREPLY(0), /* Echo Reply */
	ICMP_DEST_UNREACH(3), /* Destination Unreachable */
	ICMP_SOURCE_QUENCH(4), /* Source Quench */
	ICMP_REDIRECT(5), /* Redirect (change route) */
	ICMP_ECHO(8), /* Echo Request */
	ICMP_TIME_EXCEEDED(11), /* Time Exceeded */
	ICMP_PARAMETERPROB(12), /* Parameter Problem */
	ICMP_TIMESTAMP(13), /* Timestamp Request */
	ICMP_TIMESTAMPREPLY(14), /* Timestamp Reply */
	ICMP_INFO_REQUEST(15), /* Information Request */
	ICMP_INFO_REPLY(16), /* Information Reply */
	ICMP_ADDRESS(17), /* Address Mask Request */
	ICMP_ADDRESSREPLY(18); /* Address Mask Reply */

	private int id;

	private ICMPType(int id){
    	this.id = id;
    }

	public int getId() {
		return id;
	}
	
	public static ICMPType getType(int id){
		for(ICMPType t : ICMPType.values()){
			return t;
		}
		return null;
	}

}
