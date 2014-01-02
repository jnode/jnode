/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

package org.jnode.command.system;

import gnu.java.security.action.GetPropertiesAction;
import java.security.AccessController;
import java.util.Properties;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * <code>uname</code> command prints informations about the operation system.
 * With <code>a</code> argument the return syntax of uname posix is:
 * <pre>sysname&lt;SPACE&gt;nodename&lt;SPACE&gt;release&lt;SPACE&gt;version&lt;SPACE&gt;machine&lt;NL&gt;</pre>
 * For example:
 * <p/>
 * <table border='1'>
 * <tr><th rowspan='2'>Argument</th><th colspan='3'>Output</th></tr>
 * <tr><th>JNode</th><th>MacOS 10.8.2</th><th>OpenSUSE 12</th></tr>
 * <tr><td>m</td><td>${os.arch}</td><td>x86_64</td><td>i686</td></tr>
 * <tr><td>n</td><td>${os.name}</td><td>my_mac</td><td>suse12</td></tr>
 * <tr><td>r</td><td>${os.version}</td><td>12.2.0</td><td>3.1.0-1.2-default</td></tr>
 * <tr><td>s</td><td>${os.name}</td><td>Darwin</td><td>Linux</td></tr>
 * <tr><td>v</td><td>${os.name} ${os.version}</td><td>Darwin Kernel Version 12.2.0:
 * Sat Aug 25 00:48:52 PDT 2012; root:xnu-2050.18.24~1/RELEASE_X86_64</td>
 * <td>#1 SMP Thu Nov 3 14:45:45 UTC 2011 (187dde0)</td></tr>
 * </table>
 *
 * @author bastie
 */
public final class UnameCommand extends AbstractCommand {

    /**
     * Command description
     */
    private static final String HELP_UNAME = "Print operating system informations.";
    /**
     * Description of <code>a</code> argument.
     */
    private static final String HELP_ALL = "Same as all params [mnrsv] using.";
    /**
     * Description of <code>m</code> argument.
     */
    private static final String HELP_M = "Print the hardware type on witch system is running.";
    /**
     * Description of <code>n</code> argument.
     */
    private static final String HELP_N = "Print the name of network implementation.";
    /**
     * Description of <code>r</code> argument.
     */
    private static final String HELP_R = "Print the current release level of os implementation.";
    /**
     * Description of <code>s</code> argument.
     */
    private static final String HELP_S = "Print the name of implementation.";
    /**
     * Description of <code>v</code> argument.
     */
    private static final String HELP_V = "Print the current version level of release level of os implementation.";

    /**
     * Flag for argument 'a'.
     */
    private final FlagArgument allArg;
    /**
     * Flag for argument 'm'.
     */
    private final FlagArgument hardwareMaschineArg;
    /**
     * Flag for argument 'n'.
     */
    private final FlagArgument networkImplNameArg;
    /**
     * Flag for argument 'r'.
     */
    private final FlagArgument releaseLevelArg;
    /**
     * Flag for argument 's'.
     */
    private final FlagArgument implNameArg;
    /**
     * Flag for argument 'v'.
     */
    private final FlagArgument versionArg;


    /**
     * Construct new uname command implementation with "amnrsv" arguments.
     */
    public UnameCommand() {
        super(HELP_UNAME);
        allArg = new FlagArgument("a", Argument.OPTIONAL | Argument.SINGLE, HELP_ALL);
        hardwareMaschineArg = new FlagArgument("m", Argument.OPTIONAL | Argument.SINGLE, HELP_M);
        networkImplNameArg = new FlagArgument("n", Argument.OPTIONAL | Argument.SINGLE, HELP_N);
        releaseLevelArg = new FlagArgument("r", Argument.OPTIONAL | Argument.SINGLE, HELP_R);
        implNameArg = new FlagArgument("s", Argument.OPTIONAL | Argument.SINGLE, HELP_S);
        versionArg = new FlagArgument("v", Argument.OPTIONAL | Argument.SINGLE, HELP_V);
        registerArguments(allArg, hardwareMaschineArg, networkImplNameArg, releaseLevelArg, implNameArg, versionArg);
    }

    @Override
    public void execute() throws Exception {
        Properties ps = AccessController.doPrivileged(new GetPropertiesAction());

        String sysname = allArg.isSet() || hardwareMaschineArg.isSet() ? ps.getProperty("os.arch") : "";
        final String nodename = allArg.isSet() || networkImplNameArg.isSet() ? ps.getProperty("os.name") : "";
        final String release = allArg.isSet() || releaseLevelArg.isSet() ? ps.getProperty("os.version") : "";
        final String version = allArg.isSet() || implNameArg.isSet() ? ps.getProperty("os.name") : "";
        final String machine = allArg.isSet() || versionArg.isSet() ? ps.getProperty("os.name")
            + " "
            + ps.getProperty("os.version") : "";
        // If no argument specific, set the default
        if (!allArg.isSet() &&
            !hardwareMaschineArg.isSet() &&
            !networkImplNameArg.isSet() &&
            !releaseLevelArg.isSet() &&
            !implNameArg.isSet() &&
            !versionArg.isSet()) {
            sysname = ps.getProperty("os.arch"); // ps.getProperty("os.arch");
        }

        // Description for more than one arguments contains:
        //"separated by one or more <blank>s."
        final String result =
            String.format("%s %s %s %s %s", sysname, nodename, release, version, machine).trim() + '\n';

        getOutput().getPrintWriter().write(result);
    }
}
