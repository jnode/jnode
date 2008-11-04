/*
 * $Id: FormatCommand.java 3585 2007-11-13 13:31:18Z galatnm $
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
package org.jnode.fs.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * This command class calculates MD5 digests for files.  If the 'check' flag is
 * given, it reads a file containing digests and filenames and checks that the
 * named files digests batch the supplied digests.  Otherwise, it simply calculates
 * and outputs the digests as filenames in a format compatible with 'check' processing.
 * <p>
 * The command is based on the GNU 'md5sum' command, with some differences in the
 * 'check' file format.
 * 
 * @author Tim Sparg
 * @author crawley@jnode.org
 */
public class Md5SumCommand extends AbstractCommand {

    private final FileArgument ARG_PATHS = new FileArgument(
        "paths", Argument.OPTIONAL | Argument.MULTIPLE,
        "the files (or directories) to be calculate MD5 digests for");
    private final FlagArgument FLAG_RECURSIVE = new FlagArgument(
        "recursive", Argument.OPTIONAL,
        "if set, recursively calculate MD5 digests for the contents of any directory");
    private final FileArgument ARG_CHECKFILE = new FileArgument(
        "checkfile", Argument.OPTIONAL | Argument.SINGLE,
        "check MD5 digests for files listed in this file");


    private static final int BUFFER_SIZE = 1048576;  // 1Mb

    private PrintWriter out;
    private PrintWriter err;
    private MessageDigest digestEngine;
    
    
    public Md5SumCommand() {
        super("Calculate or check MD5 digests");
        registerArguments(ARG_PATHS, FLAG_RECURSIVE, ARG_CHECKFILE);
    }

    public void execute() throws Exception {
        this.err = getError().getPrintWriter();
        this.out = getOutput().getPrintWriter();

        // If this throws an exception, we want it to propagate ,,,
        digestEngine = MessageDigest.getInstance("md5");

        boolean ok = true;
        if (ARG_CHECKFILE.isSet()) {
            ok = checkFile(ARG_CHECKFILE.getValue());
        } else if (ARG_PATHS.isSet()) {
            boolean recursive = FLAG_RECURSIVE.isSet();
            File[] paths = ARG_PATHS.getValues();
            for (File file : paths) {
                ok &= processFile(file, recursive);
            }
        } else {
            ok = processInput();
        }
        if (!ok) {
            exit(1);
        }
    }

    /**
     * Read a check file containing a list of filenames and the expected MD5 digests
     * of the corresponding files.  We calculate the actual digests, compare with
     * the expected digests and report any differences and other problems to stdout
     * and/or stderr.
     * 
     * @param checkFile the file listing the files to be checked and their
     *        expected digests.
     * @return <code>true</code> if the check file was opened successfully, all 
     *         named files were found and their digests were as expected.
     */
    private boolean checkFile(File checkFile) {
        BufferedReader br = null;
        try {
            try {
                br = new BufferedReader(new FileReader(checkFile));
            } catch (FileNotFoundException ex) {
                err.println("Cannot open " + checkFile + ": " + ex.getMessage());
                return false;
            }
            String readLine;
            int failCount = 0;
            while ((readLine = br.readLine()) != null) {
                String[] line = readLine.split("\\s+");
                if (line.length == 2) {
                    try {
                        byte[] digest = computeDigest(new File(line[1]));
                        boolean passed = toHexString(digest).equalsIgnoreCase(line[0]);
                        if (passed) {
                            out.println(line[1] + " : OK");
                        } else {
                            out.println(line[1] + " : FAILED");
                            failCount++;
                        }
                    } catch (IOException ex) {
                        out.println(line[1] + " : IO EXCEPTION - " + ex.getMessage());
                        failCount++;
                    }
                }
            }
            if (failCount > 0) {
                err.println(failCount + " file(s) failed");
                return false;
            }
        } catch (IOException ex) {
            err.println("problem reading file " + checkFile + ": " + ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    // squash ...
                }
            }
        }
        return true;
    }

    /**
     * Calculate the digest for a file and output it in a format compatible 
     * with the 'check' option.  If the supplied file is a directory and 
     * 'recursive' is <code>true</code>, recursively process the directory
     * contents.
     * 
     * @param file the file (or directory) to be processed.
     * @param recursive if <code>true</code> process directory contents recursively.
     * @return <code>true</code> if all file digests were computed and output.
     */
    private boolean processFile(File file, boolean recursive) {
        boolean res = true;
        if (file.isDirectory()) {
            if (recursive) {
                for (File f : file.listFiles()) {
                    final String name = f.getName();
                    if (!name.equals(".") && !name.equals("..")) {
                        res &= processFile(f, recursive);
                    }
                }
            } else {
                err.println("Cannot calculate md5sum on folder: " + file);
                res = false;
            }
        } else {
            try {
                out.println(toHexString(computeDigest(file)) + "    " + file);
            } catch (IOException ex) {
                err.println(file + " was not md5summed: " + ex.getMessage());
            }
        }
        return res;
    }
    
    /**
     * Compute digest for the command's input stream.
     * @return <code>true</code> if all is well;
     */
    private boolean processInput() {
        try {
            out.println(toHexString(computeDigest(null)));
            return true;
        } catch (IOException ex) {
            err.println("Input was not md5summed: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Compute the digest of a file.
     * @param file the file.  If this is <code>null</code>, we calculate the digest
     *       for the command's input stream.
     * @return the digest.
     * @throws IOException
     */
    private byte[] computeDigest(File file) throws IOException {
        InputStream is = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            digestEngine.reset();
            is = (file != null) ? new FileInputStream(file) : getInput().getInputStream();
            int bytesRead;
            while ((bytesRead = is.read(buffer, 0, BUFFER_SIZE)) > 0) {
                digestEngine.update(buffer, 0, bytesRead);
            }
            return digestEngine.digest();
        } finally {
            if (file != null && is != null) {
                is.close();
            }
        }
    }

    /**
     * Turn a digest represented as a byte array into a hexadecimal string.
     * 
     * @param digest the digest as bytes
     * @return the corresponding hex string.
     */
    private String toHexString(byte[] digest) {
        char[] hex = new char[digest.length * 2];
        int index = 0;

        for (byte b : digest) {
            hex[index++] = hexChar(b >> 4);
            hex[index++] = hexChar(b);
        }
        return new String(hex);
    }
    
    private char hexChar(int i) {
        i = i & 0xf;
        return (char) ((i > 9 ? ('a' - 10) : '0') + i);
    }

    /**
     * The classic Java entry point.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new Md5SumCommand().execute(args);
    }

}
