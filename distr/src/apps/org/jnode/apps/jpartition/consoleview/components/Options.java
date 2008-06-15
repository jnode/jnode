package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.jnode.apps.jpartition.Context;

public class Options extends Component {
    public Options(Context context) {
        super(context);
    }

    public <T> long show(String question, T[] options) throws IOException {
        return show(question, Arrays.asList(options), null);
    }

    public <T> long show(String question, T[] options, Labelizer<T> labelizer) throws IOException {
        return show(question, Arrays.asList(options));
    }

    @SuppressWarnings("unchecked")
    public <T> long show(String question, Collection<T> options) throws IOException {
        return show(question, Arrays.asList(options), null);
    }

    public <T> long show(String question, Collection<T> options, Labelizer<T> labelizer)
        throws IOException {
        checkNonNull("question", question);
        checkNonEmpty("options", options);

        println();
        println(question);
        int i = 1;
        for (T option : options) {
            String label =
                    (labelizer == null) ? String.valueOf(option) : labelizer.getLabel(option);
            println("  " + i + " - " + label);
            i++;
        }

        NumberField choice = new NumberField(context);
        return choice.show("Choice : ", null, 1, options.size());
    }
}
