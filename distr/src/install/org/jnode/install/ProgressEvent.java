/*
 * $Id$
 */
package org.jnode.install;

/**
 * @author Levente S�ntha
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
