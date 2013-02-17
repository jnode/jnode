/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.shell.syntax.CommandSyntaxException;
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

    private static final String help_layout = "keyboard layout";
    private static final String help_country = "country code";
    private static final String help_lang = "language parameter";
    private static final String help_variant = "variant parameter";
    private static final String help_triple = "use layout triples";
    private static final String help_add = "add a layout binding";
    private static final String help_remove = "remove a layout binding";
    private static final String help_set = "set the current layout";
    private static final String help_class = "the keyboard interpreter class name";
    private static final String help_super = "display or change the current keyboard layout";
    private static final String ex_syntax_class = "'class' is required with 'add'";
    private static final String ex_syntax_layout = "'layout' is required if 'triple' is not set";
    private static final String ex_syntax_country = "'country' is required if 'triple' is set";
    private static final String ex_set_interp = "Keyboard interpreter for %s not set: %s%n";
    private static final String fmt_add = "Keyboard layout %s added%n";
    private static final String fmt_remove = "Keybard layout %s removed%n";
    private static final String fmt_set_interp = "Keyboard interpreter for %s set to %s%n";
    private static final String fmt_list_interp = "Current keyboard interpreter for %s is %s%n";
    
    
    private final KeyboardLayoutArgument argLayout;
    private final CountryArgument argCountry;
    private final LanguageArgument argLanguage;
    private final StringArgument argVariant;
    private final FlagArgument argTriple;
    private final FlagArgument argAdd;
    private final FlagArgument argRemove;
    private final FlagArgument argSet;
    private final ClassNameArgument argClass;
        
    
    public LoadkeysCommand() {
        super(help_super);
        argLayout   = new KeyboardLayoutArgument("layout", Argument.OPTIONAL, help_layout);
        argCountry  = new CountryArgument("country", Argument.OPTIONAL, help_country);
        argLanguage = new LanguageArgument("language", Argument.OPTIONAL, help_lang);
        argVariant  = new StringArgument("variant", Argument.OPTIONAL, help_variant);
        argTriple   = new FlagArgument("triple", Argument.OPTIONAL, help_triple);
        argAdd      = new FlagArgument("add", Argument.OPTIONAL, help_add);
        argRemove   = new FlagArgument("remove", Argument.OPTIONAL, help_remove);
        argSet      = new FlagArgument("set", Argument.OPTIONAL, help_set);
        argClass    = new ClassNameArgument("class", Argument.OPTIONAL, help_class);
        registerArguments(argTriple, argLayout, argCountry, argLanguage, argVariant, 
                          argAdd, argRemove, argSet, argClass);
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

        if (argAdd.isSet()) {
            String layoutID = getLayoutID(mgr);
            if (!argClass.isSet()) {
                throw new CommandSyntaxException(ex_syntax_class);
            }
            String className = argClass.getValue();
            mgr.add(layoutID, className);
            out.format(fmt_add, layoutID);
        } else if (argRemove.isSet()) {
            String layoutID = getLayoutID(mgr);
            mgr.remove(layoutID);
            out.format(fmt_remove, layoutID);
        } else if (argSet.isSet()) {
            String layoutID = getLayoutID(mgr);
            for (Device kb : kbDevs) {
                final KeyboardAPI api = kb.getAPI(KeyboardAPI.class);
                try {
                    final KeyboardInterpreter kbInt = mgr.createKeyboardInterpreter(layoutID);
                    out.format(fmt_set_interp, kb.getId(), kbInt.getClass().getName());
                    api.setKbInterpreter(kbInt);
                } catch (KeyboardInterpreterException ex) {
                    err.format(ex_set_interp, kb.getId(), ex.getLocalizedMessage());
                    // Re-throw the exception so that the shell can decide whether or not
                    // to print a stacktrace.
                    throw ex;
                }
            }
        } else {
            for (Device kb : kbDevs) {
                final KeyboardAPI api = kb.getAPI(KeyboardAPI.class);
                out.format(fmt_list_interp, kb.getId(), api.getKbInterpreter().getClass().getName());
            }
        }
    }

    private String getLayoutID(KeyboardLayoutManager mgr) throws CommandSyntaxException {
        if (!argTriple.isSet()) {
            if (argLayout.isSet()) {
                return argLayout.getValue();
            } else {
                throw new CommandSyntaxException(ex_syntax_layout);
            }
        } else {
            if (!argCountry.isSet()) {
                throw new CommandSyntaxException(ex_syntax_country);
            }
            String country = argCountry.getValue();
            String language = argLanguage.isSet() ? argLanguage.getValue() : "";
            String variant = argVariant.isSet() ? argVariant.getValue() : "";
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
