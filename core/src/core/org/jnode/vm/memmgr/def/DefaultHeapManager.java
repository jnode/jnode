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

import org.jnode.vm.*;
import org.jnode.vm.classmgr.*;
import org.jnode.vm.memmgr.*;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;

import java.io.PrintStream;

public final class DefaultHeapManager extends VmHeapManager
{

  /**
   * Default size in bytes of a new heap
   */
  public static final int DEFAULT_HEAP_SIZE = 4 * 1024 * 1024;

  /**
   * When this percentage of the free memory has been allocated, a GC is
   * triggered (0..1.0)
   */
  public static float GC_TRIGGER_PERCENTAGE = 0.75f;

  /**
   * The GC thread
   */
  private GCThread gcThread;

  /**
   * The finalizer thread
   */
  private FinalizerThread finalizerThread;

  /**
   * Monitor to synchronize heap access
   */
  private Monitor heapMonitor;

  /**
   * Are we low on memory
   */
  private boolean lowOnMemory;

  /**
   * The used write barrier
   */
  private final VmWriteBarrier writeBarrier;

  /**
   * Linked list of all heaps.
   */
  private VmAbstractHeap heapList;

  /**
   * The first heap.
   */
  private final VmAbstractHeap firstNormalHeap;

  /**
   * The heap currently used for allocation
   */
  private VmAbstractHeap currentHeap;

  /**
   * The heap used for allocations during a GC
   */
  private VmAbstractHeap gcHeap;

  /**
   * The class of the default heap type. Set by initialize
   */
  private final VmNormalClass defaultHeapClass;

  /**
   * The number of allocated bytes since the last GC trigger
   */
  private int allocatedSinceGcTrigger;

  private int triggerSize = Integer.MAX_VALUE;

  private boolean gcActive;

  private GCManager gcManager;

  /**
   * Make this private, so we cannot be instantiated
   */
  public DefaultHeapManager(VmClassLoader loader, HeapHelper helper) throws ClassNotFoundException
  {
    super(helper);
    //this.writeBarrier = new DefaultWriteBarrier(helper);
    this.writeBarrier = null;
    this.firstNormalHeap = new VmDefaultHeap(this);
    this.currentHeap = firstNormalHeap;
    this.heapList = firstNormalHeap;
    this.defaultHeapClass = (VmNormalClass) loader.loadClass(VmDefaultHeap.class.getName(), true);
  }

  /**
   * Is the given address the address of an allocated object on this heap?
   *
   * @param ptr The address to examine.
   * @return True if the given address if a valid starting address of an
   *         object, false otherwise.
   */
  public final boolean isObject(Address ptr)
  {
    long addrL = ptr.toLong();
    if ((addrL & (ObjectLayout.OBJECT_ALIGN - 1)) != 0)
    {
      // The object is not at an object aligned boundary
      return false;
    }
    if (bootHeap.isObject(ptr))
    {
      return true;
    }
    VmAbstractHeap heap = heapList;
    while (heap != null)
    {
      if (heap.isObject(ptr))
      {
        return true;
      }
      heap = heap.getNext();
    }
    return false;
  }

  /**
   * Is the system low on memory?
   *
   * @return boolean
   */
  public boolean isLowOnMemory()
  {
    return lowOnMemory;
  }

  /**
   * Start a garbage collection process
   */
  public final void gc()
  {
    gcThread.trigger(false);
  }

  /**
   * Gets the size of free memory in bytes.
   *
   * @return long
   */
  public long getFreeMemory()
  {
    Extent size = bootHeap.getFreeSize();
    VmAbstractHeap h = firstNormalHeap;
    while (h != null)
    {
      size = size.add(h.getFreeSize());
      h = h.getNext();
    }
    //size += (Unsafe.addressToLong(heapEnd) -
    // Unsafe.addressToLong(nextHeapPtr));
    size = size.add(Extent.fromLong(MemoryBlockManager.getFreeMemory()));
    return size.toLong();
  }

