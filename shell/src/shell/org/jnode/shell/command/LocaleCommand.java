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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeSet;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CountryArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.LanguageArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Manage JNode's default locale.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org) 
 * @author crawley@jnode.org
 */
public class LocaleCommand extends AbstractCommand {

    private final LanguageArgument ARG_LANGUAGE = 
        new LanguageArgument("language", Argument.OPTIONAL, "the locale's language");
    private final CountryArgument ARG_COUNTRY =
        new CountryArgument("country", Argument.OPTIONAL, "the locale's country");
    private final StringArgument ARG_VARIANT = 
        new StringArgument("variant", Argument.OPTIONAL, "the locale's variant");
    private final FlagArgument FLAG_LIST =
        new FlagArgument("list", Argument.OPTIONAL, "if set, list the available Locales");
    
    public LocaleCommand() {
        super("print or change JNode's default Locale");
        registerArguments(ARG_LANGUAGE, ARG_COUNTRY, ARG_VARIANT, FLAG_LIST);
    }
    
    public static void main(String[] args) throws Exception {
        new LocaleCommand().execute(args);
    }

    @Override
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        if (ARG_LANGUAGE.isSet()) {
            final String language = ARG_LANGUAGE.getValue();

            String country = (ARG_COUNTRY.isSet()) ? ARG_COUNTRY.getValue() : "";
            String variant = (ARG_VARIANT.isSet()) ? ARG_VARIANT.getValue() : "";

            Locale locale = findLocale(language, country, variant);
            if (locale == null) {
                err.println("No Locale is available for " + language + " " + country + " " + variant);
                exit(1);
            }
            out.println("Setting default Locale to " + formatLocale(locale));
            Locale.setDefault(locale);
        } else if (FLAG_LIST.isSet()) {
            listLocales(out);
        } else {
            out.println("Current default Locale is " + formatLocale(Locale.getDefault()));
        }
    }
    
    /**
     * List the available Locales in alphabetical order
     * 
     * @param out destination for the listing
     */
    private void listLocales(PrintStream out) {
        // (The getAvailableLocales() method returns a cloned array ...)
        Locale[] locales = Locale.getAvailableLocales();
        TreeSet<Locale> treeSet = new TreeSet<Locale>(new Comparator<Locale>() {
            public int compare(Locale o1, Locale o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        treeSet.addAll(Arrays.asList(locales));
        for (Locale l : treeSet) {
            out.println(formatLocale(l));
        }
    }

    private String formatLocale(Locale l) {
        return (l.getDisplayName() + " : " + l.getLanguage() + 
                " " + l.getCountry() + " " + l.getVariant());
    }

    /**
     * Find a Locale that matches the supplied language / country / variant triple.
     *  
     * @param language the language for the required Locale
     * @param country the country code for the required Locale
     * @param variant the variant for the required Locale
     * @return the requested Locale, or <code>null</code>
     */
    protected Locale findLocale(String language, String country, String variant)    
    {
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.getCountry().equals(country) &&
                    l.getLanguage().equals(language) &&
                    l.getVariant().equals(variant)) {
                return l;
            }
        }
        return null;
    }    
}
