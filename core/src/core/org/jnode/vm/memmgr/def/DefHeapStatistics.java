/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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

package org.jnode.vm.memmgr.def;

import org.jnode.util.Counter;
import org.jnode.vm.memmgr.HeapStatistics;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */

final class DefHeapStatistics extends HeapStatistics
{
  private TreeMap countData = new TreeMap();

  public void add(String className)
  {
    Counter count = (Counter) countData.get(className);

    if (count == null)
    {
      count = new Counter(className);

      countData.put(className, count);
    }

    count.inc();
  }


  public String toString()
  {
    Collection counted = countData.values();

    StringBuffer stringBuffer = new StringBuffer();
    final String newline = "\n";

    Object o;

    for (Iterator iterator = counted.iterator(); iterator.hasNext();)
    {
      o = iterator.next();
      stringBuffer.append(o);

      if (iterator.hasNext())
        stringBuffer.append(newline);
    }

    return stringBuffer.toString();
  }

}
