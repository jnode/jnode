// Tags: not-a-test
// Copyright (C) 2004 by Object Refinery Limited
// Written by David Gilbert (david.gilbert@object-refinery.com)

// This file is part of Mauve Reporter.

// Mauve Reporter is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.

// Mauve Reporter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve Reporter; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
package org.jnode.test.mauve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the result of running all the tests for a particular class.
 */
public class ClassResult implements Comparable {

    /**
     * The name of the test (usually the class name).
     */
    private String name;

    /**
     * A list containing results for each test applied for the class.
     */
    private List testResults;
    private boolean sorted = true;

    /**
     * Creates a new result, initially empty.
     *
     * @param name the class name.
     */
    ClassResult(String name) {
        this.name = name;
        testResults = new ArrayList();
    }

    /**
     * Returns the test name (this is most often the name of the method
     * being tested).
     *
     * @return The test name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the test name.
     *
     * @param name the name.
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a test result.
     *
     * @param result the test result.
     */
    public void add(TestResult result) {
        testResults.add(result);
        sorted = false;
    }

    /**
     * Returns an iterator that provides access to all the tests for
     * this class.
     *
     * @return An iterator.
     */
    public Iterator getTestIterator() {
        if (!sorted) {
            Collections.sort(testResults);
            sorted = true;
        }
        return testResults.iterator();
    }

    /**
     * Returns the total number of checks performed for this class.
     *
     * @return The check count.
     */
    public int getCheckCount() {
        int result = 0;
        Iterator iterator = testResults.iterator();
        while (iterator.hasNext()) {
            TestResult test = (TestResult) iterator.next();
            result = result + test.getCheckCount();
        }
        return result;
    }

    /**
     * Returns the number of checks with the specified status.
     *
     * @param passed the check status.
     * @return The number of checks passed or failed.
     */
    public int getCheckCount(boolean passed) {
        int result = 0;
        Iterator iterator = testResults.iterator();
        while (iterator.hasNext()) {
            TestResult test = (TestResult) iterator.next();
            result = result + test.getCheckCount(passed);
        }
        return result;
    }

    public int compareTo(Object obj) {
        ClassResult that = (ClassResult) obj;
        return getName().compareTo(that.getName());
    }
}
