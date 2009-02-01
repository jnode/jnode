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
 
package org.jnode.test.shell.syntax;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for org.jnode.test.shell.syntax");
        //$JUnit-BEGIN$
        suite.addTestSuite(ArgumentTypesTest.class);
        suite.addTestSuite(ArgumentBundleTest.class);
        suite.addTestSuite(DefaultTokenizerTest.class);
        suite.addTestSuite(MuSyntaxTest.class);
        suite.addTestSuite(MuParserTest.class);
        suite.addTestSuite(MuParserTest2.class);
        suite.addTestSuite(ArgumentMultiplicityTest.class);
        suite.addTestSuite(CommandLineTest.class);
        suite.addTestSuite(RepeatedSyntaxTest.class);
        suite.addTestSuite(SequenceSyntaxTest.class);
        suite.addTestSuite(OptionSyntaxTest.class);
        suite.addTestSuite(PowersetSyntaxTest.class);
        suite.addTestSuite(OptionSetSyntaxTest.class);
        suite.addTestSuite(AlternativesSyntaxTest.class);
        //$JUnit-END$
        return suite;
    }
}
