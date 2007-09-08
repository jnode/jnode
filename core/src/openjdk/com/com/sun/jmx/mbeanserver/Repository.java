/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.jmx.mbeanserver;

import com.sun.jmx.defaults.ServiceName;
import static com.sun.jmx.defaults.JmxProperties.MBEANSERVER_LOGGER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.Map;
import java.util.Set;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.RuntimeOperationsException;

/**
 * The RepositorySupport implements the Repository interface.
 * This repository does not support persistency.
 *
 * @since 1.5
 */
public class Repository {

    // Private fields -------------------------------------------->

    /**
     * The structure for storing the objects is very basic.
     * A Hashtable is used for storing the different domains
     * For each domain, a hashtable contains the instances with
     * canonical key property list string as key and named object
     * aggregated from given object name and mbean instance as value.
     */
    private final Map<String,Map<String,NamedObject>> domainTb;

    /**
     * Number of elements contained in the Repository
     */
    private volatile int nbElements = 0;

    /**
     * Domain name of the server the repository is attached to.
     * It is quicker to store the information in the repository rather
     * than querying the framework each time the info is required.
     */
    private final String domain;
    
    /**
     * We use a global reentrant read write lock to protect the repository.
     * This seems safer and more efficient: we are using Maps of Maps,
     * Guaranteing consistency while using Concurent objects at each level 
     * may be more difficult.
     **/
    private final ReentrantReadWriteLock lock;

    // Private fields <=============================================

    // Private methods --------------------------------------------->

    /* This class is used to match an ObjectName against a pattern. */
    private final static class ObjectNamePattern {
        private final char[]   domain;
        private final String[] keys;
        private final String[] values;
        private final String   properties;
        private final boolean  isPropertyListPattern;
        private final boolean  isPropertyValuePattern;

        /**
         * The ObjectName pattern against which ObjectNames are matched.
         **/
        public final ObjectName pattern;

        /**
         * Builds a new ObjectNamePattern object from an ObjectName pattern.
         * @param pattern The ObjectName pattern under examination.
         **/
        public ObjectNamePattern(ObjectName pattern) {
            this(pattern.getDomain(),
                 pattern.isPropertyListPattern(),
                 pattern.isPropertyValuePattern(),
                 pattern.getCanonicalKeyPropertyListString(),
                 pattern.getKeyPropertyList(),
                 pattern);
        }

        /**
         * Builds a new ObjectNamePattern object from an ObjectName pattern
         * constituents.
         * @param domain pattern.getDomain().
         * @param propertyListPattern pattern.isPropertyListPattern().
         * @param propertyValuePattern pattern.isPropertyValuePattern().
         * @param canonicalProps pattern.getCanonicalKeyPropertyListString().
         * @param keyPropertyList pattern.getKeyPropertyList().
         * @param pattern The ObjectName pattern under examination.
         **/
        ObjectNamePattern(String domain,
                          boolean propertyListPattern,
                          boolean propertyValuePattern,
                          String canonicalProps,
                          Map<String,String> keyPropertyList,
                          ObjectName pattern) {
            this.domain = domain.toCharArray();
            this.isPropertyListPattern = propertyListPattern;
            this.isPropertyValuePattern = propertyValuePattern;
            this.properties = canonicalProps;
            final int len = keyPropertyList.size();
            this.keys   = new String[len];
            this.values = new String[len];
            int i = 0;
            for (Map.Entry<String,String> entry : keyPropertyList.entrySet()) {
                keys[i]   = entry.getKey();
                values[i] = entry.getValue();
                i++;
            }
            this.pattern = pattern;
        }

        /**
         * Return true if the given ObjectName matches the ObjectName pattern
         * for which this object has been built.
         * WARNING: domain name is not considered here because it is supposed
         *          not to be wildcard when called. PropertyList is also
         *          supposed not to be zero-length.
         * @param name The ObjectName we want to match against the pattern.
         * @return true if <code>name</code> matches the pattern.
         **/
        public boolean matchKeys(ObjectName name) {
            // If key property value pattern but not key property list
            // pattern, then the number of key properties must be equal
            //
            if (isPropertyValuePattern &&
                !isPropertyListPattern &&
                (name.getKeyPropertyList().size() != keys.length))
                return false;

            // If key property value pattern or key property list pattern,
            // then every property inside pattern should exist in name
            //
            if (isPropertyValuePattern || isPropertyListPattern) {
                for (int i = keys.length - 1; i >= 0 ; i--) {
                    // Find value in given object name for key at current
                    // index in receiver
                    //
                    String v = name.getKeyProperty(keys[i]);
                    // Did we find a value for this key ?
                    //
                    if (v == null) return false;
                    // If this property is ok (same key, same value), go to next
                    //
                    if (isPropertyValuePattern &&
                        pattern.isPropertyValuePattern(keys[i])) {
                        // wildmatch key property values
                        final char[] val_pattern = values[i].toCharArray();
                        final char[] val_string  = v.toCharArray();
                        if (wildmatch(val_string,val_pattern))
                            continue;
                        else
                            return false;
                    }
                    if (v.equals(values[i])) continue;
                    return false;
                }
                return true;
            }

            // If no pattern, then canonical names must be equal
            //
            final String p1 = name.getCanonicalKeyPropertyListString();
            final String p2 = properties;
            return (p1.equals(p2));
        }
    }

