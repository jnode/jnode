/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.KeyboardInterpreterFactory;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author Marc DENTY
 * @version Feb 2004
 * @since 0.15
 */
public class LoadkeysCommand {

    static final Argument COUNTRY = new Argument("action", "country parameter");

    static final Argument REGION = new Argument("action", "region parameter");

    static final Parameter PARAM_COUNTRY = new Parameter(COUNTRY);

    static final Parameter PARAM_REGION = new Parameter(REGION, Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info(
            "loadkeys",
            new Syntax[] { 
            	new Syntax("Display the current keyboard layout"),
            	new Syntax("change the current keyboard layout\n\tExample : loadkeys FR fr",
                    PARAM_COUNTRY, PARAM_REGION)
				});

    public static void main(String[] args) throws Exception {
        new LoadkeysCommand().execute(args, System.in, System.out, System.err);
    }

    /**
     * Execute this command
     */
    protected void execute(String[] args, InputStream in, PrintStream out,
            PrintStream err) throws Exception {
        final Collection kbDevs = DeviceUtils
                .getDevicesByAPI(KeyboardAPI.class);

        ParsedArguments cmdLine = HELP_INFO.parse(args);

        for (Iterator i = kbDevs.iterator(); i.hasNext();) {
            final Device kb = (Device) i.next();
            final KeyboardAPI api = (KeyboardAPI) kb.getAPI(KeyboardAPI.class);

            if (!PARAM_COUNTRY.isSatisfied()) {
                out.println("layout currently loaded : "
                        + api.getKbInterpreter().getClass().getName());
            } else {
                final String country = COUNTRY.getValue(cmdLine);
                final String region = REGION.getValue(cmdLine);

                final KeyboardInterpreter kbInt = KeyboardInterpreterFactory
                        .getKeyboardInterpreter(country, region);
                if (kbInt != null) {
                	api.setKbInterpreter(kbInt);
                } else {
                	out.println("Not found");
                }
            }
        }
        out.println(" Done.");
    }
}
