package org.jnode.vm.compiler.ir;

/**
 * User: lsantha
 * Date: 2/15/15 10:18 AM
 */
public class ExceptionArgument extends MethodArgument {
    public ExceptionArgument(int type, int index) {
        super(type, index);
        setLocation(new TopStackLocation());
    }

    public ExceptionArgument(ExceptionArgument argument) {
        super(argument);
    }

    public Object clone() {
        ExceptionArgument arg = new ExceptionArgument(this);
        arg.setLocation(this.getLocation());
        return arg;
    }

    @Override
    public String toString() {
        return "e" + getIndex() + '_' + getSSAValue();
    }
}
