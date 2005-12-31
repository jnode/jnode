/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.system.acpi.vm;

/**
 * ByteField.
 * 
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ByteField  extends AcpiNamedObject{
  Buffer sourceBuffer;
  int byteIndex;

  public ByteField(String name, Buffer sourceBuffer, int byteIndex) {
    super(name);
    this.sourceBuffer=sourceBuffer;
    this.byteIndex=byteIndex;
    sourceBuffer.putInSameNameSpace(this);
  }

  public AcpiInteger getValue() {
    return sourceBuffer.getByte(byteIndex);
  }
}
