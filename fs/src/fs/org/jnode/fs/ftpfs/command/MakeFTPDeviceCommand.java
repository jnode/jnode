package org.jnode.fs.ftpfs.command;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.fs.ftpfs.FTPFSDevice;
import org.jnode.fs.ftpfs.FTPFSDriver;
import org.jnode.fs.ftpfs.FTPFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.FileSystem;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.naming.InitialNaming;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Levente S\u00e1ntha
 */
public class MakeFTPDeviceCommand implements Command {
    private static final Argument HOST_ARG = new Argument("host", "FTP host");
    private static final Argument USERNAME_ARG = new Argument("username", "FTP username");
    private static final Argument PASSWORD_ARG = new Argument("password", "FTP password");

    private static Help.Info HELP_INFO = new Help.Info("ftpdevice", "Create an FTP device",
                new Parameter[] { new Parameter(HOST_ARG, Parameter.MANDATORY),
                        new Parameter(USERNAME_ARG, Parameter.MANDATORY),
                        new Parameter(PASSWORD_ARG, Parameter.OPTIONAL) });

    public static void main(String[] args) throws Exception {
        new MakeFTPDeviceCommand().execute(new CommandLine(args), System.in,
                System.out, System.err);
    }
    /**
     * This will be execute'ed when the command is called.
     *
     * @param commandLine what comes in from the user
     * @param in          input stream, most offen this is System.in, but it can be a file or piped.
     * @param out         output stream, mostly this is System.out, but it can be a file or piped.
     * @param err         err stream, mostly this is System.err, but it can be a file or piped.
     * @throws Exception
     */
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());

        // Get the parameters
        final String host = HOST_ARG.getValue(cmdLine);
        final String user = USERNAME_ARG.getValue(cmdLine);
        final String password = PASSWORD_ARG.getValue(cmdLine);
        final FTPFSDevice dev = new FTPFSDevice(host, user, password);
        dev.setDriver(new FTPFSDriver());
        final DeviceManager dm = DeviceUtils.getDeviceManager();
        dm.register(dev);
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
        FileSystemType type = fss.getFileSystemTypeForNameSystemTypes(FTPFileSystemType.NAME);
        final FileSystem fs = type.create(dev, true);
        fss.registerFileSystem(fs);
    }
}
