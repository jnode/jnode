package org.jnode.shell.bjorne;

import org.jnode.shell.ShellException;

class BjorneControlException extends ShellException {

    private static final long serialVersionUID = 1L;

    private final int control;

    private int count;

    BjorneControlException(final int control, final int count) {
        super();
        this.control = control;
        this.count = count;
    }

    final int getControl() {
        return control;
    }

    final int getCount() {
        return count;
    }

    final void decrementCount() {
        count--;
    }

}