    /**
     * Add all the matching objects from the given hashtable in the
     * result set for the given ObjectNamePattern
     * Do not check whether the domains match (only check for matching
     * key property lists - see <i>matchKeys()</i>)
     **/
    private void addAllMatching(final Map<String,NamedObject> moiTb,
                                final Set<NamedObject> result,
                                final ObjectNamePattern pattern) {
        synchronized (moiTb) {
            for (NamedObject no : moiTb.values()) {
                final ObjectName on = no.getName();
                // if all couples (property, value) are contained
                if (pattern.matchKeys(on)) result.add(no);
            }
        }
    }

    private void addNewDomMoi(final DynamicMBean object, final String dom,
                              final ObjectName name) {
        final Map<String,NamedObject> moiTb =
            new HashMap<String,NamedObject>();
        moiTb.put(name.getCanonicalKeyPropertyListString(),
                  new NamedObject(name, object));
        domainTb.put(dom, moiTb);
        nbElements++;
    }

    /** Match a string against a shell-style pattern.  The only pattern
        characters recognised are <code>?</code>, standing for any one
        character, and <code>*</code>, standing for any string of
        characters, including the empty string.

        @param str the string to match, as a character array.
        @param pat the pattern to match the string against, as a
        character array.

        @return true if and only if the string matches the pattern.
    */
    /* The algorithm is a classical one.  We advance pointers in
       parallel through str and pat.  If we encounter a star in pat,
       we remember its position and continue advancing.  If at any
       stage we get a mismatch between str and pat, we look to see if
       there is a remembered star.  If not, we fail.  If so, we
       retreat pat to just past that star and str to the position
       after the last one we tried, and we let the match advance
       again.

       Even though there is only one remembered star position, the
       algorithm works when there are several stars in the pattern.
       When we encounter the second star, we forget the first one.
       This is OK, because if we get to the second star in A*B*C
       (where A etc are arbitrary strings), we have already seen AXB.
       We're therefore setting up a match of *C against the remainder
       of the string, which will match if that remainder looks like
       YC, so the whole string looks like AXBYC.
    */
    public static boolean wildmatch(char[] str, char[] pat) {
        int stri;     // index in str
        int pati;     // index in pat
        int starstri; // index for backtrack if "*" attempt fails
        int starpati; // index for backtrack if "*" attempt fails, +1
        final int strlen = str.length;
        final int patlen = pat.length;

        stri = pati = 0;
        starstri = starpati = -1;
        
        /* On each pass through this loop, we either advance pati,
           or we backtrack pati and advance starstri.  Since starstri
           is only ever assigned from pati, the loop must terminate.  */
        while (true) {
            if (pati < patlen) {
                final char patc = pat[pati];
                switch (patc) {
                case '?':
                    if (stri == strlen)
                        break;
                    stri++;
                    pati++;
                    continue;
                case '*':
                    pati++;
                    starpati = pati;
                    starstri = stri;
                    continue;
                default:
                    if (stri < strlen && str[stri] == patc) {
                        stri++;
                        pati++;
                        continue;
                    }
                    break;
                }
            } else if (stri == strlen)
                return true;

            // Mismatched, can we backtrack to a "*"?
            if (starpati < 0 || starstri == strlen)
                return false;

            // Retry the match one position later in str
            pati = starpati;
            starstri++;
            stri = starstri;
        }
    }

    /**
     * Retrieves the named object contained in repository
     * from the given objectname.
     */
    private NamedObject retrieveNamedObject(ObjectName name) {

        // No patterns inside reposit
        if (name.isPattern()) return null;

        // Extract the domain name.
        String dom= name.getDomain().intern();

        // Default domain case
        if (dom.length() == 0) {
            dom = domain;
        }

        Map<String,NamedObject> moiTb = domainTb.get(dom);
        if (moiTb == null) {
            return null; // No domain containing registered object names
        }

        return moiTb.get(name.getCanonicalKeyPropertyListString());
    }

