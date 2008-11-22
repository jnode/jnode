/*
 * Portions Copyright 2000-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 *  (C) Copyright IBM Corp. 1999 All Rights Reserved.
 *  Copyright 1997 The Open Group Research Institute.  All rights reserved.
 */
package sun.security.krb5;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import sun.security.krb5.internal.crypto.EType;
import sun.security.krb5.internal.ktab.*;

/**
 * This class maintains key-value pairs of Kerberos configurable constants
 * from configuration file or from user specified system properties.
 */

public class Config {

    /*
     * Only allow a single instance of Config.
     */
    private static Config singleton = null;

    /* 
     * Hashtable used to store configuration infomation.
     */
    private Hashtable<String,Object> stanzaTable; 

    private static boolean DEBUG = sun.security.krb5.internal.Krb5.DEBUG;

    // these are used for hexdecimal calculation.
    private static final int BASE16_0 = 1;
    private static final int BASE16_1 = 16;
    private static final int BASE16_2 = 16 * 16;
    private static final int BASE16_3 = 16 * 16 * 16;
    private String defaultRealm;   // default kdc realm.

    // used for native interface
    private static native String getWindowsDirectory();


    /**
     * Gets an instance of Config class. One and only one instance (the
     * singleton) is returned.
     *
     * @exception KrbException if error occurs when constructing a Config
     * instance. Possible causes would be configuration file not
     * found, either of java.security.krb5.realm or java.security.krb5.kdc
     * not specified, error reading configuration file. 
     */
    public static synchronized Config getInstance() throws KrbException {
	if (singleton == null) { 
	    singleton = new Config();
	}
	return singleton;
    }

    /**
     * Refresh and reload the Configuration. This could involve,
     * for example reading the Configuration file again or getting 
     * the java.security.krb5.* system properties again.
     *
     * @exception KrbException if error occurs when constructing a Config
     * instance. Possible causes would be configuration file not
     * found, either of java.security.krb5.realm or java.security.krb5.kdc
     * not specified, error reading configuration file. 
     */

    public static synchronized void refresh() throws KrbException {
	singleton = new Config();
	KeyTab.refresh();
    }
		
    
    /**
     * Private constructor - can not be instantiated externally.
     */
    private Config() throws KrbException {
        /*
	 * If these two system properties are being specified by the user,
	 * we ignore configuration file. If either one system property is
	 * specified, we throw exception. If neither of them are specified,
	 * we load the information from configuration file.
	 */
	String kdchost =
	    java.security.AccessController.doPrivileged(
		new sun.security.action.GetPropertyAction
		    ("java.security.krb5.kdc"));
	 defaultRealm = 
	    java.security.AccessController.doPrivileged(
		new sun.security.action.GetPropertyAction
		    ("java.security.krb5.realm"));
	if ((kdchost == null && defaultRealm != null) ||
	    (defaultRealm == null && kdchost != null)) {
	    throw new KrbException
		("System property java.security.krb5.kdc and " +
		 "java.security.krb5.realm both must be set or " +
		 "neither must be set.");
	}
	if (kdchost != null) {
	    /*
	     * If configuration information is only specified by
	     * properties java.security.krb5.kdc and
	     * java.security.krb5.realm, we put both in the hashtable
	     * under [libdefaults].
	     */
	    Hashtable<String,String> kdcs = new Hashtable<String,String> ();
	    kdcs.put("default_realm", defaultRealm);
	    // The user can specify a list of kdc hosts separated by ":"
	    kdchost = kdchost.replace(':', ' ');
	    kdcs.put("kdc", kdchost);
	    stanzaTable = new Hashtable<String,Object> ();
	    stanzaTable.put("libdefaults", kdcs);
	} else {
	    // Read the Kerberos configuration file
	    try {
		Vector<String> configFile;
		configFile = loadConfigFile();		
		stanzaTable = parseStanzaTable(configFile);
	    } catch (IOException ioe) {
		KrbException ke = new KrbException("Could not load " +
						   "configuration file " + 
						   ioe.getMessage());
		ke.initCause(ioe);
		throw(ke);
	    }
	}							 
    }
    
