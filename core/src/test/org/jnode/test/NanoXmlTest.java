/*
 * $Id$
 */
package org.jnode.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import nanoxml.XMLElement;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NanoXmlTest {

	public static void main(String[] args) 
	throws IOException {
		XMLElement xml = new XMLElement(new Hashtable(), true, false);
		xml.parseFromReader(new FileReader(args[0]));
		System.out.println(xml);
	}
}