    // Private methods <=============================================

    // Protected methods --------------------------------------------->

    // Protected methods <=============================================

    // Public methods --------------------------------------------->

    /**
     * Construct a new repository with the given default domain.
     */
    public Repository(String domain) {
        this(domain,true);
    }
    
    /**
     * Construct a new repository with the given default domain.
     */
    public Repository(String domain, boolean fairLock) {
        lock = new ReentrantReadWriteLock(fairLock);
        
        domainTb = new HashMap<String,Map<String,NamedObject>>(5);

        if (domain != null && domain.length() != 0)
            this.domain = domain;
        else
            this.domain = ServiceName.DOMAIN;

        // Creates an new hastable for the default domain
        domainTb.put(this.domain.intern(), new HashMap<String,NamedObject>());
    }

    /**
     * Returns the list of domains in which any MBean is currently
     * registered.
     *
     */
    public String[] getDomains() {
        
        lock.readLock().lock();
        final List<String> result;
        try {
            // Temporary list
            result = new ArrayList<String>(domainTb.size());
            for (Map.Entry<String,Map<String,NamedObject>> entry :
                     domainTb.entrySet()) {
                // Skip domains that are in the table but have no
                // MBean registered in them
                // in particular the default domain may be like this
                Map<String,NamedObject> t = entry.getValue();
                if (t != null && t.size() != 0)
                    result.add(entry.getKey());
            }
        } finally {
            lock.readLock().unlock();
        }

        // Make an array from result.
        return result.toArray(new String[result.size()]);
    }

