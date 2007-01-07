/* java.lang.ref.Reference
   Copyright (C) 1999, 2002, 2003, 2004, 2006 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.lang.ref;

/**
 * This is the base class of all references.  A reference allows
 * refering to an object without preventing the garbage collector to
 * collect it.  The only way to get the referred object is via the
 * <code>get()</code>-method.  This method will return
 * <code>null</code> if the object was collected. <br>
 *
 * A reference may be registered with a queue.  When a referred
 * element gets collected the reference will be put on the queue, so
 * that you will be notified. <br>
 *
 * There are currently three types of references:  soft reference,
 * weak reference and phantom reference. <br>
 *
 * Soft references will be cleared if the garbage collector is told
 * to free some memory and there are no unreferenced or weakly referenced
 * objects.  It is useful for caches. <br>
 *
 * Weak references will be cleared as soon as the garbage collector
 * determines that the refered object is only weakly reachable.  They
 * are useful as keys in hashtables (see <code>WeakHashtable</code>) as
 * you get notified when nobody has the key anymore.
 *
 * Phantom references don't prevent finalization.  If an object is only
 * phantom reachable, it will be finalized, and the reference will be
 * enqueued, but not cleared.  Since you mustn't access an finalized
 * object, the <code>get</code> method of a phantom reference will never
 * work.  It is useful to keep track, when an object is finalized.
 *
 * @author Jochen Hoenicke
 * @see java.util.WeakHashMap
 */
public abstract class Reference<T>
{
  /**
   * The underlying object.  This field is handled in a special way by
   * the garbage collector.
   */
  T referent;

  /**
   * The queue this reference is registered on. This is null, if this
   * wasn't registered to any queue or reference was already enqueued.
   */
  volatile ReferenceQueue<? super T> queue;

  /**
   * Link to the next entry on the queue.  If this is null, this
   * reference is not enqueued.  Otherwise it points to the next
   * reference.  The last reference on a queue will point to itself
   * (not to null, that value is used to mark a not enqueued
   * reference).  
   */
  volatile Reference nextOnQueue;

  /**
   * This lock should be taken by the garbage collector, before
   * determining reachability.  It will prevent the get()-method to
   * return the reference so that reachability doesn't change.
   */
  static Object lock = new Object();

  /**
   * Creates a new reference that is not registered to any queue.
   * Since it is package private, it is not possible to overload this
   * class in a different package.  
   * @param ref the object we refer to.
   */
  Reference(T ref)
  {
    referent = ref;
  }

  /**
   * Creates a reference that is registered to a queue.  Since this is
   * package private, it is not possible to overload this class in a
   * different package.  
   * @param ref the object we refer to.
   * @param q the reference queue to register on.
   * @exception NullPointerException if q is null.
   */
  Reference(T ref, ReferenceQueue<? super T> q)
  {
    if (q == null)
      throw new NullPointerException();
    referent = ref;
    queue = q;
  }

  /**
   * Returns the object, this reference refers to.
   * @return the object, this reference refers to, or null if the 
   * reference was cleared.
   */
  public T get()
  {
    synchronized (lock)
      {
	return referent;
      }
  }

  /**
   * Clears the reference, so that it doesn't refer to its object
   * anymore.  For soft and weak references this is called by the
   * garbage collector.  For phantom references you should call 
   * this when enqueuing the reference.
   */
  public void clear()
  {
    referent = null;
  }

  /**
   * Tells if the object is enqueued on a reference queue.
   * @return true if it is enqueued, false otherwise.
   */
  public boolean isEnqueued()
  {
    return nextOnQueue != null;
  }

  /**
   * Enqueue an object on a reference queue.  This is normally executed
   * by the garbage collector.
   */
  public boolean enqueue() 
  {
    ReferenceQueue q = queue;
    if (q != null)
      {
	return q.enqueue(this);
      }
    return false;
  }
}
