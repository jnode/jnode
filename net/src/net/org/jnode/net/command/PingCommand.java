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
	private Timer timer = new Timer();
	private boolean wait = true;
	
	private int count = 4;
	private boolean dontFragment = false;
	private IPv4Address dst = new IPv4Address("127.0.0.1");
	private boolean flood = false;
	private int interval = 20000;
	private int size = 64;
	private long timeout = 15000;
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
		
		Statistics stat = new Statistics();
		int id_count = 0;
		int seq_count = 0;
		while(tthis.count != 0){
			if (!tthis.flood)
			tthis.wait = true;
			
			SocketBuffer packet = new SocketBuffer();
			packet.insert(tthis.size);
			ICMPEchoHeader transportHeader = new ICMPEchoHeader(8, id_count, seq_count);
			transportHeader.prefixTo(packet);
			
			Request r = new Request(stat, System.currentTimeMillis(), id_count, seq_count);
			Request.registerRequest(r);
			netLayer.transmit(netHeader, packet);
			tthis.timer.schedule(r, tthis.timeout);
			
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
		System.out.println(stat.getStatistics());
	}
	
	private long match(int id, int seq){
		Request r = Request.getRequest(seq);
		if ((r != null) && (id == r.getId()))
		return r.getTimestamp();
		else
		return -1;
	}
	
	public void packetReceived(SocketBuffer skbuf) {
		long received = System.currentTimeMillis();
		IPv4Header hdr1 = (IPv4Header)skbuf.getNetworkLayerHeader();
		ICMPEchoHeader hdr2 = (ICMPEchoHeader)skbuf.getTransportLayerHeader();
		
		long timestamp = match(hdr2.getIdentifier() ,hdr2.getSeqNumber());
		
		if (timestamp != -1){
			System.out.print("Reply from "+ dst.toString() +": ");
			System.out.print(hdr1.getDataLength()-8 +"bytes of data ");
			System.out.print("ttl="+ hdr1.getTtl()+" ");
			System.out.print("seq="+ hdr2.getSeqNumber()+" ");
			System.out.print("time=" + (received-timestamp) + "ms");
			System.out.println();
		}
		wait = false;
	}
}

class Request extends TimerTask{
	private static Hashtable requests = new Hashtable();
	private Statistics stat;
	private long timestamp;
	private int id, seq;
	
	Request(Statistics stat, long timestamp, int id, int seq){
		this.stat = stat;
		this.timestamp = timestamp;
		this.id = id;
		this.seq = seq;
	}
	
	static void registerRequest(Request r){
		requests.put(new Integer(r.seq), r);
	}
	static Request getRequest(int seq){
		return (Request)requests.get(new Integer(seq));
	}
	
	static boolean isEmpty(){
		return requests.isEmpty();
	}
	
	public void run() {
		requests.remove(new Integer(this.seq));
		stat.recordLost();
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
	private long  received=0, lost=0;
	private long min=0, max=Integer.MAX_VALUE;
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
		long packets = received+lost;
		long percent = 0;
		if (packets != 0){
			percent = (lost/packets)*100;
		}
		long avg = sum/packets;
		return new String(
			packets +" packets transmitted, "+ 
			received +" packets received, "+ 
			percent +"% packet loss\n"+
			"round-trip min/avg/max = "+ min +"/"+ avg +"/"+ max +" ms"
		);
	}
}