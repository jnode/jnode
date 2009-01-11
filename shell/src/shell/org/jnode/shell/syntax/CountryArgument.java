/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell.syntax;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;

/**
 * This argument accepts an ISO country code.
 * 
 * @author crawley@jnode.org
 */
public class CountryArgument extends Argument<String> {
    private static final HashSet<String> validCountries = 
        new HashSet<String>(Arrays.asList(Locale.getISOCountries()));
    
    public CountryArgument(String label, int flags, String description) {
        super(label, flags, new String[0], description);
    }

    @Override
    protected String doAccept(Token token) throws CommandSyntaxException {
        if (validCountries.contains(token.text)) {
            return token.text;
        } else {
            throw new CommandSyntaxException("invalid country code");
        }
    }
    
    @Override
    public void complete(CompletionInfo completion, String partial) {
        for (String country : validCountries) {
            if (country.startsWith(partial)) {
                completion.addCompletion(country);
            }
        }
    }

    @Override
    protected String argumentKind() {
        return "country code";
    }
}
