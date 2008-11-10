// Copyright (c) 2008 Fabien DUMINY (fduminy@jnode.org)

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.  */

package org.jnode.test.mauve;


/**
 * Constants for tags used by {@link XMLReportParser} and {@link XMLReportWriter}.
 *  
 * @author fabien
 *
 */
public class XMLReportConstants {
    protected static final String RUN_RESULT = "run";
    protected static final String RUN_NAME = "name";
    
    protected static final String PACKAGE_RESULT = "package";
    protected static final String PACKAGE_NAME = "name";
    
    protected static final String CLASS_RESULT = "class";
    protected static final String CLASS_NAME = "name";
    
    protected static final String TEST_RESULT = "test";
    protected static final String TEST_NAME = "name";
    protected static final String TEST_ERROR = "error";
    
    protected static final String CHECK_RESULT = "check";
    protected static final String CHECK_NUMBER = "number";
    protected static final String CHECK_POINT = "check-point";
    protected static final String CHECK_PASSED = "passed";
    protected static final String CHECK_EXPECTED = "expected";
    protected static final String CHECK_ACTUAL = "actual";
    protected static final String CHECK_LOG = "log";
}
