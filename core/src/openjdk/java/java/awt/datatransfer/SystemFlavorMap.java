/*
 * Copyright 1997-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.awt.datatransfer;

import java.awt.Toolkit;

import java.lang.ref.SoftReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import sun.awt.datatransfer.DataTransferer;


/**
 * The SystemFlavorMap is a configurable map between "natives" (Strings), which
 * correspond to platform-specific data formats, and "flavors" (DataFlavors),
 * which correspond to platform-independent MIME types. This mapping is used
 * by the data transfer subsystem to transfer data between Java and native
 * applications, and between Java applications in separate VMs.
 * <p>
 * In the Sun reference implementation, the default SystemFlavorMap is
 * initialized by the file <code>jre/lib/flavormap.properties</code> and the
 * contents of the URL referenced by the AWT property
 * <code>AWT.DnD.flavorMapFileURL</code>. See <code>flavormap.properties</code>
 * for details.
 *
 * @since 1.2
 */
public final class SystemFlavorMap implements FlavorMap, FlavorTable {

    /**
     * Constant prefix used to tag Java types converted to native platform
     * type.
     */
    private static String JavaMIME = "JAVA_DATAFLAVOR:";

    /**
     * System singleton which maps a thread's ClassLoader to a SystemFlavorMap.
     */
    private static final WeakHashMap flavorMaps = new WeakHashMap();

    /**
     * Copied from java.util.Properties.
     */
    private static final String keyValueSeparators = "=: \t\r\n\f";
    private static final String strictKeyValueSeparators = "=:";
    private static final String whiteSpaceChars = " \t\r\n\f";

    /**
     * The list of valid, decoded text flavor representation classes, in order
     * from best to worst.
     */
    private static final String[] UNICODE_TEXT_CLASSES = {
        "java.io.Reader", "java.lang.String", "java.nio.CharBuffer", "\"[C\""
    };

    /**
     * The list of valid, encoded text flavor representation classes, in order
     * from best to worst.
     */
    private static final String[] ENCODED_TEXT_CLASSES = {
        "java.io.InputStream", "java.nio.ByteBuffer", "\"[B\""
    };

    /**
     * A String representing text/plain MIME type.
     */
    private static final String TEXT_PLAIN_BASE_TYPE = "text/plain";

    /**
     * This constant is passed to flavorToNativeLookup() to indicate that a
     * a native should be synthesized, stored, and returned by encoding the
     * DataFlavor's MIME type in case if the DataFlavor is not found in
     * 'flavorToNative' map.
     */
    private static final boolean SYNTHESIZE_IF_NOT_FOUND = true;

    /**
     * Maps native Strings to Lists of DataFlavors (or base type Strings for
     * text DataFlavors).
     */
    private Map nativeToFlavor = new HashMap();

    /**
     * Maps DataFlavors (or base type Strings for text DataFlavors) to Lists of
     * native Strings.
     */
    private Map flavorToNative = new HashMap();

    /**
     * Caches the result of getNativesForFlavor(). Maps DataFlavors to
     * SoftReferences which reference Lists of String natives.
     */
    private Map getNativesForFlavorCache = new HashMap();

    /**
     * Caches the result getFlavorsForNative(). Maps String natives to
     * SoftReferences which reference Lists of DataFlavors.
     */
    private Map getFlavorsForNativeCache = new HashMap();

    /**
     * Dynamic mapping generation used for text mappings should not be applied
     * to the DataFlavors and String natives for which the mappings have been
     * explicitly specified with setFlavorsForNative() or
     * setNativesForFlavor(). This keeps all such keys.
     */
    private Set disabledMappingGenerationKeys = new HashSet();

    /**
     * Returns the default FlavorMap for this thread's ClassLoader.
     */
    public static FlavorMap getDefaultFlavorMap() {
        ClassLoader contextClassLoader =
            Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            contextClassLoader = ClassLoader.getSystemClassLoader();
        }

        FlavorMap fm;

        synchronized(flavorMaps) {
            fm = (FlavorMap)flavorMaps.get(contextClassLoader);
            if (fm == null) {
                fm = new SystemFlavorMap();
                flavorMaps.put(contextClassLoader, fm);
            }
        }

