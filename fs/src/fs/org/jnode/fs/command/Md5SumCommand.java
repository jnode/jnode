package org.jnode.fs.command;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

public class Md5SumCommand extends AbstractCommand {

    private final FileArgument ARG_PATHS = new FileArgument(
        "paths", Argument.OPTIONAL | Argument.MULTIPLE,
        "the files or directories to be md5summed");
    private final FlagArgument FLAG_RECURSIVE = new FlagArgument(
        "recursive", Argument.OPTIONAL,
        "if set, will recursively go through all folders, md5summing all files");
    private final FlagArgument ARG_CHECK = new FlagArgument(
        "check", Argument.OPTIONAL,
        "if set, will check all md5sums against a file ");
    private final FileArgument ARG_CHECKFILE = new FileArgument(
        "checkfile", Argument.OPTIONAL | Argument.SINGLE,
        "the md5sum file to check against ");


    private static final int MEGABYTE = 1048576;
    static final byte[] HEX_CHAR_TABLE = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3',
        (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
        (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };

    private boolean recursive;
    private File[] paths;
    private PrintStream err;
    private MessageDigest msgDigest;
    private PrintStream out;
    private File checkFile;
    private String hexOutputString;

    public Md5SumCommand() {
        super("Calculate a md5sum against a file or Folder of Files");
        registerArguments(ARG_PATHS, FLAG_RECURSIVE, ARG_CHECK, ARG_CHECKFILE);
    }


    public void execute(CommandLine commandLine, InputStream in,
                        PrintStream out, PrintStream err) throws Exception {

        recursive = FLAG_RECURSIVE.isSet();
        paths = ARG_PATHS.getValues();
        checkFile = ARG_CHECKFILE.getValue();

        this.err = err;
        this.out = out;

        if (ARG_CHECK.isSet()) {
            calculatemd5sumAgainstFile();
        } else {
            boolean ok = true;
            for (File file : paths) {
                ok &= calculatemd5sum(file);
            }
        }


    }

    private void calculatemd5sumAgainstFile() {
        boolean md5Ok = true;
        if (!checkFile.exists()) {
            err.println(checkFile);
            md5Ok = false;
        }

        BufferedReader reader = null;
        if (md5Ok) {
            try {
                reader = new BufferedReader(new FileReader(checkFile));
            } catch (FileNotFoundException e) {
                //have just checked against this, so this shouldnt occour
            }
        }

        if (md5Ok) {
            String readLine;
            try {
                int failCount = 0;
                while ((readLine = reader.readLine()) != null) {
                    boolean passed = true;
                    String[] line = readLine.split("[ ]+");
                    if (calculatemd5sum(new File(line[1].trim()))) {
                        if (!hexOutputString.equals(line[0].trim())) {
                            passed = false;
                        }
                    } else {
                        passed = false;
                    }

                    if (passed) {
                        out.println(line[1] + " : OK");
                    } else {
                        out.println(line[1] + " : FAILED");
                        failCount++;
                    }
                }
                if (failCount > 0) {
                    out.println(failCount + " file(s) failed");
                }
            } catch (IOException e) {
                out.println("md5sum could not be checked against " + checkFile);
            }
        }
    }


    private boolean calculatemd5sum(File file) {
        if (!file.exists()) {
            err.println(file + " does not exist");
            return false;
        }
        boolean md5sumOk = true;

        if (file.isDirectory()) {
            if (recursive) {
                for (File f : file.listFiles()) {
                    final String name = f.getName();
                    if (!name.equals(".") && !name.equals("..")) {
                        md5sumOk &= calculatemd5sum(f);
                    }
                }
            } else {
                err.println("Cannot calculate md5sum on folder: " + file);
                md5sumOk = false;
            }
        } else {
            if (md5sumOk) {
                if (msgDigest == null) {
                    try {
                        msgDigest = MessageDigest.getInstance("md5");
                    } catch (NoSuchAlgorithmException e) {
                        err.println("md5sum algorithm Could not be found");
                        md5sumOk = false;
                    }
                } else {
                    msgDigest.reset();
                }
            }

            BufferedInputStream bis = null;
            byte[] buffer = null;
            int maxBytesToRead = 0;
            long fileSize = file.length();
            if (md5sumOk) {
                if (fileSize >= MEGABYTE) {
                    maxBytesToRead = MEGABYTE;
                    buffer = new byte[maxBytesToRead];
                } else {
                    maxBytesToRead = (int) fileSize;
                    buffer = new byte[maxBytesToRead];
                }
                try {
                    bis = new BufferedInputStream(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    md5sumOk = false;
                }

            }


            if (md5sumOk) {
                int bytesRead = 0;
                try {
                    bytesRead = bis.read(buffer, 0, maxBytesToRead);
                    do {
                        msgDigest.update(buffer);
                        bytesRead = bis.read(buffer, 0, maxBytesToRead);
                    } while (bytesRead > 0);
                    bis.close();
                    hexOutputString = convertOutputToHexString(msgDigest.digest());
                } catch (IOException e) {
                    md5sumOk = false;
                }

            }

            if (md5sumOk) {
                out.println(hexOutputString + "    " + file);
            } else {
                err.println(file + " was not md5summed");
            }

        }

        return md5sumOk;
    }


    private String convertOutputToHexString(byte[] bs) throws UnsupportedEncodingException {
        byte[] hex = new byte[2 * bs.length];
        int index = 0;

        for (byte b : bs) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        return new String(hex, "ASCII");
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new Md5SumCommand().execute(args);
    }

}
