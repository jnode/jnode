/*
 * $Id$
 */
package org.jnode.install;

/**
 * @author Levente Sántha
 */
public class ProgressEvent {
    private int progress;

    public ProgressEvent(int progress) {
        this.progress = progress;
    }


    public int getProgress() {
        return progress;
    }
}
