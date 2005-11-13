package org.jnode.fs.smbfs.command;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.smbfs.SMBFSDevice;
import org.jnode.fs.smbfs.SMBFSDriver;
import org.jnode.fs.smbfs.SMBFileSystemType;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Levente S\u00e1ntha
 */
public class MakeSMBDeviceCommand {
    private static final Argument HOST_ARG = new Argument("host", "Samba host");
    private static final Argument PATH_ARG = new Argument("path", "Samba path");
    private static final Argument USERNAME_ARG = new Argument("username", "Samba username");
    private static final Argument PASSWORD_ARG = new Argument("password", "Samba password");

    private static Help.Info HELP_INFO = new Help.Info("ftpdevice", "Create a Samba device",
            new Parameter[]{new Parameter(HOST_ARG, Parameter.MANDATORY),
                    new Parameter(PATH_ARG, Parameter.MANDATORY),
                    new Parameter(USERNAME_ARG, Parameter.MANDATORY),
                    new Parameter(PASSWORD_ARG, Parameter.OPTIONAL)});

    public static void main(String[] args) throws Exception {
        new MakeSMBDeviceCommand().execute(new CommandLine(args), System.in,
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
        final String path = PATH_ARG.getValue(cmdLine);
        final String user = USERNAME_ARG.getValue(cmdLine);
        final String password = PASSWORD_ARG.getValue(cmdLine);
        final SMBFSDevice dev = new SMBFSDevice(host, path, user, password);
        dev.setDriver(new SMBFSDriver());
        final DeviceManager dm = DeviceUtils.getDeviceManager();
        dm.register(dev);
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
        FileSystemType type = fss.getFileSystemTypeForNameSystemTypes(SMBFileSystemType.NAME);
        final FileSystem fs = type.create(dev, true);
        fss.registerFileSystem(fs);
    }
}
