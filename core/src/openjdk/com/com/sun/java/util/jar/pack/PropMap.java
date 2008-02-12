/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.java.util.jar.pack;

import java.util.*;
import java.util.jar.*;
import java.util.jar.Pack200;
import java.util.zip.*;
import java.io.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
/**
 * Control block for publishing Pack200 options to the other classes.
 */
class PropMap extends TreeMap {
    ArrayList _listeners = new ArrayList(1);
 
    void addListener(PropertyChangeListener listener) {
	_listeners.add(listener);
    }
    
    void removeListener(PropertyChangeListener listener) {
	_listeners.remove(listener);
    }
    
    void addListeners(ArrayList listeners) {
	_listeners.addAll(listeners);
    }
    
    void removeListeners(ArrayList listeners) {
	_listeners.removeAll(listeners);
    }

    // Override:
    public Object put(Object key, Object value) {
	Object oldValue = super.put(key, value);
	if (value != oldValue && _listeners.size() > 0) {
	    // Post the property change event.
	    PropertyChangeEvent event =
		new PropertyChangeEvent(this, (String) key,
					oldValue, value);
	    for (Iterator i = _listeners.iterator(); i.hasNext(); ) {
		PropertyChangeListener listener =
		    (PropertyChangeListener) i.next();
		listener.propertyChange(event);
	    }
	}
	return oldValue;
    }
    
    // All this other stuff is private to the current package.
    // Outide clients of Pack200 do not need to use it; they can
    // get by with generic SortedMap functionality.
    private static Map defaultProps;
    static {
	Properties props = new Properties();
	
	// Allow implementation selected via -Dpack.disable.native=true
        props.put(Utils.DEBUG_DISABLE_NATIVE,
		  String.valueOf(Boolean.getBoolean(Utils.DEBUG_DISABLE_NATIVE)));
	
	// Set the DEBUG_VERBOSE from system
	props.put(Utils.DEBUG_VERBOSE,
		  String.valueOf(Integer.getInteger(Utils.DEBUG_VERBOSE,0)));
	
	// Set the PACK_TIMEZONE_NO_UTC
	props.put(Utils.PACK_DEFAULT_TIMEZONE,
		  String.valueOf(Boolean.getBoolean(Utils.PACK_DEFAULT_TIMEZONE)));
	
	// Limit segment size to less than a megabyte.
	props.put(Pack200.Packer.SEGMENT_LIMIT, ""+(1*1000*1000));
	
	// Preserve file ordering by default.
	props.put(Pack200.Packer.KEEP_FILE_ORDER, Pack200.Packer.TRUE);
	
	// Preserve all modification times by default.
	props.put(Pack200.Packer.MODIFICATION_TIME, Pack200.Packer.KEEP);
	
	// Preserve deflation hints by default.
	props.put(Pack200.Packer.DEFLATE_HINT, Pack200.Packer.KEEP);
	
	// Pass through files with unrecognized attributes by default.
	props.put(Pack200.Packer.UNKNOWN_ATTRIBUTE, Pack200.Packer.PASS);
	
	// Default effort is 5, midway between 1 and 9.
	props.put(Pack200.Packer.EFFORT, "5");
	
	// Define certain attribute layouts by default.
	// Do this after the previous props are put in place,
	// to allow override if necessary.
	try {
	    String propFile = "intrinsic.properties";
	    InputStream propStr = PackerImpl.class.getResourceAsStream(propFile);
	    props.load(new BufferedInputStream(propStr));
	    propStr.close();
	    for (Iterator i = props.entrySet().iterator(); i.hasNext(); ) {
		Map.Entry e = (Map.Entry) i.next();
		String key = (String) e.getKey();
		String val = (String) e.getValue();
		if (key.startsWith("attribute.")) {
		    e.setValue(Attribute.normalizeLayoutString(val));
		}
	    }
	} catch (IOException ee) {
	    throw new RuntimeException(ee);
	}
	
	defaultProps = (new HashMap(props));  // shrink to fit
    }

    PropMap() {
	putAll(defaultProps);
    }
    
    // Return a view of this map which includes only properties
    // that begin with the given prefix.  This is easy because
    // the map is sorted, and has a subMap accessor.
    SortedMap prefixMap(String prefix) {
	int len = prefix.length();
	if (len == 0)
	    return this;
	char nextch = (char)(prefix.charAt(len-1) + 1);
	String limit = prefix.substring(0, len-1)+nextch;
	//System.out.println(prefix+" => "+subMap(prefix, limit));
	return subMap(prefix, limit);
    }
    
    String getProperty(String s) {
	return (String) get(s);
    }
    String getProperty(String s, String defaultVal) {
	String val = getProperty(s);
	if (val == null)
	    return defaultVal;
	return val;
    }
    String setProperty(String s, String val) {
	return (String) put(s, val);
    }
    
    // Get sequence of props for "prefix", and "prefix.*".
    List getProperties(String prefix) {
	Collection values = prefixMap(prefix).values();
	ArrayList res = new ArrayList(values.size());
	res.addAll(values);
	while (res.remove(null));
	return res;
    }
    
    private boolean toBoolean(String val) {
	return Boolean.valueOf(val).booleanValue();
    }
    boolean getBoolean(String s) {
	return toBoolean(getProperty(s));
    }
    boolean setBoolean(String s, boolean val) {
	return toBoolean(setProperty(s, String.valueOf(val)));
    }
    
    int toInteger(String val) {
	if (val == null)  return 0;
	if (Pack200.Packer.TRUE.equals(val))   return 1;
	if (Pack200.Packer.FALSE.equals(val))  return 0;
	return Integer.parseInt(val);
    }
    int getInteger(String s) {
	return toInteger(getProperty(s));
    }
    int setInteger(String s, int val) {
	return toInteger(setProperty(s, String.valueOf(val)));
    }
    
    long toLong(String val) {
	try {
	    return val == null ? 0 : Long.parseLong(val);
	} catch (java.lang.NumberFormatException nfe) {
	    throw new IllegalArgumentException("Invalid value");
	}
    }
    long getLong(String s) {
	return toLong(getProperty(s));
    }
    long setLong(String s, long val) {
	return toLong(setProperty(s, String.valueOf(val)));
    }
    
    int getTime(String s) {
	String sval = getProperty(s, "0");
	if (Utils.NOW.equals(sval)) {
	    return (int)((System.currentTimeMillis()+500)/1000);
	}
	long lval = toLong(sval);
	final long recentSecondCount = 1000000000;
	
	if (lval < recentSecondCount*10 && !"0".equals(sval))
	    Utils.log.warning("Supplied modtime appears to be seconds rather than milliseconds: "+sval);
	
	return (int)((lval+500)/1000);
    }
    
    void list(PrintStream out) {
	PrintWriter outw = new PrintWriter(out);
	list(outw);
	outw.flush();
    }
    void list(PrintWriter out) {
	out.println("#"+Utils.PACK_ZIP_ARCHIVE_MARKER_COMMENT+"[");
	Set defaults = defaultProps.entrySet();
	for (Iterator i = entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry e = (Map.Entry) i.next();
	    if (defaults.contains(e))  continue;
	    out.println("  " + e.getKey() + " = " + e.getValue());
	}
	out.println("#]");
    }
}
