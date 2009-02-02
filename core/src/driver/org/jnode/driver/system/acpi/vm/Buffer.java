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
 
package org.jnode.driver.system.acpi.vm;

import java.util.HashMap;
import java.util.Map;

/**
 * Buffer.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class Buffer extends AcpiNamedObject {
    private byte[] buffer = null;
    private final Map<String, ByteField> fields = new HashMap<String, ByteField>();

    public Buffer(String name, int size) {
        super(name);
        buffer = new byte[size];
    }

    public Buffer(int size) {
        super("");
        buffer = new byte[size];
    }

    public void createByteField(int byteIndex, String name) {
        ByteField f = new ByteField(name, this, byteIndex);

        fields.put(name, f);
    }

    public static void CreateByteField(Buffer sourceBuffer, int byteIndex, String name) {
        ByteField f = new ByteField(name, sourceBuffer, byteIndex);
        sourceBuffer.fields.put(name, f);
    }

    public static void CreateByteField(String bufferName, int byteIndex, String name) {
        AcpiObject sourceBuffer = NameSpace.rootNameSpace.get(bufferName);
        if (!(sourceBuffer instanceof Buffer))
            return;
        CreateByteField((Buffer) sourceBuffer, byteIndex, name);
    }

    public AcpiObject getValue() {
        return this;
    }

    public AcpiInteger getByte(int index) {
        return new AcpiInteger(buffer[index] & 0xff);
    }
}
