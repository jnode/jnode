package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;

import org.jnode.apps.jpartition.Context;

public class YesNo extends Component {
    public YesNo(Context context) {
        super(context);
    }

    public boolean show(String question) throws IOException {
        return show(question, null);
    }

    public boolean show(String question, Boolean defaultValue) throws IOException {
        checkNonNull("question", question);

        println();
        print(question);

        if (defaultValue != null) {
            String defaultValueStr = getValueStr(defaultValue);
            print("[" + defaultValueStr + "]");
        }

        Boolean value;
        do {
            value = readBoolean(defaultValue);

            if (value == null) {
                reportError("invalid value");
            }
        } while (value == null);

        return value;
    }
}
