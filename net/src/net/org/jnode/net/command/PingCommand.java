/*
 * $Id$
 */

package org.jnode.net.command;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.icmp.ICMPEchoHeader;
import org.jnode.net.ipv4.icmp.ICMPListener;
import org.jnode.net.ipv4.icmp.ICMPProtocol;
import org.jnode.net.ipv4.layer.IPv4NetworkLayer;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author JPG
 */
public class PingCommand implements ICMPListener{
	private Statistics stat = new Statistics();
	private boolean wait = true;
	
	private int count = 4;
	private boolean dontFragment = false;
	private IPv4Address dst = new IPv4Address("127.0.0.1");
	private boolean flood = false;
	private int interval = 6000;
	private int size = 64;
	private long timeout = 5000;
	private int ttl = 255;

	static final HostArgument DST = new HostArgument("host", "the target host");

	public static Help.Info HELP_INFO = new Help.Info(
		"ping",
		new Syntax[]{
			new Syntax(
				"Ping the specified host",
				new Parameter[] {
					new Parameter(DST, Parameter.MANDATORY)
				}
			)
		}
	);

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);
		PingCommand tthis = new PingCommand();
		
		tthis.dst = DST.getAddress(cmdLine);
		
		IPv4Header netHeader = new IPv4Header(0, tthis.ttl, 1, tthis.dst, 8);
		netHeader.setDontFragment(tthis.dontFragment);
		
		IPv4NetworkLayer netLayer = (IPv4NetworkLayer)NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_IP);
		ICMPProtocol icmpProtocol = (ICMPProtocol)netLayer.getProtocol(ICMPProtocol.IPPROTO_ICMP);
		icmpProtocol.addListener(tthis);
		
		int id_count = 0;
		int seq_count = 0;
		while(tthis.count != 0){
			
			if (!tthis.flood)
			tthis.wait = true;
			
			SocketBuffer packet = new SocketBuffer();
			packet.insert(tthis.size);
			ICMPEchoHeader transportHeader = new ICMPEchoHeader(8, id_count, seq_count);
			transportHeader.prefixTo(packet);
			
			Request r = new Request(tthis.stat, tthis.timeout, System.currentTimeMillis(), id_count, seq_count);
			Request.registerRequest(r);
			netLayer.transmit(netHeader, packet);
			
			while(tthis.wait){
				long time = System.currentTimeMillis() - r.getTimestamp();
				if (time > tthis.interval){
					tthis.wait = false;
				}
			}
			tthis.count--;
			seq_count++;
		}
		
		while (!Request.isEmpty()){
		}
		icmpProtocol.removeListener(tthis);
		
		System.out.println("-> Packet statistics");
		System.out.println(tthis.stat.getStatistics());
	}
	
	private long match(int id, int seq, Request r){
		if ((r != null) && (id == r.getId()))
		return r.getTimestamp();
		else
		return -1;
	}
	
	public void packetReceived(SocketBuffer skbuf) {
		long received = System.currentTimeMillis();
		
		IPv4Header hdr1 = (IPv4Header)skbuf.getNetworkLayerHeader();
		ICMPEchoHeader hdr2 = (ICMPEchoHeader)skbuf.getTransportLayerHeader();
		
		int seq = hdr2.getSeqNumber();
		Request r = Request.removeRequest(seq);
		if ((r==null) || (r.Obsolete()))
		return;
		
		long timestamp = match(hdr2.getIdentifier() ,seq ,r);
		
		long roundtrip = received-timestamp;
		if (timestamp != -1){
			System.out.print("Reply from "+ dst.toString() +": ");
			System.out.print(hdr1.getDataLength()-8 +"bytes of data ");
			System.out.print("ttl="+ hdr1.getTtl()+" ");
			System.out.print("seq="+ hdr2.getSeqNumber()+" ");
			System.out.println("time=" + (roundtrip) + "ms");
		}
		wait = false;
		this.stat.recordPacket(roundtrip);
	}
}

class Request extends TimerTask{
	private static Hashtable requests = new Hashtable();
	private Timer timer = new Timer();
	private boolean obsolete = false;
	private Statistics stat;
	private long timestamp;
	private int id, seq;
	
	Request(Statistics stat, long timeout, long timestamp, int id, int seq){
		this.stat = stat;
		this.timestamp = timestamp;
		this.id = id;
		this.seq = seq;
		
		timer.schedule(this, timeout);
	}
	
	static void registerRequest(Request r){
		requests.put(new Integer(r.seq), r);
	}
	static Request getRequest(int seq){
		return (Request)requests.get(new Integer(seq));
	}
	static Request removeRequest(int seq){
		return (Request)requests.remove(new Integer(seq));
	}
	static boolean isEmpty(){
		return requests.isEmpty();
	}
	
	public void run() {
		if (!this.Obsolete()){
			stat.recordLost();
			Request.removeRequest(this.seq);
		}
	}
	
	synchronized boolean Obsolete(){
		if (!obsolete){
			this.obsolete = true;
			this.timer.cancel();
			return false;
		}else
		return true;
	}
	
	long getTimestamp() {
		return timestamp;
	}
	int getId() {
		return id;
	}
	int getSeq() {
		return seq;
	}
}

class Statistics{
	private int received=0, lost=0;
	private long min=Integer.MAX_VALUE, max=0;
	private long sum;
	
	void recordPacket(long roundtrip){
		received++;
		
		if (roundtrip < min)
		min = roundtrip;
		if (roundtrip > max)
		max = roundtrip;
		
		sum += roundtrip;
	}
	void recordLost(){
		lost++;
	}
	
	String getStatistics(){
		int packets = received+lost;
		//float percent = 0;
//		if (packets != 0){
//			percent = lost/packets;
//			percent *= 100;
//		}
		float avg = sum/packets;
		return new String(
			packets +" packets transmitted, "+ received +" packets received\n"+ 
//			percent +"% packet loss\n"+
			"round-trip min/avg/max = "+ min +"/"+ avg +"/"+ max +" ms"
		);
	}
}