    /**
     * Gets the default int value for the specified name.
     * @param name the name.
     * @return the default Integer, null is returned if no such name and
     * value are found in configuration file, or error occurs when parsing
     * string to integer. 
     */
    public int getDefaultIntValue(String name) {
        String result = null;
        int value = Integer.MIN_VALUE;
        result = getDefault(name);
        if (result != null) {
            try {
                value = parseIntValue(result);
            } catch (NumberFormatException e) {
		if (DEBUG) {
		    System.out.println("Exception in getting value of " +
				       name + " " + 
				       e.getMessage());
		    System.out.println("Setting " + name + 
				       " to minimum value");
		}
		value = Integer.MIN_VALUE;
            }
        }
        return value;
    }
    
    /**
     * Gets the default int value for the specified name in the specified
     * section. <br>This method is quicker by using section name as the
     * search key. 
     * @param name the name.
     * @param sectio the name string of the section.
     * @return the default Integer, null is returned if no such name and
     * value are found in configuration file, or error occurs when parsing
     * string to integer. 
     */
    public int getDefaultIntValue(String name, String section) {
        String result = null;
        int value = Integer.MIN_VALUE;
        result = getDefault(name, section);
        if (result != null) {
            try {
                value = parseIntValue(result);
            } catch (NumberFormatException e) {
		if (DEBUG) {
		    System.out.println("Exception in getting value of " +
				       name +" in section " +
				       section + " "  + e.getMessage());
		    System.out.println("Setting " + name + 
				       " to minimum value");
		}
                value = Integer.MIN_VALUE;
            }
        }
        return value;
    }
    
    /**
     * Gets the default string value for the specified name.
     * @param name the name.
     * @return the default value, null is returned if it cannot be found.
     */
    public String getDefault(String name) {
	if (stanzaTable == null) {
	    return null;
	} else {
	    return getDefault(name, stanzaTable);
	}	
    }
    
    /**
     * This method does the real job to recursively search through the
     * stanzaTable. 
     * @param k the key string.
     * @param t stanzaTable or sub hashtable within it.
     * @return the value found in config file, returns null if no value
     * matched with the key is found. 
     */
    private String getDefault(String k, Hashtable t) {
	String result = null;
	String key;
	if (stanzaTable != null) {
	    for (Enumeration e = t.keys(); e.hasMoreElements(); ) {
		key = (String)e.nextElement();
		Object ob = t.get(key);
		if (ob instanceof Hashtable) {
		    result = getDefault(k, (Hashtable)ob);
		    if (result != null) {
			return result;
		    }
		} else if (key.equalsIgnoreCase(k)) {
		    if (ob instanceof String) {
			return (String)(t.get(key));
		    } else if (ob instanceof Vector) {
			result = "";
			int length = ((Vector)ob).size();
			for (int i = 0; i < length; i++) {
			    if (i == length -1) {
				result +=
				    (String)(((Vector)ob).elementAt(i));
			    } else {
				result +=
				    (String)(((Vector)ob).elementAt(i)) + " ";
			    }
			}
			return result;
		    }
		}
	    }
	}
	return result;
    }
    
