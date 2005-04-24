/*
 * $Id$
 */
package org.jnode.test.fs;

import java.io.File;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DirTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        final String name = (args.length > 0) ? args[0] : "";
        System.out.println("Creating File(\"" + name + "\")");
        final File f = new File(name);
        System.out.println("Created File(\"" + name + "\")");
        
        System.out.println("List entries on File(\"" + name + "\")");
        final String[] entries = f.list();
        System.out.println("Listed " + ((entries != null) ? ""+entries.length : "#null#") + " entries on File(\"" + name + "\")");
        
    }

}
