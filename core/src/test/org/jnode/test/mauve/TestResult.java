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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the result of running one test.  A test usually contains multiple
 * checks and corresponds to a single method in the API being tested.  There
 * are exceptions of course.
 */
public class TestResult implements Comparable {

    /**
     * The name of the test (usually the method name).
     */
    private String name;

    /**
     * A list containing results for each of the checks applied by the test.
     */
    private List checkResults;

    private String error = null;

    /**
     * Creates a new result, initially empty.
     *
     * @param name
     */
    TestResult(String name) {
        this.name = name;
        checkResults = new ArrayList();
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
     * Adds a check result.
     *
     * @param result the check result.
     */
    void add(CheckResult result) {
        checkResults.add(result);
    }

    /**
     * Returns an iterator that provides access to all the check results.
     *
     * @return An iterator.
     */
    public Iterator getCheckIterator() {
        return checkResults.iterator();
    }

    /**
     * Returns the total number of checks performed by this test.
     *
     * @return The check count.
     */
    public int getCheckCount() {
        return checkResults.size();
    }

    /**
     * Returns the number of checks with the specified status.
     *
     * @param passed the check status.
     * @return The number of checks passed or failed.
     */
    public int getCheckCount(boolean passed) {
        int result = 0;
        Iterator iterator = checkResults.iterator();
        while (iterator.hasNext()) {
            CheckResult check = (CheckResult) iterator.next();
            if (check.getPassed() == passed)
                result++;
        }
        if (!passed && error != null)
            result++; // count stacktrace as a failure
        return result;
    }

    public int compareTo(Object obj) {
        TestResult that = (TestResult) obj;
        return getName().compareTo(that.getName());
    }

    void failed(Throwable t) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(out, false);
        t.printStackTrace(w);
        w.close();
        try {
            out.close();
            error = out.toString();
        } catch (IOException e) { // this should never happen..
        }
    }

    public boolean isFailed() {
        return error != null;
    }

    public String getFailedMessage() {
        return error;
    }

    public void setFailedMessage(String error) {
        this.error = error;        
    }
}
