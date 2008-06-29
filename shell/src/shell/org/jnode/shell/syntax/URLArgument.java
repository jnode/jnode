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

import java.net.MalformedURLException;
import java.net.URL;

import org.jnode.shell.CommandLine.Token;

/**
 * This class implements URL-valued command line arguments.  At the moment, it performs
 * no special syntax checking and does no completion.
 * 
 * @author crawley@jnode.org
 */
public class URLArgument extends Argument<URL> {

    public URLArgument(String label, int flags, String description) {
        super(label, flags, new URL[0], description);
    }

    public URLArgument(String label, int flags) {
        this(label, flags, null);
    }

    public URLArgument(String label) {
        this(label, 0, null);
    }
    
//    public URL getValueAsURL() throws MalformedURLException {
//        return getValue();
//    }
    
    @Override
    protected String argumentKind() {
        return "url";
    }

    @Override
    protected URL doAccept(Token value) throws CommandSyntaxException {
        try {
            return new URL(value.token);
        } catch (MalformedURLException ex) {
            throw new CommandSyntaxException(ex.getMessage(), ex);
        }
    }
}
