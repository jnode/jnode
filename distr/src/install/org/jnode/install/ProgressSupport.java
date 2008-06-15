/*
 * $Id$
 */
package org.jnode.install;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Levente S\u00e1ntha
 */
public class ProgressSupport implements ProgressAware {
    List<ProgressListener> listenerList = new ArrayList<ProgressListener>();

    public void addProgressListener(ProgressListener listener) {
        listenerList.add(listener);
    }

    void fireProgressEvent(ProgressEvent e) {
        for (ProgressListener listener : listenerList) {
            try {
                listener.progress(e);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }
}
