/*
 * $Id$
 */
package org.jnode.vm;

final class LoadCompileThread extends Thread {

    private final LoadCompileService service;

    /**
     * Default ctor
     */
    public LoadCompileThread(LoadCompileService service, String name) {
        super(name);
        this.service = service;
    }

    /**
     * Do the actual compilation
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        while (true) {
            try {
                service.processNextRequest();
            } catch (Throwable ex) {
                // Ignore
                ex.printStackTrace(System.err);
            }
        }
    }

}