  /**
   * Gets the size of all memory in bytes.
   *
   * @return the size of all memory in bytes
   */
  public long getTotalMemory()
  {
    long size = bootHeap.getSize();
    VmAbstractHeap h = firstNormalHeap;
    while (h != null)
    {
      size += h.getSize();
      h = h.getNext();
    }
    //size += (Unsafe.addressToLong(heapEnd) -
    // Unsafe.addressToLong(nextHeapPtr));
    size += MemoryBlockManager.getFreeMemory();
    return size;
  }

  /**
   * Gets the first heap. All other heaps can be iterated through the
   * <code>getNext()</code> method.
   *
   * @return the first heap
   */
  public final VmAbstractHeap getHeapList()
  {
    return heapList;
  }

  // ------------------------------------------
  // Private natives
  // ------------------------------------------

  protected void initialize()
  {
    // Set the basic fields
    final VmArchitecture arch = Unsafe.getCurrentProcessor()
        .getArchitecture();
    final int slotSize = arch.getReferenceSize();

    // Initialize the boot heap.
    bootHeap.initialize(helper.getBootHeapStart(), helper.getBootHeapEnd(),
        slotSize);

    // Initialize the first normal heap
    final Address ptr = helper.allocateBlock(Extent.fromIntZeroExtend(DEFAULT_HEAP_SIZE));
    firstNormalHeap.initialize(ptr, ptr.add(DEFAULT_HEAP_SIZE),
        slotSize);

    // Initialize the GC heap
    gcHeap = allocHeap(Extent.fromIntZeroExtend(DEFAULT_HEAP_SIZE), false);
    gcHeap.append(firstNormalHeap);

    // Initialize the total heap list.
    heapList = gcHeap;
  }

  public void start()
  {
    // Create a Heap monitor
    heapMonitor = new Monitor();
    final VmArchitecture arch = Unsafe.getCurrentProcessor()
        .getArchitecture();
    this.gcManager = new GCManager(this, arch);
    this.gcThread = new GCThread(gcManager, heapMonitor);
    this.finalizerThread = new FinalizerThread(this);
    gcThread.start();
    finalizerThread.start();
    // Calculate the trigger size
    triggerSize = (int) Math.min(Integer.MAX_VALUE, getFreeMemory()
        * GC_TRIGGER_PERCENTAGE);
  }

  /**
   * Gets the write barrier used by this heap manager (if any).
   *
   * @return The write barrier, or null if no write barrier is used.
   */
  public final VmWriteBarrier getWriteBarrier()
  {
    return writeBarrier;
  }

