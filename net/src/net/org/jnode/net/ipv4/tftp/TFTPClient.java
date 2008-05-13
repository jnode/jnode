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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Console TFTP client.
 * Usage: TFTPClient [hostname [PUT/GET filename]]
 * 
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

    private BufferedReader br;
    private PrintStream out;
    private InetAddress serverAddress;
    private int mode = BINARY_MODE;
    private boolean quit;
    
    
    public TFTPClient(PrintStream out) {
        this.out = out;
    }

    @SuppressWarnings("deprecation")
    public void run(InputStream in) throws IOException {
        // FIXME ... figure out to how to use JNode command argument parsing
        // (and completion) for our little TFTP interactive command syntax. 
        this.br = new BufferedReader(new InputStreamReader(in));
        out.println("JNode TFTP Client");
        do {
            out.print("tftp> ");
            String line = br.readLine();
            if (line == null) {
                // EOF
                break;
            }
            executeCommand(line.trim().split("\\s+"));
        } while (!quit);
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
                out.println("Please specify a host name.");
            } 
            else {
                try {
                    // FIXME ... this is not "connecting"!!
                    serverAddress = InetAddress.getByName(args[1]);
                    serverAddress.getHostName(); // do DNS lookup
                    success = true;
                } 
                catch (UnknownHostException ex) {
                    out.println("Unknown host " + args[1] + ".");
                }
            }
        } 
        else if (cmd.equals(GET_CMD)) { // get
            if (serverAddress == null) {
                out.println("Not connected.");
            } 
            else if (args.length < 2) {
                out.println("Please specify a file name.");
            } 
            else {
                String filename = args[1];
                try {
                    FileOutputStream fileOut = new FileOutputStream(filename);
                    try {
                        open();
                        try {
                            int bytesTransferred = receiveFile(filename, mode, fileOut, serverAddress);
                            out.println(bytesTransferred + " bytes transferred.");
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
                    diagnose(ex, "Error transferring file");
                }
            }
        } 
        else if (cmd.equals(PUT_CMD)) { // put
            if (serverAddress == null) {
                out.println("Not connected.");
            } 
            else if (args.length < 2) {
                out.println("Please specify a file name.");
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
                    diagnose(ex, "Error transferring file");
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
                out.println("Please specify a timeout value.");
            } 
            else {
                try {
                    setDefaultTimeout(Integer.parseInt(args[1]));
                    success = true;
                } catch (NumberFormatException ex) {
                    out.println("Invalid timeout value.");
                }
            }
        } 
        else if (cmd.equals(RETRIES_CMD)) { // retries
            if (args.length < 2) {
                out.println("Please specify a retries value.");
            } 
            else {
                try {
                    setMaxTimeouts(Integer.parseInt(args[1]));
                    success = true;
                } catch (NumberFormatException ex) {
                    out.println("Invalid retries value.");
                }
            }
        } 
        else if (cmd.equals(STATUS_CMD)) { // status
            if (serverAddress != null) {
                out.println("Connected to " + serverAddress.getHostName() + ".");
            }
            else {
                out.println("Not connected.");
            }
            if (mode == ASCII_MODE) {
                out.print("mode: ASCII");
            }
            else if (mode == BINARY_MODE) {
                out.print("mode: BINARY");
            }
            out.print("   timeout: " + getDefaultTimeout());
            out.println("   retries: " + getMaxTimeouts());
            success = true;
        } 
        else if (cmd.equals(HELP_CMD)) { // help
            out.println(ASCII_CMD + " - set mode to ASCII");
            out.println(CONNECT_CMD + " - connect to a tftp server");
            out.println(BINARY_CMD + " - set mode to binary");
            out.println(GET_CMD + " - receive file");
            out.println(HELP_CMD + " - display this help");
            out.println(PUT_CMD + " - send file");
            out.println(QUIT_CMD + " - exit");
            out.println(RETRIES_CMD + " - set retries");
            out.println(STATUS_CMD + " - display current status");
            out.println(TIMEOUT_CMD + " - set timeout");
            success = true;
        } 
        else if (cmd.equals(QUIT_CMD)) { // quit
            quit = true;
            success = true;
        } 
        else {
            out.println("Unrecognised command.");
        }
        return success;
    }

    private void diagnose(IOException ex, String message) {
        String exMessage = ex.getClass().getSimpleName() + " - " + ex.getLocalizedMessage();
        out.println(message + ": " + exMessage);
    }
    
}
