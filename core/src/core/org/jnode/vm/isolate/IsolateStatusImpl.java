/*
 * $
 */
package org.jnode.vm.isolate;

import javax.isolate.IsolateStatus;

/**
 * @author Levente S\u00e1ntha
 */
public class IsolateStatusImpl extends IsolateStatus implements Cloneable {

    public IsolateStatusImpl(State state) {
        super(state, null, 0);
    }

    public IsolateStatusImpl(ExitReason exitReason, int exitCode) {
        super(State.EXITED, exitReason, exitCode);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public IsolateStatusImpl copy() {
        try {
            return (IsolateStatusImpl) this.clone();
        } catch (CloneNotSupportedException x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public String toString() {
        State s = getState();
        if (s.equals(State.EXITED)) {
            return getState() + "(" + getExitReason() + "," + getExitCode() + ")";
        } else {
            return getState().toString();
        }
    }
}
