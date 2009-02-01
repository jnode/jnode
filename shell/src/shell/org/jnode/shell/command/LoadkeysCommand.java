/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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

import java.io.PrintWriter;
import java.util.Collection;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.KeyboardInterpreterException;
import org.jnode.driver.input.KeyboardLayoutManager;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.shell.syntax.CountryArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.KeyboardLayoutArgument;
import org.jnode.shell.syntax.LanguageArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author Marc DENTY
 * @author crawley@jnode.org
 */
public class LoadkeysCommand extends AbstractCommand {

    private final KeyboardLayoutArgument ARG_LAYOUT = 
        new KeyboardLayoutArgument("layout", Argument.OPTIONAL, "keyboard layout");

    private final CountryArgument ARG_COUNTRY = 
        new CountryArgument("country", Argument.OPTIONAL, "country code");

    private final LanguageArgument ARG_LANGUAGE =
        new LanguageArgument("language", Argument.OPTIONAL, "language parameter");

    private final StringArgument ARG_VARIANT = 
        new StringArgument("variant", Argument.OPTIONAL, "variant parameter");
    
    private final FlagArgument ARG_TRIPLE =
        new FlagArgument("triple", Argument.OPTIONAL, "use layout triples");
    
    private final FlagArgument ARG_ADD =
        new FlagArgument("add", Argument.OPTIONAL, "add a layout binding");
    
    private final FlagArgument ARG_REMOVE =
        new FlagArgument("remove", Argument.OPTIONAL, "remove a layout binding");
    
    private final FlagArgument ARG_SET =
        new FlagArgument("set", Argument.OPTIONAL, "set the current layout");
    
    private final ClassNameArgument ARG_CLASS =
        new ClassNameArgument("class", Argument.OPTIONAL, "the keyboard interpreter class name");
        
    
    public LoadkeysCommand() {
        super("display or change the current keyboard layout");
        registerArguments(ARG_TRIPLE, ARG_LAYOUT, ARG_COUNTRY, ARG_LANGUAGE, ARG_VARIANT,
                ARG_ADD, ARG_REMOVE, ARG_SET, ARG_CLASS);
    }

    public static void main(String[] args) throws Exception {
        new LoadkeysCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        final KeyboardLayoutManager mgr = InitialNaming.lookup(KeyboardLayoutManager.NAME);
        final Collection<Device> kbDevs = 
            DeviceUtils.getDevicesByAPI(KeyboardAPI.class);

        if (ARG_ADD.isSet()) {
            String layoutID = getLayoutID(mgr);
            if (!ARG_CLASS.isSet()) {
                throw new SyntaxErrorException("'class' is required with 'add'");
            }
            String className = ARG_CLASS.getValue();
            mgr.add(layoutID, className);
            out.println("Keyboard layout " + layoutID + " added");
        } else if (ARG_REMOVE.isSet()) {
            String layoutID = getLayoutID(mgr);
            mgr.remove(layoutID);
            out.println("Keyboard layout " + layoutID + " removed");
        } else if (ARG_SET.isSet()) {
            String layoutID = getLayoutID(mgr);
            for (Device kb : kbDevs) {
                final KeyboardAPI api = kb.getAPI(KeyboardAPI.class);
                try {
                    final KeyboardInterpreter kbInt = mgr.createKeyboardInterpreter(layoutID);
                    out.println("Keyboard interpreter for " + kb.getId() + " set to " +
                            kbInt.getClass().getName());
                    api.setKbInterpreter(kbInt);
                } catch (KeyboardInterpreterException ex) {
                    err.println("Keyboard interpreter for " + kb.getId() + " not set: " + ex.getMessage());
                    // Re-throw the exception so that the shell can decide whether or not
                    // to print a stacktrace.
                    throw ex;
                }
            }
        } else {
            for (Device kb : kbDevs) {
                final KeyboardAPI api = kb.getAPI(KeyboardAPI.class);
                out.println("Current keyboard interpreter for " + kb.getId() + " is " +
                        api.getKbInterpreter().getClass().getName());
            }
        }
    }


    private String getLayoutID(KeyboardLayoutManager mgr) throws SyntaxErrorException {
        if (!ARG_TRIPLE.isSet()) {
            if (ARG_LAYOUT.isSet()) {
                return ARG_LAYOUT.getValue();
            } else {
                throw new SyntaxErrorException("'layout' is required if 'triple' is not set");
            }
        } else {
            if (!ARG_COUNTRY.isSet()) {
                throw new SyntaxErrorException("'country' is required if 'triple' is set");
            }
            String country = ARG_COUNTRY.getValue();
            String language = ARG_LANGUAGE.isSet() ? ARG_LANGUAGE.getValue() : "";
            String variant = ARG_VARIANT.isSet() ? ARG_VARIANT.getValue() : "";
            if (language.trim().length() == 0) {
                language = null;
            }
            if (variant.trim().length() == 0) {
                variant = null;
            }
            return mgr.makeKeyboardInterpreterID(country, language, variant);
        }
    }
}
