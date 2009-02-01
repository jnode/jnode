/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import org.jnode.plugin.PluginUtils;

/**
 * Documentation at {@link http://www.javaworld.com/javaworld/javaqa/2003-08/01-qa-0808-property.html}
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class ResourceTest extends TestCase {
    public static final String RELATIVE_BUNDLE_NAME = "messages";
    public static final String BAD_ABSOLUTE_BUNDLE_NAME = ResourceTest.class.getPackage().getName() + ".unknowbundle";

    /**
     * Absolute name of the bundle
     */
    public static final String BUNDLE_NAME = ResourceTest.class.getPackage().getName() + ".messages";

    /**
     * name relative to the current package of this class
     */
    public static final String RESOURCE_NAME = "messages.properties";

    public static final String TEST_KEY = "test";
    public static final String TEST_VALUE = "testok";

    public static void main(String[] args) throws IOException {
        String resName = (args.length > 0) ? args[0] : ResourceTest.class.getName().replace('.', '/') + ".class";
        URL url = ResourceTest.class.getClassLoader().getResource(resName);
        System.out.println("URL=" + url);
        InputStream is = url.openStream();
        is.close();
    }

    //
    // ClassLoader tests
    //

    public void testClassLoaderGetResource() {
        doCLGetResource(relativeToAbsolutePath(RESOURCE_NAME, false));
    }

    public void testClassLoaderGetResourceMySelf() {
        doCLGetResource(classToAbsolutePath(false));
    }

    public void testClassLoaderGetResourceAsStream() throws IOException {
        doCLGetResourceAsStream(relativeToAbsolutePath(RESOURCE_NAME, false));
    }

    public void testClassLoaderGetResourceAsStreamMySelf() throws IOException {
        doCLGetResourceAsStream(classToAbsolutePath(false));
    }

    //
    // Class tests
    //
    public void testClassGetResourceAbsolute() {
        doClassGetResource(relativeToAbsolutePath(RESOURCE_NAME, true));
    }

    public void testClassGetResourceRelative() {
        doClassGetResource(RESOURCE_NAME);
    }

    public void testClassGetResourceMySelfAbsolute() {
        doClassGetResource(classToAbsolutePath(true));
    }

    public void testClassGetResourceMySelfRelative() {
        doClassGetResource(getClassFileName());
    }

    public void testClassGetResourceAsStreamAbsolute() throws IOException {
        doClassGetResourceAsStream(relativeToAbsolutePath(RESOURCE_NAME, true));
    }

    public void testClassGetResourceAsStreamRelative() throws IOException {
        doClassGetResourceAsStream(RESOURCE_NAME);
    }

    public void testClassGetResourceAsStreamMySelfAbsolute() throws IOException {
        doClassGetResourceAsStream(classToAbsolutePath(true));
    }

    public void testClassGetResourceAsStreamMySelfRelative() throws IOException {
        doClassGetResourceAsStream(getClassFileName());
    }

    //
    // Bundle tests
    //

    public void testBundle() {
        // will load messages.properties
        doGetBundle(Locale.US, "");

        // will load messages_fr.properties
        doGetBundle(Locale.FRENCH, "_fr");

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BAD_ABSOLUTE_BUNDLE_NAME);
            fail("must not be found");
        } catch (MissingResourceException mre) {
            // OK
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(RELATIVE_BUNDLE_NAME);
            fail("relative bundle name not allowed");
        } catch (MissingResourceException mre) {
            // OK
        }
    }

    //
    // ResourceBundle tests
    //

    public void testPluginResourceBundle() {
        // will load messages.properties
        doGetLocalizedMessage(Locale.US, "");

        // will load messages_fr.properties
        doGetLocalizedMessage(Locale.FRENCH, "_fr");
    }

    //
    // Private methods
    //

    protected void doCLGetResource(String resName) {
        URL url = getClass().getClassLoader().getResource(resName);
        assertNotNull("resource " + resName + " not found", url);
        assertTrue("file part must ends with resource name", url.getFile().endsWith(resName));
    }

    protected void doCLGetResourceAsStream(String resName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resName);
        assertNotNull("resource " + resName + " not found", is);
        is.close();
    }

    protected void doClassGetResource(String resName) {
        URL url = getClass().getResource(resName);
        assertNotNull("resource " + resName + " not found", url);
        assertTrue("file part must ends with resource name", url.getFile().endsWith(resName));
    }

    protected void doClassGetResourceAsStream(String resName) throws IOException {
        InputStream is = getClass().getResourceAsStream(resName);
        assertNotNull("resource " + resName + " not found", is);
        is.close();
    }

    protected String relativeToAbsolutePath(String resName, boolean addRoot) {
        String packageName = getClass().getPackage().getName().replace('.', '/');
        String name = packageName + '/' + resName;
        return addRoot ? '/' + name : name;
    }

    protected String classToAbsolutePath(boolean addRoot) {
        String name = getClass().getName().replace('.', '/') + ".class";
        return addRoot ? '/' + name : name;
    }

    protected String getClassFileName() {
        return getShortName() + ".class";
    }

    protected String getShortName() {
        String fullName = getClass().getName();
        int idx = fullName.lastIndexOf('.');
        return (idx < 0) ? fullName : fullName.substring(idx + 1);
    }

    protected void doGetBundle(final Locale locale, String suffix) {
        changeLocale(locale);

        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        assertNotNull(bundle);
        assertEquals(PropertyResourceBundle.class, bundle.getClass());
        String msg = bundle.getString(TEST_KEY);
        assertEquals(TEST_VALUE + suffix, msg);
    }

    protected void doGetLocalizedMessage(Locale locale, String suffix) {
        changeLocale(locale);

        String msg = PluginUtils.getLocalizedMessage(getClass(), RELATIVE_BUNDLE_NAME, TEST_KEY);
        assertEquals(TEST_VALUE + suffix, msg);
    }

    private void changeLocale(final Locale locale) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Locale.setDefault(locale);
                return null;
            }
        });
    }
}
