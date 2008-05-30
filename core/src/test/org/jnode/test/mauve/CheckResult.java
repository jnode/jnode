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

/**
 * Records the details of a check that is performed by Mauve.
 */
public class CheckResult {

    /**
     * The check number.
     */
    private int number;

    /**
     * The check point string.
     */
    private String checkPoint;

    /**
     * A flag that indicates whether or not the check passed.
     */
    private boolean passed;

    /**
     * The expected result (converted to a string).
     */
    private String expected;

    /**
     * The actual result (converted to a string).
     */
    private String actual;

    /**
     * The log output for the check.
     */
    private StringBuffer log;

    /**
     * Creates a new check.
     *
     * @param number the check number.
     * @param passed a flag that indicates whether or not the check passed.
     */
    CheckResult(int number, boolean passed) {
        this.number = number;
        this.passed = passed;
    }

    /**
     * Returns the check number.
     *
     * @return The check number.
     */
    public int getNumber() {
        return this.number;
    }

    /**
     * Sets the check number.
     *
     * @param number the number.
     */
    void setNumber(int number) {
        this.number = number;
    }

    /**
     * Returns a flag that indicates whether or not the check passed.
     *
     * @return A boolean.
     */
    public boolean getPassed() {
        return passed;
    }

    /**
     * Sets the flag that indicates whether or not the check passed.
     *
     * @param passed the flag.
     */
    void setPassed(boolean passed) {
        this.passed = passed;
    }

    /**
     * Returns the check point string.
     *
     * @return The check point string.
     */
    public String getCheckPoint() {
        return checkPoint;
    }

    /**
     * Sets the check point string.
     *
     * @param checkPoint the check point string.
     */
    void setCheckPoint(String checkPoint) {
        this.checkPoint = checkPoint;
    }

    /**
     * Returns a string representing the actual value.
     *
     * @return The actual value.
     */
    public String getActual() {
        if (actual == null)
            return "n/a";
        return actual;
    }

    /**
     * Sets the actual value.
     *
     * @param actual the actual value.
     */
    void setActual(String actual) {
        this.actual = actual;
    }

    /**
     * Returns the expected value.
     *
     * @return The expected value.
     */
    public String getExpected() {
        if (expected == null)
            return "n/a";
        return expected;
    }

    /**
     * Sets the expected value.
     *
     * @param expected the expected value.
     */
    void setExpected(String expected) {
        this.expected = expected;
    }

    /**
     * Returns the log.
     *
     * @return The log.
     */
    public String getLog() {
        if (log == null)
            return "";
        return log.toString();
    }

    /**
     * Appends the specified message to the log.
     *
     * @param message the message to append.
     */
    void appendToLog(String message) {
        if (log == null)
            log = new StringBuffer();
        log.append(message);
    }
}
