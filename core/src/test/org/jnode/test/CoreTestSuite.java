package org.jnode.test;

import org.jnode.test.util.NumberUtilsTest;
import org.jnode.test.util.VersionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite calling all tests run for the core project.
 * @author ewout
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    NumberUtilsTest.class,
    VersionTest.class,
}
)
public class CoreTestSuite {

}
