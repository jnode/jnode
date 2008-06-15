package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;

import org.jnode.apps.jpartition.Context;

public class NumberField extends Component {
    public NumberField(Context context) {
        super(context);
    }

    public long show(String question) throws IOException {
        return show(question, Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public long show(String question, Long defaultValue) throws IOException {
        return show(question, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public Long show(String question, Long defaultValue, long min, long max) throws IOException {
        checkNonNull("question", question);
        checkInBounds("min", min, "max", max, "defaultValue", defaultValue);

        print(question);
        if (defaultValue != null) {
            print(" [" + defaultValue + "]");
        }

        Long value = readInt(defaultValue, min, max);
        while ((value == null) || (value < min) || (value > max)) {
            value = readInt(defaultValue);
        }

        return value;
    }
}
