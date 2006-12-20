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
 
package org.jnode.fs.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * 
 * Copy a File to a File or Directory
 * 
 * Syntaxe : cp [-u | -f | -i] [-v] [-r] file/url-src file-dst Exemples cp -ur
 * -v d:/adir d:/temp
 * 
 * 
 * TODO : support of patern file source (cp *.j*g /dir/) support file name with
 * strange char like * \ /
 * 
 * @author Yves Galante (yves.galante@jmob.net)
 * @version 0.1
 *  
 */
public class CpCommand {

    static final byte MODE_BASE = 0;

    static final byte MODE_ITERATIVE = 1;

    static final byte MODE_FORCE = 2;

    static final byte MODE_UPDATE = 3;

    static final char MODE_FORCE_C = 'f';

    static final char MODE_INTER_C = 'i';

    static final char MODE_UPDATE_C = 'u';

    static final char OPTION_VERBOSE_C = 'v';

    static final char OPTION_RECURCIVE_C = 'r';

    static final FileArgument ARG_FSRC = new FileArgument("file or url",
            "file or url source");

    static final FileArgument ARG_FDST = new FileArgument("file", "file dest");

    static final OptionArgument ARG_OPT1 = new OptionArgument(
            "option",
            "a option",
            new OptionArgument.Option[] {
                    new OptionArgument.Option("-" + MODE_FORCE_C + "",
                            "if an existing destination file overwrite"),
                    new OptionArgument.Option("-" + MODE_INTER_C + "",
                            "if an existing destination file ask"),
                    new OptionArgument.Option("-" + MODE_UPDATE_C + "",
                            "if an existing destination file overwrite only if\ndate is older")});

    static final OptionArgument ARG_OPT2 = new OptionArgument("option",
            "a option",
            new OptionArgument.Option[] { new OptionArgument.Option("-"
                    + OPTION_VERBOSE_C + "", "verbose")});

    static final OptionArgument ARG_OPT3 = new OptionArgument("option",
            "a option",
            new OptionArgument.Option[] { new OptionArgument.Option("-"
                    + OPTION_RECURCIVE_C + "", "recursie")});

    public static Help.Info HELP_INFO = new Help.Info("cp",
            new Syntax[] { new Syntax("copy a file or url to a file",
                    new Parameter[] {
                            new Parameter(ARG_OPT1, Parameter.OPTIONAL),
                            new Parameter(ARG_OPT2, Parameter.OPTIONAL),
                            new Parameter(ARG_OPT3, Parameter.OPTIONAL),
                            new Parameter(ARG_FSRC, Parameter.MANDATORY),
                            new Parameter(ARG_FDST, Parameter.MANDATORY)})});

    /**
     * ****************************** non static filed
     * ************************************
     */

    private URL _urlSrc;

    private File _fileSrc;

    private File _fileDst;

    private byte _mode = MODE_BASE;

    private boolean _recursive = false;

    private boolean _verbose = false;

    private boolean _srcIsDir = false;

    private boolean _argsOk = true;

    final byte[] buf = new byte[128*1024];    

    /**
     * Construct this class with args[] [-u | -f | -i] [-v] [-r] file/url-src
     * file-dst
     * 
     * @param args
     *            commande argument
     */
    public CpCommand(String[] args) {
        _argsOk = parseOption(args);
    }

    /**
     * Constructor with file
     * 
     * @param fileSrc
     * @param fileDst
     * @param mode
     * @param recursive
     * @param verbose
     */
    public CpCommand(File fileSrc, File fileDst, byte mode, boolean recursive,
            boolean verbose) {
        _fileSrc = fileSrc;
        _fileDst = fileDst;
        _mode = mode;
        _recursive = recursive;
        _verbose = verbose;
    }

    /**
     * Constructor with URL
     * 
     * @param urlSrc
     * @param fileDst
     * @param mode
     * @param verbose
     */
    public CpCommand(URL urlSrc, File fileDst, byte mode, boolean verbose) {
        _urlSrc = urlSrc;
        _fileDst = fileDst;
        _mode = mode;
        _verbose = verbose;
    }

    /**
     * Copy
     * 
     * @return Return the number of file copied
     */
    public int copy() throws Exception {
        if (!_argsOk) {
            HELP_INFO.parse(new String[ 0]);
            return 0;
        }
        if (_fileSrc != null) {
            if (!check()) { return 0; }
            if (_srcIsDir) {
                return copyDir();
            } else {
                return copyFile();
            }
        } else {
            if (!checkURL()) { return 0; }
            return copyURL();
        }

    }

