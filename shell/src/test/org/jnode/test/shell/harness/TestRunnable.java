package org.jnode.test.shell.harness;

/**
 * This is the API implemented by the command test runners.  We cannot
 * use / extend Runnable because we need to propagate any exceptions
 * in the {@link TestRunnable.run()} method.
 * 
 * @author crawley@jnode.org
 */
public interface TestRunnable {
    
    public int run() throws Exception;
    
    public void setup();
    
    public void cleanup();

}
