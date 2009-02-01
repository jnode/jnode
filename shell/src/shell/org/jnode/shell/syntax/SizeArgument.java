/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.shell.syntax;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;
import org.jnode.util.BinaryScaleFactor;
import org.jnode.util.DecimalScaleFactor;
import org.jnode.util.ScaleFactor;

/**
 * This Argument class accepts size values.  These are integers with an optional decimal 
 * or binary scaling suffix; e.g. 1K means 1000 or 1024
 * 
 * @author crawley@jnode.org
 */
public class SizeArgument extends Argument<Long> {
    private final boolean binaryScaling;

    public SizeArgument(String label, int flags, String description) {
        this(label, flags, true, description);
    }

    public SizeArgument(String label, int flags, boolean binaryScaling, String description) {
        super(label, flags, new Long[0], description);
        this.binaryScaling = binaryScaling;
    }

    @Override
    protected Long doAccept(Token token) throws CommandSyntaxException {
        String str = token.text;
        ScaleFactor factor = scaleFactor(str);
        if (factor != null) { 
            str = str.substring(0, str.length() - factor.getUnit().length());
        }
        try {
            long tmp = Long.parseLong(str);
            return new Long(tmp * factor.getMultiplier());
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("invalid size");
        }
    }
    
    private ScaleFactor scaleFactor(String str) {
        ScaleFactor[] prefixes = binaryScaling ? 
                BinaryScaleFactor.values() : DecimalScaleFactor.values();
        for (ScaleFactor unit : prefixes) {
            String unitStr = unit.getUnit();
            if (str.endsWith(unitStr)) {
                return unit;
            }
        }
        return null;
    }
  
    @Override
    public void complete(CompletionInfo completion, String partial) {
        // No completion for now
    }

    @Override
    protected String state() {
        return super.state() + "binaryScaling=" + binaryScaling;
    }
    
    @Override
    protected String argumentKind() {
        return "size";
    }
}
