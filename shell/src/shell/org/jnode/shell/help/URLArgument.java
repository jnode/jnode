/*
 * $Id$
 */
package org.jnode.shell.help;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class URLArgument extends Argument {

    /**
     * @param name
     * @param description
     */
    public URLArgument(String name, String description) {
        super(name, description);
    }
    
    /**
     * @param name
     * @param description
     * @param multi
     */
    public URLArgument(String name, String description, boolean multi) {
        super(name, description, multi);
    }

	public URL getURL(ParsedArguments args) throws MalformedURLException {
		return new URL(this.getValue(args));
	}
}

