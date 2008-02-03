/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.net.command;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
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
import org.jnode.shell.help.argument.HostNameArgument;

/**
 * @author JPG
 */
public class PingCommand implements ICMPListener {

    private final Statistics stat = new Statistics();

    private boolean wait = true;

    private int count = 4;

    private boolean dontFragment = false;

    private final IPv4Address dst;

    private boolean flood = false;

    private int interval = 6000;

    private int size = 64;

    private long timeout = 5000;

    private int ttl = 255;

    static final HostNameArgument DST = new HostNameArgument("host", "the target host");

    public static Help.Info HELP_INFO = new Help.Info(
            "ping",
            new Syntax[] { new Syntax("Ping the specified host",
                    new Parameter[] { new Parameter(DST, Parameter.MANDATORY)})});

    public static void main(String[] args) throws Exception {
        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        final PingCommand cmd = new PingCommand(DST.getAddress(cmdLine));
        cmd.execute();
    }

    public PingCommand(InetAddress destination) {
        this.dst = new IPv4Address(destination);
    }

    public void execute() throws SocketException, InterruptedException {
        final IPv4Header netHeader = new IPv4Header(0, this.ttl, IPv4Constants.IPPROTO_ICMP, this.dst, 8);
        netHeader.setDontFragment(this.dontFragment);

        final IPv4NetworkLayer netLayer = (IPv4NetworkLayer) NetUtils.getNLM()
                .getNetworkLayer(EthernetConstants.ETH_P_IP);
        final ICMPProtocol icmpProtocol = (ICMPProtocol) netLayer
                .getProtocol(ICMPProtocol.IPPROTO_ICMP);
        icmpProtocol.addListener(this);
        try {
            int id_count = 0;
            int seq_count = 0;
            while (this.count != 0) {
                System.out.println("Ping " + dst + " attempt " + seq_count);

                if (!this.flood) this.wait = true;

                SocketBuffer packet = new SocketBuffer();
                packet.insert(this.size);
                ICMPEchoHeader transportHeader = new ICMPEchoHeader(8,
                        id_count, seq_count);
                transportHeader.prefixTo(packet);

                Request r = new Request(this.stat, this.timeout, System
                        .currentTimeMillis(), id_count, seq_count);
                registerRequest(r);
                netLayer.transmit(netHeader, packet);

                while (this.wait) {
                    long time = System.currentTimeMillis() - r.getTimestamp();
                    if (time > this.interval) {
                        this.wait = false;
                    }
                    Thread.sleep(500);
                    synchronized(this){
                        if(response){
                            System.out.print("Reply from " + dst.toString() + ": ");
                            System.out.print(hdr1.getDataLength() - 8 + "bytes of data ");
                            System.out.print("ttl=" + hdr1.getTtl() + " ");
                            System.out.print("seq=" + hdr2.getSeqNumber() + " ");
                            System.out.println("time=" + (roundt) + "ms");
                            response = false;
                        }
                    }
                }
                this.count--;
                seq_count++;
            }

            while (!isEmpty()) {
                Thread.sleep(100);
            }
        } finally {
            icmpProtocol.removeListener(this);
        }

        System.out.println("-> Packet statistics");
        System.out.println(this.stat.getStatistics());
    }

    private long match(int id, int seq, Request r) {
        if ((r != null) && (id == r.getId()))
            return r.getTimestamp();
        else
            return -1;
    }

    public void packetReceived(SocketBuffer skbuf) {
        long received = System.currentTimeMillis();

        IPv4Header hdr1 = (IPv4Header) skbuf.getNetworkLayerHeader();
        ICMPEchoHeader hdr2 = (ICMPEchoHeader) skbuf.getTransportLayerHeader();

        int seq = hdr2.getSeqNumber();
        Request r = removeRequest(seq);
        if ((r == null) || (r.Obsolete())) return;

        long timestamp = match(hdr2.getIdentifier(), seq, r);

        long roundtrip = received - timestamp;
        gotResponse(timestamp, hdr1, hdr2, roundtrip);
    }

    private synchronized void gotResponse(long timestamp, IPv4Header hdr1, ICMPEchoHeader hdr2, long roundtrip) {
        if (timestamp != -1) {
            this.hdr1 = hdr1;
            this.hdr2 = hdr2;
            this.roundt = roundtrip;
            response = true;
        }
        wait = false;
        this.stat.recordPacket(roundtrip);
    }
    
    //respose data
    private boolean response;
    private long roundt;
    private IPv4Header hdr1;
    private ICMPEchoHeader hdr2;

    //requests are tracked here
    private Map<Integer, Request> requests = new HashMap<Integer, Request>();
    private void registerRequest(Request r) {
        requests.put(r.seq, r);
    }

    private Request removeRequest(int seq) {
        return requests.remove(seq);
    }

    private boolean isEmpty() {
        return requests.isEmpty();
    }

    class Request extends TimerTask {

        private Timer timer = new Timer();

        private boolean obsolete = false;

        private Statistics stat;

        private long timestamp;

        private int id, seq;

        Request(Statistics stat, long timeout, long timestamp, int id, int seq) {
            this.stat = stat;
            this.timestamp = timestamp;
            this.id = id;
            this.seq = seq;

            timer.schedule(this, timeout);
        }


        public void run() {
            if (!this.Obsolete()) {
                stat.recordLost();
                removeRequest(this.seq);
            }
        }

        synchronized boolean Obsolete() {
            if (!obsolete) {
                this.obsolete = true;
                this.timer.cancel();
                return false;
            } else
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
}

class Statistics {

    private int received = 0, lost = 0;

    private long min = Integer.MAX_VALUE, max = 0;

    private long sum;

    void recordPacket(long roundtrip) {
        received++;

        if (roundtrip < min) min = roundtrip;
        if (roundtrip > max) max = roundtrip;

        sum += roundtrip;
    }

    void recordLost() {
        lost++;
    }

    String getStatistics() {
        int packets = received + lost;
        //float percent = 0;
        //		if (packets != 0){
        //			percent = lost/packets;
        //			percent *= 100;
        //		}
        float avg = sum / packets;
        return packets + " packets transmitted, " + received
                + " packets received\n" + 
                //			percent +"% packet loss\n"+
                "round-trip min/avg/max = " + min + "/" + avg + "/" + max
                + " ms";
    }
}