  /**
   * Allocate a new instance for the given class. Not that this method cannot
   * be synchronized, since obtaining a monitor might require creating one,
   * which in turn needs this method.
   *
   * @param vmClass
   * @param size
   * @return Object
   */
  protected Object allocObject(VmClassType vmClass, int size)
  {
    // Make sure the class is initialized
    vmClass.initialize();

    final int alignedSize = ObjectLayout.objectAlign(size);
    //final Monitor mon = heapMonitor;

    VmAbstractHeap heap = currentHeap;
    Object result = null;
    int oomCount = 0;

    final Monitor m = heapMonitor;
    //final Monitor m = null;
    if (m != null)
    {
      m.enter();
    }
    try
    {
      if (gcActive)
      {
        Unsafe.debug("Using GC Heap type");
        Unsafe.debug(vmClass.getName());
        result = gcHeap.alloc(vmClass, alignedSize);
        if (result == null)
        {
          helper.die("Out of GC heap memory.");
        }
      }
      else
      {
        while (result == null)
        {
          // The current heap is full
          if (heap == null)
          {
            //Unsafe.debug("allocHeap in allocObject(");
            // Unsafe.debug(alignedSize);
            //Unsafe.debug(") ");
            int newHeapSize = DEFAULT_HEAP_SIZE;
            if (size > newHeapSize)
            {
              // this is a BIG object, try to allocate a new
              // heap
              // only for it
              newHeapSize = size;
            }
            if ((heap = allocHeap(Extent.fromIntZeroExtend(newHeapSize), true)) == null)
            {
              lowOnMemory = true;
              // It was not possible to allocate another heap.
              // First try to GC, if we've done that before
              // in this allocation, then we're in real panic.
              if (oomCount == 0)
              {
                oomCount++;
                Unsafe.debug("<oom/>");
                gcThread.trigger(true);
                heap = firstNormalHeap;
                currentHeap = firstNormalHeap;
              }
              else
              {
                Unsafe.debug("Out of memory in allocObject(");
                Unsafe.debug(size);
                Unsafe.debug(")");
                throw OOME;
                //Unsafe.die();
              }
            }
            else
            {
              //Unsafe.debug("AO.G");
              // We successfully allocated a new heap, set it
              // to current, so we'll use it for the following
              // allocations.
              currentHeap = heap;
            }
          }

          result = heap.alloc(vmClass, alignedSize);

          if (result == null)
          {
            heap = heap.getNext();
          }
        }
        lowOnMemory = false;

        allocatedSinceGcTrigger += alignedSize;
        if ((allocatedSinceGcTrigger > triggerSize)
            && (gcThread != null))
        {
          Unsafe.debug("<alloc:GC trigger/>");
          allocatedSinceGcTrigger = 0;
          gcThread.trigger(false);
        }
      }
      vmClass.incInstanceCount();
      // Allocated objects are initially black.
      VmMagic.setObjectFlags(result, Word.fromIntZeroExtend(ObjectFlags.GC_DEFAULT_COLOR));
    }
    finally
    {
      if (m != null)
      {
        m.exit();
      }
    }

    return result;
  }

  /**
   * Allocate a new heap with a given size. The heap object itself is
   * allocated on the new heap, so this method can be called even if all other
   * heaps are full.
   *
   * @param size
   * @return The heap
   */
  private VmAbstractHeap allocHeap(Extent size, boolean addToHeapList)
  {
    //Unsafe.debug("allocHeap");
    final Address start = helper.allocateBlock(size);
    //final Address start = MemoryBlockManager.allocateBlock(size);
    if (start == null)
    {
      return null;
    }
    final Address end = start.add(size);
    final int slotSize = Unsafe.getCurrentProcessor().getArchitecture()
        .getReferenceSize();
    final VmAbstractHeap heap = VmDefaultHeap.setupHeap(helper, start,
        defaultHeapClass, slotSize);
    heap.initialize(start, end, slotSize);

    if (addToHeapList)
    {
      heapList.append(heap);
    }
    return heap;
  }

  /**
   * Print the statics on this object on out.
   */
  public void dumpStatistics(PrintStream out)
  {
    out.println("WriteBarrier: " + writeBarrier);
  }

  /**
   * @return Returns the bootHeap.
   */
  final VmBootHeap getBootHeap()
  {
    return this.bootHeap;
  }

  /**
   * @param gcActive The gcActive to set.
   */
  final void setGcActive(boolean gcActive)
  {
    this.gcActive = gcActive;
  }

  /**
   * Sets the currentHeap to the first heap.
   */
  final void resetCurrentHeap()
  {
    this.currentHeap = this.firstNormalHeap;
    // Recalculate the trigger size
    triggerSize = (int) Math.min(Integer.MAX_VALUE, getFreeMemory()
        * GC_TRIGGER_PERCENTAGE);
  }

  /**
   * Sets the currentHeap to the first heap.
   */
  final void triggerFinalization()
  {
    finalizerThread.trigger(false);
  }

  public GCStatistics getStatistics()
  {
    return gcManager.getStatistics();
  }

  public HeapStatistics getHeapStatistics()
  {
    HeapStatistics heapStatistics = new DefHeapStatistics();
    HeapStatisticsVisitor heapStatisticsVisitor = new HeapStatisticsVisitor(heapStatistics);

    VmAbstractHeap heap = firstNormalHeap;
    final Word zero = Word.zero();

    while (heap != null)
    {
      heap.walk(heapStatisticsVisitor, false, zero, zero);
      heap = heap.getNext();
    }


    return heapStatistics;
  }


}