        return fm;
    }

    /**
     * Constructs a SystemFlavorMap by reading flavormap.properties and
     * AWT.DnD.flavorMapFileURL.
     */
    private SystemFlavorMap() {
        BufferedReader flavormapDotProperties = (BufferedReader)
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        String fileName =
                            System.getProperty("java.home") +
                            File.separator +
                            "lib" +
                            File.separator +
                            "flavormap.properties";
                        try {
                            return new BufferedReader
                                (new InputStreamReader
                                    (new File(fileName).toURI().toURL().openStream(), "ISO-8859-1"));
                        } catch (MalformedURLException e) {
                            System.err.println("MalformedURLException:" + e + " while loading default flavormap.properties file:" + fileName);
                        } catch (IOException e) {
                            System.err.println("IOException:" + e + " while loading default flavormap.properties file:" + fileName);
                        }
                        return null;
                    }
                });

        BufferedReader flavormapURL = (BufferedReader)
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        String url = Toolkit.getDefaultToolkit().getProperty
                            ("AWT.DnD.flavorMapFileURL", null);

                        if (url == null) {
                            return null;
                        }

                        try {
                            return new BufferedReader
                                (new InputStreamReader
                                    (new URL(url).openStream(), "ISO-8859-1"));
                        } catch (MalformedURLException e) {
                            System.err.println("MalformedURLException:" + e + " while reading AWT.DnD.flavorMapFileURL:" + url);
                        } catch (IOException e) {
                            System.err.println("IOException:" + e + " while reading AWT.DnD.flavorMapFileURL:" + url);
                        }
                        return null;
                    }
                });

        if (flavormapDotProperties != null) {
            try {
                parseAndStoreReader(flavormapDotProperties);
            } catch (IOException e) {
                System.err.println("IOException:" + e + " while parsing default flavormap.properties file");
            }
        }

        if (flavormapURL != null) {
            try {
                parseAndStoreReader(flavormapURL);
            } catch (IOException e) {
                System.err.println("IOException:" + e + " while parsing AWT.DnD.flavorMapFileURL");
            }
        }
    }

    /**
     * Copied code from java.util.Properties. Parsing the data ourselves is the
     * only way to handle duplicate keys and values.
     */
    private void parseAndStoreReader(BufferedReader in) throws IOException {
        while (true) {
            // Get next line
            String line = in.readLine();
            if (line == null) {
                return;
            }

            if (line.length() > 0) {
                // Continue lines that end in slashes if they are not comments
                char firstChar = line.charAt(0);
                if (firstChar != '#' && firstChar != '!') {
                    while (continueLine(line)) {
                        String nextLine = in.readLine();
                        if (nextLine == null) {
                            nextLine = new String("");
                        }
                        String loppedLine =
                            line.substring(0, line.length() - 1);
                        // Advance beyond whitespace on new line
                        int startIndex = 0;
                        for(; startIndex < nextLine.length(); startIndex++) {
                            if (whiteSpaceChars.
                                    indexOf(nextLine.charAt(startIndex)) == -1)
                            {
                                break;
                            }
                        }
                        nextLine = nextLine.substring(startIndex,
                                                      nextLine.length());
                        line = new String(loppedLine+nextLine);
                    }

                    // Find start of key
                    int len = line.length();
                    int keyStart = 0;
                    for(; keyStart < len; keyStart++) {
                        if(whiteSpaceChars.
                               indexOf(line.charAt(keyStart)) == -1) {
                            break;
                        }
                    }

                    // Blank lines are ignored
                    if (keyStart == len) {
                        continue;
                    }

                    // Find separation between key and value
                    int separatorIndex = keyStart;
                    for(; separatorIndex < len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\') {
                            separatorIndex++;
                        } else if (keyValueSeparators.
                                       indexOf(currentChar) != -1) {
                            break;
                        }
                    }

                    // Skip over whitespace after key if any
                    int valueIndex = separatorIndex;
                    for (; valueIndex < len; valueIndex++) {
                        if (whiteSpaceChars.
                                indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                    }

                    // Skip over one non whitespace key value separators if any
                    if (valueIndex < len) {
                        if (strictKeyValueSeparators.
                                indexOf(line.charAt(valueIndex)) != -1) {
                            valueIndex++;
                        }
                    }

                    // Skip over white space after other separators if any
                    while (valueIndex < len) {
                        if (whiteSpaceChars.
                                indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                        valueIndex++;
                    }

                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len)
                        ? line.substring(valueIndex, len)
                        : "";

                    // Convert then store key and value
                    key = loadConvert(key);
                    value = loadConvert(value);

                    try {
                        MimeType mime = new MimeType(value);
                        if ("text".equals(mime.getPrimaryType())) {
                            String charset = mime.getParameter("charset");
                            if (DataTransferer.doesSubtypeSupportCharset
                                    (mime.getSubType(), charset))
                            {
                                // We need to store the charset and eoln
                                // parameters, if any, so that the
                                // DataTransferer will have this information
                                // for conversion into the native format.
                                DataTransferer transferer =
                                    DataTransferer.getInstance();
                                if (transferer != null) {
                                    transferer.registerTextFlavorProperties
                                        (key, charset,
                                         mime.getParameter("eoln"),
                                         mime.getParameter("terminators"));
                                }
                            }

                            // But don't store any of these parameters in the
                            // DataFlavor itself for any text natives (even
                            // non-charset ones). The SystemFlavorMap will
                            // synthesize the appropriate mappings later.
                            mime.removeParameter("charset");
                            mime.removeParameter("class");
                            mime.removeParameter("eoln");
                            mime.removeParameter("terminators");
                            value = mime.toString();
                        }
                    } catch (MimeTypeParseException e) {
                        e.printStackTrace();
                        continue;
                    }

                    DataFlavor flavor;
                    try {
                        flavor = new DataFlavor(value);
                    } catch (Exception e) {
                        try {
                            flavor = new DataFlavor(value, (String)null);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                            continue;
                        }
                    }

                    // For text/* flavors, store mappings in separate maps to
                    // enable dynamic mapping generation at a run-time.
                    if ("text".equals(flavor.getPrimaryType())) {
                        store(value, key, flavorToNative);
                        store(key, value, nativeToFlavor);
                    } else {
                        store(flavor, key, flavorToNative);
                        store(key, flavor, nativeToFlavor);
                    }
                }
            }
        }
    }

    /**
     * Copied from java.util.Properties.
     */
    private boolean continueLine (String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while((index >= 0) && (line.charAt(index--) == '\\')) {
            slashCount++;
        }
        return (slashCount % 2 == 1);
    }

    /**
     * Copied from java.util.Properties.
     */
    private String loadConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);

        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                          case '0': case '1': case '2': case '3': case '4':
                          case '5': case '6': case '7': case '8': case '9': {
                             value = (value << 4) + aChar - '0';
                             break;
                          }
                          case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f': {
                             value = (value << 4) + 10 + aChar - 'a';
                             break;
                          }
                          case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F': {
                             value = (value << 4) + 10 + aChar - 'A';
                             break;
                          }
                          default: {
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                          }
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /**
     * Stores the listed object under the specified hash key in map. Unlike a
     * standard map, the listed object will not replace any object already at
     * the appropriate Map location, but rather will be appended to a List
     * stored in that location.
     */
    private void store(Object hashed, Object listed, Map map) {
        List list = (List)map.get(hashed);
        if (list == null) {
            list = new ArrayList(1);
            map.put(hashed, list);
        }
        if (!list.contains(listed)) {
            list.add(listed);
        }
    }

    /**
     * Semantically equivalent to 'nativeToFlavor.get(nat)'. This method
     * handles the case where 'nat' is not found in 'nativeToFlavor'. In that
     * case, a new DataFlavor is synthesized, stored, and returned, if and
     * only if the specified native is encoded as a Java MIME type.
     */
    private List nativeToFlavorLookup(String nat) {
        List flavors = (List)nativeToFlavor.get(nat);

        if (nat != null && !disabledMappingGenerationKeys.contains(nat)) {
            DataTransferer transferer = DataTransferer.getInstance();
            if (transferer != null) {
                List platformFlavors =
                    transferer.getPlatformMappingsForNative(nat);
                if (!platformFlavors.isEmpty()) {
                    if (flavors != null) {
                        platformFlavors.removeAll(new HashSet(flavors));
                        // Prepending the platform-specific mappings ensures
                        // that the flavors added with
                        // addFlavorForUnencodedNative() are at the end of
                        // list.
                        platformFlavors.addAll(flavors);
                    }
                    flavors = platformFlavors;
                }
            }
        }

        if (flavors == null && isJavaMIMEType(nat)) {
            String decoded = decodeJavaMIMEType(nat);
            DataFlavor flavor = null;

            try {
                flavor = new DataFlavor(decoded);
            } catch (Exception e) {
                System.err.println("Exception \"" + e.getClass().getName() +
                                   ": " + e.getMessage()  +
                                   "\"while constructing DataFlavor for: " +
                                   decoded);
            }

            if (flavor != null) {
                flavors = new ArrayList(1);
                nativeToFlavor.put(nat, flavors);
                flavors.add(flavor);
                getFlavorsForNativeCache.remove(nat);
                getFlavorsForNativeCache.remove(null);

                List natives = (List)flavorToNative.get(flavor);
                if (natives == null) {
                    natives = new ArrayList(1);
                    flavorToNative.put(flavor, natives);
                }
                natives.add(nat);
                getNativesForFlavorCache.remove(flavor);
                getNativesForFlavorCache.remove(null);
            }
        }

        return (flavors != null) ? flavors : new ArrayList(0);
    }

    /**
     * Semantically equivalent to 'flavorToNative.get(flav)'. This method
     * handles the case where 'flav' is not found in 'flavorToNative' depending
     * on the value of passes 'synthesize' parameter. If 'synthesize' is
     * SYNTHESIZE_IF_NOT_FOUND a native is synthesized, stored, and returned by
     * encoding the DataFlavor's MIME type. Otherwise an empty List is returned
     * and 'flavorToNative' remains unaffected.
     */
    private List flavorToNativeLookup(final DataFlavor flav, 
                                      final boolean synthesize) {
        List natives = (List)flavorToNative.get(flav);

        if (flav != null && !disabledMappingGenerationKeys.contains(flav)) {
            DataTransferer transferer = DataTransferer.getInstance();
            if (transferer != null) {
                List platformNatives =
                    transferer.getPlatformMappingsForFlavor(flav);
                if (!platformNatives.isEmpty()) {
                    if (natives != null) {
                        platformNatives.removeAll(new HashSet(natives));
                        // Prepend the platform-specific mappings to ensure
                        // that the natives added with
                        // addUnencodedNativeForFlavor() are at the end of
                        // list. 
                        platformNatives.addAll(natives);
                    }
                    natives = platformNatives;
                }
            }
        }

        if (natives == null) {
            if (synthesize) {
                String encoded = encodeDataFlavor(flav);
                natives = new ArrayList(1);
                flavorToNative.put(flav, natives);
                natives.add(encoded);
                getNativesForFlavorCache.remove(flav);
                getNativesForFlavorCache.remove(null);

                List flavors = (List)nativeToFlavor.get(encoded);
                if (flavors == null) {
                    flavors = new ArrayList(1);
                    nativeToFlavor.put(encoded, flavors);
                }
                flavors.add(flav);
                getFlavorsForNativeCache.remove(encoded);
                getFlavorsForNativeCache.remove(null);
            } else {
                natives = new ArrayList(0);
            }
        }

        return natives;
    }

    /**
     * Returns a <code>List</code> of <code>String</code> natives to which the
     * specified <code>DataFlavor</code> can be translated by the data transfer
     * subsystem. The <code>List</code> will be sorted from best native to
     * worst. That is, the first native will best reflect data in the specified
     * flavor to the underlying native platform.
     * <p>
     * If the specified <code>DataFlavor</code> is previously unknown to the
     * data transfer subsystem and the data transfer subsystem is unable to
     * translate this <code>DataFlavor</code> to any existing native, then
     * invoking this method will establish a 
     * mapping in both directions between the specified <code>DataFlavor</code>
     * and an encoded version of its MIME type as its native.
     *
     * @param flav the <code>DataFlavor</code> whose corresponding natives
     *        should be returned. If <code>null</code> is specified, all
     *        natives currently known to the data transfer subsystem are
     *        returned in a non-deterministic order.
     * @return a <code>java.util.List</code> of <code>java.lang.String</code>
     *         objects which are platform-specific representations of platform-
     *         specific data formats
     *
     * @see #encodeDataFlavor
     * @since 1.4
     */
    public synchronized List<String> getNativesForFlavor(DataFlavor flav) {
        List retval = null;

        // Check cache, even for null flav
        SoftReference ref = (SoftReference)getNativesForFlavorCache.get(flav);
        if (ref != null) {
            retval = (List)ref.get();
            if (retval != null) {
                // Create a copy, because client code can modify the returned
                // list.
                return new ArrayList(retval);
            }
        }

        if (flav == null) {
            retval = new ArrayList(nativeToFlavor.keySet());
        } else if (disabledMappingGenerationKeys.contains(flav)) {
            // In this case we shouldn't synthesize a native for this flavor,
            // since its mappings were explicitly specified.
            retval = flavorToNativeLookup(flav, !SYNTHESIZE_IF_NOT_FOUND);
        } else if (DataTransferer.isFlavorCharsetTextType(flav)) {

            // For text/* flavors, flavor-to-native mappings specified in 
            // flavormap.properties are stored per flavor's base type.
            if ("text".equals(flav.getPrimaryType())) {
                retval = (List)flavorToNative.get(flav.mimeType.getBaseType());
                if (retval != null) {
                    // To prevent the List stored in the map from modification.
                    retval = new ArrayList(retval);
                }
            }

            // Also include text/plain natives, but don't duplicate Strings
            List textPlainList = (List)flavorToNative.get(TEXT_PLAIN_BASE_TYPE);

            if (textPlainList != null && !textPlainList.isEmpty()) {
                // To prevent the List stored in the map from modification.
                // This also guarantees that removeAll() is supported.
                textPlainList = new ArrayList(textPlainList);
                if (retval != null && !retval.isEmpty()) {
                    // Use HashSet to get constant-time performance for search.
                    textPlainList.removeAll(new HashSet(retval));
                    retval.addAll(textPlainList);
                } else {
                    retval = textPlainList;
                }
            }

            if (retval == null || retval.isEmpty()) {
                retval = flavorToNativeLookup(flav, SYNTHESIZE_IF_NOT_FOUND);
            } else {
                // In this branch it is guaranteed that natives explicitly
                // listed for flav's MIME type were added with
                // addUnencodedNativeForFlavor(), so they have lower priority.
                List explicitList = 
                    flavorToNativeLookup(flav, !SYNTHESIZE_IF_NOT_FOUND);  

                // flavorToNativeLookup() never returns null.
                // It can return an empty List, however.
                if (!explicitList.isEmpty()) {
                    // To prevent the List stored in the map from modification.
                    // This also guarantees that removeAll() is supported.
                    explicitList = new ArrayList(explicitList);
                    // Use HashSet to get constant-time performance for search.
                    explicitList.removeAll(new HashSet(retval));
                    retval.addAll(explicitList);
                }
            }
        } else if (DataTransferer.isFlavorNoncharsetTextType(flav)) {
            retval = (List)flavorToNative.get(flav.mimeType.getBaseType());

            if (retval == null || retval.isEmpty()) {
                retval = flavorToNativeLookup(flav, SYNTHESIZE_IF_NOT_FOUND);
            } else {
                // In this branch it is guaranteed that natives explicitly
                // listed for flav's MIME type were added with
                // addUnencodedNativeForFlavor(), so they have lower priority.
                List explicitList = 
                    flavorToNativeLookup(flav, !SYNTHESIZE_IF_NOT_FOUND);

                // flavorToNativeLookup() never returns null.
                // It can return an empty List, however.
                if (!explicitList.isEmpty()) {
                    // To prevent the List stored in the map from modification.
                    // This also guarantees that add/removeAll() are supported.
                    retval = new ArrayList(retval);
                    explicitList = new ArrayList(explicitList);
                    // Use HashSet to get constant-time performance for search.
                    explicitList.removeAll(new HashSet(retval));
                    retval.addAll(explicitList);
                }
            }
        } else {
            retval = flavorToNativeLookup(flav, SYNTHESIZE_IF_NOT_FOUND);
        }

        getNativesForFlavorCache.put(flav, new SoftReference(retval));
        // Create a copy, because client code can modify the returned list.
        return new ArrayList(retval);
    }

    /**
     * Returns a <code>List</code> of <code>DataFlavor</code>s to which the
     * specified <code>String</code> native can be translated by the data
     * transfer subsystem. The <code>List</code> will be sorted from best
     * <code>DataFlavor</code> to worst. That is, the first
     * <code>DataFlavor</code> will best reflect data in the specified
     * native to a Java application.
     * <p>
     * If the specified native is previously unknown to the data transfer
     * subsystem, and that native has been properly encoded, then invoking this
     * method will establish a mapping in both directions between the specified
     * native and a <code>DataFlavor</code> whose MIME type is a decoded
     * version of the native.
     * <p>
     * If the specified native is not a properly encoded native and the
     * mappings for this native have not been altered with
     * <code>setFlavorsForNative</code>, then the contents of the
     * <code>List</code> is platform dependent, but <code>null</code>
     * cannot be returned.
     *
     * @param nat the native whose corresponding <code>DataFlavor</code>s
     *        should be returned. If <code>null</code> is specified, all
     *        <code>DataFlavor</code>s currently known to the data transfer
     *        subsystem are returned in a non-deterministic order.
     * @return a <code>java.util.List</code> of <code>DataFlavor</code>
     *         objects into which platform-specific data in the specified,
     *         platform-specific native can be translated
     *
     * @see #encodeJavaMIMEType
     * @since 1.4
     */
    public synchronized List<DataFlavor> getFlavorsForNative(String nat) {

        // Check cache, even for null nat
        SoftReference ref = (SoftReference)getFlavorsForNativeCache.get(nat);
        if (ref != null) {
            ArrayList retval = (ArrayList)ref.get();
            if (retval != null) {
                return (List)retval.clone();
            }
        }

        LinkedList retval = new LinkedList();

        if (nat == null) {
            List natives = getNativesForFlavor(null);
            HashSet dups = new HashSet(natives.size());

            for (Iterator natives_iter = natives.iterator();
                 natives_iter.hasNext(); )
            {
                List flavors =
                    getFlavorsForNative((String)natives_iter.next());
                for (Iterator flavors_iter = flavors.iterator();
                     flavors_iter.hasNext(); )
                {
                    Object flavor = flavors_iter.next();
                    if (dups.add(flavor)) {
                        retval.add(flavor);
                    }
                }
            }
        } else {
            List flavors = nativeToFlavorLookup(nat);

            if (disabledMappingGenerationKeys.contains(nat)) {
                return flavors;
            }

            HashSet dups = new HashSet(flavors.size());

            List flavorsAndbaseTypes = nativeToFlavorLookup(nat);

            for (Iterator flavorsAndbaseTypes_iter =
                     flavorsAndbaseTypes.iterator(); 
                 flavorsAndbaseTypes_iter.hasNext(); )
            {
                Object value = flavorsAndbaseTypes_iter.next();
                if (value instanceof String) {
                    String baseType = (String)value;
                    String subType = null;
                    try {
                        MimeType mimeType = new MimeType(baseType);
                        subType = mimeType.getSubType();
                    } catch (MimeTypeParseException mtpe) {
                        // Cannot happen, since we checked all mappings
                        // on load from flavormap.properties.
                        assert(false);
                    }
                    if (DataTransferer.doesSubtypeSupportCharset(subType, 
                                                                 null)) {
                        if (TEXT_PLAIN_BASE_TYPE.equals(baseType) &&
                            dups.add(DataFlavor.stringFlavor))
                        {
                            retval.add(DataFlavor.stringFlavor);
                        }

                        for (int i = 0; i < UNICODE_TEXT_CLASSES.length; i++) {
                            DataFlavor toAdd = null;
                            try {
                                toAdd = new DataFlavor
                                    (baseType + ";charset=Unicode;class=" +
                                     UNICODE_TEXT_CLASSES[i]);
                            } catch (ClassNotFoundException cannotHappen) {
                            }
                            if (dups.add(toAdd)) {
                                retval.add(toAdd);
                            }
                        }

                        for (Iterator charset_iter =
                                 DataTransferer.standardEncodings();
                             charset_iter.hasNext(); )
                        {
                            String charset = (String)charset_iter.next();

                            for (int i = 0; i < ENCODED_TEXT_CLASSES.length;
                                 i++)
                            {
                                DataFlavor toAdd = null;
                                try {
                                    toAdd = new DataFlavor
                                        (baseType + ";charset=" + charset +
                                         ";class=" + ENCODED_TEXT_CLASSES[i]);
                                } catch (ClassNotFoundException cannotHappen) {
                                }

                                // Check for equality to plainTextFlavor so
                                // that we can ensure that the exact charset of
                                // plainTextFlavor, not the canonical charset
                                // or another equivalent charset with a
                                // different name, is used.
                                if (toAdd.equals(DataFlavor.plainTextFlavor)) {
                                    toAdd = DataFlavor.plainTextFlavor;
                                }

                                if (dups.add(toAdd)) {
                                    retval.add(toAdd);
                                }
                            }
                        }

                        if (TEXT_PLAIN_BASE_TYPE.equals(baseType) &&
                            dups.add(DataFlavor.plainTextFlavor))
                        {
                            retval.add(DataFlavor.plainTextFlavor);
                        }
                    } else {
                        // Non-charset text natives should be treated as
                        // opaque, 8-bit data in any of its various
                        // representations.
                        for (int i = 0; i < ENCODED_TEXT_CLASSES.length; i++) {
                            DataFlavor toAdd = null;
                            try {
                                toAdd = new DataFlavor(baseType +
                                     ";class=" + ENCODED_TEXT_CLASSES[i]);
                            } catch (ClassNotFoundException cannotHappen) {
                            }

                            if (dups.add(toAdd)) {
                                retval.add(toAdd);
                            }
                        }
                    }
                } else {
                    DataFlavor flavor = (DataFlavor)value;
                    if (dups.add(flavor)) {
                        retval.add(flavor);
                    }
                }
            }
        }

        ArrayList arrayList = new ArrayList(retval);
        getFlavorsForNativeCache.put(nat, new SoftReference(arrayList));
        return (List)arrayList.clone();
    }

    /**
     * Returns a <code>Map</code> of the specified <code>DataFlavor</code>s to
     * their most preferred <code>String</code> native. Each native value will
     * be the same as the first native in the List returned by
     * <code>getNativesForFlavor</code> for the specified flavor.
     * <p>
     * If a specified <code>DataFlavor</code> is previously unknown to the
     * data transfer subsystem, then invoking this method will establish a
     * mapping in both directions between the specified <code>DataFlavor</code>
     * and an encoded version of its MIME type as its native.
     *
     * @param flavors an array of <code>DataFlavor</code>s which will be the
     *        key set of the returned <code>Map</code>. If <code>null</code> is
     *        specified, a mapping of all <code>DataFlavor</code>s known to the
     *        data transfer subsystem to their most preferred 
     *        <code>String</code> natives will be returned.
     * @return a <code>java.util.Map</code> of <code>DataFlavor</code>s to
     *         <code>String</code> natives
     *
     * @see #getNativesForFlavor
     * @see #encodeDataFlavor
     */
    public synchronized Map<DataFlavor,String>
	getNativesForFlavors(DataFlavor[] flavors)
    {
        // Use getNativesForFlavor to generate extra natives for text flavors
        // and stringFlavor

        if (flavors == null) {
            List flavor_list = getFlavorsForNative(null);
            flavors = new DataFlavor[flavor_list.size()];
            flavor_list.toArray(flavors);
        }

        HashMap retval = new HashMap(flavors.length, 1.0f);
        for (int i = 0; i < flavors.length; i++) {
            List natives = getNativesForFlavor(flavors[i]);
            String nat = (natives.isEmpty()) ? null : (String)natives.get(0);
            retval.put(flavors[i], nat);
        }

        return retval;
    }

    /**
     * Returns a <code>Map</code> of the specified <code>String</code> natives
     * to their most preferred <code>DataFlavor</code>. Each
     * <code>DataFlavor</code> value will be the same as the first
     * <code>DataFlavor</code> in the List returned by
     * <code>getFlavorsForNative</code> for the specified native.
     * <p>
     * If a specified native is previously unknown to the data transfer
     * subsystem, and that native has been properly encoded, then invoking this
     * method will establish a mapping in both directions between the specified
     * native and a <code>DataFlavor</code> whose MIME type is a decoded
     * version of the native.
     *
     * @param natives an array of <code>String</code>s which will be the
     *        key set of the returned <code>Map</code>. If <code>null</code> is
     *        specified, a mapping of all supported <code>String</code> natives
     *        to their most preferred <code>DataFlavor</code>s will be
     *        returned.
     * @return a <code>java.util.Map</code> of <code>String</code> natives to
     *         <code>DataFlavor</code>s
     *
     * @see #getFlavorsForNative
     * @see #encodeJavaMIMEType
     */
    public synchronized Map<String,DataFlavor>
	getFlavorsForNatives(String[] natives)
    {
        // Use getFlavorsForNative to generate extra flavors for text natives

        if (natives == null) {
            List native_list = getNativesForFlavor(null);
            natives = new String[native_list.size()];
            native_list.toArray(natives);
        }

        HashMap retval = new HashMap(natives.length, 1.0f);
        for (int i = 0; i < natives.length; i++) {
            List flavors = getFlavorsForNative(natives[i]);
            DataFlavor flav = (flavors.isEmpty())
                ? null : (DataFlavor)flavors.get(0);
            retval.put(natives[i], flav);
        }

        return retval;
    }

    /**
     * Adds a mapping from the specified <code>DataFlavor</code> (and all
     * <code>DataFlavor</code>s equal to the specified <code>DataFlavor</code>)
     * to the specified <code>String</code> native.
     * Unlike <code>getNativesForFlavor</code>, the mapping will only be
     * established in one direction, and the native will not be encoded. To
     * establish a two-way mapping, call
     * <code>addFlavorForUnencodedNative</code> as well. The new mapping will 
     * be of lower priority than any existing mapping.
     * This method has no effect if a mapping from the specified or equal
     * <code>DataFlavor</code> to the specified <code>String</code> native
     * already exists.
     *
     * @param flav the <code>DataFlavor</code> key for the mapping
     * @param nat the <code>String</code> native value for the mapping
     * @throws NullPointerException if flav or nat is <code>null</code>
     *
     * @see #addFlavorForUnencodedNative
     * @since 1.4
     */
    public synchronized void addUnencodedNativeForFlavor(DataFlavor flav,
                                                         String nat) {
        if (flav == null || nat == null) {
            throw new NullPointerException("null arguments not permitted");
        }
        
        List natives = (List)flavorToNative.get(flav);
        if (natives == null) {
            natives = new ArrayList(1);
            flavorToNative.put(flav, natives);
        } else if (natives.contains(nat)) {
            return;
        }
        natives.add(nat);
        getNativesForFlavorCache.remove(flav);
        getNativesForFlavorCache.remove(null);
    }

    /**
     * Discards the current mappings for the specified <code>DataFlavor</code>
     * and all <code>DataFlavor</code>s equal to the specified
     * <code>DataFlavor</code>, and creates new mappings to the 
     * specified <code>String</code> natives.
     * Unlike <code>getNativesForFlavor</code>, the mappings will only be
     * established in one direction, and the natives will not be encoded. To
     * establish two-way mappings, call <code>setFlavorsForNative</code>
     * as well. The first native in the array will represent the highest
     * priority mapping. Subsequent natives will represent mappings of
     * decreasing priority.
     * <p>
     * If the array contains several elements that reference equal
     * <code>String</code> natives, this method will establish new mappings
     * for the first of those elements and ignore the rest of them.
     * <p> 
     * It is recommended that client code not reset mappings established by the
     * data transfer subsystem. This method should only be used for
     * application-level mappings.
     *
     * @param flav the <code>DataFlavor</code> key for the mappings
     * @param natives the <code>String</code> native values for the mappings
     * @throws NullPointerException if flav or natives is <code>null</code>
     *         or if natives contains <code>null</code> elements
     *
     * @see #setFlavorsForNative
     * @since 1.4
     */
    public synchronized void setNativesForFlavor(DataFlavor flav,
                                                 String[] natives) {
        if (flav == null || natives == null) {
            throw new NullPointerException("null arguments not permitted");
        }

        flavorToNative.remove(flav);
        for (int i = 0; i < natives.length; i++) {
            addUnencodedNativeForFlavor(flav, natives[i]);
        }
        disabledMappingGenerationKeys.add(flav);
        // Clear the cache to handle the case of empty natives.
        getNativesForFlavorCache.remove(flav);
        getNativesForFlavorCache.remove(null);
    }

    /**
     * Adds a mapping from a single <code>String</code> native to a single
     * <code>DataFlavor</code>. Unlike <code>getFlavorsForNative</code>, the
     * mapping will only be established in one direction, and the native will
     * not be encoded. To establish a two-way mapping, call
     * <code>addUnencodedNativeForFlavor</code> as well. The new mapping will
     * be of lower priority than any existing mapping.
     * This method has no effect if a mapping from the specified
     * <code>String</code> native to the specified or equal
     * <code>DataFlavor</code> already exists.
     *
     * @param nat the <code>String</code> native key for the mapping
     * @param flav the <code>DataFlavor</code> value for the mapping
     * @throws NullPointerException if nat or flav is <code>null</code>
     *
     * @see #addUnencodedNativeForFlavor
     * @since 1.4
     */
    public synchronized void addFlavorForUnencodedNative(String nat,
                                                         DataFlavor flav) {
        if (nat == null || flav == null) {
            throw new NullPointerException("null arguments not permitted");
        }

        List flavors = (List)nativeToFlavor.get(nat);
        if (flavors == null) {
            flavors = new ArrayList(1);
            nativeToFlavor.put(nat, flavors);
        } else if (flavors.contains(flav)) {
            return;
        }
        flavors.add(flav);
        getFlavorsForNativeCache.remove(nat);
        getFlavorsForNativeCache.remove(null);
    }

    /**
     * Discards the current mappings for the specified <code>String</code>
     * native, and creates new mappings to the specified
     * <code>DataFlavor</code>s. Unlike <code>getFlavorsForNative</code>, the
     * mappings will only be established in one direction, and the natives need
     * not be encoded. To establish two-way mappings, call
     * <code>setNativesForFlavor</code> as well. The first
     * <code>DataFlavor</code> in the array will represent the highest priority
     * mapping. Subsequent <code>DataFlavor</code>s will represent mappings of
     * decreasing priority.
     * <p>
     * If the array contains several elements that reference equal
     * <code>DataFlavor</code>s, this method will establish new mappings
     * for the first of those elements and ignore the rest of them.
     * <p>
     * It is recommended that client code not reset mappings established by the
     * data transfer subsystem. This method should only be used for
     * application-level mappings.
     *
     * @param nat the <code>String</code> native key for the mappings
     * @param flavors the <code>DataFlavor</code> values for the mappings
     * @throws NullPointerException if nat or flavors is <code>null</code>
     *         or if flavors contains <code>null</code> elements
     *
     * @see #setNativesForFlavor
     * @since 1.4
     */
    public synchronized void setFlavorsForNative(String nat,
                                                 DataFlavor[] flavors) {
        if (nat == null || flavors == null) {
            throw new NullPointerException("null arguments not permitted");
        }

        nativeToFlavor.remove(nat);
        for (int i = 0; i < flavors.length; i++) {
            addFlavorForUnencodedNative(nat, flavors[i]);
        }
        disabledMappingGenerationKeys.add(nat);
        // Clear the cache to handle the case of empty flavors.
        getFlavorsForNativeCache.remove(nat);
        getFlavorsForNativeCache.remove(null);
    }

    /**
     * Encodes a MIME type for use as a <code>String</code> native. The format
     * of an encoded representation of a MIME type is implementation-dependent.
     * The only restrictions are:
     * <ul>
     * <li>The encoded representation is <code>null</code> if and only if the
     * MIME type <code>String</code> is <code>null</code>.</li>
     * <li>The encoded representations for two non-<code>null</code> MIME type
     * <code>String</code>s are equal if and only if these <code>String</code>s
     * are equal according to <code>String.equals(Object)</code>.</li>
     * </ul>
     * <p>
     * Sun's reference implementation of this method returns the specified MIME
     * type <code>String</code> prefixed with <code>JAVA_DATAFLAVOR:</code>.
     *
     * @param mimeType the MIME type to encode
     * @return the encoded <code>String</code>, or <code>null</code> if
     *         mimeType is <code>null</code>
     */
    public static String encodeJavaMIMEType(String mimeType) {
        return (mimeType != null)
            ? JavaMIME + mimeType
            : null;
    }

    /**
     * Encodes a <code>DataFlavor</code> for use as a <code>String</code>
     * native. The format of an encoded <code>DataFlavor</code> is 
     * implementation-dependent. The only restrictions are:
     * <ul>
     * <li>The encoded representation is <code>null</code> if and only if the
     * specified <code>DataFlavor</code> is <code>null</code> or its MIME type
     * <code>String</code> is <code>null</code>.</li>
     * <li>The encoded representations for two non-<code>null</code>
     * <code>DataFlavor</code>s with non-<code>null</code> MIME type
     * <code>String</code>s are equal if and only if the MIME type
     * <code>String</code>s of these <code>DataFlavor</code>s are equal
     * according to <code>String.equals(Object)</code>.</li>
     * </ul>
     * <p>
     * Sun's reference implementation of this method returns the MIME type
     * <code>String</code> of the specified <code>DataFlavor</code> prefixed
     * with <code>JAVA_DATAFLAVOR:</code>.
     *
     * @param flav the <code>DataFlavor</code> to encode
     * @return the encoded <code>String</code>, or <code>null</code> if
     *         flav is <code>null</code> or has a <code>null</code> MIME type
     */
    public static String encodeDataFlavor(DataFlavor flav) {
        return (flav != null)
            ? SystemFlavorMap.encodeJavaMIMEType(flav.getMimeType())
            : null;
    }

    /**
     * Returns whether the specified <code>String</code> is an encoded Java
     * MIME type.
     *
     * @param str the <code>String</code> to test
     * @return <code>true</code> if the <code>String</code> is encoded;
     *         <code>false</code> otherwise
     */
    public static boolean isJavaMIMEType(String str) {
        return (str != null && str.startsWith(JavaMIME, 0));
    }

    /**
     * Decodes a <code>String</code> native for use as a Java MIME type.
     *
     * @param nat the <code>String</code> to decode
     * @return the decoded Java MIME type, or <code>null</code> if nat is not
     *         an encoded <code>String</code> native
     */
    public static String decodeJavaMIMEType(String nat) {
        return (isJavaMIMEType(nat))
            ? nat.substring(JavaMIME.length(), nat.length()).trim()
            : null;
    }

    /**
     * Decodes a <code>String</code> native for use as a
     * <code>DataFlavor</code>.
     *
     * @param nat the <code>String</code> to decode
     * @return the decoded <code>DataFlavor</code>, or <code>null</code> if
     *         nat is not an encoded <code>String</code> native
     */
    public static DataFlavor decodeDataFlavor(String nat)
        throws ClassNotFoundException
    {
        String retval_str = SystemFlavorMap.decodeJavaMIMEType(nat);
        return (retval_str != null)
            ? new DataFlavor(retval_str)
            : null;
    }
}
