/*
 * $Id$
 */
package org.jnode.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ResourceTest {

	public static void main(String[] args) throws IOException {
		String resName = (args.length > 0) ? args[0] : ResourceTest.class.getName().replace('.', '/') + ".class";
		URL url = ResourceTest.class.getClassLoader().getResource(resName);
		System.out.println("URL=" + url);
		InputStream is = url.openStream();
		is.close();
	}
}
