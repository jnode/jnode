/*
 * $Id$
 */
package com.sun.tools.javac.resources;

import java.util.ListResourceBundle;

public final class version extends ListResourceBundle {
    private static final Object[][] contents = {
	{ "jdk", "openjdk-1.6.0" },
	{ "full", "openjdk-1.6.0-jnode" },
	{ "release", "openjdk-1.6.0-jnode" },
    };

    protected final Object[][] getContents() {
         return contents;
    }
}
