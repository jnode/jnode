/*
 * $Id$
 */
package com.sun.tools.javac.resources;

import java.util.ListResourceBundle;

public final class version extends ListResourceBundle {
    private static final Object[][] contents = {
	{ "jdk", "$(JDK_VERSION)" },
	{ "full", "$(FULL_VERSION)" },
	{ "release", "$(RELEASE)" },
    };

    protected final Object[][] getContents() {
         return contents;
    }
}