    /**
     * Gets the default string value for the specified name in the
     * specified section.
     * <br>This method is quicker by using the section name as the search key. 
     * @param name the name.
     * @param section the name of the section. 
     * @return the default value, null is returned if it cannot be found.
     */
    public String getDefault(String name, String section) {
	String stanzaName;
	String result = null;
	Hashtable subTable;
	
	/*
	 * In the situation when kdc is specified by
	 * java.security.krb5.kdc, we get the kdc from [libdefaults] in
	 * hashtable.
	 */
 	if (name.equalsIgnoreCase("kdc") && 
	    (!section.equalsIgnoreCase("libdefaults")) && 
	    (java.security.AccessController.doPrivileged(
		new sun.security.action.
		GetPropertyAction("java.security.krb5.kdc")) != null)) {
	    result = getDefault("kdc", "libdefaults");
	    return result;
        }
	if (stanzaTable != null) {
	    for (Enumeration e = stanzaTable.keys(); e.hasMoreElements(); ) {
		stanzaName = (String)e.nextElement();
		subTable = (Hashtable)stanzaTable.get(stanzaName);
		if (stanzaName.equalsIgnoreCase(section)) {
		    if (subTable.containsKey(name)) {
			return (String)(subTable.get(name));
		    }
		} else if (subTable.containsKey(section)) {
		    Object ob = subTable.get(section);
		    if (ob instanceof Hashtable) {
			Hashtable temp = (Hashtable)ob;
			if (temp.containsKey(name)) {
			    Object object = temp.get(name); 
			    if (object instanceof Vector) {
				result = "";
				int length = ((Vector)object).size();
				for (int i = 0; i < length; i++) {
				    if (i == length - 1)  {
					result +=
					(String)(((Vector)object).elementAt(i));
				    } else {
					result +=
					(String)(((Vector)object).elementAt(i))
						+ " ";
				    }
				}
			    } else {
				result = (String)object;
			    }
			}
		    }
		}
	    }
	}
	return result;
    }

    /**
     * Gets the default boolean value for the specified name.
     * @param name the name.
     * @return the default boolean value, false is returned if it cannot be
     * found.
     */
    public boolean getDefaultBooleanValue(String name) {
	String val = null;
	if (stanzaTable == null) {
	    val = null;
	} else { 
	    val = getDefault(name, stanzaTable);
	}
	if (val != null && val.equalsIgnoreCase("true")) {
	    return true;
	} else {
	    return false;
	}
    }
   
    /**
     * Gets the default boolean value for the specified name in the
     * specified section.
     * <br>This method is quicker by using the section name as the search key. 
     * @param name the name.
     * @param section the name of the section. 
     * @return the default boolean value, false is returned if it cannot be
     * found.
     */
    public boolean getDefaultBooleanValue(String name, String section) {
	String val = getDefault(name, section);
	if (val != null && val.equalsIgnoreCase("true")) {
	    return true;
	} else {
	    return false;
	}
    }
    
    /**
     * Parses a string to an integer. The convertible strings include the
     * string representations of positive integers, negative integers, and
     * hex decimal integers.  Valid inputs are, e.g., -1234, +1234,
     * 0x40000. 
     * 
     * @param input the String to be converted to an Integer.
     * @return an numeric value represented by the string
     * @exception NumberFormationException if the String does not contain a
     * parsable integer. 
     */
    private int parseIntValue(String input) throws NumberFormatException {
        int value = 0;
        if (input.startsWith("+")) {
            String temp = input.substring(1);
            return Integer.parseInt(temp);
        } else if (input.startsWith("0x")) {
            String temp = input.substring(2);
            char[] chars = temp.toCharArray();
            if (chars.length > 8) {
                throw new NumberFormatException();
            } else {
                for (int i = 0; i < chars.length; i++) {
                    int index = chars.length - i - 1;
                    switch (chars[i]) {
                    case '0':
                        value += 0;
                        break;
                    case '1':
                        value += 1 * getBase(index);
                        break;
                    case '2':
                        value += 2 * getBase(index);
                        break;
                    case '3':
                        value += 3 * getBase(index);
                        break;
                    case '4':
                        value += 4 * getBase(index);
                        break;
                    case '5':
                        value += 5 * getBase(index);
                        break;
                    case '6':
                        value += 6 * getBase(index);
                        break;
                    case '7':
                        value += 7 * getBase(index);
                        break;
                    case '8':
                        value += 8 * getBase(index);
                        break;
                    case '9':
                        value += 9 * getBase(index);
                        break;
                    case 'a':
                    case 'A':
                        value += 10 * getBase(index);
                        break;
                    case 'b':
                    case 'B':
                        value += 11 * getBase(index);
                        break;
                    case 'c':
                    case 'C':
                        value += 12 * getBase(index);
                        break;
                    case 'd':
                    case 'D':
                        value += 13 * getBase(index);
                        break;
                    case 'e':
                    case 'E':
                        value += 14 * getBase(index);
                        break;
                    case 'f':
                    case 'F':
                        value += 15 * getBase(index);
                        break;
                    default:
                        throw new NumberFormatException("Invalid numerical format");
                    }
                }
            }
            if (value < 0) {
                throw new NumberFormatException("Data overflow.");
            }
        } else {
	    value = Integer.parseInt(input);
	}
        return value;
    }
    
