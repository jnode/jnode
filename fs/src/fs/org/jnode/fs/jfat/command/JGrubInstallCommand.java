package org.jnode.fs.jfat.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.DeviceArgument;
import org.jnode.shell.help.argument.FileArgument;

/**
 * The Grub Installer command for the JNODE.
 * jnode/>jgrub HDA_TARGET
 * HDA_TARGET /dev/hda0 or /dev/fd0
 *
 * @author Tango Devian
 */
public class JGrubInstallCommand implements Command {

    static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "device to where the Grub will install");

    static final FileArgument ARG_DIR = new FileArgument("directory", "the directory where you set the Stage2 and Menu.Lst");

    static final Help.Info HELP_INFO = new Help.Info("grub", "Install the grub to the specified location.", new Parameter[]{
            new Parameter(ARG_DEVICE, Parameter.MANDATORY),
            new Parameter(ARG_DIR, Parameter.MANDATORY)});

    //static final Parameter PARAM_DEVICE = new Parameter(ARG_DEVICE,
    //Parameter.MANDATORY);
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new JGrubInstallCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }

    /**
     *
     */
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        try {
            ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
            String device = ARG_DEVICE.getValue(cmdLine);
            //i am not sure yet
            File destDir = ARG_DIR.getFile(cmdLine);

            out.println("grub The dm is now initialized.");
            DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);

            out.println("The getdevice() method invoking now.");
            Device dev = dm.getDevice(device);

            out.println("The device is successfully initialized.");
            out.println("The Grub Installer is started......wait");
            try {
                try {
                    new GrubJFatFormatter().format(dev, destDir.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    err.println("The ERROR at GRUB FAT FORMAT method.");
                }
            } catch (IOException e) {
                err.println("The ERROR at the FAT FORMAT method...");
            }
            //restart the device
            dm.stop(dev);
            out.println("The device is stopped....");
            dm.start(dev);
            out.println("The Grub successflly installed.");
        } catch (NameNotFoundException e) {
            out.println("The NameNotFoundException occured...");
        } catch (DeviceNotFoundException e) {
            err.println("The Device Not Found...");
        } catch (DriverException e) {
            out.println("The DriverException Occuered......");
        }
    }
}
	
	

	
	
	
	

	
	
	
	
	
	
