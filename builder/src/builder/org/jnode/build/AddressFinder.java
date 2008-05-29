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

package org.jnode.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import org.jnode.build.x86.BootImageBuilder;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AddressFinder {

    private static final int HDRLEN = 15;

    private static final int MAXWIDTH = 75;

    private static final String NDISASM = (System.getProperty("os.name")
        .toLowerCase().indexOf("win") >= 0) ? "ndisasmw" : "ndisasm";

    private static final ArchInfo[] archs = {
        new ArchInfo("x86", "all/build/x86/32bits/bootimage/bootimage.bin",
            "all/build/x86/bootimage/32bits/bootimage.lst", NDISASM
            + " -u -o "),
        new ArchInfo("x86_64",
            "all/build/x86/64bits/bootimage/bootimage.bin",
            "all/build/x86/64bits/bootimage/bootimage.lst",
            "udis86 --code64 --offset --origin ")};

    public static void main(String[] args) throws IllegalArgumentException,
        SecurityException, IOException, InterruptedException {

        long loadAddress = BootImageBuilder.LOAD_ADDR;
        long address = 0;
        boolean disasm = false;
        int disasmLength = 128;

        if (args.length > 0) {
            final String archName = args[0];
            final ArchInfo arch = findArch(archName);
            final String listFileName = arch.getListFile();
            final String imageFileName = arch.getBootImageFile();
            int i;
            for (i = 1; i < args.length; i++) {
                String arg = args[i];
                if (arg.charAt(0) == '-') {
                    arg = arg.substring(1);
                    if (arg.equals("d")) {
                        disasm = true;
                    } else if (arg.equals("l")) {
                        disasmLength = Integer.parseInt(args[++i]);
                    } else {
                        usage();
                    }
                } else {
                    break;
                }
            }
            address = Long.parseLong(args[i], 16);
            final long labelAddress = findLabel(listFileName, address);

            if (disasm) {
                disasm(arch, imageFileName, labelAddress, disasmLength,
                    loadAddress);
            }
        } else {
            usage();
        }
    }

    private static ArchInfo findArch(String name) {
        for (int i = 0; i < archs.length; i++) {
            if (archs[i].getArch().equals(name)) {
                return archs[i];
            }
        }
        usage();
        return null;
    }

    private static void usage() {
        System.out
            .println("Usage: findaddress architecture [-d] [-l length] address");
        System.exit(1);
    }

    private static long findLabel(String listFileName, long address)
        throws IOException {
        FileReader fin = new FileReader(listFileName);
        try {
            BufferedReader in = new BufferedReader(fin);
            String line;
            long lastAddress = -1;
            String lastLabel = null;
            String lastLine = null;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("$")) {
                    final int idx = line.indexOf('\t');
                    final long laddr = Long.parseLong(line.substring(1, idx),
                        16);
                    if (laddr <= address) {
                        lastAddress = laddr;
                        lastLabel = line.substring(idx + 1);
                        lastLine = line;
                    } else {
                        break;
                    }
                }
            }
            System.out.println("Found:");
            println("Address:", "0x" + Long.toHexString(lastAddress));
            printLabel(lastLabel);
            System.out.println();
            return lastAddress;
        } finally {
            fin.close();
        }
    }

    private static void printLabel(String label) {
        int idx;
        final String BCI = "__bci_";
        String indent = "";
        while ((idx = label.indexOf(BCI)) >= 0) {
            println(indent + "Label:", label.substring(0, idx));
            label = label.substring(idx + BCI.length());
            idx = 0;
            while ((idx < label.length())
                && Character.isDigit(label.charAt(idx))) {
                idx++;
            }
            println(indent + "BCI:", label.substring(0, idx));
            label = (idx < label.length()) ? label.substring(idx + 1) : "";
            indent += "  ";
        }
    }

    private static void println(String hdr, String arg) {
        String s = fixlen(hdr, HDRLEN) + arg;
        while (s.length() > 0) {
            if (s.length() <= MAXWIDTH) {
                System.out.println(s);
                s = "";
            } else {
                System.out.println(s.substring(0, MAXWIDTH));
                s = fixlen("", HDRLEN) + s.substring(MAXWIDTH);
            }
        }
    }

    private static String fixlen(String v, int length) {
        while (v.length() < length) {
            v += " ";
        }
        if (v.length() > length) {
            v = v.substring(0, length);
        }
        return v;
    }

    private static void disasm(ArchInfo arch, String imageFileName,
                               long address, int length, long loadAddress)
        throws IllegalArgumentException, SecurityException, IOException,
        InterruptedException {
        final RandomAccessFile raf = new RandomAccessFile(imageFileName, "r");
        try {
            final long offset = address - loadAddress;
            raf.seek(offset);
            final byte[] data = new byte[length];
            raf.readFully(data);

            File tmpFile = File.createTempFile("jnode", "bin");
            FileOutputStream os = new FileOutputStream(tmpFile);
            os.write(data);
            os.close();

            final String cmdLine = arch.getDisasmCmd() + address + " "
                + tmpFile.getAbsolutePath();
            exec(cmdLine);
            //tmpFile.delete();
        } finally {
            raf.close();
        }
    }

    private static void exec(String cmdLine) throws IOException,
        InterruptedException {
        Process proc = Runtime.getRuntime().exec(cmdLine);
        CopyThread stderr = new CopyThread(proc.getErrorStream(), System.out);
        CopyThread stdout = new CopyThread(proc.getInputStream(), System.out);
        stderr.start();
        stdout.start();
        proc.waitFor();
    }

    private static final class ArchInfo {
        private final String arch;

        private final String listFile;

        private final String bootImageFile;

        private final String disasmCmd;

        /**
         * @param bootImageFile
         * @param listFile
         * @param disasmCmd
         */
        public ArchInfo(String arch, String bootImageFile,
                        final String listFile, final String disasmCmd) {
            this.arch = arch;
            this.bootImageFile = bootImageFile;
            this.listFile = listFile;
            this.disasmCmd = disasmCmd;
        }

        public final String getArch() {
            return arch;
        }

        public final String getBootImageFile() {
            return bootImageFile;
        }

        public final String getDisasmCmd() {
            return disasmCmd;
        }

        public final String getListFile() {
            return listFile;
        }
    }

    static class CopyThread extends Thread {

        private final InputStream is;

        private final OutputStream out;

        public CopyThread(InputStream is, OutputStream out) {
            this.is = is;
            this.out = out;
        }

        public void run() {
            int ch;
            try {
                while ((ch = is.read()) >= 0) {
                    out.write(ch);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