    /**
     * Syntaxe [-u | -f | -i] [-v] [-r] filesrc filedst Exemple cp -uv file1
     * file2 cp -f -r file1 file2
     * 
     * @param args
     * 
     * @since 30 mars 04
     */
    private boolean parseOption(String[] args) {

        String options;
        StringBuilder sb = new StringBuilder(5);

        if (args.length < 2 || args.length > 10) { return false; }

        // concat all options
        for (int i = 0; i < args.length - 2; i++) {
            if (args[ i].charAt(0) != '-') return false;
            sb.append(args[ i].substring(1));
        }
        options = sb.toString();
        sb = null;
        // parse options
        for (int i = 0; i < options.length(); i++) {
            char option = options.charAt(i);
            switch (option) {
            case MODE_FORCE_C:
                if (_mode == MODE_BASE) {
                    _mode = MODE_FORCE;
                } else {
                    return false;
                }
                break;
            case MODE_UPDATE_C:
                if (_mode == MODE_BASE) {
                    _mode = MODE_UPDATE;
                } else {
                    return false;
                }
                break;
            case MODE_INTER_C:
                if (_mode == MODE_BASE) {
                    _mode = MODE_ITERATIVE;
                } else {
                    return false;
                }
                break;
            case OPTION_VERBOSE_C:
                if (!_verbose) {
                    _verbose = true;
                } else {
                    return false;
                }
                break;
            case OPTION_RECURCIVE_C:
                if (!_recursive) {
                    _recursive = true;
                } else {
                    return false;
                }
                break;
            default:
                return false;
            }

        }

        if (args[ args.length - 2].indexOf("://") >= 0) {
            try {
                _urlSrc = new URL(args[ args.length - 2]);
            } catch (MalformedURLException urlEx) {
                _urlSrc = null;
                System.err.println(args[ args.length - 2] + " is malformed");
                return false;
            }
        }
        if (_urlSrc == null) {
            _fileSrc = new File(args[ args.length - 2]);
        }
        _fileDst = new File(args[ args.length - 1]);
        return true;
    }

    /**
     * Make some check on src url and on dst file
     * 
     * @return
     */
    private boolean checkURL() {

        if (_urlSrc == null || _fileDst == null) { return false; }
        // now file is converted to ftp
        //if(_urlSrc.getProtocol().equalsIgnoreCase("file")){
        //	_urlSrc=null;
        //	System.err.println(_urlSrc.toExternalForm()+" file protocol not
        // supported!");
        //	return false;
        //}
        if (_fileDst.isDirectory()) { // directory not supported
            System.err.println(_fileDst.getAbsolutePath() + " is a directory!");
            return false;
        }

        return checkDstOverwrite();

    }

    /**
     * Make some check on src file and on dst file
     *  
     */
    private boolean check() {

        if (_fileSrc == null || _fileDst == null) { return false; }

        // same file check
        if (_fileSrc.toString().equalsIgnoreCase(_fileDst.toString())) {
            System.err.println("Same file !!!");
            return false;
        }

        // file src check
        if (!_fileSrc.exists()) {
            System.err.println(_fileSrc.getAbsolutePath() + " not found");
            return false;
        }
        if (!_fileSrc.canRead()) {
            System.err.println("You havn't right too read "
                    + _fileSrc.getAbsolutePath());
            return false;
        }

        // Source file is a directory ?
        if (_fileSrc.isDirectory()) {
            if (!_recursive) {
                System.err.println("For recursive copy, please use option -r");
                return false;
            }
            if (_fileDst.isFile()) {
                System.err.println(_fileDst.getAbsolutePath()
                        + " is not a directory ");
                return false;
            }
            _srcIsDir = true;
            return true; // OK !
        }
        // src not a dir, need more check
        _srcIsDir = false;
        // dst file is a directory ?
        if (_fileDst.isDirectory()) {
            _fileDst = new File(_fileDst.getAbsolutePath() + File.separator
                    + _fileSrc.getName());
        }

        return checkDstOverwrite();
    }

    /**
     * We can overwrite the dst file ?
     * 
     * @return
     */
    private boolean checkDstOverwrite() {
        if (_fileDst.isFile()) {
            switch (_mode) {
            case MODE_BASE:
                System.err.println(_fileDst.getAbsolutePath()
                        + " already exist");
                return false;
            case MODE_UPDATE:
                if (_fileSrc == null
                        || _fileSrc.lastModified() <= _fileDst.lastModified()) { return false; }
                break;
            case MODE_ITERATIVE:
                if (!overWriteQuerry(_fileDst.getAbsolutePath())) { return false; }
                break;
            case MODE_FORCE:
                break;
            default:
                return false;
            }
            if (!_fileDst.canWrite()) {
                System.err.println(_fileDst.getAbsolutePath()
                        + " cannot overwrite");
                return false;
            }
        }
        return true;
    }

