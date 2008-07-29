/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2008 JNode.org
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
package org.jnode.partitions.command;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.partitions.ibm.IBMPartitionTypes;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;

/**
 * Argument close for partition type codes.  Input is in hexadecimal, and 
 * completion is supported.
 * 
 * @author crawley@jnode.org
 */
public class IBMPartitionTypeArgument extends Argument<IBMPartitionTypes> {
    public IBMPartitionTypeArgument(String label, int flags, String description) {
        super(label, flags, new IBMPartitionTypes[0], description);
    }

    @Override
    protected IBMPartitionTypes doAccept(Token value) throws CommandSyntaxException {
        try {
            int code = Integer.parseInt(value.token, 16);
            return IBMPartitionTypes.valueOf(code);
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("not a valid hexadecimal number");
        } catch (IllegalArgumentException ex) {
            throw new CommandSyntaxException(ex.getMessage());
        }
    }

    @Override
    public void complete(CompletionInfo completion, String partial) {
        partial = partial.toLowerCase();
        for (IBMPartitionTypes pt : IBMPartitionTypes.values()) {
            String code = Integer.toHexString(pt.getCode());
            if (code.startsWith(partial)) {
                completion.addCompletion(code);
            }
        }
    }

    @Override
    protected String argumentKind() {
        return "partition type";
    }
}
