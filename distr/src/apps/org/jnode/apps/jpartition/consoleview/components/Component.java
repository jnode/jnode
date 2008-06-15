package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;

public class Component {
    protected static final String TRUE_LONG_STRING = "yes";
    protected static final String FALSE_LONG_STRING = "no";

    protected final Context context;

    protected Component(Context context) {
        this.context = context;
    }

    protected final void print(String s) {
        context.getOut().print(s);
    }

    protected final void println() {
        context.getOut().println();
    }

    protected final void println(String s) {
        context.getOut().println(s);
    }

    protected final int read() throws IOException {
        return context.getIn().read();
    }

    protected final String getValueStr(boolean value) {
        return value ? TRUE_LONG_STRING : FALSE_LONG_STRING;
    }

    protected final Boolean readBoolean(Boolean defaultValue) throws IOException {
        String line = context.getIn().readLine();

        Boolean value;
        if ((line == null) || (line.trim().length() == 0)) {
            value = defaultValue;
            if (value != null) {
                print(String.valueOf(defaultValue));
            }
        } else {
            try {
                line = line.trim();
                if (defaultValue == null) {
                    if (TRUE_LONG_STRING.equals(line)) {
                        value = true;
                    } else if (FALSE_LONG_STRING.equals(line)) {
                        value = false;
                    } else {
                        value = null;
                    }
                } else if (defaultValue) {
                    value = getValueStr(defaultValue).equals(line) ? false : true;
                } else {
                    value = getValueStr(defaultValue).equals(line) ? true : false;
                }
            } catch (Exception e) {
                value = defaultValue;
            }
        }

        return value;
    }

    protected final Long readInt() throws IOException {
        return readInt(null, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected final Long readInt(Long defaultValue) throws IOException {
        return readInt(defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected final Long readInt(Long defaultValue, long min, long max) throws IOException {
        checkInBounds("min", min, "max", max, "defaultValue", defaultValue);

        String line = context.getIn().readLine();
        Long value;
        if ((line == null) || (line.trim().length() == 0)) {
            value = defaultValue;
            if (value != null) {
                print(String.valueOf(defaultValue));
            }
        } else {
            line = line.trim();
            try {
                value = Long.decode(line);
            } catch (NumberFormatException e) {
                reportError("invalid value");
                value = null;
            }

            if (value != null) {
                try {
                    checkInBounds("min", min, "max", max, "value", value);
                } catch (IllegalArgumentException e) {
                    if (min != max) {
                        reportError("value must be between " + min + " and " + max);
                    } else {
                        reportError("value must be " + min);
                    }

                    value = null;
                }
            }
        }

        return value;
    }

    protected final void reportError(Logger log, Object source, Throwable t) {
        context.getErrorReporter().reportError(log, source, t);
    }

    protected final void reportError(Logger log, Object source, String message) {
        context.getErrorReporter().reportError(log, source, message);
    }

    protected final void reportError(String message) {
        context.getErrorReporter().reportError(null, null, message);
    }

    protected final void checkNonNull(String paramName, Object param) {
        if (param == null) {
            throw new NullPointerException("parameter " + paramName + " can't be null");
        }
    }

    protected final void checkNonEmpty(String paramName, Collection<?> param) {
        checkNonNull(paramName, param);

        if (param.isEmpty()) {
            throw new IllegalArgumentException("parameter " + paramName + " can't be empty");
        }
    }

    protected final void checkInBounds(String minName, long min, String maxName, long max,
            String valueName, Long value) {
        checkMinMax(minName, min, maxName, max);

        if (value != null) {
            checkMinMax(minName, min, valueName, value);
            checkMinMax(valueName, value, maxName, max);
        }
    }

    protected final void checkMinMax(String minName, long min, String maxName, long max) {
        if (min > max) {
            throw new IllegalArgumentException("parameter " + minName + " must be > parameter " +
                    maxName);
        }
    }

}
