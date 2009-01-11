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

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandCompletions;
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
    
    @Override
    protected String argumentKind() {
        return "url";
    }

    @Override
    protected URL doAccept(Token value) throws CommandSyntaxException {
        try {
            return new URL(value.text);
        } catch (MalformedURLException ex) {
            throw new CommandSyntaxException(ex.getMessage());
        }
    }

    @Override
    public void complete(final CompletionInfo completion, final String partial) {
        try {
            // If 'partial' is a well-formed "file:" URL with no host, port, 
            // user or query, do completion on the path component.
            URL url = new URL(partial);
            if (url.getProtocol().equals("file") && 
                    (url.getAuthority() == null || url.getAuthority().length() == 0) &&
                    (url.getQuery() == null || url.getQuery().length() == 0)) {
                // Use a FileArgument to do the work of completing the pathname, 
                // capturing the results using our own CompletionInfo object.
                CompletionInfo myCompletion = new CommandCompletions();
                new FileArgument(null, getFlags()).complete(myCompletion, url.getPath());
                // Then turn the completions back into "file:" URLs
                for (String c : myCompletion.getCompletions()) {
                    // (Kludge - the 'true' argument prevents an extra space
                    // character from being appended to the completions.)
                    completion.addCompletion("file:" + c, true);
                }
            }
        } catch (MalformedURLException ex) {
            // No completion possible
        }
    }
}
