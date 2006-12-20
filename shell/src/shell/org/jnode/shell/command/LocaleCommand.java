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
import java.util.Locale;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.CountryArgument;
import org.jnode.shell.help.argument.LanguageArgument;

/**
 * Change the default locale of JNode
 * 
 * @author Fabien DUMINY (fduminy@jnode.org) 
 */
public class LocaleCommand {

    static final Argument LANGUAGE = new LanguageArgument("language", "language parameter");
    static final Argument COUNTRY = new CountryArgument("country", "country parameter");
    static final Argument VARIANT = new Argument("variant", "variant parameter");
    
    static final Parameter PARAM_LANGUAGE = new Parameter(LANGUAGE, Parameter.MANDATORY);
    static final Parameter PARAM_COUNTRY = new Parameter(COUNTRY, Parameter.OPTIONAL);
    static final Parameter PARAM_VARIANT = new Parameter(VARIANT, Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info(
            "locale",
            new Syntax[] { 
            	new Syntax("Display the current locale"),
            	new Syntax("Change the current locale\n\tExample : locale fr FR",
                    PARAM_LANGUAGE, PARAM_COUNTRY, PARAM_VARIANT)
				});

    public static void main(String[] args) throws Exception {
        new LocaleCommand().execute(args, System.in, System.out, System.err);
    }

    /**
     * Execute this command
     */
    protected void execute(String[] args, InputStream in, PrintStream out,
            PrintStream err) throws Exception {

        ParsedArguments cmdLine = HELP_INFO.parse(args);
        
        if (PARAM_LANGUAGE.isSatisfied()) {
            final String language = LANGUAGE.getValue(cmdLine);

            String country = "";
            if(PARAM_COUNTRY.isSatisfied())
            {
            	country = COUNTRY.getValue(cmdLine);
            }            

            String variant = "";
            if(PARAM_VARIANT.isSatisfied())
            {
            	variant = VARIANT.getValue(cmdLine);
            }            

            Locale locale = findLocale(language, country, variant);
            if(locale == null)
            {
            	err.println("locale not available "+language+" "+country+" "+variant);
            	return;
            }
            
            Locale.setDefault(locale);
        }        
        
        out.println("current locale : " + Locale.getDefault().getDisplayName());
    }
    
    /**
     * Find the locale among the available locales
     *  
     * @param language
     * @param country
     * @param variant 
     * @return
     */
    protected Locale findLocale(String language, String country, String variant)    
    {
        Locale[] locales = Locale.getAvailableLocales();
        Locale locale = null;
        
        for(int i = 0 ; i < locales.length ; i++)
        {
        	Locale l = locales[i];
        	if(l.getCountry().equals(country) &&
        	   l.getLanguage().equals(language) &&
			   l.getVariant().equals(variant))
        	{
        		locale = l;
        		break;
        	}
        }
        
    	return locale;
    }    
}