    /**
     * Copy the file src to the dst file
     * 
     * @return number of copied file
     */
    private int copyFile() {
        InputStream is;
        FileOutputStream os;
        try {
            is = new FileInputStream(_fileSrc);
        } catch (FileNotFoundException fEx) {
            System.err.println(_fileSrc.getAbsolutePath() + " not found");
            return 0;
        }
        os = initDstFile();
        if (os == null) return 0;

        if (!copyData(is, os)) { return 0; }
        if (_verbose) {
            System.out.println("Copie : " + _fileSrc.getAbsolutePath() + " to "
                    + _fileDst.getAbsolutePath());
        }
        return 1;
    }

    private int copyDir() throws Exception {
        final File dstDir = new File(_fileDst.getAbsoluteFile()
                + File.separator);//+_fileSrc.getName()+File.separator);
        final File[] subFile = _fileSrc.listFiles();
        int numberFileCopied = 0;

        if (!dstDir.exists()) {
            if (!dstDir.mkdir()) {
                System.err.println(_fileDst.getParentFile().getAbsolutePath()
                        + " cannot create ");
                return 0;
            }
        }
        for (int i = 0; i < subFile.length; i++) {
            final String name = subFile[ i].getName();
            if (name.equals(".") || name.equals("..")) continue;
            File subDstDir = new File(dstDir.getAbsoluteFile() + File.separator
                    + name);
            CpCommand cp = new CpCommand(subFile[ i], subDstDir, _mode,
                    _recursive, _verbose);
            numberFileCopied += cp.copy();
        }
        return numberFileCopied;
    }

    /**
     * Copy the url src to the dst file
     * 
     * @return number of copied file
     */
    private int copyURL() {
        InputStream is;
        FileOutputStream os;

        os = initDstFile();
        if (os == null) return 0;
        try {
            is = _urlSrc.openStream();
        } catch (IOException fEx) {
            System.err.println("File not found " + _urlSrc.toExternalForm());
            return 0;
        }

        if (!copyData(is, os)) { return 0; }
        if (_verbose) {
            System.out.println("Copie : " + _urlSrc.toExternalForm() + " to "
                    + _fileDst.getAbsolutePath());
        }
        return 1;
    }

    /**
     * Copy an inputstream to a outputstream
     * 
     * @param is
     * @param os
     * @return
     */
    private boolean copyData(InputStream is, OutputStream os) {
        try {
            int len = 0;
            //final byte[] buf = new byte[128*1024];
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.close();
            is.close();
        } catch (IOException io) {
            System.err.println("An error is occure : copy aborted");
            io.printStackTrace();
            try {
                _fileDst.delete(); // clean
            } catch (Exception ex) {
            }
            return false;
        }
        return true;
    }

    /**
     * Open the dst file Make parent is need Delete if exist
     * 
     * @return the file outputstream
     */
    private FileOutputStream initDstFile() {
        final File dstParent = _fileDst.getParentFile();
        if (dstParent != null && !dstParent.exists()) { // make parent dir
            if (!dstParent.mkdirs()) {
                System.err.println("Directory can't create "
                        + _fileDst.getParentFile().getAbsolutePath());
                return null;
            }
        }
        if (_fileDst.exists() && !_fileDst.delete()) {
            System.err.println("File can't overwrite "
                    + _fileDst.getAbsolutePath());
            return null;
        }

        try {
            if (!_fileDst.createNewFile()) {
                System.err.println("File can't create "
                        + _fileDst.getAbsolutePath());
                return null;
            }
            return new FileOutputStream(_fileDst);
        } catch (IOException io) {
            System.err.println("File can't create "
                    + _fileDst.getAbsolutePath());
        } catch (SecurityException sec) {
            System.err.println("You havn't right too create "
                    + _fileDst.getAbsolutePath());
        }
        return null;

    }

    // STATIC METHODES

    public static void main(String[] args) throws Exception {
        CpCommand cpCommand = new CpCommand(args);
        int nbCopiedFile = cpCommand.copy();
        System.out.println(nbCopiedFile + " file(s) copied");
    }

    /**
     * Ask user for owerwrite a file
     * 
     * @param fileName
     * @return
     */
    private static boolean overWriteQuerry(String fileName) {

        final StringBuilder sbRead = new StringBuilder(256);
        final InputStreamReader reader = new InputStreamReader(System.in);

        String reponse = null;

        System.out.print("Overwrite " + fileName + " [N] ? ");
        try {
            int readInt;
            while ((readInt = reader.read()) > 0) {
                char ch = (char) readInt;
                if (ch == '\r') {
                    continue;
                }
                if (ch == '\n') {
                    break;
                }
                sbRead.append(ch);
                System.out.print(ch); // Console echo
            }
            System.out.println();
            reponse = sbRead.toString().trim();
            if (reponse.equalsIgnoreCase("Y")
                    || reponse.equalsIgnoreCase("YES")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException io) {
        }
        return false;
    }

}