    /**
     * Stores an MBean associated with its object name in the repository.
     *
     * @param object MBean to be stored in the repository.
     * @param name MBean object name.
     */
    public void addMBean(final DynamicMBean object, ObjectName name)
        throws InstanceAlreadyExistsException {

        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER, Repository.class.getName(),
                    "addMBean", "name = " + name);
        }

        // Extract the domain name.
        String dom = name.getDomain().intern();
        boolean to_default_domain = false;

        // Set domain to default if domain is empty and not already set
        if (dom.length() == 0) {
             try {
                name = new ObjectName(domain + name.toString());
            } catch (MalformedObjectNameException e) {
                if (MBEANSERVER_LOGGER.isLoggable(Level.FINEST)) {
                    MBEANSERVER_LOGGER.logp(Level.FINEST,
                            Repository.class.getName(), "addMBean",
                            "Unexpected MalformedObjectNameException", e);
                }
            }
        }

        // Do we have default domain ?
        if (dom == domain) {
            to_default_domain = true;
            dom = domain;
        } else {
            to_default_domain = false;
        }

        // Validate name for an object
        if (name.isPattern()) {
            throw new RuntimeOperationsException(
             new IllegalArgumentException("Repository: cannot add mbean for " +
                                          "pattern name " + name.toString()));
        }

        lock.writeLock().lock();
        try {
            // Domain cannot be JMImplementation if entry does not exists
            if ( !to_default_domain &&
                    dom.equals("JMImplementation") &&
                    domainTb.containsKey("JMImplementation")) {
                throw new RuntimeOperationsException(
                        new IllegalArgumentException(
                        "Repository: domain name cannot be JMImplementation"));
            }
            
            // If domain not already exists, add it to the hash table
            final Map<String,NamedObject> moiTb = domainTb.get(dom);
            if (moiTb == null) {
                addNewDomMoi(object, dom, name);
                return;
            } 
            
            // Add instance if not already present
            String cstr = name.getCanonicalKeyPropertyListString();
            NamedObject elmt= moiTb.get(cstr);
            if (elmt != null) {
                throw new InstanceAlreadyExistsException(name.toString());
            } else {
                nbElements++;
                moiTb.put(cstr, new NamedObject(name, object));
            }
            
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks whether an MBean of the name specified is already stored in
     * the repository.
     *
     * @param name name of the MBean to find.
     *
     * @return  true if the MBean is stored in the repository,
     *          false otherwise.
     */
    public boolean contains(ObjectName name) {
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER, Repository.class.getName(),
                    "contains", " name = " + name);
        }
        lock.readLock().lock();
        try {
            return (retrieveNamedObject(name) != null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves the MBean of the name specified from the repository. The
     * object name must match exactly.
     *
     * @param name name of the MBean to retrieve.
     *
     * @return  The retrieved MBean if it is contained in the repository,
     *          null otherwise.
     */
    public DynamicMBean retrieve(ObjectName name) {
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER, Repository.class.getName(),
                    "retrieve", "name = " + name);
        }

        // Calls internal retrieve method to get the named object
        lock.readLock().lock();
        try {
            NamedObject no = retrieveNamedObject(name);
            if (no == null) return null;
            else return no.getObject();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Selects and retrieves the list of MBeans whose names match the specified
     * object name pattern and which match the specified query expression
     * (optionally).
     *
     * @param pattern The name of the MBean(s) to retrieve - may be a specific
     * object or a name pattern allowing multiple MBeans to be selected.
     * @param query query expression to apply when selecting objects - this
     * parameter will be ignored when the Repository Service does not
     * support filtering.
     *
     * @return  The list of MBeans selected. There may be zero, one or many
     *          MBeans returned in the set.
     */
    public Set<NamedObject> query(ObjectName pattern, QueryExp query) {

        final Set<NamedObject> result = new HashSet<NamedObject>();

        // The following filter cases are considered:
        // null, "", "*:*" : names in all domains
        // ":*", ":[key=value],*" : names in defaultDomain
        // "domain:*", "domain:[key=value],*" : names in the specified domain

        // Surely one of the most frequent case ... query on the whole world
        ObjectName name = null;
        if (pattern == null ||
            pattern.getCanonicalName().length() == 0 ||
            pattern.equals(ObjectName.WILDCARD))
           name = ObjectName.WILDCARD;
        else name = pattern;

        lock.readLock().lock();
        try {
            
            // If pattern is not a pattern, retrieve this mbean !
            if (!name.isPattern()) {
                final NamedObject no;
                no = retrieveNamedObject(name);
                if (no != null) result.add(no);
                return result;
            }
            
            // All names in all domains
            if (name == ObjectName.WILDCARD) {
                for (Map<String,NamedObject> moiTb : domainTb.values()) {
                    result.addAll(moiTb.values());
                }
                return result;
            }
            
            final String canonical_key_property_list_string =
                    name.getCanonicalKeyPropertyListString();
            final boolean allNames = 
                    (canonical_key_property_list_string.length()==0);
            final ObjectNamePattern namePattern =
                (allNames?null:new ObjectNamePattern(name));
            
            // All names in default domain
            if (name.getDomain().length() == 0) {
                final Map<String,NamedObject> moiTb = domainTb.get(domain);
                if (allNames) 
                    result.addAll(moiTb.values());
                else 
                    addAllMatching(moiTb, result, namePattern);
                return result;
            }
            
            // Pattern matching in the domain name (*, ?)
            char[] dom2Match = name.getDomain().toCharArray();
            for (String domain : domainTb.keySet()) {
                char[] theDom = domain.toCharArray();
                if (wildmatch(theDom, dom2Match)) {
                    final Map<String,NamedObject> moiTb = domainTb.get(domain);
                    if (allNames)
                        result.addAll(moiTb.values());
                    else
                        addAllMatching(moiTb, result, namePattern);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Removes an MBean from the repository.
     *
     * @param name name of the MBean to remove.
     *
     * @exception InstanceNotFoundException The MBean does not exist in
     *            the repository.
     */
    public void remove(final ObjectName name)
        throws InstanceNotFoundException {

        // Debugging stuff
        if (MBEANSERVER_LOGGER.isLoggable(Level.FINER)) {
            MBEANSERVER_LOGGER.logp(Level.FINER, Repository.class.getName(),
                    "remove", "name = " + name);
        }

        // Extract domain name.
        String dom= name.getDomain().intern();

        // Default domain case
        if (dom.length() == 0) dom = domain;

        lock.writeLock().lock();
        try {
            // Find the domain subtable
            final Map<String,NamedObject> moiTb = domainTb.get(dom);
            if (moiTb == null) {
                throw new InstanceNotFoundException(name.toString());
            }

            // Remove the corresponding element
            if (moiTb.remove(name.getCanonicalKeyPropertyListString())==null) {
                throw new InstanceNotFoundException(name.toString());
            }

            // We removed it !
            nbElements--;

            // No more object for this domain, we remove this domain hashtable
            if (moiTb.isEmpty()) {
                domainTb.remove(dom);

                // set a new default domain table (always present)
                // need to reinstantiate a hashtable because of possible
                // big buckets array size inside table, never cleared,
                // thus the new !
                if (dom == domain)
                    domainTb.put(domain, new HashMap<String,NamedObject>());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the number of MBeans stored in the repository.
     *
     * @return  Number of MBeans.
     */
    public Integer getCount() {
        return new Integer(nbElements);
    }

    /**
     * Gets the name of the domain currently used by default in the
     * repository.
     *
     * @return  A string giving the name of the default domain name.
     */
    public  String getDefaultDomain() {
        return domain;
    }

    // Public methods <=============================================
}
