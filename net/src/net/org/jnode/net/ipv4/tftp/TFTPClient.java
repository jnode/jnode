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
 
package org.jnode.net.ipv4.tftp;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Console TFTP client.
 * Usage: TFTPClient [hostname [PUT/GET filename]]
 * @author markhale
 */
public class TFTPClient extends org.apache.commons.net.tftp.TFTPClient {

    public final static String CONNECT_CMD = "connect";
    public final static String PUT_CMD = "put";
    public final static String GET_CMD = "get";
    public final static String ASCII_CMD = "ascii";
    public final static String BINARY_CMD = "binary";
    public final static String TIMEOUT_CMD = "timeout";
    public final static String RETRIES_CMD = "retries";
    public final static String STATUS_CMD = "status";
    public final static String HELP_CMD = "help";
    public final static String QUIT_CMD = "quit";

    // BufferedReader does not currently function correctly (GNU classpath bug 5558)
    //private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    // use DataInputStream instead
    private final DataInputStream in = new DataInputStream(System.in);
    private InetAddress serverAddress;
    private int mode = BINARY_MODE;
    private boolean quit;
    

    public static void main(String[] args) {
        TFTPClient client = new TFTPClient();
        if (args.length == 3) { // non-interactive mode
            if (args[1].equalsIgnoreCase(PUT_CMD)) {
                if (client.executeCommand(new String[] {CONNECT_CMD, args[0]})) {
                    client.executeCommand(new String[] {PUT_CMD, args[2]});
                }
            }
            else if (args[1].equalsIgnoreCase(GET_CMD)) {
                if (client.executeCommand(new String[] {CONNECT_CMD, args[0]})) {
                    client.executeCommand(new String[] {GET_CMD, args[2]});
                }
            } 
            else {
                System.out.println("Unrecognised command line.");
            }
        } 
        else { // interactive mode
            if (args.length == 1) {
                client.executeCommand(new String[] {CONNECT_CMD, args[0]});
            }
            client.run();
        }
    }

    @SuppressWarnings("deprecation")
    private void run() {
        System.out.println("JNode TFTP Client");
        do {
            try {
                System.out.print("tftp> ");
                String line = in.readLine();
                String[] args = parseLine(line);
                executeCommand(args);
            } catch (IOException ex) {
            }
        } while (!quit);
    }
    
    
    private final static String[] parseLine(String line) {
        // count arguments
        int count = 0;
        int pos = -1;
        do {
            count++;
            pos = line.indexOf(' ', pos+1);
        } while (pos != -1);

        // parse
        String[] args = new String[count];
        count = 0;
        pos = -1;
        do {
            int startPos = pos + 1;
            pos = line.indexOf(' ', startPos);
            if (pos != -1) {
                args[count] = line.substring(startPos, pos);
            }
            else {
                args[count] = line.substring(startPos, line.length());
            }
            count++;
        } while (pos != -1);
        return args;
    }
    /**
     * High-level command API.
     * @return true on success.
     */
    public boolean executeCommand(String[] args) {
        if (args == null || args.length < 1) {
            return false;
        }

        boolean success = false;
        final String cmd = args[0];
        if (cmd.equals(CONNECT_CMD)) { // connect
            if (args.length < 2) {
                System.out.println("Please specify a host name.");
            } 
            else {
                try {
                    serverAddress = InetAddress.getByName(args[1]);
                    serverAddress.getHostName(); // do DNS lookup
                    success = true;
                } 
                catch (UnknownHostException ex) {
                    System.out.println("Unknown host " + args[1] + ".");
                }
            }
        } 
        else if (cmd.equals(GET_CMD)) { // get
            if (serverAddress == null) {
                System.out.println("Not connected.");
            } 
            else if (args.length < 2) {
                System.out.println("Please specify a file name.");
            } 
            else {
                String filename = args[1];
                try {
                    FileOutputStream fileOut = new FileOutputStream(filename);
                    try {
                        open();
                        try {
                            int bytesTransferred = receiveFile(filename, mode, fileOut, serverAddress);
                            System.out.println(bytesTransferred + " bytes transferred.");
                        } 
                        finally {
                            close();
                        }
                    } 
                    finally {
                        fileOut.close();
                    }
                    success = true;
                } 
                catch (IOException ex) {
                    System.out.println("Error transferring file: " + ex.getMessage());
                }
            }
        } 
        else if (cmd.equals(PUT_CMD)) { // put
            if (serverAddress == null) {
                System.out.println("Not connected.");
            } 
            else if (args.length < 2) {
                System.out.println("Please specify a file name.");
            } 
            else {
                String filename = args[1];
                try {
                    FileInputStream fileIn = new FileInputStream(filename);
                    try {
                        open();
                        try {
                            sendFile(filename, mode, fileIn, serverAddress);
                        }
                        finally {
                            close();
                        }
                    }
                    finally {
                        fileIn.close();
                    }
                    success = true;
                } 
                catch (IOException ex) {
                    System.out.println("Error transferring file: " + ex.getMessage());
                }
            }
        } 
        else if (cmd.equals(ASCII_CMD)) { // ascii
            mode = ASCII_MODE;
            success = true;
        } 
        else if (cmd.equals(BINARY_CMD)) { // binary
            mode = BINARY_MODE;
            success = true;
        } 
        else if (cmd.equals(TIMEOUT_CMD)) { // timeout
            if (args.length < 2) {
                System.out.println("Please specify a timeout value.");
            } 
            else {
                try {
                    setDefaultTimeout(Integer.parseInt(args[1]));
                    success = true;
                } catch(NumberFormatException ex) {
                    System.out.println("Invalid timeout value.");
                }
            }
        } 
        else if (cmd.equals(RETRIES_CMD)) { // retries
            if (args.length < 2) {
                System.out.println("Please specify a retries value.");
            } 
            else {
                try {
                    setMaxTimeouts(Integer.parseInt(args[1]));
                    success = true;
                } catch(NumberFormatException ex) {
                    System.out.println("Invalid retries value.");
                }
            }
        } 
        else if (cmd.equals(STATUS_CMD)) { // status
            if (serverAddress != null) {
                System.out.println("Connected to "+serverAddress.getHostName() + ".");
            }
            else {
                System.out.println("Not connected.");
            }
            if (mode == ASCII_MODE) {
                System.out.print("mode: ASCII");
            }
            else if (mode == BINARY_MODE) {
                System.out.print("mode: BINARY");
            }
            System.out.print("   timeout: " + getDefaultTimeout());
            System.out.println("   retries: " + getMaxTimeouts());
            success = true;
        } 
        else if (cmd.equals(HELP_CMD)) { // help
            System.out.println(ASCII_CMD + " - set mode to ASCII");
            System.out.println(CONNECT_CMD + " - connect to a tftp server");
            System.out.println(BINARY_CMD + " - set mode to binary");
            System.out.println(GET_CMD + " - receive file");
            System.out.println(HELP_CMD + " - display this help");
            System.out.println(PUT_CMD + " - send file");
            System.out.println(QUIT_CMD + " - exit");
            System.out.println(RETRIES_CMD + " - set retries");
            System.out.println(STATUS_CMD + " - display current status");
            System.out.println(TIMEOUT_CMD + " - set timeout");
            success = true;
        } 
        else if (cmd.equals(QUIT_CMD)) { // quit
            quit = true;
            success = true;
        } 
        else {
            System.out.println("Unrecognised command.");
        }
        return success;
    }
}
