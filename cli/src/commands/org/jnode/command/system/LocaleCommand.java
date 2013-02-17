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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeSet;
import org.jnode.shell.AbstractCommand;
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
    
    private static final String help_lang = "the local's language";
    private static final String help_country = "the locale's country";
    private static final String help_variant = "the locale's variant";
    @SuppressWarnings("unused")
    private static final String help_list = "if set, list the available Locales";
    private static final String help_super = "Print or change JNode's default Locale";
    private static final String err_no_locale = "No Locale is available for %s %s %s%n";
    private static final String fmt_set = "Setting default Locale to %s%n";
    private static final String fmt_get = "Current default Locale is %s%n";
    
    private final LanguageArgument argLanguage;
    private final CountryArgument argCountry;
    private final StringArgument argVariant;
    private final FlagArgument argList;
    
    public LocaleCommand() {
        super(help_super);
        argLanguage = new LanguageArgument("language", Argument.OPTIONAL, help_lang);
        argCountry  = new CountryArgument("country", Argument.OPTIONAL, help_country);
        argVariant  = new StringArgument("variant", Argument.OPTIONAL, help_variant);
        argList     = new FlagArgument("list", Argument.OPTIONAL, help_lang);
        registerArguments(argLanguage, argCountry, argVariant, argList);
    }
    
    public static void main(String[] args) throws Exception {
        new LocaleCommand().execute(args);
    }

    @Override
    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        if (argLanguage.isSet()) {
            final String language = argLanguage.getValue();

            String country = (argCountry.isSet()) ? argCountry.getValue() : "";
            String variant = (argVariant.isSet()) ? argVariant.getValue() : "";

            Locale locale = findLocale(language, country, variant);
            if (locale == null) {
                getError().getPrintWriter().format(err_no_locale, language, country, variant);
                exit(1);
            }
            out.format(fmt_set, formatLocale(locale));
            Locale.setDefault(locale);
        } else if (argList.isSet()) {
            listLocales(out);
        } else {
            out.format(fmt_get, formatLocale(Locale.getDefault()));
        }
    }
    
    /**
     * List the available Locales in alphabetical order
     * 
     * @param out destination for the listing
     */
    private void listLocales(PrintWriter out) {
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
            ' ' + l.getCountry() + ' ' + l.getVariant());
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
