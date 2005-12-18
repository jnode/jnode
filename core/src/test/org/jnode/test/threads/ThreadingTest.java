package org.jnode.test.threads;

/**
 * @author Levente S\u00e1ntha
 */
public class ThreadingTest extends BasicTest {

    public static void main(String[] argv) throws Exception {
        BasicTest.main(null);
        SynchronizedTest.main(null);
        MultiTest.main(null);
    }
}

