/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
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
