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
		
		int id_count = 0;
		int seq_count = 0;
		while(tthis.count != 0){
			if (!tthis.flood)
			tthis.wait = true;
			
			SocketBuffer packet = new SocketBuffer();
			packet.insert(tthis.size);
			ICMPEchoHeader transportHeader = new ICMPEchoHeader(8, id_count, seq_count);
			transportHeader.prefixTo(packet);
			
			Request r = new Request(System.currentTimeMillis(), id_count, seq_count);
			Request.registerRequest(r);
			netLayer.transmit(netHeader, packet);
			tthis.timer.schedule(r, tthis.timeout);
			
			while(tthis.wait){
				long time = System.currentTimeMillis() - r.timestamp;
				if (time > tthis.interval){
					tthis.wait = false;
				}
			}
			tthis.count--;
			seq_count++;
		}
		
		icmpProtocol.removeListener(tthis);
	}
	
	private long match(int id, int seq){
		Request r = Request.getRequest(seq);
		if ((r != null) && (id == r.id))
		return r.timestamp;
		else
		return -1;
	}
	
	public void packetReceived(SocketBuffer skbuf) {
		long received = System.currentTimeMillis();
		IPv4Header hdr1 = (IPv4Header)skbuf.getNetworkLayerHeader();
		ICMPEchoHeader hdr2 = (ICMPEchoHeader)skbuf.getTransportLayerHeader();
		
		long timestamp = match(hdr2.getIdentifier() ,hdr2.getSeqNumber());
		
		if (timestamp != -1){
			System.out.print("Reply from 127.0.0.1: ");
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
	long timestamp;
	int id, seq;
	
	public Request(long timestamp, int id, int seq){
		this.timestamp = timestamp;
		this.id = id;
		this.seq = seq;
	}
	
	public static void registerRequest(Request r){
		requests.put(new Integer(r.seq), r);
	}
	public static Request getRequest(int seq){
		return (Request)requests.get(new Integer(seq));
	}
	
	public void run() {
		requests.remove(new Integer(this.seq));
	}
}