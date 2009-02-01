/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.test.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginDescriptor;

/**
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public final class TestManager implements ExtensionPointListener {
    public static final String ALL_CATEGORY = "all";
    public static final List<String> DEFAULT_CATEGORY =
        Collections.unmodifiableList(Arrays.asList(new String[]{ALL_CATEGORY, "default"}));

    private static TestManager instance = null;

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(TestManager.class);

    /**
     * The org.jnode.tests extension point
     */
    private final ExtensionPoint typesEP;

    /**
     * The Test classes
     */
    private final List<Class<? extends Test>> tests =
        new ArrayList<Class<? extends Test>>();

    /**
     * The TestSuite classes
     */
    private final List<Class<? extends TestSuite>> suites =
        new ArrayList<Class<? extends TestSuite>>();

    /**
     * The Categories for the Tests and TestSuites
     */
    private final Map<Class<? extends Test>, List<String>> categories =
        new HashMap<Class<? extends Test>, List<String>>();

    private final Set<String> categoriesNames = new TreeSet<String>();

    public static TestManager getInstance() {
        if (TestManager.instance == null)
            throw new RuntimeException("TestManager not yet created");

        return TestManager.instance;
    }

    /**
     * Create a new instance
     */
    TestManager(ExtensionPoint typesEP) {
        if (instance != null)
            throw new RuntimeException("TestManager already created");

        TestManager.instance = this;

        this.typesEP = typesEP;
        if (typesEP == null) {
            throw new IllegalArgumentException("The types extension-point cannot be null");
        }

//log.debug("before addListener");
//typesEP.addListener(this);
//log.debug("after addListener");

        refreshTests();
        log.debug("end of Cstor");
    }

    public List<Class<? extends Test>> getTests() {
        refreshTests();
        List<Class<? extends Test>> result = new ArrayList<Class<? extends Test>>(tests);
        result.addAll(suites);
        return result;
    }

    public List<String> getCategories(Class<? extends Test> test) {
        refreshTests();
        return Collections.unmodifiableList(categories.get(test));
    }

    public Set<String> getCategories() {
        refreshTests();
        return Collections.unmodifiableSet(categoriesNames);
    }

    /**
     * Get a TestSuite with all known tests.
     */
    public TestSuite getTestSuite() {
        refreshTests();
        return getTestSuite(Collections.singletonList(ALL_CATEGORY));
    }

    /**
     * Get a TestSuite with all tests that have one of the given categories.
     */
    public synchronized TestSuite getTestSuite(List<String> wantedCategories) {
        refreshTests();
        TestSuite suite = new TestSuite();

        // add Tests
        for (Class<? extends Test> testClass : tests) {
            if (!matchCategory(wantedCategories, categories.get(testClass))) continue;

            try {
                Test test = (Test) testClass.newInstance();
                suite.addTest(test);
                log.debug("added Test " + testClass.getName());
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }

        // add TestSuites
        for (Class<? extends TestSuite> suiteClass : suites) {
            if (!matchCategory(wantedCategories, categories.get(suiteClass))) continue;

            suite.addTestSuite(suiteClass);
            log.debug("added TestSuite " + suiteClass.getName());
        }

        return suite;
    }

    private static boolean matchCategory(List<String> wantedCategories,
                                         List<String> testCategories) {
        for (String testCategory : testCategories) {
            if (wantedCategories.contains(testCategory)) {
                log.debug("testCategory:" + testCategory + " contains");
                return true;
            }
            log.debug("testCategory:" + testCategory + " NOT contains");
        }

        log.debug("testCategory FAILS");
        return false;
    }

    /**
     * Refresh all known tests.
     */
    private synchronized void refreshTests() {
        log.debug("<<< BEGIN refreshTests >>>");

        suites.clear();
        tests.clear();
        categories.clear();
        categoriesNames.clear();

        for (Extension ext : typesEP.getExtensions()) {
            final PluginDescriptor desc = ext.getDeclaringPluginDescriptor();
            log.debug("plugin " + desc.getName() + " classloader=" + desc.getPluginClassLoader());
            final ConfigurationElement[] elements = ext.getConfigurationElements();
            for (ConfigurationElement e : elements) {
                if ("test".equals(e.getName())) {
                    addTest(tests, e, desc);
                } else if ("suite".equals(e.getName())) {
                    addTest(suites, e, desc);
                }
            }
        }
        log.debug("<<< END refreshTests >>>");
    }

    private <T extends Test> void addTest(List<Class<? extends T>> list,
                                          ConfigurationElement e, PluginDescriptor desc) {
        String className = e.getAttribute("class");
        Class<T> clazz = (Class<T>) loadClass(className, desc);
        if (clazz != null) {
            list.add(clazz);
            log.debug("adding class " + className);

            String testCategories = e.getAttribute("category");
            log.debug("testCategories=" + testCategories);
            List<String> categs;
            if ((testCategories == null) || (testCategories.trim().length() == 0)) {
                log.debug("DEFAULT_CATEGORY");
                categs = DEFAULT_CATEGORY;
            } else {
                categs = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(testCategories.trim(), ",", false);
                log.debug(st.countTokens() + " tokens");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    log.debug("token=" + token);
                    categs.add(token);
                }
                categs.add(ALL_CATEGORY);
                log.debug("ALL_CATEGORY");
            }

            categories.put(clazz, categs);
            categoriesNames.addAll(categs);
        }
    }

    private static Class loadClass(String className, PluginDescriptor desc) {
        log.debug("searching class " + className);

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class clazz = loadClass(className, loader);
        log.debug("ContextClassLoader:" + ((clazz == null) ? "not found" : "FOUND"));

        loader = desc.getPluginClassLoader();
        if (clazz == null) {
            clazz = loadClass(className, loader);
            log.debug("PluginClassLoader:" + ((clazz == null) ? "not found" : "FOUND"));
        }

        return clazz;
    }

    private static Class loadClass(String className, ClassLoader loader) {
        try {
            log.debug("loadClass: method 1");
            return Class.forName(className, true, loader);
        } catch (ClassNotFoundException e) {
            try {
                log.debug("loadClass: method 2");
                return loader.loadClass(className);
            } catch (ClassNotFoundException e1) {
                log.debug("loadClass: all methods failed");
                return null;
            }
        }
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint,
     * org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
//log.debug("extensionAdded");
//log.debug("extensionAdded : before refreshTests");
//refreshTests();
//log.debug("extensionAdded : after refreshTests");
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint,
     * org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
//log.debug("extensionRemoved : before refreshTests");
//refreshTests;
//log.debug("extensionRemoved : after refreshTests");
    }
}
