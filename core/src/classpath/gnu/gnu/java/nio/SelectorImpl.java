/* SelectorImpl.java -- 
   Copyright (C) 2002 Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

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

package gnu.java.nio;

import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SelectorImpl extends AbstractSelector
{
  boolean closed = false;
  Set keys, selected, canceled;

  public SelectorImpl (SelectorProvider provider)
  {
    super (provider);
  }

  public Set keys ()
  {
    return keys;
  }
    
  public int selectNow ()
  {
    return select (1);
  }

  public int select ()
  {
    return select (Long.MAX_VALUE);
  }

//   private static native int java_do_select(int[] read, int[] write,
//                                            int[] except, long timeout);

  private static int java_do_select (int[] read, int[] write,
                                     int[] except, long timeout)
  {
    return 0;
  }

  public int select (long timeout)
  {
    if (closed)
      {
        throw new ClosedSelectorException ();
      }

    if (keys == null)
	    {
        return 0;
	    }

    int[] read = new int[keys.size ()];
    int[] write = new int[keys.size ()];
    int[] except = new int[keys.size ()];
    int i = 0;
    Iterator it = keys.iterator ();

    while (it.hasNext ())
	    {
        SelectionKeyImpl k = (SelectionKeyImpl) it.next ();
        read[i] = k.fd;
        write[i] = k.fd;
        except[i] = k.fd;
        i++;
	    }

    int ret = java_do_select (read, write, except, timeout);

    i = 0;
    it = keys.iterator ();

    while (it.hasNext ())
	    {
        SelectionKeyImpl k = (SelectionKeyImpl) it.next ();

        if (read[i] != -1 ||
            write[i] != -1 ||
            except[i] != -1)
          {
            add_selected (k);
          }

        i++;
	    }

    return ret;
  }
    
  public Set selectedKeys ()
  {
    return selected;
  }

  public Selector wakeup ()
  {
    return null;
  }

  public void add (SelectionKeyImpl k)
  {
    if (keys == null)
	    keys = new HashSet ();

    keys.add (k);
  }

  void add_selected (SelectionKeyImpl k)
  {
    if (selected == null)
	    selected = new HashSet ();

    selected.add(k);
  }

  protected void implCloseSelector ()
  {
    closed = true;
  }
    
  protected SelectionKey register (SelectableChannel ch, int ops, Object att)
  {
    return register ((AbstractSelectableChannel) ch, ops, att);
  }

  protected SelectionKey register (AbstractSelectableChannel ch, int ops,
                                   Object att)
  {
    /*
	  // filechannel is not selectable ?
    if (ch instanceof FileChannelImpl)
      {
        FileChannelImpl fc = (FileChannelImpl) ch;
        SelectionKeyImpl impl = new SelectionKeyImpl (ch, this, fc.fd);
        keys.add (impl);
        return impl;
      }
    else
    */
	
    if (ch instanceof SocketChannelImpl)
	    {
        SocketChannelImpl sc = (SocketChannelImpl) ch;
        SelectionKeyImpl impl = new SelectionKeyImpl (ch, this, sc.fd);
        add (impl);
        return impl;
	    }
    else if (ch instanceof ServerSocketChannelImpl)
      {
        ServerSocketChannelImpl ssc = (ServerSocketChannelImpl) ch;
        SelectionKeyImpl impl = new SelectionKeyImpl (ch, this, ssc.fd);
        add (impl);
        return impl;
      }
    else
	    {
        System.err.println ("INTERNAL ERROR, no known channel type");
	    }

    return null;
  }
}
