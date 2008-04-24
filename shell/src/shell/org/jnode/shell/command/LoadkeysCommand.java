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
 
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.KeyboardInterpreterFactory;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CountryArgument;
import org.jnode.shell.syntax.LanguageArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author Marc DENTY
 * @author crawley@jnode.org
 */
public class LoadkeysCommand extends AbstractCommand {

    private final CountryArgument ARG_COUNTRY = 
        new CountryArgument("country", Argument.OPTIONAL, "country code");

    private final LanguageArgument ARG_LANGUAGE =
        new LanguageArgument("language", Argument.OPTIONAL, "language parameter");

    private final StringArgument ARG_VARIANT = 
        new StringArgument("variant", Argument.OPTIONAL, "variant parameter");
    
    public LoadkeysCommand() {
        super("display or change the current keyboard layout");
        registerArguments(ARG_COUNTRY, ARG_LANGUAGE, ARG_VARIANT);
    }

    public static void main(String[] args) throws Exception {
        new LoadkeysCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute(CommandLine cmdLine, InputStream in, PrintStream out,
            PrintStream err) throws Exception {
        final Collection<Device> kbDevs = 
            DeviceUtils.getDevicesByAPI(KeyboardAPI.class);

        final String country = ARG_COUNTRY.isSet() ? ARG_COUNTRY.getValue() : null;
        
        if (country == null) {
            for (Device kb : kbDevs) {
                final KeyboardAPI api = kb.getAPI(KeyboardAPI.class);
                out.println("Current layout for keyboard " + kb.getId() + ": " +
                        api.getKbInterpreter().getClass().getName());
            }
        }
        else {
            String language = ARG_LANGUAGE.isSet() ? ARG_LANGUAGE.getValue() : "";
            String variant = ARG_VARIANT.isSet() ? ARG_VARIANT.getValue() : "";
            if (language.trim().length() == 0) {
                language = null;
            }
            if (variant.trim().length() == 0) {
                variant = null;
            }

            for (Device kb : kbDevs) {
                final KeyboardAPI api = kb.getAPI(KeyboardAPI.class);
                final KeyboardInterpreter kbInt = 
                    KeyboardInterpreterFactory.getKeyboardInterpreter(
                            country, language, variant);
                if (kbInt != null) {
                    out.println("Setting layout for keyboard " + kb.getId() + " to " +
                        kbInt.getClass().getName());
                    api.setKbInterpreter(kbInt);
                } 
                else {
                    out.println("No suitable keyboard layout found");
                    break;
                }
            }
        }
    }
}
