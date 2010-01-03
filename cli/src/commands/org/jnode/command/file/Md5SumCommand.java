/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.command.file;

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

    private static final String help_paths = "the files (or directories) to calculate the MD5 digests for";
    private static final String help_recurse = "if set, recursively calculate the MD5 digests for the contents of " +
                                               "a directory";
    private static final String help_check = "check the MD5 digests for files listed in this file";
    private static final String help_super = "Calculate or check MD5 digests";
    private static final String fmt_err_open = "Cannot open %s: %s%n";
    @SuppressWarnings("unused")
    private static final String str_ok = "Ok";
    @SuppressWarnings("unused")
    private static final String str_fail = "Failed";
    private static final String fmt_io_ex = "%s : IO Exception - %s%n";
    private static final String fmt_fail_count = "%d file(s) failed%n";
    private static final String fmt_err_read = "Problem reading file %s: %s";
    private static final String err_norecurse = "Cannot calculate MD5 on directory, use recurse flag";
    private static final String fmt_err_md5 = "%s was not processed: %s%n";
    private static final String str_input = "Input";
    
    private final FileArgument argPaths;
    private final FlagArgument flagRecursive;
    private final FileArgument argCheckfile;


    private static final int BUFFER_SIZE = 1048576;  // 1Mb

    private PrintWriter out;
    private PrintWriter err;
    private MessageDigest digestEngine;
    
    
    public Md5SumCommand() {
        super(help_super);
        argPaths      = new FileArgument("paths", Argument.MULTIPLE | Argument.EXISTING, help_paths);
        flagRecursive = new FlagArgument("recursive", Argument.OPTIONAL, help_recurse);
        argCheckfile  = new FileArgument("checkfile", Argument.SINGLE | Argument.EXISTING, help_check);
        registerArguments(argPaths, flagRecursive, argCheckfile);
    }

    public void execute() throws Exception {
        this.err = getError().getPrintWriter();
        this.out = getOutput().getPrintWriter();

        // If this throws an exception, we want it to propagate ,,,
        digestEngine = MessageDigest.getInstance("md5");

        boolean ok = true;
        if (argCheckfile.isSet()) {
            ok = checkFile(argCheckfile.getValue());
        } else if (argPaths.isSet()) {
            boolean recursive = flagRecursive.isSet();
            File[] paths = argPaths.getValues();
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
                err.format(fmt_err_open, checkFile, ex.getLocalizedMessage());
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
                        out.format(fmt_io_ex, line[1], ex.getLocalizedMessage());
                        failCount++;
                    }
                }
            }
            if (failCount > 0) {
                err.format(fmt_fail_count, failCount);
                return false;
            }
        } catch (IOException ex) {
            err.format(fmt_err_read, checkFile, ex.getLocalizedMessage());
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
                err.println(err_norecurse);
                res = false;
            }
        } else {
            try {
                out.println(toHexString(computeDigest(file)) + "    " + file);
            } catch (IOException ex) {
                err.format(fmt_err_md5, file, ex.getLocalizedMessage());
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
            err.format(fmt_err_md5, str_input, ex.getLocalizedMessage());
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
