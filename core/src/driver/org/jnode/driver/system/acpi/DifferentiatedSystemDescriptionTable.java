/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.system.acpi;

import java.nio.ByteBuffer;
import org.jnode.driver.system.acpi.aml.ParseNode;
import org.jnode.driver.system.acpi.aml.Parser;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;

/**
 * DifferentiatedSystemDescriptionTable.
 *
 * @author Francois-Frederic Ozog
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DifferentiatedSystemDescriptionTable extends AcpiSystemTable {

    private final ParseNode root;

    public DifferentiatedSystemDescriptionTable(AcpiDriver driver, ResourceManager rm, MemoryResource tableResource)
        throws ResourceNotFreeException {
        super(driver, tableResource);
        root = parse();
    }

    private final ParseNode parse() throws ResourceNotFreeException {
        final Parser p = new Parser();
        // the AML starts at offsset 36 of DSDT
        final int amlLength = getSize() - 36;
        final byte[] table = new byte[amlLength];
        getBytes(36, table, 0, amlLength);
        ByteBuffer amlBuffer = ByteBuffer.wrap(table);
        amlBuffer.rewind();
        return p.parse(amlBuffer);
    }

    public ParseNode getParsedAml() {
        return root;
    }
}
