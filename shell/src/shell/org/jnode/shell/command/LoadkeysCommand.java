/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author Mark DENTY
 */
public class LoadkeysCommand {

    static String PS2_KEYBOARD_DEV = "ps2keyboard";

    static final OptionArgument COUNTRY = new OptionArgument("action",
            "country parameter",
            new OptionArgument.Option[] { new OptionArgument.Option("",
                    "Specify country")});

    static final OptionArgument REGION = new OptionArgument("action",
            "region parameter",
            new OptionArgument.Option[] { new OptionArgument.Option("",
                    "Specify region")});

    static final Parameter PARAM_COUNTRY = new Parameter(COUNTRY,
            Parameter.MANDATORY);

    static final Parameter PARAM_REGION = new Parameter(REGION,
            Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info(
            "loadkeys",
            new Syntax[] { new Syntax(
                    "change the current keyboard layout\n\tExample : loadkeys FR fr",
                    new Parameter[] { PARAM_COUNTRY, PARAM_REGION})});

    public static void main(String[] args) throws Exception {
        new LoadkeysCommand().execute(args, System.in, System.out, System.err);
    }

    /**
     * Execute this command
     */
    public void execute(String[] args, InputStream in, PrintStream out,
            PrintStream err) throws Exception {
        String classI10N = "org.jnode.driver.input.l10n.KeyboardInterpreter_";

        ParsedArguments cmdLine = HELP_INFO.parse(args);
        if (!PARAM_COUNTRY.isSatisfied()) {
            err.println("You *MUST* specify a country !");
            return;
        }

        String country = COUNTRY.getValue(cmdLine).toUpperCase();
        String region = REGION.getValue(cmdLine);
        if (region != null) {
            region = region.toLowerCase();
        }

        out.print("Searching for " + country
                + (region == null ? "" : "_" + region) + "...");
        KeyboardInterpreter interpreter = null;
        try {
            if (region == null) {
                interpreter = (KeyboardInterpreter) Thread.currentThread()
                        .getContextClassLoader().loadClass(classI10N + country)
                        .newInstance();
            } else {
                try {
                    interpreter = (KeyboardInterpreter) Thread.currentThread()
                            .getContextClassLoader().loadClass(
                                    classI10N + country + "_" + region)
                            .newInstance();
                } catch (ClassNotFoundException e) {
                    // try to load without region
                    err.print("Class not found : " + classI10N + country + "_"
                            + region + ", trying to load :" + classI10N
                            + country);
                    interpreter = (KeyboardInterpreter) Thread.currentThread()
                            .getContextClassLoader().loadClass(
                                    classI10N + country).newInstance();
                }
            }
        } catch (ClassNotFoundException e) {
            err.println(" Failed, not found");
            e.printStackTrace(err);
        }

        DeviceManager dm;

        dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
        Device kb = dm.getDevice(/* PS2Constants. */PS2_KEYBOARD_DEV);
        KeyboardAPI api = (KeyboardAPI) kb.getAPI(KeyboardAPI.class);
        api.setKbInterpreter(interpreter);
        out.println(" Done.");
    }

}
