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

import java.util.TreeMap;
import java.util.Collection;
import java.util.Iterator;

import org.jnode.util.Counter;
import org.jnode.vm.memmgr.HeapStatistics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmNormalClass;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */

final class DefHeapStatistics extends HeapStatistics
{
  private TreeMap<String, HeapCounter> countData = new TreeMap<String, HeapCounter>();
  private static final char newline = '\n';


  public boolean contains(String classname)
  {
    return countData.containsKey(classname);
  }

  public void add(String className, int size)
  {
    HeapCounter count = (HeapCounter) countData.get(className);

    if (count == null)
    {
      count = new HeapCounter(className, size);
      countData.put(className, count);
    }

    count.inc();

    count = null;
  }

  public String toString()
  {
    final StringBuilder stringBuilder = new StringBuilder();
    boolean first = true;

    for (HeapCounter c : countData.values())
    {
      if (first)
      {
        first = false;
      }
      else
      {
        stringBuilder.append(newline);
      }
      stringBuilder.append(c.toStringBuilder());
    }

    return stringBuilder.toString();
  }

  final class HeapCounter
  {
    private Counter counter;
    private int objectSize = 0;
    private final static String usage = " memory usage=";

    public HeapCounter(String objectName, int objectSize)
    {
      this.objectSize = objectSize;
      counter = new Counter(objectName);
    }

    public void inc()
    {
      counter.inc();
    }

    public int getObjectSize()
    {
      return objectSize;
    }


    public StringBuilder toStringBuilder()
    {
      StringBuilder stringBuilder = new StringBuilder(counter.toString());

      if (objectSize != 0)
      {
        stringBuilder.append(usage);
        stringBuilder.append(objectSize * counter.get());
      }

      return stringBuilder;
    }
  }
}
