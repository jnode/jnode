/* LinkedList.java -- Linked list implementation of the List interface
   Copyright (C) 1998, 1999, 2000, 2001, 2002, 2004, 2005, 2006  Free Software Foundation, Inc.

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


package java.util;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * Linked list implementation of the List interface. In addition to the
 * methods of the List interface, this class provides access to the first
 * and last list elements in O(1) time for easy stack, queue, or double-ended
 * queue (deque) creation. The list is doubly-linked, with traversal to a
 * given index starting from the end closest to the element.<p>
 *
 * LinkedList is not synchronized, so if you need multi-threaded access,
 * consider using:<br>
 * <code>List l = Collections.synchronizedList(new LinkedList(...));</code>
 * <p>
 *
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a
 * {@link ConcurrentModificationException} rather than exhibit
 * non-deterministic behavior.
 *
 * @author Original author unknown
 * @author Bryce McKinlay
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Tom Tromey (tromey@redhat.com)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 * @see List
 * @see ArrayList
 * @see Vector
 * @see Collections#synchronizedList(List)
 * @since 1.2
 * @status Complete to 1.6
 */
public class LinkedList<T> extends AbstractSequentialList<T>
  implements List<T>, Deque<T>, Cloneable, Serializable
{
  /**
   * Compatible with JDK 1.2.
   */
  private static final long serialVersionUID = 876323262645176354L;

  /**
   * The first element in the list.
   */
  transient Entry<T> first;

  /**
   * The last element in the list.
   */
  transient Entry<T> last;

  /**
   * The current length of the list.
   */
  transient int size = 0;

  /**
   * Class to represent an entry in the list. Holds a single element.
   */
  private static final class Entry<T>
  {
    /** The element in the list. */
    T data;

    /** The next list entry, null if this is last. */
    Entry<T> next;

    /** The previous list entry, null if this is first. */
    Entry<T> previous;

    /**
     * Construct an entry.
     * @param data the list element
     */
    Entry(T data)
    {
      this.data = data;
    }
  } // class Entry

  /**
   * Obtain the Entry at a given position in a list. This method of course
   * takes linear time, but it is intelligent enough to take the shorter of the
   * paths to get to the Entry required. This implies that the first or last
   * entry in the list is obtained in constant time, which is a very desirable
   * property.
   * For speed and flexibility, range checking is not done in this method:
   * Incorrect values will be returned if (n &lt; 0) or (n &gt;= size).
   *
   * @param n the number of the entry to get
   * @return the entry at position n
   */
  // Package visible for use in nested classes.
  Entry<T> getEntry(int n)
  {
    Entry<T> e;
    if (n < size / 2)
      {
        e = first;
        // n less than size/2, iterate from start
        while (n-- > 0)
          e = e.next;
      }
    else
      {
        e = last;
        // n greater than size/2, iterate from end
        while (++n < size)
          e = e.previous;
      }
    return e;
  }

  /**
   * Remove an entry from the list. This will adjust size and deal with
   *  `first' and  `last' appropriatly.
   *
   * @param e the entry to remove
   */
  // Package visible for use in nested classes.
  void removeEntry(Entry<T> e)
  {
    modCount++;
    size--;
    if (size == 0)
      first = last = null;
    else
      {
        if (e == first)
          {
            first = e.next;
            e.next.previous = null;
          }
        else if (e == last)
          {
            last = e.previous;
            e.previous.next = null;
          }
        else
          {
            e.next.previous = e.previous;
            e.previous.next = e.next;
          }
      }
  }

  /**
   * Checks that the index is in the range of possible elements (inclusive).
   *
   * @param index the index to check
   * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size
   */
  private void checkBoundsInclusive(int index)
  {
    if (index < 0 || index > size)
      throw new IndexOutOfBoundsException("Index: " + index + ", Size:"
                                          + size);
  }

  /**
   * Checks that the index is in the range of existing elements (exclusive).
   *
   * @param index the index to check
   * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size
   */
  private void checkBoundsExclusive(int index)
  {
    if (index < 0 || index >= size)
      throw new IndexOutOfBoundsException("Index: " + index + ", Size:"
                                          + size);
  }

  /**
   * Create an empty linked list.
   */
  public LinkedList()
  {
  }

  /**
   * Create a linked list containing the elements, in order, of a given
   * collection.
   *
   * @param c the collection to populate this list from
   * @throws NullPointerException if c is null
   */
  public LinkedList(Collection<? extends T> c)
  {
    addAll(c);
  }

  /**
   * Returns the first element in the list.
   *
   * @return the first list element
   * @throws NoSuchElementException if the list is empty
   */
  public T getFirst()
  {
    if (size == 0)
      throw new NoSuchElementException();
    return first.data;
  }

  /**
   * Returns the last element in the list.
   *
   * @return the last list element
   * @throws NoSuchElementException if the list is empty
   */
  public T getLast()
  {
    if (size == 0)
      throw new NoSuchElementException();
    return last.data;
  }

  /**
   * Remove and return the first element in the list.
   *
   * @return the former first element in the list
   * @throws NoSuchElementException if the list is empty
   */
  public T removeFirst()
  {
    if (size == 0)
      throw new NoSuchElementException();
    modCount++;
    size--;
    T r = first.data;

    if (first.next != null)
      first.next.previous = null;
    else
      last = null;

    first = first.next;

    return r;
  }

  /**
   * Remove and return the last element in the list.
   *
   * @return the former last element in the list
   * @throws NoSuchElementException if the list is empty
   */
  public T removeLast()
  {
    if (size == 0)
      throw new NoSuchElementException();
    modCount++;
    size--;
    T r = last.data;

    if (last.previous != null)
      last.previous.next = null;
    else
      first = null;

    last = last.previous;

    return r;
  }

  /**
   * Insert an element at the first of the list.
   *
   * @param o the element to insert
   */
  public void addFirst(T o)
  {
    Entry<T> e = new Entry(o);

    modCount++;
    if (size == 0)
      first = last = e;
    else
      {
        e.next = first;
        first.previous = e;
        first = e;
      }
    size++;
  }

  /**
   * Insert an element at the last of the list.
   *
   * @param o the element to insert
   */
  public void addLast(T o)
  {
    addLastEntry(new Entry<T>(o));
  }

  /**
   * Inserts an element at the end of the list.
   *
   * @param e the entry to add
   */
  private void addLastEntry(Entry<T> e)
  {
    modCount++;
    if (size == 0)
      first = last = e;
    else
      {
        e.previous = last;
        last.next = e;
        last = e;
      }
    size++;
  }

  /**
   * Returns true if the list contains the given object. Comparison is done by
   * <code>o == null ? e = null : o.equals(e)</code>.
   *
   * @param o the element to look for
   * @return true if it is found
   */
  public boolean contains(Object o)
  {
    Entry<T> e = first;
    while (e != null)
      {
        if (equals(o, e.data))
          return true;
        e = e.next;
      }
    return false;
  }

  /**
   * Returns the size of the list.
   *
   * @return the list size
   */
  public int size()
  {
    return size;
  }

  /**
   * Adds an element to the end of the list.
   *
   * @param o the entry to add
   * @return true, as it always succeeds
   */
  public boolean add(T o)
  {
    addLastEntry(new Entry<T>(o));
    return true;
  }

  /**
   * Removes the entry at the lowest index in the list that matches the given
   * object, comparing by <code>o == null ? e = null : o.equals(e)</code>.
   *
   * @param o the object to remove
   * @return true if an instance of the object was removed
   */
  public boolean remove(Object o)
  {
    Entry<T> e = first;
    while (e != null)
      {
        if (equals(o, e.data))
          {
            removeEntry(e);
            return true;
          }
        e = e.next;
      }
    return false;
  }

  /**
   * Append the elements of the collection in iteration order to the end of
   * this list. If this list is modified externally (for example, if this
   * list is the collection), behavior is unspecified.
   *
   * @param c the collection to append
   * @return true if the list was modified
   * @throws NullPointerException if c is null
   */
  public boolean addAll(Collection<? extends T> c)
  {
    return addAll(size, c);
  }

  /**
   * Insert the elements of the collection in iteration order at the given
   * index of this list. If this list is modified externally (for example,
   * if this list is the collection), behavior is unspecified.
   *
   * @param c the collection to append
   * @return true if the list was modified
   * @throws NullPointerException if c is null
   * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
   */
  public boolean addAll(int index, Collection<? extends T> c)
  {
    checkBoundsInclusive(index);
    int csize = c.size();

    if (csize == 0)
      return false;

    Iterator<? extends T> itr = c.iterator();

    // Get the entries just before and after index. If index is at the start
    // of the list, BEFORE is null. If index is at the end of the list, AFTER
    // is null. If the list is empty, both are null.
    Entry<T> after = null;
    Entry<T> before = null;
    if (index != size)
      {
        after = getEntry(index);
        before = after.previous;
      }
    else
      before = last;

    // Create the first new entry. We do not yet set the link from `before'
    // to the first entry, in order to deal with the case where (c == this).
    // [Actually, we don't have to handle this case to fufill the
    // contract for addAll(), but Sun's implementation appears to.]
    Entry<T> e = new Entry<T>(itr.next());
    e.previous = before;
    Entry<T> prev = e;
    Entry<T> firstNew = e;

    // Create and link all the remaining entries.
    for (int pos = 1; pos < csize; pos++)
      {
        e = new Entry<T>(itr.next());
        e.previous = prev;
        prev.next = e;
        prev = e;
      }

    // Link the new chain of entries into the list.
    modCount++;
    size += csize;
    prev.next = after;
    if (after != null)
      after.previous = e;
    else
      last = e;

    if (before != null)
      before.next = firstNew;
    else
      first = firstNew;
    return true;
  }

  /**
   * Remove all elements from this list.
   */
  public void clear()
  {
    if (size > 0)
      {
        modCount++;
        first = null;
        last = null;
        size = 0;
      }
  }

  /**
   * Return the element at index.
   *
   * @param index the place to look
   * @return the element at index
   * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
   */
  public T get(int index)
  {
    checkBoundsExclusive(index);
    return getEntry(index).data;
  }

  /**
   * Replace the element at the given location in the list.
   *
   * @param index which index to change
   * @param o the new element
   * @return the prior element
   * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
   */
  public T set(int index, T o)
  {
    checkBoundsExclusive(index);
    Entry<T> e = getEntry(index);
    T old = e.data;
    e.data = o;
    return old;
  }

  /**
   * Inserts an element in the given position in the list.
   *
   * @param index where to insert the element
   * @param o the element to insert
   * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
   */
  public void add(int index, T o)
  {
    checkBoundsInclusive(index);
    Entry<T> e = new Entry<T>(o);

    if (index < size)
      {
        modCount++;
        Entry<T> after = getEntry(index);
        e.next = after;
        e.previous = after.previous;
        if (after.previous == null)
          first = e;
        else
          after.previous.next = e;
        after.previous = e;
        size++;
      }
    else
      addLastEntry(e);
  }

  /**
   * Removes the element at the given position from the list.
   *
   * @param index the location of the element to remove
   * @return the removed element
   * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
   */
  public T remove(int index)
  {
    checkBoundsExclusive(index);
    Entry<T> e = getEntry(index);
    removeEntry(e);
    return e.data;
  }

  /**
   * Returns the first index where the element is located in the list, or -1.
   *
   * @param o the element to look for
   * @return its position, or -1 if not found
   */
  public int indexOf(Object o)
  {
    int index = 0;
    Entry<T> e = first;
    while (e != null)
      {
        if (equals(o, e.data))
          return index;
        index++;
        e = e.next;
      }
    return -1;
  }

  /**
   * Returns the last index where the element is located in the list, or -1.
   *
   * @param o the element to look for
   * @return its position, or -1 if not found
   */
  public int lastIndexOf(Object o)
  {
    int index = size - 1;
    Entry<T> e = last;
    while (e != null)
      {
        if (equals(o, e.data))
          return index;
        index--;
        e = e.previous;
      }
    return -1;
  }

  /**
   * Obtain a ListIterator over this list, starting at a given index. The
   * ListIterator returned by this method supports the add, remove and set
   * methods.
   *
   * @param index the index of the element to be returned by the first call to
   *        next(), or size() to be initially positioned at the end of the list
   * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
   */
  public ListIterator<T> listIterator(int index)
  {
    checkBoundsInclusive(index);
    return new LinkedListItr<T>(index);
  }

  /**
   * Create a shallow copy of this LinkedList (the elements are not cloned).
   *
   * @return an object of the same class as this object, containing the
   *         same elements in the same order
   */
  public Object clone()
  {
    LinkedList<T> copy = null;
    try
      {
        copy = (LinkedList<T>) super.clone();
      }
    catch (CloneNotSupportedException ex)
      {
      }
    copy.clear();
    copy.addAll(this);
    return copy;
  }

  /**
   * Returns an array which contains the elements of the list in order.
   *
   * @return an array containing the list elements
   */
  public Object[] toArray()
  {
    Object[] array = new Object[size];
    Entry<T> e = first;
    for (int i = 0; i < size; i++)
      {
        array[i] = e.data;
        e = e.next;
      }
    return array;
  }

  /**
   * Returns an Array whose component type is the runtime component type of
   * the passed-in Array.  The returned Array is populated with all of the
   * elements in this LinkedList.  If the passed-in Array is not large enough
   * to store all of the elements in this List, a new Array will be created 
   * and returned; if the passed-in Array is <i>larger</i> than the size
   * of this List, then size() index will be set to null.
   *
   * @param a the passed-in Array
   * @return an array representation of this list
   * @throws ArrayStoreException if the runtime type of a does not allow
   *         an element in this list
   * @throws NullPointerException if a is null
   */
  public <S> S[] toArray(S[] a)
  {
    if (a.length < size)
      a = (S[]) Array.newInstance(a.getClass().getComponentType(), size);
    else if (a.length > size)
      a[size] = null;
    Entry<T> e = first;
    for (int i = 0; i < size; i++)
      {
        a[i] = (S) e.data;
        e = e.next;
      }
    return a;
  }

  /**
   * Adds the specified element to the end of the list.
   *
   * @param value the value to add.
   * @return true.
   * @since 1.5
   */
  public boolean offer(T value)
  {
    return add(value);
  }

  /**
   * Returns the first element of the list without removing
   * it.
   *
   * @return the first element of the list.
   * @throws NoSuchElementException if the list is empty.
   * @since 1.5
   */
  public T element()
  {
    return getFirst();
  }

  /**
   * Returns the first element of the list without removing
   * it.
   *
   * @return the first element of the list, or <code>null</code>
   *         if the list is empty.
   * @since 1.5
   */
  public T peek()
  {
    if (size == 0)
      return null;
    return getFirst();
  }

  /**
   * Removes and returns the first element of the list.
   *
   * @return the first element of the list, or <code>null</code>
   *         if the list is empty.
   * @since 1.5
   */
  public T poll()
  {
    if (size == 0)
      return null;
    return removeFirst();
  }

  /**
   * Removes and returns the first element of the list.
   *
   * @return the first element of the list.
   * @throws NoSuchElementException if the list is empty.
   * @since 1.5
   */
  public T remove()
  {
    return removeFirst();
  }

  /**
   * Serializes this object to the given stream.
   *
   * @param s the stream to write to
   * @throws IOException if the underlying stream fails
   * @serialData the size of the list (int), followed by all the elements
   *             (Object) in proper order
   */
  private void writeObject(ObjectOutputStream s) throws IOException
  {
    s.defaultWriteObject();
    s.writeInt(size);
    Entry<T> e = first;
    while (e != null)
      {
        s.writeObject(e.data);
        e = e.next;
      }
  }

  /**
   * Deserializes this object from the given stream.
   *
   * @param s the stream to read from
   * @throws ClassNotFoundException if the underlying stream fails
   * @throws IOException if the underlying stream fails
   * @serialData the size of the list (int), followed by all the elements
   *             (Object) in proper order
   */
  private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
  {
    s.defaultReadObject();
    int i = s.readInt();
    while (--i >= 0)
      addLastEntry(new Entry<T>((T) s.readObject()));
  }

  /**
   * A ListIterator over the list. This class keeps track of its
   * position in the list and the two list entries it is between.
   *
   * @author Original author unknown
   * @author Eric Blake (ebb9@email.byu.edu)
   */
  private final class LinkedListItr<I>
    implements ListIterator<I>
  {
    /** Number of modifications we know about. */
    private int knownMod = modCount;

    /** Entry that will be returned by next(). */
    private Entry<I> next;

    /** Entry that will be returned by previous(). */
    private Entry<I> previous;

    /** Entry that will be affected by remove() or set(). */
    private Entry<I> lastReturned;

    /** Index of `next'. */
    private int position;

    /**
     * Initialize the iterator.
     *
     * @param index the initial index
     */
    LinkedListItr(int index)
    {
      if (index == size)
        {
          next = null;
          previous = (Entry<I>) last;
        }
      else
        {
          next = (Entry<I>) getEntry(index);
          previous = next.previous;
        }
      position = index;
    }

    /**
     * Checks for iterator consistency.
     *
     * @throws ConcurrentModificationException if the list was modified
     */
    private void checkMod()
    {
      if (knownMod != modCount)
        throw new ConcurrentModificationException();
    }

    /**
     * Returns the index of the next element.
     *
     * @return the next index
     */
    public int nextIndex()
    {
      return position;
    }

    /**
     * Returns the index of the previous element.
     *
     * @return the previous index
     */
    public int previousIndex()
    {
      return position - 1;
    }

    /**
     * Returns true if more elements exist via next.
     *
     * @return true if next will succeed
     */
    public boolean hasNext()
    {
      return (next != null);
    }

    /**
     * Returns true if more elements exist via previous.
     *
     * @return true if previous will succeed
     */
    public boolean hasPrevious()
    {
      return (previous != null);
    }

    /**
     * Returns the next element.
     *
     * @return the next element
     * @throws ConcurrentModificationException if the list was modified
     * @throws NoSuchElementException if there is no next
     */
    public I next()
    {
      checkMod();
      if (next == null)
        throw new NoSuchElementException();
      position++;
      lastReturned = previous = next;
      next = lastReturned.next;
      return lastReturned.data;
    }

    /**
     * Returns the previous element.
     *
     * @return the previous element
     * @throws ConcurrentModificationException if the list was modified
     * @throws NoSuchElementException if there is no previous
     */
    public I previous()
    {
      checkMod();
      if (previous == null)
        throw new NoSuchElementException();
      position--;
      lastReturned = next = previous;
      previous = lastReturned.previous;
      return lastReturned.data;
    }

    /**
     * Remove the most recently returned element from the list.
     *
     * @throws ConcurrentModificationException if the list was modified
     * @throws IllegalStateException if there was no last element
     */
    public void remove()
    {
      checkMod();
      if (lastReturned == null)
        throw new IllegalStateException();

      // Adjust the position to before the removed element, if the element
      // being removed is behind the cursor.
      if (lastReturned == previous)
        position--;

      next = lastReturned.next;
      previous = lastReturned.previous;
      removeEntry((Entry<T>) lastReturned);
      knownMod++;

      lastReturned = null;
    }

    /**
     * Adds an element between the previous and next, and advance to the next.
     *
     * @param o the element to add
     * @throws ConcurrentModificationException if the list was modified
     */
    public void add(I o)
    {
      checkMod();
      modCount++;
      knownMod++;
      size++;
      position++;
      Entry<I> e = new Entry<I>(o);
      e.previous = previous;
      e.next = next;

      if (previous != null)
        previous.next = e;
      else
        first = (Entry<T>) e;

      if (next != null)
        next.previous = e;
      else
        last = (Entry<T>) e;

      previous = e;
      lastReturned = null;
    }

    /**
     * Changes the contents of the element most recently returned.
     *
     * @param o the new element
     * @throws ConcurrentModificationException if the list was modified
     * @throws IllegalStateException if there was no last element
     */
    public void set(I o)
    {
      checkMod();
      if (lastReturned == null)
        throw new IllegalStateException();
      lastReturned.data = o;
    }
  } // class LinkedListItr

  /**
   * Obtain an Iterator over this list in reverse sequential order.
   *
   * @return an Iterator over the elements of the list in
   *         reverse order.
   * @since 1.6
   */
  public Iterator<T> descendingIterator()
  {
    return new Iterator<T>()
    {
      /** Number of modifications we know about. */
      private int knownMod = modCount;

      /** Entry that will be returned by next(). */
      private Entry<T> next = last;

      /** Entry that will be affected by remove() or set(). */
      private Entry<T> lastReturned;

      /** Index of `next'. */
      private int position = size() - 1;

      // This will get inlined, since it is private.
      /**
       * Checks for modifications made to the list from
       * elsewhere while iteration is in progress.
       *
       * @throws ConcurrentModificationException if the
       *         list has been modified elsewhere.
       */
      private void checkMod()
      {
        if (knownMod != modCount)
          throw new ConcurrentModificationException();
      }

      /**
       * Tests to see if there are any more objects to
       * return.
       *
       * @return true if the start of the list has not yet been
       *         reached.
       */
      public boolean hasNext()
      {
        return next != null;
      }

      /**
       * Retrieves the next object from the list.
       *
       * @return The next object.
       * @throws NoSuchElementException if there are
       *         no more objects to retrieve.
       * @throws ConcurrentModificationException if the
       *         list has been modified elsewhere.
       */
      public T next()
      {
        checkMod();
        if (next == null)
          throw new NoSuchElementException();
        --position;
	lastReturned = next;
	next = lastReturned.previous;
        return lastReturned.data;
      }

      /**
       * Removes the last object retrieved by <code>next()</code>
       * from the list, if the list supports object removal.
       *
       * @throws ConcurrentModificationException if the list
       *         has been modified elsewhere.
       * @throws IllegalStateException if the iterator is positioned
       *         before the start of the list or the last object has already
       *         been removed.
       */
      public void remove()
      {
        checkMod();
        if (lastReturned == null)
          throw new IllegalStateException();
	removeEntry(lastReturned);
	lastReturned = null;
	++knownMod;
      }
    };
  }

  /**
   * Inserts the specified element at the front of the list.
   *
   * @param value the element to insert.
   * @return true.
   * @since 1.6
   */
  public boolean offerFirst(T value)
  {
    addFirst(value);
    return true;
  }

  /**
   * Inserts the specified element at the end of the list.
   *
   * @param value the element to insert.
   * @return true.
   * @since 1.6
   */
  public boolean offerLast(T value)
  {
    return add(value);
  }

  /**
   * Returns the first element of the list without removing
   * it.
   *
   * @return the first element of the list, or <code>null</code>
   *         if the list is empty.
   * @since 1.6
   */
  public T peekFirst()
  {
    return peek();
  }

  /**
   * Returns the last element of the list without removing
   * it.
   *
   * @return the last element of the list, or <code>null</code>
   *         if the list is empty.
   * @since 1.6
   */
  public T peekLast()
  {
    if (size == 0)
      return null;
    return getLast();
  }

  /**
   * Removes and returns the first element of the list.
   *
   * @return the first element of the list, or <code>null</code>
   *         if the list is empty.
   * @since 1.6
   */
  public T pollFirst()
  {
    return poll();
  }

  /**
   * Removes and returns the last element of the list.
   *
   * @return the last element of the list, or <code>null</code>
   *         if the list is empty.
   * @since 1.6
   */
  public T pollLast()
  {
    if (size == 0)
      return null;
    return removeLast();
  }

  /**
   * Pops an element from the stack by removing and returning
   * the first element in the list.  This is equivalent to
   * calling {@link #removeFirst()}.
   *
   * @return the top of the stack, which is the first element
   *         of the list.
   * @throws NoSuchElementException if the list is empty.
   * @since 1.6
   * @see #removeFirst()
   */
  public T pop()
  {
    return removeFirst();
  }

  /**
   * Pushes an element on to the stack by adding it to the
   * front of the list.  This is equivalent to calling
   * {@link #addFirst(T)}.
   *
   * @param value the element to push on to the stack.
   * @throws NoSuchElementException if the list is empty.
   * @since 1.6
   * @see #addFirst(T)
   */
  public void push(T value)
  {
    addFirst(value);
  }
  
  /**
   * Removes the first occurrence of the specified element
   * from the list, when traversing the list from head to
   * tail.  The list is unchanged if the element is not found.
   * This is equivalent to calling {@link #remove(Object)}.
   *
   * @param o the element to remove.
   * @return true if an instance of the object was removed.
   * @since 1.6
   */
  public boolean removeFirstOccurrence(Object o)
  {
    return remove(o);
  }

  /**
   * Removes the last occurrence of the specified element
   * from the list, when traversing the list from head to
   * tail.  The list is unchanged if the element is not found.
   *
   * @param o the element to remove.
   * @return true if an instance of the object was removed.
   * @since 1.6
   */
  public boolean removeLastOccurrence(Object o)
  {
    Entry<T> e = last;
    while (e != null)
      {
	if (equals(o, e.data))
	  {
	    removeEntry(e);
	    return true;
	  }
	e = e.previous;
      }
    return false;
  }

}