    private int getBase(int i) {
        int result = 16;
        switch (i) {
	case 0:
	    result = BASE16_0;
	    break;
	case 1:
	    result = BASE16_1;
	    break;
	case 2:
	    result = BASE16_2;
	    break;
	case 3:
	    result = BASE16_3;
	    break;
	default:
	    for (int j = 1; j < i; j++) {
		result *= 16; 
	    }
	}
        return result;
    }

    /**
     * Finds the matching value in the hashtable.
     */
    private String find(String key1, String key2) {
	String result;
	if ((stanzaTable != null) && 
	    ((result = (String)
		(((Hashtable)(stanzaTable.get(key1))).get(key2))) != null)) {
	    return result;
	} else {
	    return "";
	}
    }
    
    /**
     * Reads name/value pairs to the memory from the configuration
     * file. The default location of the configuration file is in java home
     * directory. 
     *
     * Configuration file contains information about the default realm,
     * ticket parameters, location of the KDC and the admin server for
     * known realms, etc. The file is divided into sections. Each section
     * contains one or more name/value pairs with one pair per line. A
     * typical file would be: 
     * [libdefaults]
     *		default_realm = EXAMPLE.COM
     *		default_tgs_enctypes = des-cbc-md5
     * 		default_tkt_enctypes = des-cbc-md5
     * [realms]
     *		EXAMPLE.COM = {
     *			kdc = kerberos.example.com
     *			kdc = kerberos-1.example.com
     *			admin_server = kerberos.example.com
     *			}
     * 		SAMPLE_COM = {
     *			kdc = orange.sample.com
     *			admin_server = orange.sample.com
     *			}
     * [domain_realm]
     *		blue.sample.com = TEST.SAMPLE.COM
     *		.backup.com	= EXAMPLE.COM
     */
    private Vector<String> loadConfigFile() throws IOException {
        try { 
            final String fileName = getFileName();
	    if (!fileName.equals("")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
		java.security.AccessController.doPrivileged(
		new java.security.PrivilegedExceptionAction<FileInputStream> () { 
		public FileInputStream run() throws IOException { 
		    return new FileInputStream(fileName); 
		} 
		})));
		String Line;
		Vector<String> v = new Vector<String> ();
                String previous = null;
		while ((Line = br.readLine()) != null) {
                    // ignore comments and blank line in the configuration file.
		    // Comments start with #.
                    if (!(Line.startsWith("#") || Line.trim().isEmpty())) {
                        String current = Line.trim();
                        // In practice, a subsection might look like:
                        //      EXAMPLE.COM =
                        //      {
                        //              kdc = kerberos.example.com
                        //              ...
                        //      }
                        // Before parsed into stanza table, it needs to be
                        // converted into formal style:
                        //      EXAMPLE.COM = {
                        //              kdc = kerberos.example.com
                        //              ...
                        //      }
                        //
                        // So, if a line is "{", adhere to the previous line.
                        if (current.equals("{")) {
                            if (previous == null) {
                                throw new IOException(
                                    "Config file should not start with \"{\"");
                            }
                            previous += " " + current;
                        } else {
                            if (previous != null) {
                                v.addElement(previous);
                            }
                            previous = current;
                        }
                    }
                    }
                if (previous != null) {
                    v.addElement(previous);
		}

		br.close(); 
		return v;
	    } 
	    return null;
	} catch (java.security.PrivilegedActionException pe) {
	    throw (IOException)pe.getException();
        }
    } 
    

    /**
     * Parses stanza names and values from configuration file to
     * stanzaTable (Hashtable). Hashtable key would be stanza names,
     * (libdefaults, realms, domain_realms, etc), and the hashtable value
     * would be another hashtable which contains the key-value pairs under
     * a stanza name.
     */
    private Hashtable<String,Object> parseStanzaTable(Vector<String> v) throws KrbException {
	if (v == null) {
            throw new KrbException("I/O error while reading" +
			" configuration file.");
        }
	Hashtable<String,Object> table = new Hashtable<String,Object> ();
	for (int i = 0; i < v.size(); i++) {
	    String line = v.elementAt(i).trim();
	    if (line.equalsIgnoreCase("[realms]")) {
		for (int count = i + 1; count < v.size() + 1; count++) {
		    // find the next stanza name
		    if ((count == v.size()) || 
			(v.elementAt(count).startsWith("["))) {
			Hashtable<String,Hashtable<String,Vector<String>>> temp = 
                            new Hashtable<String,Hashtable<String,Vector<String>>>();
			temp = parseRealmField(v, i + 1, count);
			table.put("realms", temp); 
                        i = count - 1;
			break;
		    }
		}
	    } else if (line.equalsIgnoreCase("[capaths]")) {
                for (int count = i + 1; count < v.size() + 1; count++) {
		    // find the next stanza name
                    if ((count == v.size()) || 
			(v.elementAt(count).startsWith("["))) {
                        Hashtable<String,Hashtable<String,Vector<String>>> temp = 
                            new Hashtable<String,Hashtable<String,Vector<String>>>();
                        temp = parseRealmField(v, i + 1, count);
                        table.put("capaths", temp); 
                        i = count - 1;
                        break;
                    }
                }
            } else if (line.startsWith("[") && line.endsWith("]")) {
		String key = line.substring(1, line.length() - 1);
		for (int count = i + 1; count < v.size() + 1; count++) {
		    // find the next stanza name
		    if ((count == v.size()) || 
			(v.elementAt(count).startsWith("["))) {
			Hashtable<String,String> temp = 
                            parseField(v, i + 1, count);
			table.put(key, temp);
			i = count - 1;
			break;
		    }
		}
	    }
	}
	return table;
    }
    
    /**
     * Gets the default configuration file name. The file will be searched
     * in a list of possible loations in the following order: 
     * 1. the location and file name defined by system property
     * "java.security.krb5.conf", 
     * 2. at Java home lib\security directory with "krb5.conf" name, 
     * 3. "krb5.ini" at Java home,
     * 4. at windows directory with the name of "krb5.ini" for Windows,
     * /etc/krb5/krb5.conf for Solaris, /etc/krb5.conf for Linux.
     */
    private String getFileName() {
        String name = 
	    java.security.AccessController.doPrivileged(
				new sun.security.action.
				GetPropertyAction("java.security.krb5.conf"));
        if (name != null) {
	    boolean temp = 
		java.security.AccessController.doPrivileged(
				new FileExistsAction(name));
	    if (temp)
		return name;
	} else {
            name = java.security.AccessController.doPrivileged(
			new sun.security.action.
			GetPropertyAction("java.home")) + File.separator + 
				"lib" + File.separator + "security" + 
				File.separator + "krb5.conf";
	    boolean temp = 
		java.security.AccessController.doPrivileged(
				new FileExistsAction(name));
	    if (temp) {
                return name;
	    } else {
		String osname = 
			java.security.AccessController.doPrivileged(
			new sun.security.action.GetPropertyAction("os.name"));
		if (osname.startsWith("Windows")) {
		    try {
			Credentials.ensureLoaded();
		    } catch (Exception e) {
 			// ignore exceptions
		    }
		    if (Credentials.alreadyLoaded) {
			if ((name = getWindowsDirectory()) == null) {
			    name = "c:\\winnt\\krb5.ini";
			} else if (name.endsWith("\\")) {
			    name += "krb5.ini";
			} else {
			    name += "\\krb5.ini";
			}
		    } else {
			name = "c:\\winnt\\krb5.ini";
		    }
                } else if (osname.startsWith("SunOS")) {
                    name =  "/etc/krb5/krb5.conf";
                } else if (osname.startsWith("Linux")) {
                    name =  "/etc/krb5.conf";
                }
            }   
        }
        if (DEBUG) {
            System.out.println("Config name: " + name);
        }
        return name;
    }

    /**
     * Parses key-value pairs under a stanza name.	 
     */
    private Hashtable<String,String>  parseField(Vector<String> v, int start, int end) {
	Hashtable<String,String> table = new Hashtable<String,String> ();
	String line;
	for (int i = start; i < end; i++) {
	    line = v.elementAt(i);
	    for (int j = 0; j < line.length(); j++) {
		if (line.charAt(j) == '=') {
		    String key = (line.substring(0, j)).trim();
		    String value = (line.substring(j + 1)).trim();
		    table.put(key, value);
		    break;
		}
	    }
	}
	return table;
    }
    
    /**
     * Parses key-value pairs under [realms].  The key would be the realm
     * name, the value would be another hashtable which contains
     * information for the realm given within a pair of braces.
     */
    private Hashtable<String,Hashtable<String,Vector<String>>> parseRealmField(Vector<String> v, int start, int end) {
	Hashtable<String,Hashtable<String,Vector<String>>> table = new Hashtable<String,Hashtable<String,Vector<String>>> ();
	String line;
	for (int i = start; i < end; i++) {
	    line = v.elementAt(i).trim();
	    if (line.endsWith("{")) {
		String key = "";
		for (int j = 0; j < line.length(); j++) {
		    if (line.charAt(j) == '=') {
			key = line.substring(0, j).trim();
			// get the key
			break;
		    }
		}
		for (int k = i + 1; k < end; k++) {
		    boolean found = false;
		    line = v.elementAt(k).trim();
		    for (int l = 0; l < line.length(); l++) {
			if (line.charAt(l) == '}') {
			    found = true;
			    break;
			}
		    }
		    if (found == true) {
			Hashtable<String,Vector<String>> temp = parseRealmFieldEx(v, i + 1, k);
			table.put(key, temp);
			i = k;
			found = false;
			break;
		    }
		    
		}
	    }
	}
	return table;
    }
    
    /**
     * Parses key-value pairs within each braces under [realms].
     */
    private Hashtable<String,Vector<String>> parseRealmFieldEx(Vector<String> v, int start, int end) {
	Hashtable<String,Vector<String>> table = 
                new Hashtable<String,Vector<String>> ();
	Vector<String> keyVector = new Vector<String> ();
	Vector<String> nameVector = new Vector<String> ();
	String line = "";
	String key;
	for (int i = start; i < end; i++) {
	    line = v.elementAt(i);
	    for (int j = 0; j < line.length(); j++) {
		if (line.charAt(j) == '=') {
		    int index;
		    key = line.substring(0, j - 1).trim();
		    if (! exists(key, keyVector)) {
			keyVector.addElement(key);
			nameVector = new Vector<String> ();
		    } else {
			nameVector = table.get(key);
		    }
		    nameVector.addElement((line.substring(j + 1)).trim());
		    table.put(key, nameVector);
		    break;
		}
	    }
	}
	return table;
    }									  
    
    /**
     * Compares the key with the known keys to see if it exists.
     */
    private boolean exists(String key, Vector v) {
	boolean exists = false;
	for (int i = 0; i < v.size(); i++) {
	    if (((String)(v.elementAt(i))).equals(key)) {
		exists = true;
	    }
	}
	return exists;
    }
    
    /**
     * For testing purpose. This method lists all information being parsed from
     * the configuration file to the hashtable.
     */
    public void listTable() {
        listTable(stanzaTable);
    }
    
    private void listTable(Hashtable table) {
	Vector v = new Vector();
	String key;
	if (stanzaTable != null) {
	    for (Enumeration e = table.keys(); e.hasMoreElements(); ) {
		key = (String)e.nextElement();
		Object object = table.get(key);
		if (table == stanzaTable) {
		    System.out.println("[" + key + "]");
		}
		if (object instanceof Hashtable) {
		    if (table != stanzaTable) 
			System.out.println("\t" + key + " = {");
		    listTable((Hashtable)object);
		    if (table != stanzaTable) 
			System.out.println("\t}");
		    
		} else if (object instanceof String) {
		    System.out.println("\t" + key + " = " + 
				(String)table.get(key));
		} else if (object instanceof Vector) {
		    v = (Vector)object;
		    for (int i = 0; i < v.size(); i++) {
			System.out.println("\t" + key + " = " + 
				(String)v.elementAt(i));
		    }
		}
	    }
	} else {
	    System.out.println("Configuration file not found.");
	}
    }
    
    /**
     * Returns the default encryption types.
     * 
     */
    public int[] defaultEtype(String enctypes) {
        String default_enctypes;
        default_enctypes = getDefault(enctypes, "libdefaults");
        String delim = " ";
        StringTokenizer st;
        int[] etype;
        if (default_enctypes == null) {
	    if (DEBUG) {
		System.out.println("Using builtin default etypes for " + 
		    enctypes);
	    }
            etype = EType.getBuiltInDefaults();
        } else {
            for (int j = 0; j < default_enctypes.length(); j++) {
                if (default_enctypes.substring(j, j + 1).equals(",")) {
		    // only two delimiters are allowed to use 
		    // according to Kerberos DCE doc.
                    delim = ",";       
                    break;
                }
            }
            st = new StringTokenizer(default_enctypes, delim);
	    int len = st.countTokens();
	    ArrayList<Integer> ls = new ArrayList<Integer> (len);
	    int type;
            for (int i = 0; i < len; i++) {
                type = getType(st.nextToken());
                if ((type != -1) && 
		    (EType.isSupported(type))) {
		    ls.add(type);
		}
            }
	    if (ls.size() == 0) {
		if (DEBUG) {
		    System.out.println(
			"no supported default etypes for " + enctypes);
		}
		return null;
	    } else {
		etype = new int[ls.size()];
		for (int i = 0; i < etype.length; i++) {
		    etype[i] = ls.get(i);
		}
	    }
        }

        if (DEBUG) {
            System.out.print("default etypes for " + enctypes + ":");
	    for (int i = 0; i < etype.length; i++) {
		System.out.print(" " + etype[i]);
	    }
	    System.out.println(".");
        }
        return etype;
    }
    
    
    /**
     * Get the etype and checksum value for the specified encryption and
     * checksum type.
     * 
     */
    /*
     * This method converts the string representation of encryption type and
     * checksum type to int value that can be later used by EType and
     * Checksum classes.
     */
    public int getType(String input) {
	int result = -1;
        if (input == null) {
            return result;
        }
        if (input.startsWith("d") || (input.startsWith("D"))) { 
            if (input.equalsIgnoreCase("des-cbc-crc")) {
                result = EncryptedData.ETYPE_DES_CBC_CRC;
            } else if (input.equalsIgnoreCase("des-cbc-md5")) {
                result = EncryptedData.ETYPE_DES_CBC_MD5;
            } else if (input.equalsIgnoreCase("des-mac")) {
                result = Checksum.CKSUMTYPE_DES_MAC;
            } else if (input.equalsIgnoreCase("des-mac-k")) {
                result = Checksum.CKSUMTYPE_DES_MAC_K;
            } else if (input.equalsIgnoreCase("des-cbc-md4")) {
                result = EncryptedData.ETYPE_DES_CBC_MD4;
            } else if (input.equalsIgnoreCase("des3-cbc-sha1") ||
		input.equalsIgnoreCase("des3-hmac-sha1") ||
		input.equalsIgnoreCase("des3-cbc-sha1-kd") ||
		input.equalsIgnoreCase("des3-cbc-hmac-sha1-kd")) {
		result = EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD;
	    }
        } else if (input.startsWith("a") || (input.startsWith("A"))) {
	    // AES
	    if (input.equalsIgnoreCase("aes128-cts") ||
		input.equalsIgnoreCase("aes128-cts-hmac-sha1-96")) {
		result = EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96;
	    } else if (input.equalsIgnoreCase("aes256-cts") ||
		input.equalsIgnoreCase("aes256-cts-hmac-sha1-96")) {
		result = EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96;
	    // ARCFOUR-HMAC
	    } else if (input.equalsIgnoreCase("arcfour-hmac") ||
		   input.equalsIgnoreCase("arcfour-hmac-md5")) {
		result = EncryptedData.ETYPE_ARCFOUR_HMAC;
	    }
	// RC4-HMAC
        } else if (input.equalsIgnoreCase("rc4-hmac")) {
	    result = EncryptedData.ETYPE_ARCFOUR_HMAC;
        } else if (input.equalsIgnoreCase("CRC32")) {
            result = Checksum.CKSUMTYPE_CRC32;
        } else if (input.startsWith("r") || (input.startsWith("R"))) {
            if (input.equalsIgnoreCase("rsa-md5")) {
                result = Checksum.CKSUMTYPE_RSA_MD5;
            } else if (input.equalsIgnoreCase("rsa-md5-des")) {
                result = Checksum.CKSUMTYPE_RSA_MD5_DES;
	    }
	} else if (input.equalsIgnoreCase("hmac-sha1-des3-kd")) {
	    result = Checksum.CKSUMTYPE_HMAC_SHA1_DES3_KD;
	} else if (input.equalsIgnoreCase("hmac-sha1-96-aes128")) {
	    result = Checksum.CKSUMTYPE_HMAC_SHA1_96_AES128;
	} else if (input.equalsIgnoreCase("hmac-sha1-96-aes256")) {
	    result = Checksum.CKSUMTYPE_HMAC_SHA1_96_AES256;
	} else if (input.equalsIgnoreCase("hmac-md5-rc4") || 
		input.equalsIgnoreCase("hmac-md5-arcfour") ||
		input.equalsIgnoreCase("hmac-md5-enc")) {
	    result = Checksum.CKSUMTYPE_HMAC_MD5_ARCFOUR;
        } else if (input.equalsIgnoreCase("NULL")) {
            result = EncryptedData.ETYPE_NULL;
        }

        return result;
    } 

    /**
     * Resets the default kdc realm.
     * We do not need to synchronize these methods since assignments are atomic
     */
    public void resetDefaultRealm(String realm) {
	defaultRealm = realm;
        if (DEBUG) {
            System.out.println(">>> Config reset default kdc " + defaultRealm);
        }

    }

    /**
     * Check to use addresses in tickets
     * use addresses if "no_addresses" or "noaddresses" is set to false
     */
    public boolean useAddresses() {
	boolean useAddr = false;
	// use addresses if "no_addresses" is set to false
	String value = getDefault("no_addresses", "libdefaults");
	useAddr = (value != null && value.equalsIgnoreCase("false"));
	if (useAddr == false) {
	    // use addresses if "noaddresses" is set to false
	    value = getDefault("noaddresses", "libdefaults");
	    useAddr = (value != null && value.equalsIgnoreCase("false"));
	}
	return useAddr;
    }

    /**
     * Gets default realm.
     */
    public String getDefaultRealm() {
        return getDefault("default_realm", "libdefaults");
    }

    /**
     * Returns a list of KDC's with each KDC separated by a space
     *
     * @param realm the realm for which the master KDC is desired
     * @return the list of KDCs
     */
    public String getKDCList(String realm) {
	if (realm == null) {
	    realm = getDefaultRealm();
	}
	String kdcs = getDefault("kdc", realm);
        if (kdcs == null) {
            return null;
	}
	return kdcs;
    }

    static class FileExistsAction 
	implements java.security.PrivilegedAction<Boolean> {

	private String fileName;

	public FileExistsAction(String fileName) {
	    this.fileName = fileName;
	}

	public Boolean run() {
	    return new File(fileName).exists();
	}
    }

}
