/*
 * Portions Copyright 2000-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 * ===========================================================================
 *  (C) Copyright IBM Corp. 1999 All Rights Reserved.
 * 
 *  Copyright 1997 The Open Group Research Institute.  All rights reserved.
 * ===========================================================================
 * 
 */
package sun.security.krb5.internal.ccache;

import sun.security.krb5.*;
import sun.security.krb5.internal.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.*;

/**
 * CredentialsCache stores credentials(tickets, session keys, etc) in a
 * semi-permanent store
 * for later use by different program.
 *
 * @author Yanni Zhang
 * @author Ram Marti
 */

public class FileCredentialsCache extends CredentialsCache
    implements FileCCacheConstants {	
    public int version;
    public Tag tag; // optional
    public PrincipalName primaryPrincipal;
    public Realm primaryRealm;
    private Vector<Credentials> credentialsList;
    private static String dir;
    private static boolean DEBUG = Krb5.DEBUG;

    public static synchronized FileCredentialsCache acquireInstance(
		PrincipalName principal, String cache) {
        try {
            FileCredentialsCache fcc = new FileCredentialsCache();
            if (cache == null) {
                cacheName = fcc.getDefaultCacheName();
            } else {
		cacheName = fcc.checkValidation(cache);
	    }
            if ((cacheName == null) || !(new File(cacheName)).exists()) { 
		// invalid cache name or the file doesn't exist
                return null;
            }
            if (principal != null) {
                fcc.primaryPrincipal = principal;
                fcc.primaryRealm = principal.getRealm();
            }
            fcc.load(cacheName);
            return fcc;
        } catch (IOException e) {  
	    // we don't handle it now, instead we return a null at the end.
	    if (DEBUG) {
		e.printStackTrace();
	    }
        } catch (KrbException e) {
	    // we don't handle it now, instead we return a null at the end.
	    if (DEBUG) {
		e.printStackTrace();
	    }
        }
        return null;
    }
    
    public static FileCredentialsCache acquireInstance() {
        return acquireInstance(null, null);
    }

    static synchronized FileCredentialsCache New(PrincipalName principal,
						String name) {
        try {
            FileCredentialsCache fcc = new FileCredentialsCache();
            cacheName = fcc.checkValidation(name);
            if (cacheName == null) { 
		// invalid cache name or the file doesn't exist
                return null;
            }
            fcc.init(principal, cacheName);
            return fcc;
        }
        catch (IOException e) {
        }
        catch (KrbException e) {
        } 
        return null;
    }

    static synchronized FileCredentialsCache New(PrincipalName principal) {
	try {
            FileCredentialsCache fcc = new FileCredentialsCache();
            cacheName = fcc.getDefaultCacheName();
            fcc.init(principal, cacheName);
            return fcc;
        }
        catch (IOException e) {
	    if (DEBUG) {
		e.printStackTrace();
	    }
        } catch (KrbException e) {
	    if (DEBUG) {
		e.printStackTrace();
	    }
	    
        }
        return null;
    }

    private FileCredentialsCache() {
    }

    boolean exists(String cache) {
	File file = new File(cache);
	if (file.exists()) {
	    return true;
	} else return false;
    }
    
    synchronized void init(PrincipalName principal, String name)
	throws IOException, KrbException {
        primaryPrincipal = principal;
        primaryRealm = principal.getRealm();
        CCacheOutputStream cos =
	    new CCacheOutputStream(new FileOutputStream(name));
        version = KRB5_FCC_FVNO_3;
        cos.writeHeader(primaryPrincipal, version);
        cos.close();
        load(name);
    }

    synchronized void load(String name) throws IOException, KrbException {
        PrincipalName p;
	CCacheInputStream cis =
	    new CCacheInputStream(new FileInputStream(name));
        version = cis.readVersion();
        if (version == KRB5_FCC_FVNO_4) {
	    tag = cis.readTag();
        } else {
            tag = null;
            if (version == KRB5_FCC_FVNO_1 || version == KRB5_FCC_FVNO_2) {
                cis.setNativeByteOrder();
            }
        }
        p = cis.readPrincipal(version);

        if (primaryPrincipal != null) {
            if (!(primaryPrincipal.match(p))) {
                throw new IOException("Primary principals don't match.");
            }
        } else
	    primaryPrincipal = p;
        primaryRealm = primaryPrincipal.getRealm();
        credentialsList = new Vector<Credentials> ();
	while (cis.available() > 0) {
	    credentialsList.addElement(cis.readCred(version));
	}
        cis.close();
    }


    /**
     * Updates the credentials list. If the specified credentials for the
     * service is new, add it to the list. If there is an entry in the list, 
     * replace the old credentials with the new one.
     * @param c the credentials. 
     */

    public synchronized void update(Credentials c) {
        if (credentialsList != null) {
	    if (credentialsList.isEmpty()) {
		credentialsList.addElement(c);
	    } else {
		Credentials tmp = null;
		boolean matched = false;
	    
		for (int i = 0; i < credentialsList.size(); i++) {
		    tmp = credentialsList.elementAt(i);
		    if (match(c.sname.getNameStrings(), 
			      tmp.sname.getNameStrings()) && 
			((c.sname.getRealmString()).equalsIgnoreCase(
				     tmp.sname.getRealmString()))) {
			matched = true;
			if (c.endtime.getTime() >= tmp.endtime.getTime()) {
			    if (DEBUG) {
				System.out.println(" >>> FileCredentialsCache "
					 +  "Ticket matched, overwrite "
					 +  "the old one.");
			    }
			    credentialsList.removeElementAt(i);
			    credentialsList.addElement(c);
			}
		    }
		}
		if (matched == false) {
		    if (DEBUG) {
			System.out.println(" >>> FileCredentialsCache Ticket " 
					+   "not exactly matched, "
					+   "add new one into cache.");
		    }
		
		    credentialsList.addElement(c);
		}
	    }
	}
    }
    
    public synchronized PrincipalName getPrimaryPrincipal() {
        return primaryPrincipal;
    }
    
    
    /** 
     * Saves the credentials cache file to the disk.
     */
    public synchronized void save() throws IOException, Asn1Exception {
        CCacheOutputStream cos 
	    = new CCacheOutputStream(new FileOutputStream(cacheName));
	cos.writeHeader(primaryPrincipal, version);
	Credentials[] tmp = null;
	if ((tmp = getCredsList()) != null) {
	    for (int i = 0; i < tmp.length; i++) {
		cos.addCreds(tmp[i]);
	    }
	}
	cos.close();
    }
    
    boolean match(String[] s1, String[] s2) {
	if (s1.length != s2.length) {
	    return false;
	} else {
	    for (int i = 0; i < s1.length; i++) {
		if (!(s1[i].equalsIgnoreCase(s2[i]))) {
		    return false;
		}
	    }
	}
	return true;
    }	 
    
    /**
     * Returns the list of credentials entries in the cache file.
     */
    public synchronized Credentials[] getCredsList() {
	if ((credentialsList == null) || (credentialsList.isEmpty())) {
	    return null;
	} else {
	    Credentials[] tmp = new Credentials[credentialsList.size()];
	    for (int i = 0; i < credentialsList.size(); i++) {
		tmp[i] = credentialsList.elementAt(i);
	    }
	    return tmp;
	}
	
    }
    
    public Credentials getCreds(LoginOptions options,
				PrincipalName sname, Realm srealm) {
        if (options == null) {
            return getCreds(sname, srealm);
        } else {
	    Credentials[] list = getCredsList();
	    if (list == null) {			
		return null;
	    } else {
		for (int i = 0; i < list.length; i++) {
		    if (sname.match(list[i].sname) &&
			(srealm.toString().equals(list[i].srealm.toString()))) {
                        if (list[i].flags.match(options)) {
			    return list[i];
                        }
		    }
		}
	    }
	    return null;
        }
    }
    
    
    /**
     * Gets a credentials for a specified service.
     * @param sname service principal name.
     * @param srealm the realm that the service belongs to.
     */
    public Credentials getCreds(PrincipalName sname, Realm srealm) {
	Credentials[] list = getCredsList();
	if (list == null) {			
	    return null;
	} else {
	    for (int i = 0; i < list.length; i++) {
		if (sname.match(list[i].sname) &&
		    (srealm.toString().equals(list[i].srealm.toString()))) {
		    return list[i];
		}
	    }
	}
	return null;
    }
    
    public Credentials getDefaultCreds() {
        Credentials[] list = getCredsList();
        if (list == null) {
            return null;
        } else {
            for (int i = list.length-1; i >= 0; i--) {
                if (list[i].sname.toString().startsWith("krbtgt")) {
		    String[] nameStrings = list[i].sname.getNameStrings();
		    // find the TGT for the current realm krbtgt/realm@realm
		    if (nameStrings[1].equals(list[i].srealm.toString())) {
                       return list[i];
		    }
                }
            }
        }
        return null;
    }

    /*
     * Returns path name of the credentials cache file.
     * The path name is searched in the following order:
     * 
     * 1. /tmp/krb5cc_<uid> on unix systems
     * 2. <user.home>/krb5cc_<user.name>
     * 3. <user.home>/krb5cc (if can't get <user.name>)
     */

    public static String getDefaultCacheName() {
        String stdCacheNameComponent = "krb5cc";
        String name;
        // get cache name from system.property
	
	String osname = 
	    java.security.AccessController.doPrivileged(
			new sun.security.action.GetPropertyAction("os.name"));
	
	/*
	 * For Unix platforms we use the default cache name to be 
	 * /tmp/krbcc_uid ; for all other platforms  we use
	 * {user_home}/krb5_cc{user_name}
	 * Please note that for Windows 2K we will use LSA to get 
	 * the TGT from the the default cache even before we come here;
	 * however when we create cache we will create a cache under
	 * {user_home}/krb5_cc{user_name} for non-Unix platforms including
	 * Windows 2K.
	 */
	
	if (osname != null) { 
	    String cmd = null;
	    String uidStr = null;
	    long uid = 0;
	    
	    if (osname.startsWith("SunOS") ||
		(osname.startsWith("Linux"))) {
		try {
		    Class<?> c = Class.forName
			("com.sun.security.auth.module.UnixSystem");
		    Constructor<?> constructor = c.getConstructor();
		    Object obj = constructor.newInstance();
		    Method method = c.getMethod("getUid");
		    uid =  ((Long)method.invoke(obj)).longValue();
		    name = File.separator + "tmp" +
			File.separator + stdCacheNameComponent + "_" + uid;
		    if (DEBUG) {
	    		System.out.println(">>>KinitOptions cache name is " + 
					   name);
		    }
		    return name;
		} catch (Exception e) {
		    if (DEBUG) {
			System.out.println("Exception in obtaining uid " +
					    "for Unix platforms " +
					    "Using user's home directory");
			
			
			e.printStackTrace();
		    }
		}
	    }
	}
	
	// we did not get the uid;
	
	
	String user_name = 
	    java.security.AccessController.doPrivileged(
			new sun.security.action.GetPropertyAction("user.name"));
	
	String user_home =
	    java.security.AccessController.doPrivileged(
			new sun.security.action.GetPropertyAction("user.home"));
	
	if (user_home == null) {
	    user_home = 
		java.security.AccessController.doPrivileged(
			new sun.security.action.GetPropertyAction("user.dir"));
	}
	
	if (user_name != null) {
	    name = user_home + File.separator  +
		stdCacheNameComponent + "_" + user_name;
	} else {  
	    name = user_home + File.separator + stdCacheNameComponent; 
	}
	
	if (DEBUG) {
	    System.out.println(">>>KinitOptions cache name is " + name);
	}
    
	return name;
    }

    public static String checkValidation(String name) {
        String fullname = null;
        if (name == null) {
            return null;
        }
        try {
	    // get full path name
            fullname = (new File(name)).getCanonicalPath();
	    File fCheck = new File(fullname);
	    if (!(fCheck.exists())) {
		// get absolute directory
		File temp = new File(fCheck.getParent());
		// test if the directory exists
                if (!(temp.isDirectory()))
                    fullname = null;
		temp = null;
	    }
	    fCheck = null;
	    
        } catch (IOException e) {
            fullname = null; // invalid name
        }
        return fullname;
    }
    
  
    private static String exec(String c) {
	StringTokenizer st = new StringTokenizer(c);
	Vector<String> v = new Vector<String> ();
	while (st.hasMoreTokens()) {
	    v.addElement(st.nextToken());
	}
	final String[] command = new String[v.size()];
	v.copyInto(command);
	try {
	    
	    Process p =
		java.security.AccessController.doPrivileged
		(new java.security.PrivilegedAction<Process> () {
			public Process run() {
			    try {
				return (Runtime.getRuntime().exec(command));
			    } catch (java.io.IOException e) {
	    			if (DEBUG) {
				    e.printStackTrace();
	    			}
				return null;
			    }
			}
		    });
	    if (p == null) {
		// exception occured in execing the command
		return null;
	    }

	    BufferedReader commandResult = 
		new BufferedReader
		    (new InputStreamReader(p.getInputStream(), "8859_1"));
	    String s1 = null;
	    if ((command.length == 1) &&
		(command[0].equals("/usr/bin/env"))) {
		while ((s1 = commandResult.readLine()) != null) {
		    if (s1.length() >= 11) {
			if ((s1.substring(0, 11)).equalsIgnoreCase
			    ("KRB5CCNAME=")) {
			    s1 = s1.substring(11);
			    break;
			}
		    }
		}
	    } else     s1 = commandResult.readLine();
	    commandResult.close();
	    return s1;
	} catch (Exception e) {
	    if (DEBUG) {
		e.printStackTrace();
	    }
	}
	return null;
    }    
}
